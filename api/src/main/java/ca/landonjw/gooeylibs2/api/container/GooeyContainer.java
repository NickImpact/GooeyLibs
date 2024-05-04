package ca.landonjw.gooeylibs2.api.container;

import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.ButtonAction;
import ca.landonjw.gooeylibs2.api.button.ButtonClick;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.moveable.Movable;
import ca.landonjw.gooeylibs2.api.button.moveable.MovableButtonAction;
import ca.landonjw.gooeylibs2.api.page.Page;
import ca.landonjw.gooeylibs2.api.page.PageAction;
import ca.landonjw.gooeylibs2.api.tasks.Task;
import ca.landonjw.gooeylibs2.api.template.Template;
import ca.landonjw.gooeylibs2.api.template.slot.TemplateSlot;
import ca.landonjw.gooeylibs2.api.template.slot.TemplateSlotDelegate;
import ca.landonjw.gooeylibs2.api.template.types.InventoryTemplate;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import jakarta.annotation.Nonnull;
import java.util.List;

public class GooeyContainer extends AbstractContainerMenu {

    private final MinecraftServer server;
    private final ServerPlayer player;
    private final Container container;

    private final Page page;
    public InventoryTemplate inventoryTemplate;

    private long lastClickTick;
    private boolean closing;

    /*
     *  Keeps track of a movable button that is on the cursor.
     *  If there is not a button on the cursor, this is null.
     */
    private Button cursorButton;

    public GooeyContainer(@Nonnull ServerPlayer player, @Nonnull Page page) {
        super(page.getTemplate().getTemplateType().getContainerType(page.getTemplate()), 1);
        this.server = player.level().getServer();
        this.player = player;

        this.page = page;
        this.inventoryTemplate = page.getInventoryTemplate().orElse(null);
        this.container = new SimpleContainer(page.getTemplate().getSize() + 36);

        bindSlots();
        bindPage();
    }

    private void bindPage() {
        page.subscribe(this, this::refresh);
    }

    public void refresh() {
        unbindSlots();
        inventoryTemplate = page.getInventoryTemplate().orElse(null);
        bindSlots();
        openWindow();
    }

    private void bindSlots() {
        List<TemplateSlotDelegate> delegates = page.getTemplate().getSlots();
        int slotIndex = 0;
        for (int i = 0; i < delegates.size(); i++) {
            final int index = i;
            final TemplateSlotDelegate delegate = delegates.get(i);
            final TemplateSlot slot = new TemplateSlot(this.container, delegate, 0, 0);
            delegate.subscribe(this, () -> this.updateSlotStack(index, getItemAtSlot(index), false));
            this.addSlot(slot);
            this.container.setItem(slotIndex++, slot.getItem());
        }

        /*
         * Add user inventory portion to the container slots and stacks.
         * Adding these slots are necessary to stop Sponge from having an aneurysm about missing slots,
         */
        if (inventoryTemplate == null) {
            // Sets the slots for the main inventory.
            for (int i = 9; i < 36; i++) {
                final GooeyButton button = GooeyButton.of(player.getInventory().items.get(i));
                final TemplateSlotDelegate delegate = new TemplateSlotDelegate(button, i - 9);
                addSlot(new TemplateSlot(this.container, delegate, 0, 0));
                this.container.setItem(slotIndex++, button.getDisplay());
            }
            // Sets the slots for the hotbar.
            for (int i = 0; i < 9; i++) {
                final GooeyButton button = GooeyButton.of(player.getInventory().items.get(i));
                final TemplateSlotDelegate delegate = new TemplateSlotDelegate(button, i + 27);

                addSlot(new TemplateSlot(this.container, delegate, 0, 0));
                this.container.setItem(slotIndex++, button.getDisplay());
            }
        }
        else {
            for (int i = 0; i < inventoryTemplate.getSize(); i++) {
                final int index = i;
                final int itemSlot = i + page.getTemplate().getSize();
                final TemplateSlotDelegate delegate = this.inventoryTemplate.getSlot(i);

                final TemplateSlot slot = new TemplateSlot(this.container, delegate, 0, 0);

                delegate.subscribe(this, () -> this.updateSlotStack(index, getItemAtSlot(itemSlot), true));
                addSlot(slot);
                this.container.setItem(itemSlot, slot.getItem());
            }
        }
    }

    private void unbindSlots() {
        this.slots.forEach(slot -> {
            ((TemplateSlot) slot).getDelegate().unsubscribe(this);
        });

        if (this.inventoryTemplate != null) {
            this.inventoryTemplate.getSlots().forEach(delegate -> delegate.unsubscribe(this));
        }
        this.slots.clear();
    }

    private void updateSlotStack(int index, ItemStack stack, boolean playerInventory) {
        player.connection.send(new ClientboundContainerSetSlotPacket(
                this.containerId,
                this.player.containerMenu.getStateId(),
                playerInventory ? page.getTemplate().getSize() + index : index,
                stack
        ));
    }

    private int getTemplateIndex(int slotIndex) {
        if (isSlotInPlayerInventory(slotIndex)) {
            return slotIndex - page.getTemplate().getSize();
        }
        else {
            return slotIndex;
        }
    }

    private Template getTemplateFromIndex(int slotIndex) {
        if (isSlotInPlayerInventory(slotIndex)) {
            return inventoryTemplate;
        }
        else {
            return page.getTemplate();
        }
    }

    private boolean isSlotInPlayerInventory(int slot) {
        int templateSize = page.getTemplate().getSize();
        return slot >= templateSize && slot - templateSize < player.inventoryMenu.slots.size();
    }

    private ItemStack getItemAtSlot(int slot) {
        if (slot == -999 || slot >= slots.size()) {
            return ItemStack.EMPTY;
        }
        return slots.get(slot).getItem();
    }

    public void open() {
        player.doCloseContainer();
        player.containerMenu = this;
        player.containerCounter = player.containerMenu.containerId;
        openWindow();
        page.onOpen(new PageAction(player, page));
    }

    private void openWindow() {
        ClientboundOpenScreenPacket openWindow = new ClientboundOpenScreenPacket(
                player.containerCounter,
                page.getTemplate().getTemplateType().getContainerType(page.getTemplate()),
                page.getTitle()
        );
        player.connection.send(openWindow);
        updateAllContainerContents();

        this.setPlayersCursor(ItemStack.EMPTY);
    }

    private void patchDesyncs(int slot, ClickType clickType) {
        if (clickType == ClickType.PICKUP || clickType == ClickType.CLONE || clickType == ClickType.THROW) {
            updateSlotStack(getTemplateIndex(slot), getItemAtSlot(slot), isSlotInPlayerInventory(slot));
        }
        else if (clickType == ClickType.QUICK_MOVE || clickType == ClickType.PICKUP_ALL) {
            updateAllContainerContents();
        }
    }

    @Override
    public ItemStack quickMoveStack(@NotNull Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slot, int dragType, @NotNull ClickType type, @NotNull Player player) {
        // Don't do anything if user is only clicking edge of UI.
        if (slot == -1 || slot == -999) {
            if (cursorButton != null) {
                setPlayersCursor(cursorButton.getDisplay());
            }
            return;
        }

        /*
         * These click types represent the user quickly picking up or moving items.
         * The click type proliferates and invokes slotClick for each stack that would be affected.
         * In order to prevent this method invoking logic every time, we track the last time
         * the click type was used. If a click of the same type has run on the same tick,
         * it will return out.
         */
        Slot target = this.slots.get(slot);
        if (this.lastClickTick == this.server.getTickCount()) {
            if (type == ClickType.PICKUP) {
                if (this.cursorButton != null) {
                    ItemStack clickedItem = getItemAtSlot(slot);
                    ItemStack cursorItem = this.cursorButton.getDisplay();

                    if (clickedItem.getItem() == cursorItem.getItem() && ItemStack.isSameItem(clickedItem, cursorItem)) {
                        ItemStack copy = getItemAtSlot(slot).copy();
                        copy.setCount(copy.getCount() + this.cursorButton.getDisplay().getCount());
                        target.onTake(this.player, copy);
                    }
                    return;
                }
            }
            patchDesyncs(slot, type);
            setPlayersCursor((cursorButton != null) ? cursorButton.getDisplay() : ItemStack.EMPTY);
            return;
        }
        this.lastClickTick = this.server.getTickCount();

        if (type == ClickType.QUICK_CRAFT && dragType == 8) {
            /*
             * If the user middle clicks and drags, this refreshes the container at the end of the tick.
             * This is done because the click type propagates with the drag, yet does not always have a
             * termination drag type. So we track the entry drag, and prevent the rest of the clicks from
             * invoking.
             */
            Task.builder()
                    .execute(() -> {
                        updateAllContainerContents();
                        setPlayersCursor((cursorButton != null) ? cursorButton.getDisplay() : ItemStack.EMPTY);
                    })
                    .build();
            return;
        }

        patchDesyncs(slot, type);
        Button button = this.getButton(slot);

        /*
         *  If the button being interacted with is moveable, or there is currently a moveable button on the cursor,
         *  send it to a separate handler.
         */
        if (button instanceof Movable || cursorButton != null) {
            this.handleMovableButton(slot, dragType, type);
            return;
        }

        // Interacting with non-movable button, force empty cursor
        setPlayersCursor(ItemStack.EMPTY);

        if (type == ClickType.SWAP) {
            // During a swap, the drag type variable is used for the target swap slot
            ItemStack inventory = this.player.getInventory().getItem(dragType);

            updateSlotStack(27 + dragType, inventory, true);
            updateSlotStack(slot, this.getItemAtSlot(slot), false);
        }

        if (type == ClickType.QUICK_CRAFT) {
            updateSlotStack(getTemplateIndex(slot), ItemStack.EMPTY, isSlotInPlayerInventory(slot));
            return;
        }

        ButtonClick buttonClickType = getButtonClickType(type, dragType);
        if (button != null) {
            ButtonAction action = new ButtonAction(this.player, buttonClickType, button, page.getTemplate(), page, slot);
            button.onClick(action);
        }
    }

    private ButtonClick getButtonClickType(ClickType type, int dragType) {
        return switch (type) {
            case PICKUP -> (dragType == 0) ? ButtonClick.LEFT_CLICK : ButtonClick.RIGHT_CLICK;
            case CLONE -> ButtonClick.MIDDLE_CLICK;
            case QUICK_MOVE -> (dragType == 0) ? ButtonClick.SHIFT_LEFT_CLICK : ButtonClick.SHIFT_RIGHT_CLICK;
            case THROW -> ButtonClick.THROW;
            default -> ButtonClick.OTHER;
        };
    }

    private void handleMovableButton(int slot, int dragType, ClickType clickType) {
        /*
         * This prevents a desync with dragging an item.
         * Quick crafts begin and end with a click on slot -999,
         * we want to ignore those calls.
         */
        if (clickType == ClickType.QUICK_CRAFT && slot == -999) {
            return;
        }

        Template template = getTemplateFromIndex(slot);

        if (template == null) {
            if (clickType == ClickType.PICKUP && isSlotOccupied(slot)) {
                setPlayersCursor((cursorButton != null) ? cursorButton.getDisplay() : ItemStack.EMPTY);
                return;
            }
            if (clickType == ClickType.QUICK_CRAFT) {
                this.updateAllContainerContents();
            }
            if (cursorButton != null) {
                setPlayersCursor(cursorButton.getDisplay());
            }
        }
        else {
            if (cursorButton == null) {
                onMovablePickup(slot, dragType, clickType);
            }
            else {
                onMovableDrop(slot, dragType, clickType);
            }
        }
    }

    private void onMovablePickup(int slot, int dragType, ClickType clickType) {
        Template template = getTemplateFromIndex(slot);
        int targetTemplateSlot = getTemplateIndex(slot);
        Button clickedButton = getButton(slot);

        if (slot == -999) return;

        setPlayersCursor(getItemAtSlot(slot));

        if (clickedButton == null) {
            return;
        }
        if (clickType == ClickType.QUICK_CRAFT && dragType == 9) {
            setPlayersCursor(ItemStack.EMPTY);
            return;
        }

        ButtonClick click = getButtonClickType(clickType, dragType);
        MovableButtonAction action = new MovableButtonAction(player, click, clickedButton, template, page, targetTemplateSlot);
        clickedButton.onClick(action);
        ((Movable) clickedButton).onPickup(action);

        if (action.isCancelled()) {
            setPlayersCursor(ItemStack.EMPTY);
            updateSlotStack(targetTemplateSlot, clickedButton.getDisplay(), template instanceof InventoryTemplate);
        }
        else {
            cursorButton = clickedButton;
            setButton(slot, null);

            // Clone needs to return empty ItemStack or it desyncs.
            if (clickType == ClickType.CLONE || clickType == ClickType.QUICK_MOVE || clickType == ClickType.THROW) {
                setPlayersCursor(cursorButton.getDisplay());
            }
        }
    }

    private void onMovableDrop(int slot, int dragType, ClickType clickType) {
        Template template = getTemplateFromIndex(slot);
        int targetTemplateSlot = getTemplateIndex(slot);

        // This prevents a desync on double clicking when dropping
        if (clickType == ClickType.PICKUP_ALL || slot == -999) {
            setPlayersCursor(cursorButton.getDisplay());
            return;
        }

        // Handle collision
        if (isSlotOccupied(slot)) {
            setPlayersCursor(cursorButton.getDisplay());

            /*
             * When a quick move is performed, it will apply slot clicks to all identical items, causing
             * collisions when trying to drop. Quick move wants a return type of an empty ItemStack,
             * so this guarantees it, otherwise there will be a desync.
             */
            if (clickType == ClickType.QUICK_MOVE || clickType == ClickType.CLONE || clickType == ClickType.THROW) {
                this.resetQuickCraft();
            }
            else if (clickType == ClickType.QUICK_CRAFT) {
                updateSlotStack(getTemplateIndex(slot), getItemAtSlot(slot), isSlotInPlayerInventory(slot));
            }
        }
        else {
            ButtonClick click = getButtonClickType(clickType, dragType);
            MovableButtonAction action = new MovableButtonAction(player, click, cursorButton, template, page, targetTemplateSlot);
            cursorButton.onClick(action);
            ((Movable) cursorButton).onDrop(action);

            if (action.isCancelled()) {
                // Clone needs to return empty ItemStack or it desyncs.
                if (clickType == ClickType.CLONE) {
                    return;
                }

                setPlayersCursor(cursorButton.getDisplay());
                updateSlotStack(targetTemplateSlot, ItemStack.EMPTY, template instanceof InventoryTemplate);
                if (clickType == ClickType.QUICK_CRAFT) {
                    this.updateAllContainerContents();
                }
            }
            else {
                setButton(slot, cursorButton);
                cursorButton = null;
                setPlayersCursor(ItemStack.EMPTY);

                if (clickType == ClickType.QUICK_CRAFT) {
                    this.updateAllContainerContents();
                    this.setPlayersCursor(ItemStack.EMPTY);
                }
            }
        }
    }

    private boolean isSlotOccupied(int slot) {
        if (isSlotInPlayerInventory(slot) && inventoryTemplate == null) {
            return player.inventoryMenu.slots.get(getTemplateIndex(slot) + 9).hasItem();
        }
        else {
            return getButton(slot) != null;
        }
    }

    public Page getPage() {
        return page;
    }

    private void updateAllContainerContents() {
        this.refresh(this.player, this.player.containerMenu, this.getItems());

        /*
         * Detects change in the player's inventory and updates them. This is to prevent desyncs if a player
         * gets items added to their inventory while in the user interface.
         */
        player.inventoryMenu.broadcastChanges();
        if (inventoryTemplate != null) {
            this.refresh(this.player, this.player.inventoryMenu, inventoryTemplate.getFullDisplay(player));
        }
        else {
            this.refresh(this.player, this.player.inventoryMenu, player.inventoryMenu.getItems());
        }
    }

    private void refresh(ServerPlayer player, AbstractContainerMenu menu, NonNullList<ItemStack> contents) {
        player.connection.send(new ClientboundContainerSetContentPacket(
                menu.containerId,
                menu.getStateId(),
                contents,
                player.getItemInHand(InteractionHand.MAIN_HAND)
        ));
    }

    private void setPlayersCursor(ItemStack stack) {
        ClientboundContainerSetSlotPacket setCursorSlot = new ClientboundContainerSetSlotPacket(-1, this.player.containerMenu.getStateId(), 0, stack);
        player.connection.send(setCursorSlot);
    }

    private void setButton(int slot, Button button) {
        if (slot < 0) return;

        ((TemplateSlot) this.getSlot(slot)).setButton(button);
    }

    @Override
    public void removed(@NotNull Player player) {
        if (closing) return;
        closing = true;

        page.onClose(new PageAction(this.player, page));
        page.unsubscribe(this);
        this.slots.forEach((slot) -> ((TemplateSlot) slot).getDelegate().unsubscribe(this));

        this.player.inventoryMenu.broadcastChanges();
        refresh(this.player, player.inventoryMenu, player.inventoryMenu.getItems());
        setPlayersCursor(ItemStack.EMPTY);
        super.removed(player);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    private Button getButton(int slot) {
        if (slot < 0) return null;

        //Check if it's player's inventory or UI slot
        if (slot >= page.getTemplate().getSize()) {

            int targetedPlayerSlotIndex = slot - page.getTemplate().getSize();

            if (inventoryTemplate != null) {
                return inventoryTemplate.getSlot(targetedPlayerSlotIndex).getButton().orElse(null);
            }
            else {
                return null;
            }
        }
        else {
            return page.getTemplate().getSlot(slot).getButton().orElse(null);
        }
    }

}

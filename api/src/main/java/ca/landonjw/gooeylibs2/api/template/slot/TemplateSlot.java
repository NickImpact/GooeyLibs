package ca.landonjw.gooeylibs2.api.template.slot;

import ca.landonjw.gooeylibs2.api.button.Button;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import jakarta.annotation.Nullable;
import java.util.Optional;

public final class TemplateSlot extends Slot {

    private final TemplateSlotDelegate delegate;

    public TemplateSlot(Container container, @NotNull TemplateSlotDelegate delegate, int xPosition, int yPosition) {
        super(container, delegate.getIndex(), xPosition, yPosition);
        this.delegate = delegate;
    }

    public TemplateSlotDelegate getDelegate() {
        return this.delegate;
    }

    public void setButton(@Nullable Button button) {
        this.delegate.setButton(button);
    }

    @Override
    public ItemStack getItem() {
        return this.delegate.getButton().map(Button::getDisplay).orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return false;
    }

}
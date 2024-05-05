package ca.landonjw.gooeylibs2.api.button;

import com.google.common.collect.Lists;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Unit;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.Unbreakable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GooeyButton extends ButtonBase {

    private final Consumer<ButtonAction> onClick;

    protected GooeyButton(@Nonnull ItemStack display, @Nullable Consumer<ButtonAction> onClick) {
        super(display);
        this.onClick = onClick;
    }

    @Override
    public void onClick(@Nonnull ButtonAction action) {
        if (onClick != null) onClick.accept(action);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static GooeyButton of(ItemStack stack) {
        return builder()
                .display(stack)
                .build();
    }

    public static class Builder {

        protected ItemStack display;
        protected Component title;
        protected Collection<Component> lore = Lists.newArrayList();
        protected Consumer<ButtonAction> onClick;
        protected Set<FlagType> hideFlags = new LinkedHashSet<>();

        public Builder display(@Nonnull ItemStack display) {
            this.display = display;
            return this;
        }

        public Builder title(@Nullable String title) {
            if(title == null) {
                return this;
            }

            return this.title(Component.literal(title));
        }

        public Builder title(@Nullable Component title) {
            this.title = title;
            return this;
        }

        public Builder lore(@Nullable Collection<String> lore) {
            if(lore == null) {
                return this;
            }

            this.lore = lore.stream().map(Component::literal).collect(Collectors.toList());
            return this;
        }

        @SuppressWarnings("unchecked")
        public <T> Builder lore(Class<T> type, @Nullable Collection<T> lore) {
            if(lore == null) {
                return this;
            }

            if(Component.class.isAssignableFrom(type)) {
                this.lore = (Collection<Component>) lore;
                return this;
            } else if(String.class.isAssignableFrom(type)) {
                return this.lore((Collection<String>) lore);
            }

            throw new UnsupportedOperationException("Invalid Type: " + type.getName());
        }

        public Builder hideFlags(FlagType... flags) {
            this.hideFlags.addAll(Arrays.asList(flags));
            return this;
        }

        public Builder onClick(@Nullable Consumer<ButtonAction> behaviour) {
            this.onClick = behaviour;
            return this;
        }

        public Builder onClick(@Nullable Runnable behaviour) {
            this.onClick = (behaviour != null) ? (action) -> behaviour.run() : null;
            return this;
        }

        public GooeyButton build() {
            validate();
            return new GooeyButton(buildDisplay(), onClick);
        }

        protected void validate() {
            if (display == null) throw new IllegalStateException("button display must be defined");
        }

        protected ItemStack buildDisplay() {
            if (title != null) {
                MutableComponent result = Component.empty()
                        .setStyle(Style.EMPTY.withItalic(false))
                        .append(this.title);
                display.applyComponents(DataComponentPatch.builder()
                        .set(DataComponents.CUSTOM_NAME, result)
                        .build());
            }

            if (!lore.isEmpty()) {
                display.applyComponents(DataComponentPatch.builder()
                        .set(DataComponents.LORE, new ItemLore(Collections.emptyList(), lore.stream().map((it) ->
                                Component.empty()
                                .setStyle(Style.EMPTY.withItalic(false))
                                .append(it)).collect(Collectors.toList())))
                        .build());
            }

            if (!this.hideFlags.isEmpty()) {
                if (this.hideFlags.contains(FlagType.Hide_Tooltip) || this.hideFlags.contains(FlagType.All)) {
                    display.applyComponents(DataComponentPatch.builder()
                            .set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE)
                            .build());
                }
                if (this.hideFlags.contains(FlagType.Enchantments) || this.hideFlags.contains(FlagType.All)) {
                    display.applyComponents(DataComponentPatch.builder()
                            .set(DataComponents.ENCHANTMENTS, display.getEnchantments().withTooltip(false))
                            .build());
                }
                if (this.hideFlags.contains(FlagType.Attribute_Modifiers) || this.hideFlags.contains(FlagType.All)) {
                    display.applyComponents(DataComponentPatch.builder()
                            .set(DataComponents.ATTRIBUTE_MODIFIERS, display.getOrDefault(
                                            DataComponents.ATTRIBUTE_MODIFIERS,
                                            ItemAttributeModifiers.EMPTY
                                    ).withTooltip(false))
                            .build());
                }
                if (this.hideFlags.contains(FlagType.Unbreakable) || this.hideFlags.contains(FlagType.All)) {
                    Unbreakable unbreakable = display.get(DataComponents.UNBREAKABLE);
                    if (unbreakable != null) {
                        display.applyComponents(DataComponentPatch.builder()
                                .set(DataComponents.UNBREAKABLE, unbreakable.withTooltip(false))
                                .build());
                    }
                }
                if (this.hideFlags.contains(FlagType.Can_Break) || this.hideFlags.contains(FlagType.All)) {
                    AdventureModePredicate canBreak = display.get(DataComponents.CAN_BREAK);
                    if (canBreak != null) {
                        display.applyComponents(DataComponentPatch.builder()
                                .set(DataComponents.CAN_BREAK, canBreak.withTooltip(false))
                                .build());
                    }
                }
                if (this.hideFlags.contains(FlagType.Can_Place_On) || this.hideFlags.contains(FlagType.All)) {
                    AdventureModePredicate canPlaceOn = display.get(DataComponents.CAN_PLACE_ON);
                    if (canPlaceOn != null) {
                        display.applyComponents(DataComponentPatch.builder()
                                .set(DataComponents.CAN_PLACE_ON, canPlaceOn.withTooltip(false))
                                .build());
                    }
                }
                if (this.hideFlags.contains(FlagType.Stored_Enchantments) || this.hideFlags.contains(FlagType.All)) {
                    display.applyComponents(DataComponentPatch.builder()
                            .set(DataComponents.STORED_ENCHANTMENTS, display.getEnchantments().withTooltip(false))
                            .build());
                }
                if (this.hideFlags.contains(FlagType.Dyed_Color) || this.hideFlags.contains(FlagType.All)) {
                    DyedItemColor dyedColor = display.get(DataComponents.DYED_COLOR);
                    if (dyedColor != null) {
                        display.applyComponents(DataComponentPatch.builder()
                                .set(DataComponents.DYED_COLOR, dyedColor.withTooltip(false))
                                .build());
                    }
                }
                if (this.hideFlags.contains(FlagType.Trim) || this.hideFlags.contains(FlagType.All)) {
                    ArmorTrim trim = display.get(DataComponents.TRIM);
                    if (trim != null) {
                        display.applyComponents(DataComponentPatch.builder()
                                .set(DataComponents.TRIM, trim.withTooltip(false))
                                .build());
                    }
                }
                if (this.hideFlags.contains(FlagType.Hide_Additional_Tooltip) || this.hideFlags.contains(FlagType.All)) {
                    display.applyComponents(DataComponentPatch.builder()
                            .set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                            .build());
                }
            }
            return display;
        }

    }

}
/*
 * GooeyLibs
 * Copyright (C) 201x - 2024 landonjw
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package ca.landonjw.gooeylibs2.api.button;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class GooeyButton extends ButtonBase {

    private final Consumer<ButtonAction> onClick;

    protected GooeyButton(@NotNull ItemStack display, @Nullable Consumer<ButtonAction> onClick) {
        super(display);
        this.onClick = onClick;
    }

    @Override
    public void onClick(@NotNull ButtonAction action) {
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
        protected Consumer<ButtonAction> onClick;

        public Builder display(@NotNull ItemStack display) {
            this.display = display.copy();
            return this;
        }

        public <T> Builder with(DataComponentType<T> type, T value) {
            this.display.set(type, value);
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
            return new GooeyButton(this.display, onClick);
        }

        protected void validate() {
            if (display == null) throw new IllegalStateException("button display must be defined");
        }

    }

}
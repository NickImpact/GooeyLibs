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

package ca.landonjw.gooeylibs2.api.button.moveable;

import ca.landonjw.gooeylibs2.api.button.ButtonAction;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Consumer;

public class MovableButton extends GooeyButton implements Movable {

    private Consumer<MovableButtonAction> onPickup;
    private Consumer<MovableButtonAction> onDrop;

    protected MovableButton(@NotNull ItemStack display,
                            @Nullable Consumer<ButtonAction> onClick,
                            @Nullable Consumer<MovableButtonAction> onPickup,
                            @Nullable Consumer<MovableButtonAction> onDrop) {
        super(display, onClick);
        this.onPickup = onPickup;
        this.onDrop = onDrop;
    }

    public void onPickup(MovableButtonAction action) {
        if (onPickup != null) {
            this.onPickup.accept(action);
        }
    }

    public void onDrop(MovableButtonAction action) {
        if (onDrop != null) {
            this.onDrop.accept(action);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends GooeyButton.Builder {

        protected Consumer<MovableButtonAction> onPickup;
        protected Consumer<MovableButtonAction> onDrop;

        public Builder display(@NotNull ItemStack display) {
            super.display(display);
            return this;
        }

        public Builder onClick(@Nullable Consumer<ButtonAction> behaviour) {
            super.onClick(behaviour);
            return this;
        }

        public Builder onClick(@Nullable Runnable behaviour) {
            super.onClick(behaviour);
            return this;
        }

        public Builder onPickup(@Nullable Consumer<MovableButtonAction> behaviour) {
            this.onPickup = behaviour;
            return this;
        }

        public Builder onPickup(@Nullable Runnable behaviour) {
            if (behaviour != null) {
                this.onPickup = (action) -> behaviour.run();
            }
            return this;
        }

        public Builder onDrop(@Nullable Consumer<MovableButtonAction> behaviour) {
            this.onDrop = behaviour;
            return this;
        }

        public Builder onDrop(@Nullable Runnable behaviour) {
            if (behaviour != null) {
                this.onDrop = (action) -> behaviour.run();
            }
            return this;
        }

        @Override
        public <T> GooeyButton.Builder with(DataComponentType<T> type, T value) {
            super.with(type, value);
            return this;
        }

        public MovableButton build() {
            validate();
            return new MovableButton(this.display, onClick, onPickup, onDrop);
        }

    }

}

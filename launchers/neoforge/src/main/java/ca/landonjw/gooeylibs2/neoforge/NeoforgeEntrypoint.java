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

package ca.landonjw.gooeylibs2.neoforge;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.Page;
import ca.landonjw.gooeylibs2.api.template.Template;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Unbreakable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("gooeylibs")
public class NeoforgeEntrypoint {

    private final Logger logger = LogManager.getLogger("GooeyLibs");

    private final FMLModContainer container;
    private final IEventBus bus;

    public NeoforgeEntrypoint(FMLModContainer container, IEventBus bus) {
        this.container = container;
        this.bus = bus;

        if (SharedConstants.IS_RUNNING_IN_IDE) {
            NeoForge.EVENT_BUS.addListener(this::onCommandRegistration);
        }
    }

    private void onCommandRegistration(RegisterCommandsEvent event) {
        this.logger.info("Registering commands");

        GooeyButton button = GooeyButton.builder()
                .display(new ItemStack(Items.DIAMOND))
                .with(DataComponents.CUSTOM_NAME, Component.literal("GooeyLibs Test").withColor(0x32a852))
                .with(DataComponents.UNBREAKABLE, new Unbreakable(true))
                .onClick(() -> this.logger.info("Button click detected and executed"))
                .build();

        GooeyButton border = GooeyButton.builder()
                .display(new ItemStack(Items.BLACK_STAINED_GLASS_PANE))
                .with(DataComponents.CUSTOM_NAME, Component.empty())
                .build();

        Template template = ChestTemplate.builder(3)
                .border(0, 0, 3, 9, border)
                .set(13, button)
                .build();

        Page page = new GooeyPage(template, null, Component.literal("GooeyLibs Test"), null, null);

        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
                Commands.literal("gooeylibs")
                        .then(Commands.literal("test")
                                .then(Commands.literal("ui")
                                        .executes(context -> {
                                            try {
                                                ServerPlayer source = context.getSource().getPlayerOrException();
                                                UIManager.openUIForcefully(source, page);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            return 0;
                                        })
                                )
                        )
        );
    }
}

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

package ca.landonjw.gooeylibs2.fabric;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.page.Page;
import ca.landonjw.gooeylibs2.api.template.Template;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Unbreakable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public final class FabricEntrypoint implements ModInitializer {

    private final Logger logger = LogManager.getLogger("GooeyLibs");

    @Override
    public void onInitialize() {
        if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
            this.logger.warn("Applying dev environment configurations...");

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

            net.kyori.adventure.text.Component adventure = MiniMessage.miniMessage().deserialize("<gradient:green:blue>Gradient Title Test</gradient>");

            Page page = new GooeyPage(template, null, Component.literal("GooeyLibs Test"), null, null);

            CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
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

                dispatcher.register(
                        Commands.literal("gooeylibs")
                                .then(Commands.literal("test")
                                        .then(Commands.literal("linked")
                                                .executes(context -> {
                                                    ServerPlayer source = context.getSource().getPlayerOrException();
                                                    MinecraftServer server = source.server;

                                                    FabricServerAudiences audiences = FabricServerAudiences.of(server);
                                                    LinkedPage linked = new LinkedPage(template, null, audiences.toNative(adventure), null, null, null, null);
                                                    UIManager.openUIForcefully(source, linked);

                                                    return 0;
                                                })
                                        )
                                )
                );
            });
        }
    }
}

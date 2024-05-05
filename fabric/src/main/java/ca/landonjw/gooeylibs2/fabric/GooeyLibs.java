package ca.landonjw.gooeylibs2.fabric;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import ca.landonjw.gooeylibs2.api.template.types.InventoryTemplate;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static net.minecraft.commands.Commands.literal;

public class GooeyLibs implements ModInitializer {

    private final FabricBootstrapper bootstrapper = new FabricBootstrapper();

    @Override
    public void onInitialize() {
        this.bootstrapper.bootstrap();
        CommandRegistrationCallback.EVENT.register((dispatcher, registries, environment) -> {
            dispatcher.register(literal("gooey")
                    .then(literal("test")
                            .executes(context -> {
                                final CommandSourceStack source = context.getSource();

                                Button button = GooeyButton.builder()
                                        .display(new ItemStack(Items.BLACK_STAINED_GLASS_PANE))
                                        .title(Component.literal(""))
                                        .build();

                                Button diamond = GooeyButton.builder()
                                        .display(new ItemStack(Items.DIAMOND))
                                        .title(Component.literal("Test Item"))
                                        .onClick(() -> System.out.println("Clicked diamond"))
                                        .build();

                                ChestTemplate template = ChestTemplate.builder(5)
                                        .border(0, 0, 6, 9, button)
                                        .set(22, diamond)
                                        .build();

                                InventoryTemplate invTemplate = InventoryTemplate.builder().build();

                                GooeyPage page = GooeyPage.builder()
                                        .title(Component.literal("1.19.2 Test UI"))
                                        .template(template)
                                        .inventory(invTemplate)
                                        .build();
                                UIManager.openUIForcefully(source.getPlayerOrException(), page);
                                return 1;
                            })
                    )
            );
        });
    }
}

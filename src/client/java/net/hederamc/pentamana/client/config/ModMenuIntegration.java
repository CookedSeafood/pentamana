package net.hederamc.pentamana.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.hederamc.pentamana.PentamanaClient;
import net.minecraft.network.chat.Component;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> YetAnotherConfigLib.createBuilder()
            .title(Component.literal("Pentamana"))
            .category(ConfigCategory.createBuilder()
                .name(Component.literal("Manabar"))
                .group(OptionGroup.createBuilder()
                    .name(Component.literal("Miscellaneous"))
                    .option(Option.<Integer>createBuilder()
                        .name(Component.literal("Max Stars"))
                        .description(OptionDescription.of(Component.literal("Limit the manabar length so it doesn't overflow the screen. Mana displayed will be scaled accordingly.")))
                        .binding(PentamanaClient.DEFAULTS.manabarMaxStars,
                            () -> {
                                return PentamanaClient.CONFIG.manabarMaxStars;
                            },
                            newVal -> {
                                PentamanaClient.CONFIG.manabarMaxStars = newVal;
                            })
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                            .range(10, 32)
                            .step(1)
                        )
                        .build())
                    .build())
                .build())
            .build()
            .generateScreen(parentScreen);
    }
}

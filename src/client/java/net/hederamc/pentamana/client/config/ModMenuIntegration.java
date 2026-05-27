package net.hederamc.pentamana.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.hederamc.fishbonetrehalose.api.Text;
import net.hederamc.pentamana.PentamanaClient;
import net.hederamc.pentamana.client.gui.ManaBarAlignment;
import net.hederamc.pentamana.math.Direction2D;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> YetAnotherConfigLib.createBuilder()
                .title(Text.literal("Pentamana"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.fromTranslatable("config.category.manaBar"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.fromTranslatable("config.group.offset"))
                                .description(OptionDescription.of(Text.fromTranslatable("config.description.offset")))
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.fromTranslatable("config.option.x"))
                                        .binding(
                                                PentamanaClient.DEFAULTS.manaBarOffsetX,
                                                () -> {
                                                    return PentamanaClient.CONFIG.manaBarOffsetX;
                                                },
                                                newVal -> {
                                                    PentamanaClient.CONFIG.manaBarOffsetX = newVal;
                                                })
                                        .controller(
                                                opt -> IntegerFieldControllerBuilder.create(opt))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.fromTranslatable("config.option.y"))
                                        .binding(
                                                PentamanaClient.DEFAULTS.manaBarOffsetY,
                                                () -> {
                                                    return PentamanaClient.CONFIG.manaBarOffsetY;
                                                },
                                                newVal -> {
                                                    PentamanaClient.CONFIG.manaBarOffsetY = newVal;
                                                })
                                        .controller(
                                                opt -> IntegerFieldControllerBuilder.create(opt))
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.fromTranslatable("config.group.properties"))
                                .option(Option.<Direction2D>createBuilder()
                                        .name(Text.fromTranslatable("config.option.direction"))
                                        .binding(
                                                PentamanaClient.DEFAULTS.manaBarDirection,
                                                () -> {
                                                    return PentamanaClient.CONFIG.manaBarDirection;
                                                },
                                                newVal -> {
                                                    PentamanaClient.CONFIG.manaBarDirection = newVal;
                                                })
                                        .controller(
                                                opt -> EnumControllerBuilder.create(opt)
                                                        .enumClass(Direction2D.class)
                                                        .formatValue(val -> Text.fromTranslatable("config.controller." + val.getName())))
                                        .build())
                                .option(Option.<ManaBarAlignment>createBuilder()
                                        .name(Text.fromTranslatable("config.option.alignment"))
                                        .binding(
                                                PentamanaClient.DEFAULTS.manaBarAlignment,
                                                () -> {
                                                    return PentamanaClient.CONFIG.manaBarAlignment;
                                                },
                                                newVal -> {
                                                    PentamanaClient.CONFIG.manaBarAlignment = newVal;
                                                })
                                        .controller(
                                                opt -> EnumControllerBuilder.create(opt)
                                                        .enumClass(ManaBarAlignment.class)
                                                        .formatValue(val -> Text.fromTranslatable("config.controller." + val.getName())))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.fromTranslatable("config.option.max_stars"))
                                        .description(OptionDescription.of(Text.fromTranslatable("config.description.max_stars")))
                                        .binding(
                                                PentamanaClient.DEFAULTS.manaBarMaxStars,
                                                () -> {
                                                    return PentamanaClient.CONFIG.manaBarMaxStars;
                                                },
                                                newVal -> {
                                                    PentamanaClient.CONFIG.manaBarMaxStars = newVal;
                                                })
                                        .controller(
                                                opt -> IntegerSliderControllerBuilder.create(opt)
                                                        .range(10, 32)
                                                        .step(1))
                                        .build())
                                .build())
                        .build())
                .build()
                .generateScreen(parentScreen);
    }
}

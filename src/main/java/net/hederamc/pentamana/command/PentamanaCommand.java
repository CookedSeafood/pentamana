package net.hederamc.pentamana.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.hederamc.pentamana.Pentamana;
import net.hederamc.pentamana.config.PentamanaConfig;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class PentamanaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(
            Commands.literal(Pentamana.MOD_NAMESPACE)
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(
                Commands.literal("set")
                .then(
                    Commands.literal("manaCapacityBase")
                    .then(
                        Commands.argument("manaCapacityBase", FloatArgumentType.floatArg())
                        .executes(context -> executeSetManaCapacityBase(context.getSource(), FloatArgumentType.getFloat(context, "manaCapacityBase")))
                    )
                )
                .then(
                    Commands.literal("manaRegenerationBase")
                    .then(
                        Commands.argument("manaRegenerationBase", FloatArgumentType.floatArg())
                        .executes(context -> executeSetManaRegenerationBase(context.getSource(), FloatArgumentType.getFloat(context, "manaRegenerationBase")))
                    )
                )
                .then(
                    Commands.literal("enchantmentCapacityBase")
                    .then(
                        Commands.argument("enchantmentCapacityBase", FloatArgumentType.floatArg())
                        .executes(context -> executeSetEnchantmentCapacityBase(context.getSource(), FloatArgumentType.getFloat(context, "enchantmentCapacityBase")))
                    )
                )
                .then(
                    Commands.literal("enchantmentStreamBase")
                    .then(
                        Commands.argument("enchantmentStreamBase", FloatArgumentType.floatArg())
                        .executes(context -> executeSetEnchantmentStreamBase(context.getSource(), FloatArgumentType.getFloat(context, "enchantmentStreamBase")))
                    )
                )
                .then(
                    Commands.literal("enchantmentManaEfficiencyBase")
                    .then(
                        Commands.argument("enchantmentManaEfficiencyBase", FloatArgumentType.floatArg())
                        .executes(context -> executeSetEnchantmentManaEfficiencyBase(context.getSource(), FloatArgumentType.getFloat(context, "enchantmentManaEfficiencyBase")))
                    )
                )
                .then(
                    Commands.literal("enchantmentPotencyBase")
                    .then(
                        Commands.argument("enchantmentPotencyBase", FloatArgumentType.floatArg())
                        .executes(context -> executeSetEnchantmentPotencyBase(context.getSource(), FloatArgumentType.getFloat(context, "enchantmentPotencyBase")))
                    )
                )
                .then(
                    Commands.literal("statusEffectManaBoostBase")
                    .then(
                        Commands.argument("statusEffectManaBoostBase", FloatArgumentType.floatArg())
                        .executes(context -> executeSetStatusEffectManaBoostBase(context.getSource(), FloatArgumentType.getFloat(context, "statusEffectManaBoostBase")))
                    )
                )
                .then(
                    Commands.literal("statusEffectManaReductionBase")
                    .then(
                        Commands.argument("statusEffectManaReductionBase", FloatArgumentType.floatArg())
                        .executes(context -> executeSetStatusEffectManaReductionBase(context.getSource(), FloatArgumentType.getFloat(context, "statusEffectManaReductionBase")))
                    )
                )
                .then(
                    Commands.literal("statusEffectInstantManaBase")
                    .then(
                        Commands.argument("statusEffectInstantManaBase", FloatArgumentType.floatArg())
                        .executes(context -> executeSetStatusEffectInstantManaBase(context.getSource(), FloatArgumentType.getFloat(context, "statusEffectInstantManaBase")))
                    )
                )
                .then(
                    Commands.literal("statusEffectInstantDepleteBase")
                    .then(
                        Commands.argument("statusEffectInstantDepleteBase", FloatArgumentType.floatArg())
                        .executes(context -> executeSetStatusEffectInstantDepleteBase(context.getSource(), FloatArgumentType.getFloat(context, "statusEffectInstantDepleteBase")))
                    )
                )
                .then(
                    Commands.literal("statusEffectManaPowerBase")
                    .then(
                        Commands.argument("statusEffectManaPowerBase", FloatArgumentType.floatArg())
                        .executes(context -> executeSetStatusEffectManaPowerBase(context.getSource(), FloatArgumentType.getFloat(context, "statusEffectManaPowerBase")))
                    )
                )
                .then(
                    Commands.literal("statusEffectManaSicknessBase")
                    .then(
                        Commands.argument("statusEffectManaSicknessBase", FloatArgumentType.floatArg())
                        .executes(context -> executeSetStatusEffectManaSicknessBase(context.getSource(), FloatArgumentType.getFloat(context, "statusEffectManaSicknessBase")))
                    )
                )
                .then(
                    Commands.literal("statusEffectManaRegenerationBase")
                    .then(
                        Commands.argument("statusEffectManaRegenerationBase", IntegerArgumentType.integer())
                        .executes(context -> executeSetStatusEffectManaRegenerationBase(context.getSource(), IntegerArgumentType.getInteger(context, "statusEffectManaRegenerationBase")))
                    )
                )
                .then(
                    Commands.literal("statusEffectManaInhibitionBase")
                    .then(
                        Commands.argument("statusEffectManaInhibitionBase", IntegerArgumentType.integer())
                        .executes(context -> executeSetStatusEffectManaInhibitionBase(context.getSource(), IntegerArgumentType.getInteger(context, "statusEffectManaInhibitionBase")))
                    )
                )
                .then(
                    Commands.literal("shouldConvertExperienceLevel")
                    .then(
                        Commands.argument("shouldConvertExperienceLevel", BoolArgumentType.bool())
                        .executes(context -> executeSetShouldConvertExperienceLevel(context.getSource(), BoolArgumentType.getBool(context, "shouldConvertExperienceLevel")))
                    )
                )
                .then(
                    Commands.literal("experienceLevelConversionBase")
                    .then(
                        Commands.argument("experienceLevelConversionBase", FloatArgumentType.floatArg())
                        .executes(context -> executeSetExperienceLevelConversionBase(context.getSource(), FloatArgumentType.getFloat(context, "experienceLevelConversionBase")))
                    )
                )
            )
            .then(
                Commands.literal("reset")
                .executes(context -> executeReset(context.getSource()))
                .then(
                    Commands.literal("manaCapacityBase")
                    .executes(context -> executeResetManaCapacityBase(context.getSource()))
                )
                .then(
                    Commands.literal("manaRegenerationBase")
                    .executes(context -> executeResetManaRegenerationBase(context.getSource()))
                )
                .then(
                    Commands.literal("enchantmentCapacityBase")
                    .executes(context -> executeResetEnchantmentCapacityBase(context.getSource()))
                )
                .then(
                    Commands.literal("enchantmentStreamBase")
                    .executes(context -> executeResetEnchantmentStreamBase(context.getSource()))
                )
                .then(
                    Commands.literal("enchantmentManaEfficiencyBase")
                    .executes(context -> executeResetEnchantmentManaEfficiencyBase(context.getSource()))
                )
                .then(
                    Commands.literal("enchantmentPotencyBase")
                    .executes(context -> executeResetEnchantmentPotencyBase(context.getSource()))
                )
                .then(
                    Commands.literal("statusEffectManaBoostBase")
                    .executes(context -> executeResetStatusEffectManaBoostBase(context.getSource()))
                )
                .then(
                    Commands.literal("statusEffectManaReductionBase")
                    .executes(context -> executeResetStatusEffectManaReductionBase(context.getSource()))
                )
                .then(
                    Commands.literal("statusEffectInstantManaBase")
                    .executes(context -> executeResetStatusEffectInstantManaBase(context.getSource()))
                )
                .then(
                    Commands.literal("statusEffectInstantDepleteBase")
                    .executes(context -> executeResetStatusEffectInstantDepleteBase(context.getSource()))
                )
                .then(
                    Commands.literal("statusEffectManaPowerBase")
                    .executes(context -> executeResetStatusEffectManaPowerBase(context.getSource()))
                )
                .then(
                    Commands.literal("statusEffectManaSicknessBase")
                    .executes(context -> executeResetStatusEffectManaSicknessBase(context.getSource()))
                )
                .then(
                    Commands.literal("statusEffectManaRegenerationBase")
                    .executes(context -> executeResetStatusEffectManaRegenerationBase(context.getSource()))
                )
                .then(
                    Commands.literal("statusEffectManaInhibitionBase")
                    .executes(context -> executeResetStatusEffectManaInhibitionBase(context.getSource()))
                )
                .then(
                    Commands.literal("shouldConvertExperienceLevel")
                    .executes(context -> executeResetShouldConvertExperienceLevel(context.getSource()))
                )
                .then(
                    Commands.literal("experienceLevelConversionBase")
                    .executes(context -> executeResetExperienceLevelConversionBase(context.getSource()))
                )
            )
            .then(
                Commands.literal("reload")
                .executes(context -> executeReload(context.getSource()))
            )
        );
    }

    public static int executeSetManaCapacityBase(CommandSourceStack source, float manaCapacityBase) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        config.manaCapacityBase = manaCapacityBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config manaCapacityBase is now set to: " + manaCapacityBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeSetManaRegenerationBase(CommandSourceStack source, float manaRegenerationBase) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        config.manaRegenerationBase = manaRegenerationBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config manaRegenerationBase is now set to: " + manaRegenerationBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeSetEnchantmentCapacityBase(CommandSourceStack source, float enchantmentCapacityBase) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        config.enchantmentCapacityBase = enchantmentCapacityBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config enchantmentCapacityBase is now set to: " + enchantmentCapacityBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeSetEnchantmentStreamBase(CommandSourceStack source, float enchantmentStreamBase) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        config.enchantmentStreamBase = enchantmentStreamBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config enchantmentStreamBase is now set to: " + enchantmentStreamBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeSetEnchantmentManaEfficiencyBase(CommandSourceStack source, float enchantmentManaEfficiencyBase) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        config.enchantmentManaEfficiencyBase = enchantmentManaEfficiencyBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config enchantmentManaEfficiencyBase is now set to: " + enchantmentManaEfficiencyBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeSetEnchantmentPotencyBase(CommandSourceStack source, float enchantmentPotencyBase) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        config.enchantmentPotencyBase = enchantmentPotencyBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config enchantmentPotencyBase is now set to: " + enchantmentPotencyBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeSetStatusEffectManaBoostBase(CommandSourceStack source, float statusEffectManaBoostBase) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        config.statusEffectManaBoostBase = statusEffectManaBoostBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config statusEffectManaBoostBase is now set to: " + statusEffectManaBoostBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeSetStatusEffectManaReductionBase(CommandSourceStack source, float statusEffectManaReductionBase) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        config.statusEffectManaReductionBase = statusEffectManaReductionBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config statusEffectManaReductionBase is now set to: " + statusEffectManaReductionBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeSetStatusEffectInstantManaBase(CommandSourceStack source, float statusEffectInstantManaBase) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        config.statusEffectInstantManaBase = statusEffectInstantManaBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config statusEffectInstantManaBase is now set to: " + statusEffectInstantManaBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeSetStatusEffectInstantDepleteBase(CommandSourceStack source, float statusEffectInstantDepleteBase) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        config.statusEffectInstantDepleteBase = statusEffectInstantDepleteBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config statusEffectInstantDepleteBase is now set to: " + statusEffectInstantDepleteBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeSetStatusEffectManaPowerBase(CommandSourceStack source, float statusEffectManaPowerBase) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        config.statusEffectManaPowerBase = statusEffectManaPowerBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config statusEffectManaPowerBase is now set to: " + statusEffectManaPowerBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeSetStatusEffectManaSicknessBase(CommandSourceStack source, float statusEffectManaSicknessBase) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        config.statusEffectManaSicknessBase = statusEffectManaSicknessBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config statusEffectManaSicknessBase is now set to: " + statusEffectManaSicknessBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeSetStatusEffectManaRegenerationBase(CommandSourceStack source, int statusEffectManaRegenerationBase) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        config.statusEffectManaRegenerationBase = statusEffectManaRegenerationBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config statusEffectManaRegenerationBase is now set to: " + statusEffectManaRegenerationBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeSetStatusEffectManaInhibitionBase(CommandSourceStack source, int statusEffectManaInhibitionBase) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        config.statusEffectManaInhibitionBase = statusEffectManaInhibitionBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config statusEffectManaInhibitionBase is now set to: " + statusEffectManaInhibitionBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeSetShouldConvertExperienceLevel(CommandSourceStack source, boolean shouldConvertExperienceLevel) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        config.shouldConvertExperienceLevel = shouldConvertExperienceLevel;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config shouldConvertExperienceLevel is now set to: " + shouldConvertExperienceLevel), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeSetExperienceLevelConversionBase(CommandSourceStack source, float experienceLevelConversionBase) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        config.experienceLevelConversionBase = experienceLevelConversionBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config experienceLevelConversionBase is now set to: " + experienceLevelConversionBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeReset(CommandSourceStack source) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        PentamanaConfig defaultConfig = PentamanaConfig.HANDLER.defaults();
        config.manaCapacityBase = defaultConfig.manaCapacityBase;
        config.manaRegenerationBase = defaultConfig.manaRegenerationBase;
        config.enchantmentCapacityBase = defaultConfig.enchantmentCapacityBase;
        config.enchantmentStreamBase = defaultConfig.enchantmentStreamBase;
        config.enchantmentManaEfficiencyBase = defaultConfig.enchantmentManaEfficiencyBase;
        config.enchantmentPotencyBase = defaultConfig.enchantmentPotencyBase;
        config.statusEffectManaBoostBase = defaultConfig.statusEffectManaBoostBase;
        config.statusEffectManaReductionBase = defaultConfig.statusEffectManaReductionBase;
        config.statusEffectInstantManaBase = defaultConfig.statusEffectInstantManaBase;
        config.statusEffectInstantDepleteBase = defaultConfig.statusEffectInstantDepleteBase;
        config.statusEffectManaPowerBase = defaultConfig.statusEffectManaPowerBase;
        config.statusEffectManaSicknessBase = defaultConfig.statusEffectManaSicknessBase;
        config.statusEffectManaRegenerationBase = defaultConfig.statusEffectManaRegenerationBase;
        config.statusEffectManaInhibitionBase = defaultConfig.statusEffectManaInhibitionBase;
        config.shouldConvertExperienceLevel = defaultConfig.shouldConvertExperienceLevel;
        config.experienceLevelConversionBase = defaultConfig.experienceLevelConversionBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config is now set to defaults."), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeResetManaCapacityBase(CommandSourceStack source) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        PentamanaConfig defaultConfig = PentamanaConfig.HANDLER.defaults();
        config.manaCapacityBase = defaultConfig.manaCapacityBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config manaCapacityBase is now set to default: " + defaultConfig.manaCapacityBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeResetManaRegenerationBase(CommandSourceStack source) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        PentamanaConfig defaultConfig = PentamanaConfig.HANDLER.defaults();
        config.manaRegenerationBase = defaultConfig.manaRegenerationBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config manaRegenerationBase is now set to default: " + defaultConfig.manaRegenerationBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeResetEnchantmentCapacityBase(CommandSourceStack source) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        PentamanaConfig defaultConfig = PentamanaConfig.HANDLER.defaults();
        config.enchantmentCapacityBase = defaultConfig.enchantmentCapacityBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config enchantmentCapacityBase is now set to default: " + defaultConfig.enchantmentCapacityBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeResetEnchantmentStreamBase(CommandSourceStack source) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        PentamanaConfig defaultConfig = PentamanaConfig.HANDLER.defaults();
        config.enchantmentStreamBase = defaultConfig.enchantmentStreamBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config enchantmentStreamBase is now set to default: " + defaultConfig.enchantmentStreamBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeResetEnchantmentManaEfficiencyBase(CommandSourceStack source) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        PentamanaConfig defaultConfig = PentamanaConfig.HANDLER.defaults();
        config.enchantmentManaEfficiencyBase = defaultConfig.enchantmentManaEfficiencyBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config enchantmentManaEfficiencyBase is now set to default: " + defaultConfig.enchantmentManaEfficiencyBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeResetEnchantmentPotencyBase(CommandSourceStack source) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        PentamanaConfig defaultConfig = PentamanaConfig.HANDLER.defaults();
        config.enchantmentPotencyBase = defaultConfig.enchantmentPotencyBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config enchantmentPotencyBase is now set to default: " + defaultConfig.enchantmentPotencyBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeResetStatusEffectManaBoostBase(CommandSourceStack source) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        PentamanaConfig defaultConfig = PentamanaConfig.HANDLER.defaults();
        config.statusEffectManaBoostBase = defaultConfig.statusEffectManaBoostBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config statusEffectManaBoostBase is now set to default: " + defaultConfig.statusEffectManaBoostBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeResetStatusEffectManaReductionBase(CommandSourceStack source) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        PentamanaConfig defaultConfig = PentamanaConfig.HANDLER.defaults();
        config.statusEffectManaReductionBase = defaultConfig.statusEffectManaReductionBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config statusEffectManaReductionBase is now set to default: " + defaultConfig.statusEffectManaReductionBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeResetStatusEffectInstantManaBase(CommandSourceStack source) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        PentamanaConfig defaultConfig = PentamanaConfig.HANDLER.defaults();
        config.statusEffectInstantManaBase = defaultConfig.statusEffectInstantManaBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config statusEffectInstantManaBase is now set to default: " + defaultConfig.statusEffectInstantManaBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeResetStatusEffectInstantDepleteBase(CommandSourceStack source) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        PentamanaConfig defaultConfig = PentamanaConfig.HANDLER.defaults();
        config.statusEffectInstantDepleteBase = defaultConfig.statusEffectInstantDepleteBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config statusEffectInstantDepleteBase is now set to default: " + defaultConfig.statusEffectInstantDepleteBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeResetStatusEffectManaPowerBase(CommandSourceStack source) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        PentamanaConfig defaultConfig = PentamanaConfig.HANDLER.defaults();
        config.statusEffectManaPowerBase = defaultConfig.statusEffectManaPowerBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config statusEffectManaPowerBase is now set to default: " + defaultConfig.statusEffectManaPowerBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeResetStatusEffectManaSicknessBase(CommandSourceStack source) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        PentamanaConfig defaultConfig = PentamanaConfig.HANDLER.defaults();
        config.statusEffectManaSicknessBase = defaultConfig.statusEffectManaSicknessBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config statusEffectManaSicknessBase is now set to default: " + defaultConfig.statusEffectManaSicknessBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeResetStatusEffectManaRegenerationBase(CommandSourceStack source) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        PentamanaConfig defaultConfig = PentamanaConfig.HANDLER.defaults();
        config.statusEffectManaRegenerationBase = defaultConfig.statusEffectManaRegenerationBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config statusEffectManaRegenerationBase is now set to default: " + defaultConfig.statusEffectManaRegenerationBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeResetStatusEffectManaInhibitionBase(CommandSourceStack source) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        PentamanaConfig defaultConfig = PentamanaConfig.HANDLER.defaults();
        config.statusEffectManaInhibitionBase = defaultConfig.statusEffectManaInhibitionBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config statusEffectManaInhibitionBase is now set to default: " + defaultConfig.statusEffectManaInhibitionBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeResetShouldConvertExperienceLevel(CommandSourceStack source) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        PentamanaConfig defaultConfig = PentamanaConfig.HANDLER.defaults();
        config.shouldConvertExperienceLevel = defaultConfig.shouldConvertExperienceLevel;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config shouldConvertExperienceLevel is now set to default: " + defaultConfig.shouldConvertExperienceLevel), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeResetExperienceLevelConversionBase(CommandSourceStack source) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        PentamanaConfig defaultConfig = PentamanaConfig.HANDLER.defaults();
        config.experienceLevelConversionBase = defaultConfig.experienceLevelConversionBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config experienceLevelConversionBase is now set to default: " + defaultConfig.experienceLevelConversionBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeReload(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Reloading Pentamana!"), true);
        return PentamanaConfig.HANDLER.load() ? 1 : -1;
    }
}

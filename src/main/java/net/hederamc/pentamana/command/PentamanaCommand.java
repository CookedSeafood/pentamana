package net.hederamc.pentamana.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
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
            )
            .then(
                Commands.literal("reload")
                .executes(context -> executeReload(context.getSource()))
            )
        );
    }

    public static int executeSetManaCapacityBase(CommandSourceStack source, float manaCapacityBase) {
        Pentamana.CONFIG.manaCapacityBase = manaCapacityBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config manaCapacityBase is now set to: " + manaCapacityBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeSetManaRegenerationBase(CommandSourceStack source, float manaRegenerationBase) {
        Pentamana.CONFIG.manaRegenerationBase = manaRegenerationBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config manaRegenerationBase is now set to: " + manaRegenerationBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeReset(CommandSourceStack source) {
        Pentamana.CONFIG.manaCapacityBase = Pentamana.DEFAULTS.manaCapacityBase;
        Pentamana.CONFIG.manaRegenerationBase = Pentamana.DEFAULTS.manaRegenerationBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config is now set to defaults."), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeResetManaCapacityBase(CommandSourceStack source) {
        Pentamana.CONFIG.manaCapacityBase = Pentamana.DEFAULTS.manaCapacityBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config manaCapacityBase is now set to default: " + Pentamana.DEFAULTS.manaCapacityBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeResetManaRegenerationBase(CommandSourceStack source) {
        Pentamana.CONFIG.manaRegenerationBase = Pentamana.DEFAULTS.manaRegenerationBase;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Pentamana config manaRegenerationBase is now set to default: " + Pentamana.DEFAULTS.manaRegenerationBase), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeReload(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Reloading Pentamana!"), true);
        return PentamanaConfig.HANDLER.load() ? 1 : -1;
    }
}

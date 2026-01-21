package net.hederamc.pentamana.client.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.hederamc.pentamana.PentamanaClient;
import net.hederamc.pentamana.client.config.PentamanaConfig;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ManaBarCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(
            Commands.literal("manabar")
            .then(
                Commands.literal("set")
                .then(
                    Commands.literal("maxStars")
                    .then(
                        Commands.argument("maxStars", IntegerArgumentType.integer())
                        .executes(context -> executeSetMaxStars(context.getSource(), IntegerArgumentType.getInteger(context, "maxStars")))
                    )
                )
            )
            .then(
                Commands.literal("reset")
                .executes(context -> executeReset(context.getSource()))
                .then(
                    Commands.literal("maxStars")
                    .executes(context -> executeResetMaxStars(context.getSource()))
                )
            )
        );
    }

    public static int executeSetMaxStars(CommandSourceStack source, int maxStars) throws CommandSyntaxException {
        PentamanaClient.CONFIG.manabarMaxStars = maxStars;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Manabar maxStars is now set to: " + maxStars), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeReset(CommandSourceStack source) {
        PentamanaClient.CONFIG.manabarMaxStars = PentamanaClient.DEFAULTS.manabarMaxStars;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Manabar is now set to defaults."), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeResetMaxStars(CommandSourceStack source) {
        PentamanaClient.CONFIG.manabarMaxStars = PentamanaClient.DEFAULTS.manabarMaxStars;
        PentamanaConfig.HANDLER.save();
        source.sendSuccess(() -> Component.literal("Manabar maxStars is now set to default: " + PentamanaClient.DEFAULTS.manabarMaxStars), false);
        return Command.SINGLE_SUCCESS;
    }
}

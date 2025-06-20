package net.cookedseafood.pentamana.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.cookedseafood.pentamana.data.PentamanaConfig;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ManaCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(
            CommandManager.literal("mana")
            .then(
                CommandManager.literal("get")
                .requires(source -> source.hasPermissionLevel(2))
                .then(
                    CommandManager.argument("amount", FloatArgumentType.floatArg())
                    .executes(context -> executeSet(context.getSource(), FloatArgumentType.getFloat(context, "amount")))
                )
            )
            .then(
                CommandManager.literal("set")
                .requires(source -> source.hasPermissionLevel(2))
                .then(
                    CommandManager.argument("amount", FloatArgumentType.floatArg())
                    .executes(context -> executeSet(context.getSource(), FloatArgumentType.getFloat(context, "amount")))
                )
            )
            .then(
                CommandManager.literal("add")
                .requires(source -> source.hasPermissionLevel(2))
                .then(
                    CommandManager.argument("amount", FloatArgumentType.floatArg())
                    .executes(context -> executeAdd(context.getSource(), FloatArgumentType.getFloat(context, "amount")))
                )
            )
            .then(
                CommandManager.literal("subtract")
                .requires(source -> source.hasPermissionLevel(2))
                .then(
                    CommandManager.argument("amount", FloatArgumentType.floatArg())
                    .executes(context -> executeSubtract(context.getSource(), FloatArgumentType.getFloat(context, "amount")))
                )
            )
        );
    }

    public static int executeGet(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        float supply = player.getMana();
        source.sendFeedback(() -> Text.literal(player.getNameForScoreboard() + " has " + supply + " mana."), false);
        return (int)(supply / PentamanaConfig.manaPerPoint);
    }

    public static int executeSet(ServerCommandSource source, float amount) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        player.setMana(amount);
        source.sendFeedback(() -> Text.literal("Set mana for player ").append(player.getDisplayName()).append(" to " + amount + "."), false);
        return (int)(amount / PentamanaConfig.manaPerPoint);
    }

    public static int executeAdd(ServerCommandSource source, float amount) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        float targetSupply = player.getMana() + amount;
        player.setMana(targetSupply);
        source.sendFeedback(() -> Text.literal("Added " + amount + " mana for player ").append(player.getDisplayName()).append("."), false);
        return (int)(targetSupply / PentamanaConfig.manaPerPoint);
    }

    public static int executeSubtract(ServerCommandSource source, float amount) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        float targetSupply = player.getMana() - amount;
        player.setMana(targetSupply);
        source.sendFeedback(() -> Text.literal("Subtracted " + amount + " mana for player ").append(player.getDisplayName()).append("."), false);
        return (int)(targetSupply / PentamanaConfig.manaPerPoint);
    }
}

package net.cookedseafood.pentamana.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Iterator;
import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.component.CustomStatusEffectManagerComponentInstance;
import net.cookedseafood.pentamana.effect.CustomStatusEffect;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CustomCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(
            CommandManager.literal("custom")
            .then(
                CommandManager.literal("effect")
                .then(
                    CommandManager.literal("add")
                    .then(
                        CommandManager.argument("players", EntityArgumentType.players())
                        .then(
                            CommandManager.argument("effect", StringArgumentType.string())
                            .executes(context -> executeGiveEffect(context.getSource(), EntityArgumentType.getPlayers(context, "players"), StringArgumentType.getString(context, "effect")))
                            .then(
                                CommandManager.argument("duration", IntegerArgumentType.integer(1))
                                .executes(context -> executeGiveEffect(context.getSource(), EntityArgumentType.getPlayers(context, "players"), StringArgumentType.getString(context, "effect"), IntegerArgumentType.getInteger(context, "duration")))
                                .then(
                                    CommandManager.argument("amplifier", IntegerArgumentType.integer(0))
                                    .executes(context -> executeGiveEffect(context.getSource(), EntityArgumentType.getPlayers(context, "players"), StringArgumentType.getString(context, "effect"), IntegerArgumentType.getInteger(context, "duration"), IntegerArgumentType.getInteger(context, "amplifier")))
                                )
                            )
                            .then(
                                CommandManager.literal("infinite")
                                .executes(context -> executeGiveEffect(context.getSource(), EntityArgumentType.getPlayers(context, "players"), StringArgumentType.getString(context, "effect"), -1))
                                .then(
                                    CommandManager.argument("amplifier", IntegerArgumentType.integer(0))
                                    .executes(context -> executeGiveEffect(context.getSource(), EntityArgumentType.getPlayers(context, "players"), StringArgumentType.getString(context, "effect"), -1, IntegerArgumentType.getInteger(context, "amplifier")))
                                )
                            )
                        )
                    )
                )
                .then(
                    CommandManager.literal("clear")
                    .then(
                        CommandManager.argument("players", EntityArgumentType.players())
                        .executes(context -> executeClearEffect(context.getSource(), EntityArgumentType.getPlayers(context, "players")))
                        .then(
                            CommandManager.argument("effect", StringArgumentType.string())
                            .executes(context -> executeClearEffect(context.getSource(), EntityArgumentType.getPlayers(context, "players"), StringArgumentType.getString(context, "effect")))
                        )
                    )
                )
            )
        );
    }

    public static int executeGiveEffect(ServerCommandSource source, Collection<ServerPlayerEntity> players, String effect) throws CommandSyntaxException {
        return executeGiveEffect(source, players, effect, 1, 0);
    }

    public static int executeGiveEffect(ServerCommandSource source, Collection<ServerPlayerEntity> players, String effect, int duration) throws CommandSyntaxException {
        return executeGiveEffect(source, players, effect, duration, 0);
    }

    public static int executeGiveEffect(ServerCommandSource source, Collection<ServerPlayerEntity> players, String effect, int duration, int amplifier) throws CommandSyntaxException {
        Iterator<ServerPlayerEntity> iterator = players.iterator();

        while (iterator.hasNext()) {
            ServerPlayerEntity player = iterator.next();
            CustomStatusEffectManagerComponentInstance.CUSTOM_STATUS_EFFECT_MANAGER.get(player).getStatusEffectManager().add(new CustomStatusEffect(Pentamana.CUSTOM_STATUS_EFFECT_IDENTIFIER_REGISTRY.get(Identifier.of(effect.replace('.', ':'))), duration, amplifier));
        }

        return 1;
    }

    public static int executeClearEffect(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
        Iterator<ServerPlayerEntity> iterator = players.iterator();

        while (iterator.hasNext()) {
            ServerPlayerEntity player = iterator.next();
            CustomStatusEffectManagerComponentInstance.CUSTOM_STATUS_EFFECT_MANAGER.get(player).getStatusEffectManager().clear();
        }

        return 1;
    }

    public static int executeClearEffect(ServerCommandSource source, Collection<ServerPlayerEntity> players, String effect) {
        Iterator<ServerPlayerEntity> iterator = players.iterator();

        while (iterator.hasNext()) {
            ServerPlayerEntity player = iterator.next();
            CustomStatusEffectManagerComponentInstance.CUSTOM_STATUS_EFFECT_MANAGER.get(player).getStatusEffectManager().remove(Pentamana.CUSTOM_STATUS_EFFECT_IDENTIFIER_REGISTRY.get(Identifier.of(effect.replace('.', ':'))));
        }

        return 1;
    }
}

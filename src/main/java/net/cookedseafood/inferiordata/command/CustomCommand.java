package net.cookedseafood.inferiordata.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Iterator;
import net.cookedseafood.genericregistry.registry.Registries;
import net.cookedseafood.inferiordata.component.CustomStatusEffectManagerComponentInstance;
import net.cookedseafood.inferiordata.effect.CustomStatusEffect;
import net.cookedseafood.inferiordata.effect.CustomStatusEffectIdentifier;
import net.cookedseafood.inferiordata.suggestion.CustomStatusEffectSuggestionProvider;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CustomCommand {
    private static final SimpleCommandExceptionType UNREGISTED_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("The effect is not registed."));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(
            CommandManager.literal("custom")
            .then(
                CommandManager.literal("effect")
                .then(
                    CommandManager.literal("add")
                    .then(
                        CommandManager.argument("entities", EntityArgumentType.entities())
                        .then(
                            CommandManager.argument("effect", StringArgumentType.string())
                            .suggests(new CustomStatusEffectSuggestionProvider())
                            .executes(context -> executeGiveEffect(context.getSource(), EntityArgumentType.getEntities(context, "entities"), StringArgumentType.getString(context, "effect")))
                            .then(
                                CommandManager.argument("duration", IntegerArgumentType.integer(1))
                                .executes(context -> executeGiveEffect(context.getSource(), EntityArgumentType.getEntities(context, "entities"), StringArgumentType.getString(context, "effect"), IntegerArgumentType.getInteger(context, "duration")))
                                .then(
                                    CommandManager.argument("amplifier", IntegerArgumentType.integer(0))
                                    .executes(context -> executeGiveEffect(context.getSource(), EntityArgumentType.getEntities(context, "entities"), StringArgumentType.getString(context, "effect"), IntegerArgumentType.getInteger(context, "duration"), IntegerArgumentType.getInteger(context, "amplifier")))
                                )
                            )
                            .then(
                                CommandManager.literal("infinite")
                                .executes(context -> executeGiveEffect(context.getSource(), EntityArgumentType.getEntities(context, "entities"), StringArgumentType.getString(context, "effect"), -1))
                                .then(
                                    CommandManager.argument("amplifier", IntegerArgumentType.integer(0))
                                    .executes(context -> executeGiveEffect(context.getSource(), EntityArgumentType.getEntities(context, "entities"), StringArgumentType.getString(context, "effect"), -1, IntegerArgumentType.getInteger(context, "amplifier")))
                                )
                            )
                        )
                    )
                )
                .then(
                    CommandManager.literal("clear")
                    .then(
                        CommandManager.argument("entities", EntityArgumentType.entities())
                        .executes(context -> executeClearEffect(context.getSource(), EntityArgumentType.getEntities(context, "entities")))
                        .then(
                            CommandManager.argument("effect", StringArgumentType.string())
                            .suggests(new CustomStatusEffectSuggestionProvider())
                            .executes(context -> executeClearEffect(context.getSource(), EntityArgumentType.getEntities(context, "entities"), StringArgumentType.getString(context, "effect")))
                        )
                    )
                )
            )
        );
    }

    public static int executeGiveEffect(ServerCommandSource source, Collection<? extends Entity> targets, String effect) throws CommandSyntaxException {
        return executeGiveEffect(source, targets, effect, 1, 0);
    }

    public static int executeGiveEffect(ServerCommandSource source, Collection<? extends Entity> targets, String effect, int duration) throws CommandSyntaxException {
        return executeGiveEffect(source, targets, effect, duration, 0);
    }

    public static int executeGiveEffect(ServerCommandSource source, Collection<? extends Entity> targets, String effect, int duration, int amplifier) throws CommandSyntaxException {
        CustomStatusEffectIdentifier effectId = Registries.get(CustomStatusEffectIdentifier.class, Identifier.of(effect.replace('.', ':')));
        if (effectId == null) {
            UNREGISTED_EXCEPTION.create();
        }

        Iterator<? extends Entity> iterator = targets.iterator();

        while (iterator.hasNext()) {
            Entity target = iterator.next();
            CustomStatusEffectManagerComponentInstance.CUSTOM_STATUS_EFFECT_MANAGER.get(target).getStatusEffectManager().add(new CustomStatusEffect(effectId, duration, amplifier));
        }

        int count = targets.size();
        if (count == 1) {
            source.sendFeedback(() -> Text.literal("Applied effect " + effectId.getName() + " to " + targets.iterator().next().getDisplayName() + "."), true);
        } else {
            source.sendFeedback(() -> Text.literal("Applied effect " + effectId.getName() + " to " + count + " targets."), true);
        }

        return 1;
    }

    public static int executeClearEffect(ServerCommandSource source, Collection<? extends Entity> targets) {
        Iterator<? extends Entity> iterator = targets.iterator();

        while (iterator.hasNext()) {
            Entity target = iterator.next();
            CustomStatusEffectManagerComponentInstance.CUSTOM_STATUS_EFFECT_MANAGER.get(target).getStatusEffectManager().clear();
        }

        int count = targets.size();
        if (count == 1) {
            source.sendFeedback(() -> Text.literal("Removed every effect from " + targets.iterator().next().getDisplayName() + "."), true);
        } else {
            source.sendFeedback(() -> Text.literal("Removed every effect from " + count + " targets."), true);
        }

        return 1;
    }

    public static int executeClearEffect(ServerCommandSource source, Collection<? extends Entity> targets, String effect) {
        CustomStatusEffectIdentifier effectId = Registries.get(CustomStatusEffectIdentifier.class, Identifier.of(effect.replace('.', ':')));
        if (effectId == null) {
            UNREGISTED_EXCEPTION.create();
        }

        Iterator<? extends Entity> iterator = targets.iterator();

        while (iterator.hasNext()) {
            Entity target = iterator.next();
            CustomStatusEffectManagerComponentInstance.CUSTOM_STATUS_EFFECT_MANAGER.get(target).getStatusEffectManager().remove(effectId);
        }

        int count = targets.size();
        if (count == 1) {
            source.sendFeedback(() -> Text.literal("Removed effect " + effectId.getName() + " from " + targets.iterator().next().getDisplayName() + "."), true);
        } else {
            source.sendFeedback(() -> Text.literal("Removed effect " + effectId.getName() + " from " + count + " targets."), true);
        }

        return 1;
    }
}

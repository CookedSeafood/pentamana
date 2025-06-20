package net.cookedseafood.inferiordata.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.cookedseafood.genericregistry.registry.Registries;
import net.cookedseafood.inferiordata.effect.CustomStatusEffect;
import net.cookedseafood.inferiordata.effect.CustomStatusEffectIdentifier;
import net.cookedseafood.inferiordata.effect.ServerCustomStatusEffectManager;
import net.cookedseafood.inferiordata.suggestion.CustomStatusEffectSuggestionProvider;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class CustomCommand {
    private static final SimpleCommandExceptionType UNREGISTED_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("The effect is not registed."));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(
            CommandManager.literal("custom")
            .then(
                CommandManager.literal("effect")
                .requires(source -> source.hasPermissionLevel(2))
                .then(
                    CommandManager.literal("list")
                    .executes(context -> executeListEffect(context.getSource()))
                    .then(
                        CommandManager.argument("entities", EntityArgumentType.entity())
                        .executes(context -> executeListEffect(context.getSource(), EntityArgumentType.getEntity(context, "entities")))
                    )
                )
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
                    .executes(context -> executeClearEffect(context.getSource()))
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
            .then(
                CommandManager.literal("reset")
                .executes(context -> executeReset(context.getSource()))
                .then(
                    CommandManager.literal("effect")
                    .executes(context -> executeResetEffect(context.getSource()))
                )
            )
        );
    }

    public static int executeListEffect(ServerCommandSource source) throws CommandSyntaxException {
        return executeListEffect(source, source.getEntityOrThrow());
    }

    public static int executeListEffect(ServerCommandSource source, Entity target) {
        if (!(target instanceof LivingEntity)) {
            return 0;
        }

        ServerCustomStatusEffectManager manager = ((LivingEntity)target).getCustomStatusEffectManager();

        if (manager.isEmpty()) {
            source.sendFeedback(() -> Text.literal("The target does not have any custom status effect."), false);
        }

        MutableText feedback = MutableText.of(PlainTextContent.EMPTY);
        feedback.append(Text.literal("The target has " + manager.size() + " status effects:"));

        ((LivingEntity)target).getCustomStatusEffectManager().values().forEach(playlist -> {
            feedback.append(Text.literal("\n" + playlist.getId().getId().toString()).formatted(Formatting.YELLOW));
            feedback.append(Text.literal(" " + playlist.getActiveDuration()).formatted(Formatting.GREEN));
            feedback.append(Text.literal(" " + playlist.getActiveAmplifier()).formatted(Formatting.LIGHT_PURPLE));
            feedback.append(Text.literal(" " + playlist.getId().getColor()).formatted(Formatting.GRAY));
        });

        source.sendFeedback(() -> feedback, false);
        return 0;
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

            if (!(target instanceof LivingEntity)) {
                continue;
            }

            ((LivingEntity)target).addCustomStatusEffect(new CustomStatusEffect(effectId, duration, amplifier));
        }

        int count = targets.size();
        if (count == 1) {
            source.sendFeedback(() -> Text.literal("Applied effect " + effectId.getName() + " to ").append(targets.iterator().next().getDisplayName()).append("."), true);
        } else {
            source.sendFeedback(() -> Text.literal("Applied effect " + effectId.getName() + " to " + count + " targets."), true);
        }

        return 1;
    }

    public static int executeClearEffect(ServerCommandSource source) throws CommandSyntaxException {
        return executeClearEffect(source, source.getEntityOrThrow());
    }

    public static int executeClearEffect(ServerCommandSource source, Entity target) {
        return executeClearEffect(source, List.of(target));
    }

    public static int executeClearEffect(ServerCommandSource source, Collection<? extends Entity> targets) {
        Iterator<? extends Entity> iterator = targets.iterator();

        while (iterator.hasNext()) {
            Entity target = iterator.next();

            if (!(target instanceof LivingEntity)) {
                continue;
            }

            ((LivingEntity)target).clearCustomStatusEffect();
        }

        int count = targets.size();
        if (count == 1) {
            source.sendFeedback(() -> Text.literal("Removed every effect from ").append(targets.iterator().next().getDisplayName()).append("."), true);
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

            if (!(target instanceof LivingEntity)) {
                continue;
            }

            ((LivingEntity)target).removeCustomStatusEffect(effectId);
        }

        int count = targets.size();
        if (count == 1) {
            source.sendFeedback(() -> Text.literal("Removed effect " + effectId.getName() + " from ").append(targets.iterator().next().getDisplayName()).append("."), true);
        } else {
            source.sendFeedback(() -> Text.literal("Removed effect " + effectId.getName() + " from " + count + " targets."), true);
        }

        return 1;
    }

    public static int executeReset(ServerCommandSource source) throws CommandSyntaxException {
        executeResetEffect(source);
        return 1;
    }

    public static int executeResetEffect(ServerCommandSource source) throws CommandSyntaxException {
        source.getPlayerOrThrow().setCustomStatusEffects(new NbtCompound());
        return 1;
    }
}

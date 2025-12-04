package net.hederamc.pentamana.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.hederamc.pentamana.data.PentamanaConfig;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ManaCommand {
    public static final SimpleCommandExceptionType REQUIRES_LIVING_ENTITY_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("A living entity is required to run this command here"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(
            CommandManager.literal("mana")
            .requires(source -> source.hasPermissionLevel(2))
            .then(
                CommandManager.literal("get")
                .executes(context -> executeGet(context.getSource()))
                .then(
                    CommandManager.argument("entity", EntityArgumentType.entity())
                    .executes(context -> executeGet(context.getSource(), EntityArgumentType.getEntity(context, "entity")))
                )
            )
            .then(
                CommandManager.literal("set")
                .then(
                    CommandManager.argument("entity", EntityArgumentType.entity())
                    .then(
                        CommandManager.argument("amount", FloatArgumentType.floatArg())
                        .executes(context -> executeSet(context.getSource(), EntityArgumentType.getEntity(context, "entity"), FloatArgumentType.getFloat(context, "amount")))
                    )
                )
            )
            .then(
                CommandManager.literal("add")
                .then(
                    CommandManager.argument("entity", EntityArgumentType.entity())
                    .then(
                        CommandManager.argument("amount", FloatArgumentType.floatArg())
                        .executes(context -> executeAdd(context.getSource(), EntityArgumentType.getEntity(context, "entity"), FloatArgumentType.getFloat(context, "amount")))
                    )
                )
            )
            .then(
                CommandManager.literal("subtract")
                .then(
                    CommandManager.argument("entity", EntityArgumentType.entity())
                    .then(
                        CommandManager.argument("amount", FloatArgumentType.floatArg())
                        .executes(context -> executeSubtract(context.getSource(), EntityArgumentType.getEntity(context, "entity"), FloatArgumentType.getFloat(context, "amount")))
                    )
                )
            )
        );
    }

    public static int executeGet(ServerCommandSource source) throws CommandSyntaxException {
        return executeGet(source, source.getEntityOrThrow());
    }

    public static int executeGet(ServerCommandSource source, Entity entity) throws CommandSyntaxException {
        if (!(entity instanceof LivingEntity livingEntity)) {
            throw REQUIRES_LIVING_ENTITY_EXCEPTION.create();
        }

        float supply = livingEntity.getMana();
        source.sendFeedback(() -> Text.literal(livingEntity.getNameForScoreboard() + " has " + supply + " mana."), false);
        return (int)(supply / PentamanaConfig.manaPerPoint);
    }

    public static int executeSet(ServerCommandSource source, Entity entity, float amount) throws CommandSyntaxException {
        if (!(entity instanceof LivingEntity livingEntity)) {
            throw REQUIRES_LIVING_ENTITY_EXCEPTION.create();
        }

        livingEntity.setMana(amount);
        source.sendFeedback(() -> Text.literal("Set mana for entity ").append(livingEntity.getDisplayName()).append(" to " + amount + "."), false);
        return (int)(amount / PentamanaConfig.manaPerPoint);
    }

    public static int executeAdd(ServerCommandSource source, Entity entity, float amount) throws CommandSyntaxException {
        if (!(entity instanceof LivingEntity livingEntity)) {
            throw REQUIRES_LIVING_ENTITY_EXCEPTION.create();
        }

        float targetSupply = livingEntity.getMana() + amount;
        livingEntity.setMana(targetSupply);
        source.sendFeedback(() -> Text.literal("Added " + amount + " mana for entity ").append(livingEntity.getDisplayName()).append("."), false);
        return (int)(targetSupply / PentamanaConfig.manaPerPoint);
    }

    public static int executeSubtract(ServerCommandSource source, Entity entity, float amount) throws CommandSyntaxException {
        if (!(entity instanceof LivingEntity livingEntity)) {
            throw REQUIRES_LIVING_ENTITY_EXCEPTION.create();
        }

        float targetSupply = livingEntity.getMana() - amount;
        livingEntity.setMana(targetSupply);
        source.sendFeedback(() -> Text.literal("Subtracted " + amount + " mana for entity ").append(livingEntity.getDisplayName()).append("."), false);
        return (int)(targetSupply / PentamanaConfig.manaPerPoint);
    }
}

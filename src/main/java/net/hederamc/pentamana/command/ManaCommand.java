package net.hederamc.pentamana.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class ManaCommand {
    public static final SimpleCommandExceptionType REQUIRES_LIVING_ENTITY_EXCEPTION =
        new SimpleCommandExceptionType(Component.literal("A living entity is required to run this command here"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(
            Commands.literal("mana")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(
                Commands.literal("get")
                .executes(context -> executeGet(context.getSource()))
                .then(
                    Commands.argument("entity", EntityArgument.entity())
                    .executes(context -> executeGet(context.getSource(), EntityArgument.getEntity(context, "entity")))
                )
            )
            .then(
                Commands.literal("set")
                .then(
                    Commands.argument("entity", EntityArgument.entity())
                    .then(
                        Commands.argument("amount", FloatArgumentType.floatArg())
                        .executes(context -> executeSet(context.getSource(), EntityArgument.getEntity(context, "entity"), FloatArgumentType.getFloat(context, "amount")))
                    )
                )
            )
            .then(
                Commands.literal("add")
                .then(
                    Commands.argument("entity", EntityArgument.entity())
                    .then(
                        Commands.argument("amount", FloatArgumentType.floatArg())
                        .executes(context -> executeAdd(context.getSource(), EntityArgument.getEntity(context, "entity"), FloatArgumentType.getFloat(context, "amount")))
                    )
                )
            )
            .then(
                Commands.literal("subtract")
                .then(
                    Commands.argument("entity", EntityArgument.entity())
                    .then(
                        Commands.argument("amount", FloatArgumentType.floatArg())
                        .executes(context -> executeSubtract(context.getSource(), EntityArgument.getEntity(context, "entity"), FloatArgumentType.getFloat(context, "amount")))
                    )
                )
            )
        );
    }

    public static int executeGet(CommandSourceStack source) throws CommandSyntaxException {
        return executeGet(source, source.getEntityOrException());
    }

    public static int executeGet(CommandSourceStack source, Entity entity) throws CommandSyntaxException {
        if (!(entity instanceof LivingEntity livingEntity)) {
            throw REQUIRES_LIVING_ENTITY_EXCEPTION.create();
        }

        float supply = livingEntity.getMana();
        source.sendSuccess(() -> Component.literal(livingEntity.getScoreboardName() + " has " + supply + " mana."), false);
        return (int)supply;
    }

    public static int executeSet(CommandSourceStack source, Entity entity, float amount) throws CommandSyntaxException {
        if (!(entity instanceof LivingEntity livingEntity)) {
            throw REQUIRES_LIVING_ENTITY_EXCEPTION.create();
        }

        livingEntity.setMana(amount);
        source.sendSuccess(() -> Component.literal("Set mana for entity ").append(livingEntity.getDisplayName()).append(" to " + amount + "."), false);
        return (int)amount;
    }

    public static int executeAdd(CommandSourceStack source, Entity entity, float amount) throws CommandSyntaxException {
        if (!(entity instanceof LivingEntity livingEntity)) {
            throw REQUIRES_LIVING_ENTITY_EXCEPTION.create();
        }

        float targetSupply = livingEntity.getMana() + amount;
        livingEntity.setMana(targetSupply);
        source.sendSuccess(() -> Component.literal("Added " + amount + " mana for entity ").append(livingEntity.getDisplayName()).append("."), false);
        return (int)targetSupply;
    }

    public static int executeSubtract(CommandSourceStack source, Entity entity, float amount) throws CommandSyntaxException {
        if (!(entity instanceof LivingEntity livingEntity)) {
            throw REQUIRES_LIVING_ENTITY_EXCEPTION.create();
        }

        float targetSupply = livingEntity.getMana() - amount;
        livingEntity.setMana(targetSupply);
        source.sendSuccess(() -> Component.literal("Subtracted " + amount + " mana for entity ").append(livingEntity.getDisplayName()).append("."), false);
        return (int)targetSupply;
    }
}

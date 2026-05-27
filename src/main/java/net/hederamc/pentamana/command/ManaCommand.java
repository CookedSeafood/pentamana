package net.hederamc.pentamana.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.hederamc.fishbonetrehalose.api.Text;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class ManaCommand {
    private static final SimpleCommandExceptionType REQUIRES_LIVING_ENTITY_EXCEPTION = new SimpleCommandExceptionType(
            Text.literal("A living entity is required to run this command here"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(
                Commands.literal("mana")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(
                                Commands.literal("get")
                                        .executes(
                                                context -> get(
                                                            context.getSource()))
                                        .then(
                                                Commands.argument("entity", EntityArgument.entity())
                                                        .executes(
                                                                context -> get(
                                                                        context.getSource(),
                                                                        EntityArgument.getEntity(context, "entity")))))
                        .then(
                                Commands.literal("set")
                                        .then(
                                                Commands.argument("entity", EntityArgument.entity())
                                                        .then(
                                                                Commands.argument("amount", FloatArgumentType.floatArg())
                                                                        .executes(
                                                                                context -> set(
                                                                                        context.getSource(),
                                                                                        EntityArgument.getEntity(context, "entity"),
                                                                                        FloatArgumentType.getFloat(context, "amount"))))))
                        .then(
                                Commands.literal("add")
                                        .then(
                                                Commands.argument("entity", EntityArgument.entity())
                                                        .then(
                                                                Commands.argument("amount", FloatArgumentType.floatArg())
                                                                        .executes(
                                                                                context -> add(
                                                                                        context.getSource(),
                                                                                        EntityArgument.getEntity(context, "entity"),
                                                                                        FloatArgumentType.getFloat(context, "amount"))))))
                        .then(
                                Commands.literal("subtract")
                                        .then(
                                                Commands.argument("entity", EntityArgument.entity())
                                                        .then(
                                                                Commands.argument("amount", FloatArgumentType.floatArg())
                                                                        .executes(
                                                                                context -> subtract(
                                                                                        context.getSource(),
                                                                                        EntityArgument.getEntity(context, "entity"),
                                                                                        FloatArgumentType.getFloat(context, "amount")))))));
    }

    public static int get(CommandSourceStack source) throws CommandSyntaxException {
        return get(source, source.getEntityOrException());
    }

    public static int get(CommandSourceStack source, Entity entity) throws CommandSyntaxException {
        if (!(entity instanceof LivingEntity livingEntity)) {
            throw REQUIRES_LIVING_ENTITY_EXCEPTION.create();
        }

        float supply = livingEntity.getMana();
        source.sendSuccess(
                () -> Text.fromEmpty()
                        .append(livingEntity.getDisplayName())
                        .append(" has " + supply + " mana"),
                false);
        return (int) supply;
    }

    public static int set(CommandSourceStack source, Entity entity, float amount) throws CommandSyntaxException {
        if (!(entity instanceof LivingEntity livingEntity)) {
            throw REQUIRES_LIVING_ENTITY_EXCEPTION.create();
        }

        livingEntity.setMana(amount);
        source.sendSuccess(
                () -> Text.fromEmpty()
                        .append("Set mana for entity ")
                        .append(livingEntity.getDisplayName())
                        .append(" to " + amount),
                false);
        return (int) amount;
    }

    public static int add(CommandSourceStack source, Entity entity, float amount) throws CommandSyntaxException {
        if (!(entity instanceof LivingEntity livingEntity)) {
            throw REQUIRES_LIVING_ENTITY_EXCEPTION.create();
        }

        float targetSupply = livingEntity.getMana() + amount;
        livingEntity.setMana(targetSupply);
        source.sendSuccess(
                () -> Text.fromEmpty()
                        .append("Added " + amount + " mana for entity ")
                        .append(livingEntity.getDisplayName()),
                false);
        return (int) targetSupply;
    }

    public static int subtract(CommandSourceStack source, Entity entity, float amount) throws CommandSyntaxException {
        if (!(entity instanceof LivingEntity livingEntity)) {
            throw REQUIRES_LIVING_ENTITY_EXCEPTION.create();
        }

        float targetSupply = livingEntity.getMana() - amount;
        livingEntity.setMana(targetSupply);
        source.sendSuccess(
                () -> Text.fromEmpty()
                        .append("Subtracted " + amount + " mana for entity ")
                        .append(livingEntity.getDisplayName()),
                false);
        return (int) targetSupply;
    }
}

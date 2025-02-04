package org.charcoalwhite.pentamana;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ColorArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.charcoalwhite.pentamana.api.ConsumeManaCallback;
import org.charcoalwhite.pentamana.api.RegenManaCallback;
import org.charcoalwhite.pentamana.api.TickManaCallback;

public class ManaCommand {
    public ManaCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(
            CommandManager.literal("mana")
            .then(
                CommandManager.literal("enable")
                .executes(context -> {
                    return executeEnable((ServerCommandSource)context.getSource());
                })
            )
            .then(
                CommandManager.literal("disable")
                .executes(context -> {
                    return executeDisable((ServerCommandSource)context.getSource());
                })
            )
            .then(
                CommandManager.literal("color")
                .then(
                    CommandManager.argument("value", ColorArgumentType.color())
                    .executes(context -> {
                        return executeColor((ServerCommandSource)context.getSource(), ColorArgumentType.getColor(context, "value"));
                    })
                )
            )
            .then(
                CommandManager.literal("character")
                .then(
                    CommandManager.literal("full")
                    .then(
                        CommandManager.argument("full", TextArgumentType.text(registryAccess))
                        .executes(context -> {
                            return executeCharacterFull((ServerCommandSource)context.getSource(), TextArgumentType.getTextArgument(context, "full"));
                        })
                    )
                )
                .then(
                    CommandManager.literal("half")
                    .then(
                        CommandManager.argument("half", TextArgumentType.text(registryAccess))
                        .executes(context -> {
                            return executeCharacterHalf((ServerCommandSource)context.getSource(), TextArgumentType.getTextArgument(context, "half"));
                        })
                    )
                )
                .then(
                    CommandManager.literal("zero")
                    .then(
                        CommandManager.argument("zero", TextArgumentType.text(registryAccess))
                        .executes(context -> {
                            return executeCharacterZero((ServerCommandSource)context.getSource(), TextArgumentType.getTextArgument(context, "zero"));
                        })
                    )
                )
            )
            .then(
                CommandManager.literal("reset")
                .executes(context -> {
                    return executeReset((ServerCommandSource)context.getSource());
                })
            )
        );
    }

    public static int executeEnable(ServerCommandSource source) throws CommandSyntaxException {
        return executeSetEnabled(source, 1);
    }

    public static int executeDisable(ServerCommandSource source) throws CommandSyntaxException {
        return executeSetEnabled(source, 0);
    }

    public static int executeColor(ServerCommandSource source, Formatting color) {
        return 1;
    }

    public static int executeCharacterFull(ServerCommandSource source, Text full_charge) {
        return 1;
    }

    public static int executeCharacterHalf(ServerCommandSource source, Text half_charge) {
        return 1;
    }

    public static int executeCharacterZero(ServerCommandSource source, Text zero_charge) {
        return 1;
    }

    public static int executeReset(ServerCommandSource source) {
        return 1;
    }

	public static int executeTick(ServerCommandSource source) throws CommandSyntaxException {
        if (executeGetEnabled(source) != 1) {
            return 0;
        }

		executeIncrementManabarLife(source);

        ServerPlayerEntity player = source.getPlayerOrThrow();
		executeSetManaCapacity(source, Pentamana.manaCapacityBase + Pentamana.manaCapacityIncrementBase * player.getWeaponStack().getEnchantments().getLevel("pentamana:capacity"));

		TickManaCallback.EVENT.invoker().interact(player);

		int mana = executeGetMana(source);
		int manaCapacity = executeGetManaCapacity(source);
		if (mana < manaCapacity && mana >= 0) {
			executeRegen(source);
            executeUpdate(source);
            return 2;
		} else if (mana != manaCapacity) {
			executeSetMana(source, manaCapacity);
            executeUpdate(source);
            return 3;
		} else if (executeGetManabarLife(source) >= 0) {
			executeUpdate(source);
            return 1;
		}

        return 0;
    };

    /*
     * Performence Consideration:
     * It does not check if regen is needed and always modifies a scoreboard score.
     */
    public static int executeRegen(ServerCommandSource source) throws CommandSyntaxException {
		int result = 1;

        ServerPlayerEntity player = source.getPlayerOrThrow();
		executeSetManaRegen(source, Pentamana.manaRegenBase + Pentamana.manaRegenIncrementBase * player.getWeaponStack().getEnchantments().getLevel("pentamana:emanation"));

		RegenManaCallback.EVENT.invoker().interact(player);

		int mana = executeGetMana(source);
		int manaCapacity = executeGetManaCapacity(source);
		int manaRegen = executeGetManaRegen(source);
		mana += manaRegen;
		if (mana > manaCapacity || mana < 0) {
			mana = manaCapacity;
			result = 2;
		}

		executeSetMana(source, mana);
		return result;
    }
	
	public static int executeConsume(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
		executeSetManaConsume(source, executeGetManaConsume(source) * (10 - player.getWeaponStack().getEnchantments().getLevel("pentamana:utilization")) / 10);

		ConsumeManaCallback.EVENT.invoker().interact(player);

		int mana = executeGetMana(source);
		int manaConsume = executeGetManaConsume(source);
		mana -= manaConsume;
		if (mana >= 0) {
			executeSetMana(source, mana);
			executeUpdate(source);
			return 1;
		}

		return 0;
	}

    public static int executeUpdate(ServerCommandSource source) throws CommandSyntaxException {
        int manabarLife = executeGetManabarLife(source);
        if (manabarLife > 0 && manabarLife < Pentamana.maxManabarLife) {
            return 0;
        }

		executeSetManabarLife(source, -Pentamana.maxManabarLife);
		
		int mana = executeGetMana(source);
		int manaCapacity = executeGetManaCapacity(source);
		mana = (-mana - 1) / -Pentamana.manaScale;
		manaCapacity = (-manaCapacity - 1) / -Pentamana.manaScale;

		StringBuilder manabar = new StringBuilder();
		for (int i = mana / 2; i > 0; --i) {
			manabar.append(Pentamana.manaCharFull);
		}

		if (mana % 2 == 1 && mana != manaCapacity) {
			manabar.append(Pentamana.manaCharHalf);
		}

		for (int i = (manaCapacity - mana - mana % 2) / 2; i > 0; --i) {
			manabar.append(Pentamana.manaCharZero);
		}

		source.getPlayerOrThrow().sendMessage(Text.literal(manabar.toString()).formatted(Pentamana.manaColor), true);
        return manabarLife;
    }

    public static int executeGetMana(ServerCommandSource source) throws CommandSyntaxException {
		Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana", ScoreboardCriterion.DUMMY, Text.of("Mana"), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementMana(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana", ScoreboardCriterion.DUMMY, Text.of("Mana"), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementMana(ServerCommandSource source) throws CommandSyntaxException {
        return executeIncrementMana(source, 1);
    }

    public static int executeSetMana(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana", ScoreboardCriterion.DUMMY, Text.of("Mana"), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetMana(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana", ScoreboardCriterion.DUMMY, Text.of("Mana"), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
        return 0;
    }

    public static int executeGetManaCapacity(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_capacity", ScoreboardCriterion.DUMMY, Text.of("Mana Capacity"), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementManaCapacity(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_capacity", ScoreboardCriterion.DUMMY, Text.of("Mana Capacity"), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementManaCapacity(ServerCommandSource source) throws CommandSyntaxException {
        return executeIncrementManaCapacity(source, 1);
    }

    public static int executeSetManaCapacity(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_capacity", ScoreboardCriterion.DUMMY, Text.of("Mana Capacity"), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetManaCapacity(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_capacity", ScoreboardCriterion.DUMMY, Text.of("Mana Capacity"), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
        return 0;
    }

    public static int executeGetManaRegen(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_regen", ScoreboardCriterion.DUMMY, Text.of("Mana Regen"), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementManaRegen(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_regen", ScoreboardCriterion.DUMMY, Text.of("Mana Regen"), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementManaRegen(ServerCommandSource source) throws CommandSyntaxException {
        return executeIncrementManaRegen(source, 1);
    }

    public static int executeSetManaRegen(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_regen", ScoreboardCriterion.DUMMY, Text.of("Mana Regen"), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetManaRegen(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_regen", ScoreboardCriterion.DUMMY, Text.of("Mana Regen"), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
        return 0;
    }

    public static int executeGetManaConsume(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_consume", ScoreboardCriterion.DUMMY, Text.of("Mana Consume"), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementManaConsume(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_consume", ScoreboardCriterion.DUMMY, Text.of("Mana Consume"), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementManaConsume(ServerCommandSource source) throws CommandSyntaxException {
        return executeIncrementManaConsume(source, 1);
    }

    public static int executeSetManaConsume(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_consume", ScoreboardCriterion.DUMMY, Text.of("Mana Consume"), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetManaConsume(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_consume", ScoreboardCriterion.DUMMY, Text.of("Mana Consume"), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
        return 0;
    }

    public static int executeGetManabarLife(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.manabar_life", ScoreboardCriterion.DUMMY, Text.of("Manabar Life"), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementManabarLife(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.manabar_life", ScoreboardCriterion.DUMMY, Text.of("Manabar Life"), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementManabarLife(ServerCommandSource source) throws CommandSyntaxException {
        return executeIncrementManabarLife(source, 1);
    }

    public static int executeSetManabarLife(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.manabar_life", ScoreboardCriterion.DUMMY, Text.of("Manabar Life"), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetManabarLife(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.manabar_life", ScoreboardCriterion.DUMMY, Text.of("Manabar Life"), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
        return 0;
    }

    public static int executeGetEnabled(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.enabled", ScoreboardCriterion.DUMMY, Text.of("Enabled"), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementEnabled(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.enabled", ScoreboardCriterion.DUMMY, Text.of("Enabled"), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementEnabled(ServerCommandSource source) throws CommandSyntaxException {
        return executeIncrementEnabled(source, 1);
    }

    public static int executeSetEnabled(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.enabled", ScoreboardCriterion.DUMMY, Text.of("Enabled"), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetEnabled(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.enabled", ScoreboardCriterion.DUMMY, Text.of("Enabled"), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
        return 0;
    }
}

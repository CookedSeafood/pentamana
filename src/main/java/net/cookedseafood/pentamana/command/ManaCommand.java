package net.cookedseafood.pentamana.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Set;
import java.util.stream.Collectors;
import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.api.ConsumeManaCallback;
import net.cookedseafood.pentamana.api.RegenManaCallback;
import net.cookedseafood.pentamana.api.TickManaCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ColorArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.mutable.MutableDouble;

public class ManaCommand {
    private static final SimpleCommandExceptionType NOT_PLAIN_TEXT_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Not a plain text."));
    private static final SimpleCommandExceptionType MULTIPLE_CHARACTER_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Not a single character."));
    private static final SimpleCommandExceptionType OPTION_ALREADY_ENABLED_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Nothing changed. Mana is already enabled for that player."));
    private static final SimpleCommandExceptionType OPTION_ALREADY_DISABLED_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Nothing changed. Mana is already disbaled for that player."));
    private static final SimpleCommandExceptionType OPTION_DISPLAY_ALREADY_TRUE_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Nothing changed. Mana Display is already set to true for that player"));
    private static final SimpleCommandExceptionType OPTION_DISPLAY_ALREADY_FALSE_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Nothing changed. Mana Display is already set to false for that player"));
    private static final SimpleCommandExceptionType OPTION_CHARACTER_FULL_UNCHANGED_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Nothing changed. That player already has that mana character of 2 points mana."));
    private static final SimpleCommandExceptionType OPTION_CHARACTER_HALF_UNCHANGED_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Nothing changed. That player already has that mana character of 1 point mana."));
    private static final SimpleCommandExceptionType OPTION_CHARACTER_ZERO_UNCHANGED_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Nothing changed. That player already has that mana character of 0 point mana."));
    private static final SimpleCommandExceptionType OPTION_COLOR_FULL_UNCHANGED_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Nothing changed. That player already has that mana color of 2 points mana."));
    private static final SimpleCommandExceptionType OPTION_COLOR_HALF_UNCHANGED_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Nothing changed. That player already has that mana color of 1 point mana."));
    private static final SimpleCommandExceptionType OPTION_COLOR_ZERO_UNCHANGED_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Nothing changed. That player already has that mana color of 0 point mana."));

    public ManaCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(
            CommandManager.literal("mana")
            .then(
                CommandManager.literal("enable")
                .executes(context -> executeEnable((ServerCommandSource)context.getSource()))
            )
            .then(
                CommandManager.literal("disable")
                .executes(context -> executeDisable((ServerCommandSource)context.getSource()))
            )
            .then(
                CommandManager.literal("set")
                .then(
                    CommandManager.literal("display")
                    .then(
                        CommandManager.literal("true")
                        .executes(context -> executeSetDisplayTrue((ServerCommandSource)context.getSource()))
                    )
                    .then(
                        CommandManager.literal("false")
                        .executes(context -> executeSetDisplayFalse((ServerCommandSource)context.getSource()))
                    )
                )
                .then(
                    CommandManager.literal("character")
                    .then(
                        CommandManager.literal("full")
                        .then(
                            CommandManager.argument("text", TextArgumentType.text(registryAccess))
                            .executes(context -> executeSetCharacterFull((ServerCommandSource)context.getSource(), TextArgumentType.getTextArgument(context, "text")))
                        )
                    )
                    .then(
                        CommandManager.literal("half")
                        .then(
                            CommandManager.argument("text", TextArgumentType.text(registryAccess))
                            .executes(context -> executeSetCharacterHalf((ServerCommandSource)context.getSource(), TextArgumentType.getTextArgument(context, "text")))
                        )
                    )
                    .then(
                        CommandManager.literal("zero")
                        .then(
                            CommandManager.argument("text", TextArgumentType.text(registryAccess))
                            .executes(context -> executeSetCharacterZero((ServerCommandSource)context.getSource(), TextArgumentType.getTextArgument(context, "text")))
                        )
                    )
                )
                .then(
                    CommandManager.literal("color")
                    .then(
                        CommandManager.literal("full")
                        .then(
                            CommandManager.argument("value", ColorArgumentType.color())
                            .executes(context -> executeSetColorFull((ServerCommandSource)context.getSource(), ColorArgumentType.getColor(context, "value")))
                        )
                    )
                    .then(
                        CommandManager.literal("half")
                        .then(
                            CommandManager.argument("value", ColorArgumentType.color())
                            .executes(context -> executeSetColorHalf((ServerCommandSource)context.getSource(), ColorArgumentType.getColor(context, "value")))
                        )
                    )
                    .then(
                        CommandManager.literal("zero")
                        .then(
                            CommandManager.argument("value", ColorArgumentType.color())
                            .executes(context -> executeSetColorZero((ServerCommandSource)context.getSource(), ColorArgumentType.getColor(context, "value")))
                        )
                    )
                )
            )
            .then(
                CommandManager.literal("reset")
                .executes(context -> executeReset((ServerCommandSource)context.getSource()))
            )
            .then(
                CommandManager.literal("reload")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> executeReload((ServerCommandSource)context.getSource()))
            )
            .then(
                CommandManager.literal("version")
                .executes(context -> executeVersion((ServerCommandSource)context.getSource()))
            )
        );
    }

    public static int executeEnable(ServerCommandSource source) throws CommandSyntaxException {
        String name = source.getPlayerOrThrow().getNameForScoreboard();
        if (executeGetEnabled(source) != 1) {
            source.sendFeedback(() -> Text.literal("Enabled mana for player " + name + "."), false);
            return executeSetEnabled(source, 1);
        }

        throw OPTION_ALREADY_ENABLED_EXCEPTION.create();
    }

    public static int executeDisable(ServerCommandSource source) throws CommandSyntaxException {
        if (Pentamana.forceEnabled) {
            source.sendFeedback(() -> Text.literal("Mana calculation will continue due to the force enabled mode is turned on in server."), false);
        }

        String name = source.getPlayerOrThrow().getNameForScoreboard();
        if (executeGetEnabled(source) == 1) {
            source.sendFeedback(() -> Text.literal("Disabled mana for player " + name + "."), false);
            return executeSetEnabled(source, 0);
        }

        throw OPTION_ALREADY_DISABLED_EXCEPTION.create();
    }

    public static int executeSetDisplayTrue(ServerCommandSource source) throws CommandSyntaxException {
        String name = source.getPlayerOrThrow().getNameForScoreboard();
        if (executeGetDisplay(source) != 1) {
            source.sendFeedback(() -> Text.literal("Updated the mana display for player " + name + " to True."), false);
            return executeSetDisplay(source, 1);
        }

        throw OPTION_DISPLAY_ALREADY_TRUE_EXCEPTION.create();
    }

    public static int executeSetDisplayFalse(ServerCommandSource source) throws CommandSyntaxException {
        String name = source.getPlayerOrThrow().getNameForScoreboard();
        if (executeGetDisplay(source) == 1) {
            source.sendFeedback(() -> Text.literal("Updated the mana display for player " + name + " to False."), false);
            return executeSetDisplay(source, 0);
        }

        throw OPTION_DISPLAY_ALREADY_FALSE_EXCEPTION.create();
    }

    private static int getAsInt(Text text) throws CommandSyntaxException {
        String string = text.getLiteralString();
        if (string == null) {
            throw NOT_PLAIN_TEXT_EXCEPTION.create();
        }

        if (string.codePointCount(0, string.length()) != 1) {
            throw MULTIPLE_CHARACTER_EXCEPTION.create();
        }

        return string.codePointAt(0);
    };

    public static int executeSetCharacterFull(ServerCommandSource source, Text full) throws CommandSyntaxException {
        int manaCharFull = getAsInt(full);

        String name = source.getPlayerOrThrow().getNameForScoreboard();
        if (executeGetManaCharFull(source) != manaCharFull) {
            source.sendFeedback(() -> Text.literal("Updated the mana character of 2 point mana for player " + name + " to " + full.getLiteralString() + "."), false);
            return executeSetManaCharFull(source, manaCharFull);
        }

        throw OPTION_CHARACTER_FULL_UNCHANGED_EXCEPTION.create();
    }

    public static int executeSetCharacterHalf(ServerCommandSource source, Text half) throws CommandSyntaxException {
        int manaCharHalf = getAsInt(half);

        String name = source.getPlayerOrThrow().getNameForScoreboard();
        if (executeGetManaCharHalf(source) != manaCharHalf) {
            source.sendFeedback(() -> Text.literal("Updated the mana character of 1 point mana for player " + name + " to " + half.getLiteralString() + "."), false);
            return executeSetManaCharHalf(source, manaCharHalf);
        }

        throw OPTION_CHARACTER_HALF_UNCHANGED_EXCEPTION.create();
    }

    public static int executeSetCharacterZero(ServerCommandSource source, Text zero) throws CommandSyntaxException {
        int manaCharZero = getAsInt(zero);

        String name = source.getPlayerOrThrow().getNameForScoreboard();
        if (executeGetManaCharZero(source) != manaCharZero) {
            source.sendFeedback(() -> Text.literal("Updated the mana character of 0 point mana for player " + name + " to " + zero.getLiteralString() + "."), false);
            return executeSetManaCharZero(source, manaCharZero);
        }

        throw OPTION_CHARACTER_ZERO_UNCHANGED_EXCEPTION.create();
    }

    public static int executeSetColorFull(ServerCommandSource source, Formatting colorFull) throws CommandSyntaxException {
        int manaColorFull = colorFull.getColorIndex() + 1;

        String name = source.getPlayerOrThrow().getNameForScoreboard();
        if (executeGetManaColorFull(source) != manaColorFull) {
            source.sendFeedback(() -> Text.literal("Updated the mana color for player " + name + " to " + colorFull.getName() + "."), false);
            return executeSetManaColorFull(source, manaColorFull);
        }

        throw OPTION_COLOR_FULL_UNCHANGED_EXCEPTION.create();
    }

    public static int executeSetColorHalf(ServerCommandSource source, Formatting colorHalf) throws CommandSyntaxException {
        int manaColorHalf = colorHalf.getColorIndex() + 1;

        String name = source.getPlayerOrThrow().getNameForScoreboard();
        if (executeGetManaColorHalf(source) != manaColorHalf) {
            source.sendFeedback(() -> Text.literal("Updated the mana color for player " + name + " to " + colorHalf.getName() + "."), false);
            return executeSetManaColorHalf(source, manaColorHalf);
        }

        throw OPTION_COLOR_HALF_UNCHANGED_EXCEPTION.create();
    }

    public static int executeSetColorZero(ServerCommandSource source, Formatting colorZero) throws CommandSyntaxException {
        int manaColorZero = colorZero.getColorIndex() + 1;

        String name = source.getPlayerOrThrow().getNameForScoreboard();
        if (executeGetManaColorZero(source) != manaColorZero) {
            source.sendFeedback(() -> Text.literal("Updated the mana color for player " + name + " to " + colorZero.getName() + "."), false);
            return executeSetManaColorZero(source, manaColorZero);
        }

        throw OPTION_COLOR_ZERO_UNCHANGED_EXCEPTION.create();
    }

    public static int executeReset(ServerCommandSource source) throws CommandSyntaxException {
        String name = source.getPlayerOrThrow().getNameForScoreboard();
        source.sendFeedback(() -> Text.literal("Reset mana options for player " + name + "."), false);
        executeResetManaCharFull(source);
        executeResetManaCharHalf(source);
        executeResetManaCharZero(source);
        executeResetManaColorFull(source);
        executeResetManaColorHalf(source);
        executeResetManaColorZero(source);
        return 0;
    }

    public static int executeReload(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("Reloading Pentamana!"), true);
        return Pentamana.reload();
	}

    public static int executeVersion(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("Pentamana " + Pentamana.VERSION_MAJOR + "." + Pentamana.VERSION_MINOR + "." + Pentamana.VERSION_PATCH + (Pentamana.forceEnabled ? " (Force Enabled Mode)" : "")), false);
        return 0;
    }

	public static int executeTick(ServerCommandSource source) throws CommandSyntaxException {
        if (!Pentamana.forceEnabled && executeGetEnabled(source) != 1) {
            return 0;
        }

		executeIncrementManabarLife(source);

		executeSetManaCapacity(source, executeCalcManaCapacitySettled(source));

		TickManaCallback.EVENT.invoker().interact(source.getPlayerOrThrow());

		int mana = executeGetMana(source);
		int manaCapacity = executeGetManaCapacity(source);
		if (mana < manaCapacity && mana >= 0) {
			executeRegen(source);
            executeDisplay(source);
            return 2;
		}

        if (mana != manaCapacity) {
			executeSetMana(source, manaCapacity);
            executeDisplay(source);
            return 3;
		}

        if (executeGetManabarLife(source) >= 0) {
			executeDisplay(source);
            return 1;
		}

        return 0;
    };

    /**
     * Performence Consideration: There is no check for full capacity. Will alaways modify a scoreboard score when called.
     */
    public static int executeRegen(ServerCommandSource source) throws CommandSyntaxException {
		int result = 1;

		executeSetManaRegen(source, executeCalcManaRegenSettled(source));

		RegenManaCallback.EVENT.invoker().interact(source.getPlayerOrThrow());

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
		executeSetManaConsum(source, executeCalcManaConsumSettled(source));

		ConsumeManaCallback.EVENT.invoker().interact(source.getPlayerOrThrow());

		int mana = executeGetMana(source);
		int manaConsume = executeGetManaConsum(source);
		mana -= manaConsume;
		if (mana >= 0) {
			executeSetMana(source, mana);
			return 1;
		}

		return 0;
	}

    public static int executeDisplay(ServerCommandSource source) throws CommandSyntaxException {
        if (executeGetDisplay(source) != 1) {
            return 0;
        }

        int manabarLife = executeGetManabarLife(source);
        if (manabarLife > 0 && manabarLife < Pentamana.maxManabarLife) {
            return 0;
        }
		
		int mana = executeGetMana(source);
		int manaCapacity = executeGetManaCapacity(source);
		byte manaPoint = (byte)((-mana - 1) / -Pentamana.manaScale);
		byte manaCapacityPoint = (byte)((-manaCapacity - 1) / -Pentamana.manaScale);
        byte manaPointTrimmed = (byte)(manaPoint - manaPoint % 2);
        byte manaCapacityPointTrimmed = (byte)(manaCapacityPoint - manaCapacityPoint % 2);


        executeSetManabarLife(source, -Pentamana.maxManabarLife);

        int manaCharFull = executeGetManaCharFull(source);
        char[] charFull =
            manaCharFull == 0 ?
            Pentamana.manaCharFull :
            Character.toChars(manaCharFull);
        int manaCharHalf = executeGetManaCharHalf(source);
        char[] charHalf =
            manaCharHalf == 0 ?
            Pentamana.manaCharHalf :
            Character.toChars(manaCharHalf);
        int manaCharZero = executeGetManaCharZero(source);
        char[] charZero =
            manaCharZero == 0 ?
            Pentamana.manaCharZero :
            Character.toChars(manaCharZero);
        int manaColorFull = executeGetManaColorFull(source);
        Formatting colorFull =
            manaColorFull == 0 ?
            Pentamana.manaColorFull :
            Formatting.byColorIndex(manaColorFull - 1);
        int manaColorHalf = executeGetManaColorHalf(source);
        Formatting colorHalf =
            manaColorHalf == 0 ?
            Pentamana.manaColorHalf :
            Formatting.byColorIndex(manaColorHalf - 1);
        int manaColorZero = executeGetManaColorZero(source);
        Formatting colorZero =
            manaColorZero == 0 ?
            Pentamana.manaColorZero :
            Formatting.byColorIndex(manaColorZero - 1);

        MutableText manabar = MutableText.of(PlainTextContent.EMPTY);
        for (byte i = 0; i < manaCapacityPointTrimmed; i += 2) {
            char[] manaChar = i < manaPointTrimmed ?
                charFull : i < manaPoint ?
                    charHalf : charZero;
            Formatting manaColor = i < manaPointTrimmed ?
                colorFull : i < manaPoint ?
                    colorHalf : colorZero;
            manabar.append(Text.literal(String.valueOf(manaChar)).formatted(manaColor));
        }

		source.getPlayerOrThrow().sendMessage(manabar, true);
        return manabarLife;
    }

    private static NbtList getModifiers(ServerPlayerEntity player) {
        NbtList modifiers = new NbtList();
        player.getEquippedItems().forEach(stack -> modifiers.addAll(stack.getCustomModifiers()));
        return modifiers;
    }

    private static int getModified(int base, Set<NbtCompound> modifiers) {
        MutableDouble modified = new MutableDouble(base);

        modifiers.stream()
            .filter(modifier -> "add_value".equals(modifier.getString("operation")))
            .forEach(modifier -> modified.add(modifier.getDouble("base")));

        MutableDouble multiplier = new MutableDouble(1);

        modifiers.stream()
            .filter(modifier -> "add_multiplied_base".equals(modifier.getString("operation")))
            .forEach(modifier -> multiplier.add(modifier.getDouble("base")));

        modified.setValue(modified.getValue() * multiplier.getValue());

        modifiers.stream()
            .filter(modifier -> "add_multiplied_total".equals(modifier.getString("operation")))
            .forEach(modifier -> modified.setValue(modifier.getDouble("base") * modified.getValue()));

        return modified.getValue().intValue();
    }

    public static int executeCalcManaCapacityModified(ServerCommandSource source) throws CommandSyntaxException {
        NbtList modifiers = getModifiers(source.getPlayerOrThrow());
        return modifiers.isEmpty() ?
            Pentamana.manaCapacityBase :
            getModified(Pentamana.manaCapacityBase, modifiers.stream().map(nbtElement -> (NbtCompound)nbtElement).filter(modifier -> "pentamana:mana_capacity".equals(modifier.getString("attribute"))).collect(Collectors.toUnmodifiableSet()));
    }

    public static int executeCalcManaRegenModified(ServerCommandSource source) throws CommandSyntaxException {
        NbtList modifiers = getModifiers(source.getPlayerOrThrow());
        return modifiers.isEmpty() ?
            Pentamana.manaRegenBase :
            getModified(Pentamana.manaRegenBase, modifiers.stream().map(nbtElement -> (NbtCompound)nbtElement).filter(modifier -> "pentamana:mana_regeneration".equals(modifier.getString("attribute"))).collect(Collectors.toUnmodifiableSet()));
    }

    public static int executeCalcManaConsumModified(ServerCommandSource source) throws CommandSyntaxException {
        NbtList modifiers = getModifiers(source.getPlayerOrThrow());
        return modifiers.isEmpty() ?
            executeGetManaConsum(source) :
            getModified(executeGetManaConsum(source), modifiers.stream().map(nbtElement -> (NbtCompound)nbtElement).filter(modifier -> "pentamana:mana_consumption".equals(modifier.getString("attribute"))).collect(Collectors.toUnmodifiableSet()));
    }

    public static int executeCalcManaCapacitySettled(ServerCommandSource source) throws CommandSyntaxException {
        return executeCalcManaCapacityModified(source) + source.getPlayerOrThrow().getWeaponStack().getEnchantments().getLevel("pentamana:capacity") * Pentamana.manaCapacityIncrementBase;
    }

    public static int executeCalcManaRegenSettled(ServerCommandSource source) throws CommandSyntaxException {
        return executeCalcManaRegenModified(source) + source.getPlayerOrThrow().getWeaponStack().getEnchantments().getLevel("pentamana:steam") * Pentamana.manaRegenIncrementBase;
    }

    public static int executeCalcManaConsumSettled(ServerCommandSource source) throws CommandSyntaxException {
        return executeCalcManaConsumModified(source) * (10 - source.getPlayerOrThrow().getWeaponStack().getEnchantments().getLevel("pentamana:utilization")) / 10;
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
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_regeneration", ScoreboardCriterion.DUMMY, Text.of("Mana Regen"), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementManaRegen(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_regeneration", ScoreboardCriterion.DUMMY, Text.of("Mana Regen"), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementManaRegen(ServerCommandSource source) throws CommandSyntaxException {
        return executeIncrementManaRegen(source, 1);
    }

    public static int executeSetManaRegen(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_regeneration", ScoreboardCriterion.DUMMY, Text.of("Mana Regen"), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetManaRegen(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_regeneration", ScoreboardCriterion.DUMMY, Text.of("Mana Regen"), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
        return 0;
    }

    public static int executeGetManaConsum(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_consumption", ScoreboardCriterion.DUMMY, Text.of("Mana Consume"), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementManaConsum(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_consumption", ScoreboardCriterion.DUMMY, Text.of("Mana Consume"), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementManaConsum(ServerCommandSource source) throws CommandSyntaxException {
        return executeIncrementManaConsum(source, 1);
    }

    public static int executeSetManaConsum(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_consumption", ScoreboardCriterion.DUMMY, Text.of("Mana Consume"), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetManaConsum(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_consumption", ScoreboardCriterion.DUMMY, Text.of("Mana Consume"), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
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

    public static int executeGetManaCharFull(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_char_full", ScoreboardCriterion.DUMMY, Text.of("Mana Char Full"), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementManaCharFull(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_char_full", ScoreboardCriterion.DUMMY, Text.of("Mana Char Full"), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementManaCharFull(ServerCommandSource source) throws CommandSyntaxException {
        return executeIncrementManaCharFull(source, 1);
    }

    public static int executeSetManaCharFull(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_char_full", ScoreboardCriterion.DUMMY, Text.of("Mana Char Full"), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetManaCharFull(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_char_full", ScoreboardCriterion.DUMMY, Text.of("Mana Char Full"), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
        return 0;
    }

    public static int executeGetManaCharHalf(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_char_half", ScoreboardCriterion.DUMMY, Text.of("Mana Char Half"), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementManaCharHalf(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_char_half", ScoreboardCriterion.DUMMY, Text.of("Mana Char Half"), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementManaCharHalf(ServerCommandSource source) throws CommandSyntaxException {
        return executeIncrementManaCharHalf(source, 1);
    }

    public static int executeSetManaCharHalf(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_char_half", ScoreboardCriterion.DUMMY, Text.of("Mana Char Half"), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetManaCharHalf(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_char_half", ScoreboardCriterion.DUMMY, Text.of("Mana Char Half"), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
        return 0;
    }

    public static int executeGetManaCharZero(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_char_zero", ScoreboardCriterion.DUMMY, Text.of("Mana Char Zero"), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementManaCharZero(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_char_zero", ScoreboardCriterion.DUMMY, Text.of("Mana Char Zero"), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementManaCharZero(ServerCommandSource source) throws CommandSyntaxException {
        return executeIncrementManaCharZero(source, 1);
    }

    public static int executeSetManaCharZero(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_char_zero", ScoreboardCriterion.DUMMY, Text.of("Mana Char Zero"), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetManaCharZero(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_char_zero", ScoreboardCriterion.DUMMY, Text.of("Mana Char Zero"), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
        return 0;
    }

    public static int executeGetManaColorFull(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_color_full", ScoreboardCriterion.DUMMY, Text.of("Mana Color Full"), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementManaColorFull(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_color_full", ScoreboardCriterion.DUMMY, Text.of("Mana Color Full"), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementManaColorFull(ServerCommandSource source) throws CommandSyntaxException {
        return executeIncrementManaColorFull(source, 1);
    }

    public static int executeSetManaColorFull(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_color_full", ScoreboardCriterion.DUMMY, Text.of("Mana Color Full"), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetManaColorFull(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_color_full", ScoreboardCriterion.DUMMY, Text.of("Mana Color Full"), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
        return 0;
    }

    public static int executeGetManaColorHalf(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_color_half", ScoreboardCriterion.DUMMY, Text.of("Mana Color Half"), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementManaColorHalf(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_color_half", ScoreboardCriterion.DUMMY, Text.of("Mana Color Half"), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementManaColorHalf(ServerCommandSource source) throws CommandSyntaxException {
        return executeIncrementManaColorHalf(source, 1);
    }

    public static int executeSetManaColorHalf(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_color_half", ScoreboardCriterion.DUMMY, Text.of("Mana Color Half"), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetManaColorHalf(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_color_half", ScoreboardCriterion.DUMMY, Text.of("Mana Color Half"), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
        return 0;
    }

    public static int executeGetManaColorZero(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_color_zero", ScoreboardCriterion.DUMMY, Text.of("Mana Color Zero"), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementManaColorZero(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_color_zero", ScoreboardCriterion.DUMMY, Text.of("Mana Color Zero"), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementManaColorZero(ServerCommandSource source) throws CommandSyntaxException {
        return executeIncrementManaColorZero(source, 1);
    }

    public static int executeSetManaColorZero(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_color_zero", ScoreboardCriterion.DUMMY, Text.of("Mana Color Zero"), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetManaColorZero(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_color_zero", ScoreboardCriterion.DUMMY, Text.of("Mana Color Zero"), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
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

    public static int executeGetDisplay(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.display", ScoreboardCriterion.DUMMY, Text.of("Display"), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementDisplay(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.display", ScoreboardCriterion.DUMMY, Text.of("Display"), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementDisplay(ServerCommandSource source) throws CommandSyntaxException {
        return executeIncrementDisplay(source, 1);
    }

    public static int executeSetDisplay(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.display", ScoreboardCriterion.DUMMY, Text.of("Display"), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetDisplay(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.display", ScoreboardCriterion.DUMMY, Text.of("Display"), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
        return 0;
    }
}

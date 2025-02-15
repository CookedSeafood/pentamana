package net.cookedseafood.pentamana.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.api.ConsumeManaCallback;
import net.cookedseafood.pentamana.api.RegenManaCallback;
import net.cookedseafood.pentamana.api.TickManaCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

public class ManaCommand {
    private static final SimpleCommandExceptionType NO_CHARACTER_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Not contain a character."));
    private static final SimpleCommandExceptionType MULTIPLE_CHARACTER_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Not a single character."));
    private static final SimpleCommandExceptionType OPTION_ALREADY_ENABLED_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Nothing changed. Mana is already enabled for that player."));
    private static final SimpleCommandExceptionType OPTION_ALREADY_DISABLED_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Nothing changed. Mana is already disbaled for that player."));
    private static final DynamicCommandExceptionType OPTION_DISPLAY_UNCHANGED_EXCEPTION =
        new DynamicCommandExceptionType((displayBoolean) -> Text.literal("Nothing changed. Mana display is already set to " + (boolean)displayBoolean + " for that player."));
    private static final DynamicCommandExceptionType OPTION_FORMAT_UNCHANGED_EXCEPTION =
        new DynamicCommandExceptionType((formatString) -> Text.literal("Nothing changed. Mana format is already set to " + (String)formatString + " for that player."));
    private static final Dynamic2CommandExceptionType OPTION_CHARACTER_UNCHANGED_EXCEPTION =
        new Dynamic2CommandExceptionType((manaCharTypeIndex, manaCharIndex) -> Text.literal("Nothing changed. That player already has that" + ((int)manaCharIndex == -1 ? "" : (" #" + (int)manaCharIndex)) + ((int)manaCharTypeIndex == -1 ? "" : (" " + (int)manaCharTypeIndex + " point")) + " mana character."));

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
                        .executes(context -> executeSetDisplay((ServerCommandSource)context.getSource(), true))
                    )
                    .then(
                        CommandManager.literal("false")
                        .executes(context -> executeSetDisplay((ServerCommandSource)context.getSource(), false))
                    )
                )
                .then(
                    CommandManager.literal("render_type")
                    .then(
                        CommandManager.literal("graphic")
                        .executes(context -> executeSetRenderType((ServerCommandSource)context.getSource(), "graphic"))
                    )
                    .then(
                        CommandManager.literal("numberic")
                        .executes(context -> executeSetRenderType((ServerCommandSource)context.getSource(), "numberic"))
                    )
                )
                .then(
                    CommandManager.literal("character")
                    .then(
                        CommandManager.argument("text", TextArgumentType.text(registryAccess))
                        .executes(context -> executeSetCharacter((ServerCommandSource)context.getSource(), TextArgumentType.getTextArgument(context, "text")))
                        .then(
                            CommandManager.argument("type_index", IntegerArgumentType.integer(-1, Integer.MAX_VALUE))
                            .executes(context -> executeSetCharacter((ServerCommandSource)context.getSource(), TextArgumentType.getTextArgument(context, "text"), IntegerArgumentType.getInteger(context, "type_index")))
                            .then(
                                CommandManager.argument("character_index", IntegerArgumentType.integer(-1, Integer.MAX_VALUE))
                                .executes(context -> executeSetCharacter((ServerCommandSource)context.getSource(), TextArgumentType.getTextArgument(context, "text"), IntegerArgumentType.getInteger(context, "type_index"), IntegerArgumentType.getInteger(context, "character_index")))
                            )
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

    public static int executeSetDisplay(ServerCommandSource source, boolean displayBoolean) throws CommandSyntaxException {
        int display = displayBoolean ? 1 : 0;

        String name = source.getPlayerOrThrow().getNameForScoreboard();
        if (executeGetDisplay(source) != display) {
            source.sendFeedback(() -> Text.literal("Updated the mana display for player " + name + " to " + displayBoolean + "."), false);
            return executeSetDisplay(source, display);
        }

        throw OPTION_DISPLAY_UNCHANGED_EXCEPTION.create(displayBoolean);
    }

    public static int executeSetRenderType(ServerCommandSource source, String formatString) throws CommandSyntaxException {
        int format = "numberic".equals(formatString) ? 1 : 0;

        String name = source.getPlayerOrThrow().getNameForScoreboard();
        if (executeGetRenderType(source) != format) {
            source.sendFeedback(() -> Text.literal("Updated the mana format for player " + name + " to " + formatString + "."), false);
            return executeSetRenderType(source, format);
        }

        throw OPTION_FORMAT_UNCHANGED_EXCEPTION.create(formatString);
    }

    public static int executeSetCharacter(ServerCommandSource source, Text manaCharText, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        String manaCharString = manaCharText.getString();
        if (manaCharString == null || manaCharString.isEmpty() || manaCharString.isBlank()) {
            throw NO_CHARACTER_EXCEPTION.create();
        }

        if (manaCharString.codePointCount(0, manaCharString.length()) != 1) {
            throw MULTIPLE_CHARACTER_EXCEPTION.create();
        }

        int manaChar = manaCharString.codePointAt(0);

        Style manaCharTextStyle = manaCharText.getStyle();
        TextColor manaCharTextColor = manaCharTextStyle.getColor();
        int manaColor = manaCharTextColor != null ? manaCharTextColor.getRgb() + 1 : 0;
        int manaBold = manaCharTextStyle.isBold() ? 1 : 2;
        int manaItalic = manaCharTextStyle.isItalic() ? 1 : 2;
        int manaUnderlined = manaCharTextStyle.isUnderlined() ? 1 : 2;
        int manaStrikethrough = manaCharTextStyle.isStrikethrough() ? 1 : 2;
        int manaObfuscated = manaCharTextStyle.isObfuscated() ? 1 : 2;

        int startManaTypeIndex = manaCharTypeIndex == -1 ? 0 : manaCharTypeIndex;
        int endManaTypeIndex = manaCharTypeIndex == -1 ? Pentamana.manaCharTypes : manaCharTypeIndex;/* Exclude */
        int startManaCharIndex = manaCharIndex == -1 ? 0 : manaCharIndex;
        int endManaCharIndex = manaCharIndex == -1 ? Pentamana.maxManaChar : manaCharIndex;/* Exclude */

        int miss = 0;
        for (int t = startManaTypeIndex; t < endManaTypeIndex; ++t) {
            for (int i = startManaCharIndex; i < endManaCharIndex; ++i) {
                if (executeGetManaChar(source, t, i) != manaChar) {
                    ++miss;
                    executeSetManaChar(source, manaChar, t, i);
                }
                if (executeGetManaColor(source, t, i) != manaColor) {
                    ++miss;
                    executeSetManaColor(source, manaColor, t, i);
                }
                if (executeGetManaBold(source, t, i) != manaBold) {
                    ++miss;
                    executeSetManaBold(source, manaBold, t, i);
                }
                if (executeGetManaItalic(source, t, i) != manaItalic) {
                    ++miss;
                    executeSetManaItalic(source, manaItalic, t, i);
                }
                if (executeGetManaUnderlined(source, t, i) != manaUnderlined) {
                    ++miss;
                    executeSetManaUnderlined(source, manaUnderlined, t, i);
                }
                if (executeGetManaStrikethrough(source, t, i) != manaStrikethrough) {
                    ++miss;
                    executeSetManaStrikethrough(source, manaStrikethrough, t, i);
                }
                if (executeGetManaObfuscated(source, t, i) != manaObfuscated) {
                    ++miss;
                    executeSetManaObfuscated(source, manaObfuscated, t, i);
                }
            }
        }

        if (miss > 0) {
            String name = source.getPlayerOrThrow().getNameForScoreboard();
            source.sendFeedback(() -> Text.literal("Updated the" + (manaCharIndex == -1 ? "" : (" #" + manaCharIndex)) + (manaCharTypeIndex == -1 ? "" : (" " + manaCharTypeIndex + " point")) + " mana character for player " + name + " to " + manaCharText.getLiteralString() + "."), false);
            return 1;
        }

        throw OPTION_CHARACTER_UNCHANGED_EXCEPTION.create(manaCharTypeIndex, manaCharIndex);
    }

    public static int executeSetCharacter(ServerCommandSource source, Text manaCharText, int manaCharTypeIndex) throws CommandSyntaxException {
        return executeSetCharacter(source, manaCharText, manaCharTypeIndex, -1);
    }

    public static int executeSetCharacter(ServerCommandSource source, Text manaCharText) throws CommandSyntaxException {
        return executeSetCharacter(source, manaCharText, -1);
    }

    public static int executeReset(ServerCommandSource source) throws CommandSyntaxException {
        String name = source.getPlayerOrThrow().getNameForScoreboard();
        source.sendFeedback(() -> Text.literal("Reset mana options for player " + name + "."), false);
        for (int t = 0; t < Pentamana.manaCharTypes; ++t) {
            for (int i = 0; i < Pentamana.maxManaChar; ++i) {
                executeResetManaChar(source, t, i);
                executeResetManaColor(source, t, i);
                executeResetManaBold(source, t, i);
                executeResetManaItalic(source, t, i);
                executeResetManaUnderlined(source, t, i);
                executeResetManaStrikethrough(source, t, i);
                executeResetManaObfuscated(source, t, i);
            }
        }
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

		executeSetManaCapacity(source, executeCalcManaCapacity(source));

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

		executeSetManaRegen(source, executeCalcManaRegen(source));

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
		executeSetManaConsum(source, executeCalcManaConsum(source));

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
            return 1;
        }
		
		int mana = executeGetMana(source);
		int manaCapacity = executeGetManaCapacity(source);
		int manaPoint = (-mana - 1) / -Pentamana.manaPerPoint;
		int manaCapacityPoint = (-manaCapacity - 1) / -Pentamana.manaPerPoint;
        if (executeGetManaPoint(source) == manaPoint && executeGetManaCapacityPoint(source) == manaCapacityPoint && manabarLife < 0) {
            return 2;
        }

        executeSetManaPoint(source, manaPoint);
        executeSetManaCapacityPoint(source, manaCapacityPoint);
        executeSetManabarLife(source, -Pentamana.maxManabarLife);

        if (executeGetRenderType(source) == 1) {
            int playerManaColor = executeGetManaColor(source, 0, 0);
            TextColor manaColor =
                playerManaColor == 0 ?
                    Pentamana.manaColors.get(0) : 
                    TextColor.fromRgb(playerManaColor - 1);

            source.getPlayerOrThrow().sendMessage(Text.literal(manaPoint + "/" + manaCapacityPoint).setStyle(Style.EMPTY.withColor(manaColor)), true);
            return 4;
        }

        int manaPointTrimmed = manaPoint - manaPoint % Pentamana.pointsPerChar;
        int manaCapacityPointTrimmed = manaCapacityPoint - manaCapacityPoint % Pentamana.pointsPerChar;

        MutableText manabar = MutableText.of(PlainTextContent.EMPTY);
        for (int manaPointIndex = 0; manaPointIndex < manaCapacityPointTrimmed; manaPointIndex += Pentamana.pointsPerChar) {
            int manaCharTypeIndex =
                manaPointIndex < manaPointTrimmed ?
                    0 : manaPointIndex < manaPoint ?
                        manaPoint - manaPointIndex : Pentamana.pointsPerChar;
            int manaCharIndex = manaPointIndex / Pentamana.pointsPerChar;

            int playerManaChar = executeGetManaChar(source, manaCharTypeIndex, manaCharIndex);
            char[] manaChar =
                playerManaChar == 0 ?
                    Character.toChars(Pentamana.manaChars.get(manaCharTypeIndex)) :
                    Character.toChars(playerManaChar);
            int playerManaColor = executeGetManaColor(source, manaCharTypeIndex, manaCharIndex);
            TextColor manaColor =
                playerManaColor == 0 ?
                    Pentamana.manaColors.get(manaCharTypeIndex) : 
                    TextColor.fromRgb(playerManaColor - 1);
            int playerManaBold = executeGetManaBold(source, manaCharTypeIndex, manaCharIndex);
            boolean manaBold =
                playerManaBold == 0 ?
                    Pentamana.manaBolds.get(manaCharTypeIndex) :
                    playerManaBold == 1;
            int playerManaItalic = executeGetManaItalic(source, manaCharTypeIndex, manaCharIndex);
            boolean manaItalic =
                playerManaItalic == 0 ?
                    Pentamana.manaItalics.get(manaCharTypeIndex) :
                    playerManaItalic == 1;
            int playerManaUnderlined = executeGetManaUnderlined(source, manaCharTypeIndex, manaCharIndex);
            boolean manaUnderlined =
                playerManaUnderlined == 0 ?
                    Pentamana.manaUnderlineds.get(manaCharTypeIndex) :
                    playerManaUnderlined == 1;
            int playerManaStrikethrough = executeGetManaStrikethrough(source, manaCharTypeIndex, manaCharIndex);
            boolean manaStrikethrough =
                playerManaStrikethrough == 0 ?
                    Pentamana.manaStrikethroughs.get(manaCharTypeIndex) :
                    playerManaStrikethrough == 1;
            int playerManaObfuscated = executeGetManaObfuscated(source, manaCharTypeIndex, manaCharIndex);
            boolean manaObfuscated =
                playerManaObfuscated == 0 ?
                    Pentamana.manaObfuscateds.get(manaCharTypeIndex) :
                    playerManaObfuscated == 1;

            manabar.append(
                Text.literal(String.valueOf(manaChar)).setStyle(
                    Style.EMPTY
                        .withColor(manaColor)
                        .withBold(manaBold)
                        .withItalic(manaItalic)
                        .withUnderline(manaUnderlined)
                        .withStrikethrough(manaStrikethrough)
                        .withObfuscated(manaObfuscated)
                )
            );
        }

		source.getPlayerOrThrow().sendMessage(manabar, true);
        return 3;
    }

    public static int executeCalcManaCapacity(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        return (int)(player.getCustomModifiedValue("pentamana:mana_capacity", Pentamana.manaCapacityBase) + player.getWeaponStack().getEnchantments().getLevel("pentamana:capacity") * Pentamana.manaCapacityIncrementBase);
    }

    public static int executeCalcManaRegen(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        return (int)player.getCustomModifiedValue("pentamana:mana_regeneration", Pentamana.manaRegenBase) + player.getWeaponStack().getEnchantments().getLevel("pentamana:steam") * Pentamana.manaRegenIncrementBase;
    }

    public static int executeCalcManaConsum(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        return (int)player.getCustomModifiedValue("pentamana:mana_consumption", executeGetManaConsum(source)) * (10 - player.getWeaponStack().getEnchantments().getLevel("pentamana:utilization")) / 10;
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

    public static int executeGetManaPoint(ServerCommandSource source) throws CommandSyntaxException {
		Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_point", ScoreboardCriterion.DUMMY, Text.of("Mana Point"), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementManaPoint(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_point", ScoreboardCriterion.DUMMY, Text.of("Mana Point"), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementManaPoint(ServerCommandSource source) throws CommandSyntaxException {
        return executeIncrementManaPoint(source, 1);
    }

    public static int executeSetManaPoint(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_point", ScoreboardCriterion.DUMMY, Text.of("Mana Point"), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetManaPoint(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_point", ScoreboardCriterion.DUMMY, Text.of("Mana Point"), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
        return 0;
    }

    public static int executeGetManaCapacityPoint(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_capacity_point", ScoreboardCriterion.DUMMY, Text.of("Mana Capacity Point"), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementManaCapacityPoint(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_capacity_point", ScoreboardCriterion.DUMMY, Text.of("Mana Capacity Point"), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementManaCapacityPoint(ServerCommandSource source) throws CommandSyntaxException {
        return executeIncrementManaCapacityPoint(source, 1);
    }

    public static int executeSetManaCapacityPoint(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_capacity_point", ScoreboardCriterion.DUMMY, Text.of("Mana Capacity Point"), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetManaCapacityPoint(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_capacity_point", ScoreboardCriterion.DUMMY, Text.of("Mana Capacity Point"), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
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

    public static int executeGetRenderType(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.render_type", ScoreboardCriterion.DUMMY, Text.of("Render Type"), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementRenderType(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.render_type", ScoreboardCriterion.DUMMY, Text.of("Render Type"), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementRenderType(ServerCommandSource source) throws CommandSyntaxException {
        return executeIncrementRenderType(source, 1);
    }

    public static int executeSetRenderType(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.render_type", ScoreboardCriterion.DUMMY, Text.of("Render Type"), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetRenderType(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.render_type", ScoreboardCriterion.DUMMY, Text.of("Render Type"), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
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

    public static int executeGetManaChar(ServerCommandSource source, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_char_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Character " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementManaChar(ServerCommandSource source, int amount, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_char_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Character " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementManaChar(ServerCommandSource source, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        return executeIncrementManaChar(source, 1, manaCharTypeIndex, manaCharIndex);
    }

    public static int executeSetManaChar(ServerCommandSource source, int amount, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_char_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Character " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetManaChar(ServerCommandSource source, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_char_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Character " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
        return 0;
    }

    public static int executeGetManaColor(ServerCommandSource source, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_color_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Color " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementManaColor(ServerCommandSource source, int amount, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_color_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Color " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementManaColor(ServerCommandSource source, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        return executeIncrementManaColor(source, 1, manaCharTypeIndex, manaCharIndex);
    }

    public static int executeSetManaColor(ServerCommandSource source, int amount, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_color_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Color " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetManaColor(ServerCommandSource source, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_color_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Color " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
        return 0;
    }

    public static int executeGetManaBold(ServerCommandSource source, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_bold_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Bold " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementManaBold(ServerCommandSource source, int amount, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_bold_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Bold " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementManaBold(ServerCommandSource source, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        return executeIncrementManaBold(source, 1, manaCharTypeIndex, manaCharIndex);
    }

    public static int executeSetManaBold(ServerCommandSource source, int amount, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_bold_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Bold " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetManaBold(ServerCommandSource source, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_bold_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Bold " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
        return 0;
    }

    public static int executeGetManaItalic(ServerCommandSource source, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_italic_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Italic " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementManaItalic(ServerCommandSource source, int amount, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_italic_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Italic " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementManaItalic(ServerCommandSource source, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        return executeIncrementManaItalic(source, 1, manaCharTypeIndex, manaCharIndex);
    }

    public static int executeSetManaItalic(ServerCommandSource source, int amount, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_italic_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Italic " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetManaItalic(ServerCommandSource source, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_italic_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Italic " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
        return 0;
    }

    public static int executeGetManaUnderlined(ServerCommandSource source, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_underlined_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Underlined " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementManaUnderlined(ServerCommandSource source, int amount, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_underlined_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Underlined " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementManaUnderlined(ServerCommandSource source, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        return executeIncrementManaUnderlined(source, 1, manaCharTypeIndex, manaCharIndex);
    }

    public static int executeSetManaUnderlined(ServerCommandSource source, int amount, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_underlined_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Underlined " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetManaUnderlined(ServerCommandSource source, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_underlined_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Underlined " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
        return 0;
    }

    public static int executeGetManaStrikethrough(ServerCommandSource source, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_strikethrough_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Strikethrough " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementManaStrikethrough(ServerCommandSource source, int amount, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_strikethrough_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Strikethrough " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementManaStrikethrough(ServerCommandSource source, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        return executeIncrementManaStrikethrough(source, 1, manaCharTypeIndex, manaCharIndex);
    }

    public static int executeSetManaStrikethrough(ServerCommandSource source, int amount, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_strikethrough_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Strikethrough " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetManaStrikethrough(ServerCommandSource source, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_strikethrough_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Strikethrough " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
        return 0;
    }

    public static int executeGetManaObfuscated(ServerCommandSource source, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_obfuscated_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Obfuscated " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementManaObfuscated(ServerCommandSource source, int amount, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_obfuscated_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Obfuscated " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementManaObfuscated(ServerCommandSource source, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        return executeIncrementManaObfuscated(source, 1, manaCharTypeIndex, manaCharIndex);
    }

    public static int executeSetManaObfuscated(ServerCommandSource source, int amount, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_obfuscated_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Obfuscated " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetManaObfuscated(ServerCommandSource source, int manaCharTypeIndex, int manaCharIndex) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_obfuscated_" + manaCharTypeIndex + "_" + manaCharIndex, ScoreboardCriterion.DUMMY, Text.of("Mana Obfuscated " + manaCharTypeIndex + " " + manaCharIndex), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
        return 0;
    }
}

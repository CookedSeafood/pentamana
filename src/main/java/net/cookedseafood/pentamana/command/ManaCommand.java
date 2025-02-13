package net.cookedseafood.pentamana.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.api.ConsumeManaCallback;
import net.cookedseafood.pentamana.api.RegenManaCallback;
import net.cookedseafood.pentamana.api.TickManaCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ColorArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.jetbrains.annotations.Nullable;

public class ManaCommand {
    private static final SimpleCommandExceptionType NOT_PLAIN_TEXT_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Not a plain text."));
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
        new Dynamic2CommandExceptionType((type, ordinal) -> Text.literal("Nothing changed. That player already has that" + ((int)ordinal == 0 ? "" : (" #" + ordinal)) + (type == null ? "" : (" " + (String)type + " point")) + " mana character."));
    private static final Dynamic2CommandExceptionType OPTION_COLOR_UNCHANGED_EXCEPTION =
        new Dynamic2CommandExceptionType((type, ordinal) -> Text.literal("Nothing changed. That player already has that" + ((int)ordinal == 0 ? "" : (" #" + ordinal)) + (type == null ? "" : (" " + (String)type + " point")) + " mana color."));

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
                    CommandManager.literal("format")
                    .then(
                        CommandManager.literal("graphic")
                        .executes(context -> executeSetFormat((ServerCommandSource)context.getSource(), "graphic"))
                    )
                    .then(
                        CommandManager.literal("numberic")
                        .executes(context -> executeSetFormat((ServerCommandSource)context.getSource(), "numberic"))
                    )
                )
                .then(
                    CommandManager.literal("character")
                    .then(
                        CommandManager.argument("text", TextArgumentType.text(registryAccess))
                        .executes(context -> executeSetCharacter((ServerCommandSource)context.getSource(), TextArgumentType.getTextArgument(context, "text")))
                        .then(
                            CommandManager.literal("full")
                            .executes(context -> executeSetCharacter((ServerCommandSource)context.getSource(), TextArgumentType.getTextArgument(context, "text"), "full"))
                            .then(
                                CommandManager.argument("ordinal", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                .executes(context -> executeSetCharacter((ServerCommandSource)context.getSource(), TextArgumentType.getTextArgument(context, "text"), "full", IntegerArgumentType.getInteger(context, "ordinal")))
                            )
                        )
                        .then(
                            CommandManager.literal("half")
                            .executes(context -> executeSetCharacter((ServerCommandSource)context.getSource(), TextArgumentType.getTextArgument(context, "text"), "half"))
                            .then(
                                CommandManager.argument("ordinal", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                .executes(context -> executeSetCharacter((ServerCommandSource)context.getSource(), TextArgumentType.getTextArgument(context, "text"), "half", IntegerArgumentType.getInteger(context, "ordinal")))
                            )
                        )
                        .then(
                            CommandManager.literal("zero")
                            .executes(context -> executeSetCharacter((ServerCommandSource)context.getSource(), TextArgumentType.getTextArgument(context, "text"), "zero"))
                            .then(
                                CommandManager.argument("ordinal", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                .executes(context -> executeSetCharacter((ServerCommandSource)context.getSource(), TextArgumentType.getTextArgument(context, "text"), "zero", IntegerArgumentType.getInteger(context, "ordinal")))
                            )
                        )
                    )
                )
                .then(
                    CommandManager.literal("color")
                    .then(
                        CommandManager.argument("value", ColorArgumentType.color())
                        .executes(context -> executeSetColor((ServerCommandSource)context.getSource(), ColorArgumentType.getColor(context, "value")))
                        .then(
                            CommandManager.literal("full")
                            .executes(context -> executeSetColor((ServerCommandSource)context.getSource(), ColorArgumentType.getColor(context, "value"), "full"))
                            .then(
                                CommandManager.argument("ordinal", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                .executes(context -> executeSetColor((ServerCommandSource)context.getSource(), ColorArgumentType.getColor(context, "value"), "full", IntegerArgumentType.getInteger(context, "ordinal")))
                            )
                        )
                        .then(
                            CommandManager.literal("half")
                            .executes(context -> executeSetColor((ServerCommandSource)context.getSource(), ColorArgumentType.getColor(context, "value"), "half"))
                            .then(
                                CommandManager.argument("ordinal", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                .executes(context -> executeSetColor((ServerCommandSource)context.getSource(), ColorArgumentType.getColor(context, "value"), "half", IntegerArgumentType.getInteger(context, "ordinal")))
                            )
                        )
                        .then(
                            CommandManager.literal("zero")
                            .executes(context -> executeSetColor((ServerCommandSource)context.getSource(), ColorArgumentType.getColor(context, "value"), "zero"))
                            .then(
                                CommandManager.argument("ordinal", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                .executes(context -> executeSetColor((ServerCommandSource)context.getSource(), ColorArgumentType.getColor(context, "value"), "zero", IntegerArgumentType.getInteger(context, "ordinal")))
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

    public static int executeSetFormat(ServerCommandSource source, String formatString) throws CommandSyntaxException {
        int format = "numberic".equals(formatString) ? 1 : 0;

        String name = source.getPlayerOrThrow().getNameForScoreboard();
        if (executeGetFormat(source) != format) {
            source.sendFeedback(() -> Text.literal("Updated the mana format for player " + name + " to " + formatString + "."), false);
            return executeSetFormat(source, format);
        }

        throw OPTION_FORMAT_UNCHANGED_EXCEPTION.create(formatString);
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

    public static int executeSetCharacter(ServerCommandSource source, Text manaCharText, @Nullable String type, int ordinal) throws CommandSyntaxException {
        int manaChar = getAsInt(manaCharText);

        int miss = 0;
        if (type == null) {
            for (int o = 1; o <= 64; ++o) {
                if (executeGetManaChar(source, "full", o) != manaChar) {
                    ++miss;
                    executeSetManaChar(source, manaChar, "full", o);
                }
                if (executeGetManaChar(source, "half", o) != manaChar) {
                    ++miss;
                    executeSetManaChar(source, manaChar, "half", o);
                }
                if (executeGetManaChar(source, "zero", o) != manaChar) {
                    ++miss;
                    executeSetManaChar(source, manaChar, "zero", o);
                }
            }
        } else if (ordinal == 0) {
            for (int o = 1; o <= 64; ++o) {
                if (executeGetManaChar(source, type, o) != manaChar) {
                    ++miss;
                    executeSetManaChar(source, manaChar, type, o);
                }
            }
        } else {
            if (executeGetManaChar(source, type, ordinal) != manaChar) {
                ++miss;
                executeSetManaChar(source, manaChar, type, ordinal);
            }
        }

        if (miss > 0) {
            String name = source.getPlayerOrThrow().getNameForScoreboard();
            source.sendFeedback(() -> Text.literal("Updated the" + (ordinal == 0 ? "" : (" #" + ordinal)) + (type == null ? "" : (" " + type + " point")) + " mana character for player " + name + " to " + manaCharText.getLiteralString() + "."), false);
            return 1;
        }

        throw OPTION_CHARACTER_UNCHANGED_EXCEPTION.create(type, ordinal);
    }

    public static int executeSetCharacter(ServerCommandSource source, Text manaCharText, @Nullable String type) throws CommandSyntaxException {
        return executeSetCharacter(source, manaCharText, type, 0);
    }

    public static int executeSetCharacter(ServerCommandSource source, Text manaCharText) throws CommandSyntaxException {
        return executeSetCharacter(source, manaCharText, null);
    }

    public static int executeSetColor(ServerCommandSource source, Formatting manaColorFormatting, @Nullable String type, int ordinal) throws CommandSyntaxException {
        int manaColor = manaColorFormatting.getColorIndex() + 1;

        int miss = 0;
        if (type == null) {
            for (int o = 1; o <= 64; ++o) {
                if (executeGetManaColor(source, "full", o) != manaColor) {
                    ++miss;
                    executeSetManaColor(source, manaColor, "full", o);
                }
                if (executeGetManaColor(source, "half", o) != manaColor) {
                    ++miss;
                    executeSetManaColor(source, manaColor, "half", o);
                }
                if (executeGetManaColor(source, "zero", o) != manaColor) {
                    ++miss;
                    executeSetManaColor(source, manaColor, "zero", o);
                }
            }
        } else if (ordinal == 0) {
            for (int o = 1; o <= 64; ++o) {
                if (executeGetManaColor(source, type, o) != manaColor) {
                    ++miss;
                    executeSetManaColor(source, manaColor, type, o);
                }
            }
        } else {
            if (executeGetManaColor(source, type, ordinal) != manaColor) {
                ++miss;
                executeSetManaColor(source, manaColor, type, ordinal);
            }
        }

        if (miss > 0) {
            String name = source.getPlayerOrThrow().getNameForScoreboard();
            source.sendFeedback(() -> Text.literal("Updated the" + (ordinal == 0 ? "" : (" #" + ordinal)) + (type == null ? "" : (" " + type + " point")) + " mana character for player " + name + " to " + manaColorFormatting.getName() + "."), false);
            return 1;
        }

        throw OPTION_COLOR_UNCHANGED_EXCEPTION.create(type, ordinal);
    }

    public static int executeSetColor(ServerCommandSource source, Formatting manaColorFormatting, @Nullable String type) throws CommandSyntaxException {
        return executeSetColor(source, manaColorFormatting, type, 0);
    }

    public static int executeSetColor(ServerCommandSource source, Formatting manaColorFormatting) throws CommandSyntaxException {
        return executeSetColor(source, manaColorFormatting, null);
    }

    public static int executeReset(ServerCommandSource source) throws CommandSyntaxException {
        String name = source.getPlayerOrThrow().getNameForScoreboard();
        source.sendFeedback(() -> Text.literal("Reset mana options for player " + name + "."), false);
        for (int o = 1; o <= 64; ++o) {
            executeResetManaChar(source, "full", o);
            executeResetManaChar(source, "half", o);
            executeResetManaChar(source, "zero", o);
            executeResetManaColor(source, "full", o);
            executeResetManaColor(source, "half", o);
            executeResetManaColor(source, "zero", o);
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
            return 1;
        }
		
		int mana = executeGetMana(source);
		int manaCapacity = executeGetManaCapacity(source);
		int manaPoint = (-mana - 1) / -Pentamana.manaScale;
		int manaCapacityPoint = (-manaCapacity - 1) / -Pentamana.manaScale;
        if (executeGetManaPoint(source) == manaPoint && executeGetManaCapacityPoint(source) == manaCapacityPoint && manabarLife < 0) {
            return 2;
        }

        executeSetManaPoint(source, manaPoint);
        executeSetManaCapacityPoint(source, manaCapacityPoint);
        executeSetManabarLife(source, -Pentamana.maxManabarLife);

        if (executeGetFormat(source) == 1) {
            int manaColor = executeGetManaColor(source, "full", 1);
            Formatting manaFormatting = manaColor == 0 ? Pentamana.manaColorFull : Formatting.byColorIndex(manaColor - 1);
            source.getPlayerOrThrow().sendMessage(Text.literal(manaPoint + "/" + manaCapacityPoint).formatted(manaFormatting), true);
            return 4;
        }

        int manaPointTrimmed = manaPoint - manaPoint % 2;
        int manaCapacityPointTrimmed = manaCapacityPoint - manaCapacityPoint % 2;

        MutableText manabar = MutableText.of(PlainTextContent.EMPTY);
        for (int i = 0; i < manaCapacityPointTrimmed; i += 2) {
            String type =
                i < manaPointTrimmed ?
                    "full" : i < manaPoint ?
                        "half" : "zero";
            int o = i / 2 + 1;

            int manaChar = executeGetManaChar(source, type, o);
            char[] manaCharPair =
                manaChar == 0 ?
                    "full".equals(type) ?
                        Pentamana.manaCharFull : "half".equals(type) ?
                            Pentamana.manaCharHalf : Pentamana.manaCharZero :
                    Character.toChars(manaChar);

            int manaColor = executeGetManaColor(source, type, o);
            Formatting manaFormatting =
                manaColor == 0 ?
                    "full".equals(type) ?
                        Pentamana.manaColorFull : "half".equals(type) ?
                            Pentamana.manaColorHalf : Pentamana.manaColorZero :
                    Formatting.byColorIndex(manaColor - 1);

            manabar.append(Text.literal(String.valueOf(manaCharPair)).formatted(manaFormatting));
        }

		source.getPlayerOrThrow().sendMessage(manabar, true);
        return 3;
    }

    private static Set<NbtCompound> getModifiers(ServerPlayerEntity player) {
        Set<NbtCompound> modifiers = new HashSet<NbtCompound>();
        modifiers.addAll(player.getEquippedStack(EquipmentSlot.MAINHAND).getCustomModifiers().stream().map(nbtElement -> (NbtCompound)nbtElement).filter(modifier -> "mainhand".equals(modifier.getString("slot"))).collect(Collectors.toUnmodifiableSet()));
        modifiers.addAll(player.getEquippedStack(EquipmentSlot.OFFHAND).getCustomModifiers().stream().map(nbtElement -> (NbtCompound)nbtElement).filter(modifier -> "offhand".equals(modifier.getString("slot"))).collect(Collectors.toUnmodifiableSet()));
        modifiers.addAll(player.getEquippedStack(EquipmentSlot.FEET).getCustomModifiers().stream().map(nbtElement -> (NbtCompound)nbtElement).filter(modifier -> "feet".equals(modifier.getString("slot"))).collect(Collectors.toUnmodifiableSet()));
        modifiers.addAll(player.getEquippedStack(EquipmentSlot.LEGS).getCustomModifiers().stream().map(nbtElement -> (NbtCompound)nbtElement).filter(modifier -> "legs".equals(modifier.getString("slot"))).collect(Collectors.toUnmodifiableSet()));
        modifiers.addAll(player.getEquippedStack(EquipmentSlot.CHEST).getCustomModifiers().stream().map(nbtElement -> (NbtCompound)nbtElement).filter(modifier -> "chest".equals(modifier.getString("slot"))).collect(Collectors.toUnmodifiableSet()));
        modifiers.addAll(player.getEquippedStack(EquipmentSlot.HEAD).getCustomModifiers().stream().map(nbtElement -> (NbtCompound)nbtElement).filter(modifier -> "head".equals(modifier.getString("slot"))).collect(Collectors.toUnmodifiableSet()));
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
            .forEach(modifier -> modified.setValue((1 + modifier.getDouble("base")) * modified.getValue()));

        return modified.getValue().intValue();
    }

    public static int executeCalcManaCapacityModified(ServerCommandSource source) throws CommandSyntaxException {
        Set<NbtCompound> modifiers = getModifiers(source.getPlayerOrThrow());
        return modifiers.isEmpty() ?
            Pentamana.manaCapacityBase :
            getModified(Pentamana.manaCapacityBase, modifiers.stream().filter(modifier -> "pentamana:mana_capacity".equals(modifier.getString("attribute"))).collect(Collectors.toUnmodifiableSet()));
    }

    public static int executeCalcManaRegenModified(ServerCommandSource source) throws CommandSyntaxException {
        Set<NbtCompound> modifiers = getModifiers(source.getPlayerOrThrow());
        return modifiers.isEmpty() ?
            Pentamana.manaRegenBase :
            getModified(Pentamana.manaRegenBase, modifiers.stream().filter(modifier -> "pentamana:mana_regeneration".equals(modifier.getString("attribute"))).collect(Collectors.toUnmodifiableSet()));
    }

    public static int executeCalcManaConsumModified(ServerCommandSource source) throws CommandSyntaxException {
        Set<NbtCompound> modifiers = getModifiers(source.getPlayerOrThrow());
        return modifiers.isEmpty() ?
            executeGetManaConsum(source) :
            getModified(executeGetManaConsum(source), modifiers.stream().filter(modifier -> "pentamana:mana_consumption".equals(modifier.getString("attribute"))).collect(Collectors.toUnmodifiableSet()));
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

    public static int executeGetManaChar(ServerCommandSource source, String type, int ordinal) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_char_" + type + "_" + ordinal, ScoreboardCriterion.DUMMY, Text.of("Mana Character " + StringUtils.capitalize(type) + " " + ordinal), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementManaChar(ServerCommandSource source, int amount, String type, int ordinal) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_char_" + type + "_" + ordinal, ScoreboardCriterion.DUMMY, Text.of("Mana Character " + StringUtils.capitalize(type) + " " + ordinal), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementManaChar(ServerCommandSource source, String type, int ordinal) throws CommandSyntaxException {
        return executeIncrementManaChar(source, 1, type, ordinal);
    }

    public static int executeSetManaChar(ServerCommandSource source, int amount, String type, int ordinal) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_char_" + type + "_" + ordinal, ScoreboardCriterion.DUMMY, Text.of("Mana Character " + StringUtils.capitalize(type) + " " + ordinal), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetManaChar(ServerCommandSource source, String type, int ordinal) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_char_" + type + "_" + ordinal, ScoreboardCriterion.DUMMY, Text.of("Mana Character " + StringUtils.capitalize(type) + " " + ordinal), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
        return 0;
    }

    public static int executeGetManaColor(ServerCommandSource source, String type, int ordinal) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_color_" + type + "_" + ordinal, ScoreboardCriterion.DUMMY, Text.of("Mana Color " + StringUtils.capitalize(type) + " " + ordinal), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementManaColor(ServerCommandSource source, int amount, String type, int ordinal) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_color_" + type + "_" + ordinal, ScoreboardCriterion.DUMMY, Text.of("Mana Color " + StringUtils.capitalize(type) + " " + ordinal), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementManaColor(ServerCommandSource source, String type, int ordinal) throws CommandSyntaxException {
        return executeIncrementManaColor(source, 1, type, ordinal);
    }

    public static int executeSetManaColor(ServerCommandSource source, int amount, String type, int ordinal) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_color_" + type + "_" + ordinal, ScoreboardCriterion.DUMMY, Text.of("Mana Color " + StringUtils.capitalize(type) + " " + ordinal), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetManaColor(ServerCommandSource source, String type, int ordinal) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.mana_color_" + type + "_" + ordinal, ScoreboardCriterion.DUMMY, Text.of("Mana Color " + StringUtils.capitalize(type) + " " + ordinal), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
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

    public static int executeGetFormat(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.format", ScoreboardCriterion.DUMMY, Text.of("Format"), ScoreboardCriterion.RenderType.INTEGER, true, null)).getScore();
    }

    public static int executeIncrementFormat(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        return scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.format", ScoreboardCriterion.DUMMY, Text.of("Format"), ScoreboardCriterion.RenderType.INTEGER, true, null)).incrementScore(amount);
    }

    public static int executeIncrementFormat(ServerCommandSource source) throws CommandSyntaxException {
        return executeIncrementFormat(source, 1);
    }

    public static int executeSetFormat(ServerCommandSource source, int amount) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.format", ScoreboardCriterion.DUMMY, Text.of("Format"), ScoreboardCriterion.RenderType.INTEGER, true, null)).setScore(amount);
        return 0;
    }

    public static int executeResetFormat(ServerCommandSource source) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.getOrCreateScore(source.getPlayerOrThrow().getScoreHolder(), scoreboard.getOrAddObjective("pentamana.format", ScoreboardCriterion.DUMMY, Text.of("Format"), ScoreboardCriterion.RenderType.INTEGER, true, null)).resetScore();
        return 0;
    }
}

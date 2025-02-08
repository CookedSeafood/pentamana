package net.cookedseafood.pentamana.command;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.api.ConsumeManaCallback;
import net.cookedseafood.pentamana.api.RegenManaCallback;
import net.cookedseafood.pentamana.api.TickManaCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ColorArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.jetbrains.annotations.Nullable;

public class ManaCommand {
    private static final SimpleCommandExceptionType NOT_PLAIN_TEXT_EXCEPTION = new SimpleCommandExceptionType(Text.literal("Not a plain text."));
    private static final SimpleCommandExceptionType NOT_SINGLE_CHARACTER_EXCEPTION = new SimpleCommandExceptionType(Text.literal("Not a single character."));

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
                    CommandManager.literal("character")
                    .then(
                        CommandManager.literal("full")
                        .then(
                            CommandManager.argument("text", TextArgumentType.text(registryAccess))
                            .executes(context -> executeCharacterFull((ServerCommandSource)context.getSource(), TextArgumentType.getTextArgument(context, "text")))
                        )
                    )
                    .then(
                        CommandManager.literal("half")
                        .then(
                            CommandManager.argument("text", TextArgumentType.text(registryAccess))
                            .executes(context -> executeCharacterHalf((ServerCommandSource)context.getSource(), TextArgumentType.getTextArgument(context, "text")))
                        )
                    )
                    .then(
                        CommandManager.literal("zero")
                        .then(
                            CommandManager.argument("text", TextArgumentType.text(registryAccess))
                            .executes(context -> executeCharacterZero((ServerCommandSource)context.getSource(), TextArgumentType.getTextArgument(context, "text")))
                        )
                    )
                )
                .then(
                    CommandManager.literal("color")
                    .then(
                        CommandManager.literal("full")
                        .then(
                            CommandManager.argument("value", ColorArgumentType.color())
                            .executes(context -> executeColorFull((ServerCommandSource)context.getSource(), ColorArgumentType.getColor(context, "value")))
                        )
                    )
                    .then(
                        CommandManager.literal("half")
                        .then(
                            CommandManager.argument("value", ColorArgumentType.color())
                            .executes(context -> executeColorHalf((ServerCommandSource)context.getSource(), ColorArgumentType.getColor(context, "value")))
                        )
                    )
                    .then(
                        CommandManager.literal("zero")
                        .then(
                            CommandManager.argument("value", ColorArgumentType.color())
                            .executes(context -> executeColorZero((ServerCommandSource)context.getSource(), ColorArgumentType.getColor(context, "value")))
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

    private static int getAsInt(Text text) throws CommandSyntaxException {
        String string = text.getLiteralString();
        if (string == null) {
            throw NOT_PLAIN_TEXT_EXCEPTION.create();
        }

        if (string.codePointCount(0, string.length()) != 1) {
            throw NOT_SINGLE_CHARACTER_EXCEPTION.create();
        }

        return string.codePointAt(0);
    };

    public static int executeEnable(ServerCommandSource source) throws CommandSyntaxException {
        String name = source.getPlayerOrThrow().getNameForScoreboard();
        if (executeGetEnabled(source) != 1) {
            source.sendFeedback(() -> Text.literal("Enabled mana calculation for player " + name + "."), false);
        } else {
            source.sendFeedback(() -> Text.literal("Nothing changed. Mana calculation is already enabled for that player."), false);
        }

        return executeSetEnabled(source, 1);
    }

    public static int executeDisable(ServerCommandSource source) throws CommandSyntaxException {
        String name = source.getPlayerOrThrow().getNameForScoreboard();
        if (executeGetEnabled(source) == 1) {
            source.sendFeedback(() -> Text.literal("Disabled mana calculation for player " + name + "."), false);
        } else {
            source.sendFeedback(() -> Text.literal("Nothing changed. Mana calculation is already disbaled for that player."), false);
        }

        if (Pentamana.forceEnabled) {
            source.sendFeedback(() -> Text.literal("Mana calculation will continue due to the force enabled mode is turned on in server."), false);
        }

        return executeSetEnabled(source, 0);
    }

    public static int executeCharacterFull(ServerCommandSource source, Text full) throws CommandSyntaxException {
        int manaCharFull = getAsInt(full);

        String name = source.getPlayerOrThrow().getNameForScoreboard();
        if (executeGetManaCharFull(source) != manaCharFull) {
            source.sendFeedback(() -> Text.literal("Updated the mana character of 2 point mana for player " + name + " to " + full.getLiteralString() + "."), false);
        } else {
            source.sendFeedback(() -> Text.literal("Nothing changed. That player already has that mana character of 2 point mana."), false);
        }

        return executeSetManaCharFull(source, manaCharFull);
    }

    public static int executeCharacterHalf(ServerCommandSource source, Text half) throws CommandSyntaxException {
        int manaCharHalf = getAsInt(half);

        String name = source.getPlayerOrThrow().getNameForScoreboard();
        if (executeGetManaCharHalf(source) != manaCharHalf) {
            source.sendFeedback(() -> Text.literal("Updated the mana character of 1 point mana for player " + name + " to " + half.getLiteralString() + "."), false);
        } else {
            source.sendFeedback(() -> Text.literal("Nothing changed. That player already has that mana character of 1 point mana."), false);
        }

        return executeSetManaCharHalf(source, manaCharHalf);
    }

    public static int executeCharacterZero(ServerCommandSource source, Text zero) throws CommandSyntaxException {
        int manaCharZero = getAsInt(zero);

        String name = source.getPlayerOrThrow().getNameForScoreboard();
        if (executeGetManaCharZero(source) != manaCharZero) {
            source.sendFeedback(() -> Text.literal("Updated the mana character of 0 point mana for player " + name + " to " + zero.getLiteralString() + "."), false);
        } else {
            source.sendFeedback(() -> Text.literal("Nothing changed. That player already has that mana character of 0 point mana."), false);
        }

        return executeSetManaCharZero(source, manaCharZero);
    }

    public static int executeColorFull(ServerCommandSource source, Formatting colorFull) throws CommandSyntaxException {
        int manaColorFull = colorFull.getColorIndex() + 1;

        String name = source.getPlayerOrThrow().getNameForScoreboard();
        if (executeGetManaColorFull(source) != manaColorFull) {
            source.sendFeedback(() -> Text.literal("Updated the mana color for player " + name + " to " + colorFull.getName() + "."), false);
        } else {
            source.sendFeedback(() -> Text.literal("Nothing changed. That player already has that mana color."), false);
        }

        return executeSetManaColorFull(source, manaColorFull);
    }

    public static int executeColorHalf(ServerCommandSource source, Formatting colorHalf) throws CommandSyntaxException {
        int manaColorHalf = colorHalf.getColorIndex() + 1;

        String name = source.getPlayerOrThrow().getNameForScoreboard();
        if (executeGetManaColorHalf(source) != manaColorHalf) {
            source.sendFeedback(() -> Text.literal("Updated the mana color for player " + name + " to " + colorHalf.getName() + "."), false);
        } else {
            source.sendFeedback(() -> Text.literal("Nothing changed. That player already has that mana color."), false);
        }

        return executeSetManaColorHalf(source, manaColorHalf);
    }

    public static int executeColorZero(ServerCommandSource source, Formatting colorZero) throws CommandSyntaxException {
        int manaColorZero = colorZero.getColorIndex() + 1;

        String name = source.getPlayerOrThrow().getNameForScoreboard();
        if (executeGetManaColorZero(source) != manaColorZero) {
            source.sendFeedback(() -> Text.literal("Updated the mana color for player " + name + " to " + colorZero.getName() + "."), false);
        } else {
            source.sendFeedback(() -> Text.literal("Nothing changed. That player already has that mana color."), false);
        }

        return executeSetManaColorZero(source, manaColorZero);
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

		String configString = null;
		try {
			configString = FileUtils.readFileToString(new File("./config/pentamana.json"), StandardCharsets.UTF_16);
		} catch (IOException e) {
			return 0;
		}

		JsonObject configObject = new Gson().fromJson(configString, JsonObject.class);

        Pentamana.manaScale =
            configObject.has("manaScale") ? 
            configObject.get("manaScale").getAsInt() :
            16777216/* 2^24 */;
        Pentamana.manaCapacityBase =
            configObject.has("manaCapacityBase") ?
            configObject.get("manaCapacityBase").getAsInt() :
            33554431/* 2^24*2-1 */;
        Pentamana.manaCapacityIncrementBase =
            configObject.has("manaCapacityIncrementBase") ?
            configObject.get("manaCapacityIncrementBase").getAsInt() :
            33554432/* 2^24*2 */;
        Pentamana.manaRegenBase =
            configObject.has("manaRegenBase") ?
            configObject.get("manaRegenBase").getAsInt() :
            1048576/* 2^20 */;
        Pentamana.manaRegenIncrementBase =
            configObject.has("manaRegenIncrementBase") ?
            configObject.get("manaRegenIncrementBase").getAsInt() :
            65536/* 2^16 */;
        Pentamana.maxManabarLife =
            configObject.has("maxManabarLife") ?
            configObject.get("maxManabarLife").getAsInt() :
            40/* 20*2 */;
        Pentamana.manaCharFull =
            configObject.has("manaCharFull") ?
            Character.toChars(configObject.get("manaCharFull").getAsString().codePointAt(0)) :
            new char[] {'\u2605'};
        Pentamana.manaCharHalf =
            configObject.has("manaCharHalf") ?
            Character.toChars(configObject.get("manaCharHalf").getAsString().codePointAt(0)) :
            new char[] {'\u2bea'};
        Pentamana.manaCharZero =
            configObject.has("manaCharZero") ?
            Character.toChars(configObject.get("manaCharZero").getAsString().codePointAt(0)) :
            new char[] {'\u2606'};
        Pentamana.manaColorFull =
            configObject.has("manaColorFull") ?
            Formatting.byName(configObject.get("manaColorFull").getAsString()) :
            Formatting.AQUA;
        Pentamana.manaColorHalf =
            configObject.has("manaColorHalf") ?
            Formatting.byName(configObject.get("manaColorHalf").getAsString()) :
            Formatting.AQUA;
        Pentamana.manaColorZero =
            configObject.has("manaColorZero") ?
            Formatting.byName(configObject.get("manaColorZero").getAsString()) :
            Formatting.AQUA;
        Pentamana.forceEnabled =
            configObject.has("forceEnabled") ?
            configObject.get("forceEnabled").getAsBoolean() :
            false;

		return 1;
	}

    public static int executeVersion(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("Pentamana " + Pentamana.versionMajor + "." + Pentamana.versionMinor + "." + Pentamana.versionPatch), false);
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
			executeDisplay(source);
			return 1;
		}

		return 0;
	}

    public static int executeDisplay(ServerCommandSource source) throws CommandSyntaxException {
        int manabarLife = executeGetManabarLife(source);
        if (manabarLife > 0 && manabarLife < Pentamana.maxManabarLife) {
            return 0;
        }

		executeSetManabarLife(source, -Pentamana.maxManabarLife);
		
		int mana = executeGetMana(source);
		int manaCapacity = executeGetManaCapacity(source);
		mana = (-mana - 1) / -Pentamana.manaScale;
		manaCapacity = (-manaCapacity - 1) / -Pentamana.manaScale;

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
        int manaColorHalf = executeGetManaColorFull(source);
        Formatting colorHalf =
            manaColorHalf == 0 ?
            Pentamana.manaColorHalf :
            Formatting.byColorIndex(manaColorHalf - 1);
        int manaColorZero = executeGetManaColorFull(source);
        Formatting colorZero =
            manaColorZero == 0 ?
            Pentamana.manaColorZero :
            Formatting.byColorIndex(manaColorZero - 1);

		StringBuilder partialManabar = new StringBuilder();
        MutableText manabar = MutableText.of(PlainTextContent.EMPTY);
		for (int i = mana / 2; i > 0; --i) {
			partialManabar.append(charFull);
		}

        if (partialManabar.length() > 0) {
            manabar.append(Text.literal(partialManabar.toString()).formatted(colorFull));
        }

		if (mana % 2 == 1 && mana != manaCapacity) {
            manabar.append(Text.literal(String.valueOf(charHalf)).formatted(colorHalf));
		}

        partialManabar.setLength(0);
		for (int i = (manaCapacity - mana - mana % 2) / 2; i > 0; --i) {
			partialManabar.append(charZero);
		}

        if (partialManabar.length() > 0) {
            manabar.append(Text.literal(partialManabar.toString()).formatted(colorZero));
        }

		source.getPlayerOrThrow().sendMessage((Text)manabar, true);
        return manabarLife;
    }

    @Nullable
    private static Stream<NbtCompound> getModifiers(ServerCommandSource source) throws CommandSyntaxException {
        ItemStack weaponStack = source.getPlayerOrThrow().getWeaponStack();
        if (!weaponStack.contains(DataComponentTypes.CUSTOM_DATA)) {
            return null;
        }

        NbtCompound customData = weaponStack.get(DataComponentTypes.CUSTOM_DATA).copyNbt();
        if (!customData.contains("modifiers", NbtElement.LIST_TYPE)) {
            return null;
        }

        return customData.getList("modifiers", NbtElement.COMPOUND_TYPE).stream().map(element -> (NbtCompound)element);
    }

    private static int getModified(Set<NbtCompound> modifiers) {
        MutableDouble modified = new MutableDouble(Pentamana.manaCapacityBase);

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
        Stream<NbtCompound> modifiers = getModifiers(source);
        if (modifiers == null) {
            return Pentamana.manaCapacityBase;
        }

        Set<NbtCompound> manaCapacityModifiers = modifiers.filter(modifier -> "pentamana:mana_capacity".equals(modifier.getString("attribute"))).collect(Collectors.toUnmodifiableSet());
        if (manaCapacityModifiers.isEmpty()) {
            return Pentamana.manaCapacityBase;
        }

        return getModified(manaCapacityModifiers);
    }

    public static int executeCalcManaRegenModified(ServerCommandSource source) throws CommandSyntaxException {
        Stream<NbtCompound> modifiers = getModifiers(source);
        if (modifiers == null) {
            return Pentamana.manaRegenBase;
        }

        Set<NbtCompound> manaRegenModifiers = modifiers.filter(modifier -> "pentamana:mana_regeneration".equals(modifier.getString("attribute"))).collect(Collectors.toUnmodifiableSet());
        if (manaRegenModifiers.isEmpty()) {
            return Pentamana.manaRegenBase;
        }

        return getModified(manaRegenModifiers);
    }

    public static int executeCalcManaConsumModified(ServerCommandSource source) throws CommandSyntaxException {
        Stream<NbtCompound> modifiers = getModifiers(source);
        if (modifiers == null) {
            return executeGetManaConsum(source);
        }

        Set<NbtCompound> manaConsumModifiers = modifiers.filter(modifier -> "pentamana:mana_consumption".equals(modifier.getString("attribute"))).collect(Collectors.toUnmodifiableSet());
        if (manaConsumModifiers.isEmpty()) {
            return executeGetManaConsum(source);
        }

        return getModified(manaConsumModifiers);
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
}

package net.cookedseafood.pentamana.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.component.ManaPreference;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.commons.lang3.mutable.MutableInt;

public class ManaCommand {
    private static final SimpleCommandExceptionType NOT_CUSTOMIZABLE_CHARACTER_TYPE_INDEX =
        new SimpleCommandExceptionType(Text.literal("Not a customizable character type index."));
    private static final SimpleCommandExceptionType NOT_CUSTOMIZABLE_CHARACTER_INDEX =
        new SimpleCommandExceptionType(Text.literal("Not a customizable character index."));
    private static final SimpleCommandExceptionType OPTION_ALREADY_ENABLED_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Nothing changed. Mana is already enabled for that player."));
    private static final SimpleCommandExceptionType OPTION_ALREADY_DISABLED_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Nothing changed. Mana is already disbaled for that player."));
    private static final DynamicCommandExceptionType OPTION_DISPLAY_UNCHANGED_EXCEPTION =
        new DynamicCommandExceptionType(display -> Text.literal("Nothing changed. That player already has " + (boolean)display + " for mana display."));
    private static final SimpleCommandExceptionType OPTION_RENDER_TYPE_ALREADY_FLEX_SIZE_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Nothing changed. Mana render type is already set to flex size for that player."));
    private static final DynamicCommandExceptionType OPTION_RENDER_TYPE_ALREADY_FIXED_SIZE_EXCEPTION =
        new DynamicCommandExceptionType(fixedSize -> Text.literal("Nothing changed. Mana render type is already set to fixed size " + (int)fixedSize + " for that player."));
    private static final SimpleCommandExceptionType OPTION_RENDER_TYPE_ALREADY_NUMBERIC_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Nothing changed. Mana render type is already set to numberic for that player."));
    private static final SimpleCommandExceptionType OPTION_RENDER_TYPE_ALREADY_PERCENTAGE_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Nothing changed. Mana render type is already set to percentage for that player."));
    private static final DynamicCommandExceptionType OPTION_POINTS_PER_CHARACTER_UNCHANGED_EXCEPTION =
        new DynamicCommandExceptionType(pointsPerCharacter -> Text.literal("Nothing changed. That player already has " + (int)pointsPerCharacter + " for points per character."));
    private static final Dynamic2CommandExceptionType OPTION_CHARACTER_UNCHANGED_EXCEPTION =
        new Dynamic2CommandExceptionType((manaCharacterTypeIndex, manaCharacterIndex) -> Text.literal("Nothing changed. That player already has that" + ((int)manaCharacterIndex == -1 ? "" : (" #" + (int)manaCharacterIndex)) + ((int)manaCharacterTypeIndex == -1 ? "" : (" " + (int)manaCharacterTypeIndex + " point")) + " mana character."));

    public ManaCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(
            CommandManager.literal("mana")
            .then(
                CommandManager.literal("disable")
                .executes(context -> executeDisable((ServerCommandSource)context.getSource()))
            )
            .then(
                CommandManager.literal("enable")
                .executes(context -> executeEnable((ServerCommandSource)context.getSource()))
            )
            .then(
                CommandManager.literal("set")
                .then(
                    CommandManager.literal("display")
                    .then(
                        CommandManager.literal("false")
                        .executes(context -> executeSetDisplay((ServerCommandSource)context.getSource(), false))
                    )
                    .then(
                        CommandManager.literal("true")
                        .executes(context -> executeSetDisplay((ServerCommandSource)context.getSource(), true))
                    )
                )
                .then(
                    CommandManager.literal("render_type")
                    .then(
                        CommandManager.literal("flex_size")
                        .executes(context -> executeSetManaRenderTypeFlexSize((ServerCommandSource)context.getSource()))
                    )
                    .then(
                        CommandManager.literal("fixed_size")
                        .executes(context -> executeSetManaRenderTypeFixedSize((ServerCommandSource)context.getSource()))
                        .then(
                            CommandManager.argument("size", IntegerArgumentType.integer(1))
                            .executes(context -> executeSetManaRenderTypeFixedSize((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger(context, "size")))
                        )
                    )
                    .then(
                        CommandManager.literal("numberic")
                        .executes(context -> executeSetManaRenderTypeNumberic((ServerCommandSource)context.getSource()))
                    )
                    .then(
                        CommandManager.literal("percentage")
                        .executes(context -> executeSetManaRenderTypePercentage((ServerCommandSource)context.getSource()))
                    )
                )
                .then(
                    CommandManager.literal("points_per_character")
                    .then(
                        CommandManager.argument("value", IntegerArgumentType.integer(1))
                        .executes(context -> executeSetPointsPerCharacter((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger(context, "value")))
                    )
                )
                .then(
                    CommandManager.literal("character")
                    .then(
                        CommandManager.argument("text", TextArgumentType.text(registryAccess))
                        .executes(context -> executeSetCharacter((ServerCommandSource)context.getSource(), TextArgumentType.getTextArgument(context, "text")))
                        .then(
                            CommandManager.argument("type_index", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                            .executes(context -> executeSetCharacter((ServerCommandSource)context.getSource(), TextArgumentType.getTextArgument(context, "text"), IntegerArgumentType.getInteger(context, "type_index")))
                            .then(
                                CommandManager.argument("character_index", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                .executes(context -> executeSetCharacter((ServerCommandSource)context.getSource(), TextArgumentType.getTextArgument(context, "text"), IntegerArgumentType.getInteger(context, "type_index"), IntegerArgumentType.getInteger(context, "character_index")))
                            )
                        )
                    )
                )
            )
            .then(
                CommandManager.literal("reset")
                .executes(context -> executeReset((ServerCommandSource)context.getSource()))
                .then(
                    CommandManager.literal("display")
                    .executes(context -> executeResetDisplay((ServerCommandSource)context.getSource()))
                )
                .then(
                    CommandManager.literal("render_type")
                    .executes(context -> executeResetManaRenderType((ServerCommandSource)context.getSource()))
                )
                .then(
                    CommandManager.literal("points_per_character")
                    .executes(context -> executeResetPointsPerCharacter((ServerCommandSource)context.getSource()))
                )
                .then(
                    CommandManager.literal("characters")
                    .executes(context -> executeResetCharacters((ServerCommandSource)context.getSource()))
                )
            )
            .then(
                CommandManager.literal("reload")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> executeReload((ServerCommandSource)context.getSource()))
            )
        );
    }

    public static int executeEnable(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.getEnabled()) {
            throw OPTION_ALREADY_ENABLED_EXCEPTION.create();
        }

        manaPreference.setEnabled(true);

        source.sendFeedback(() -> Text.literal("Enabled mana for player " + player.getNameForScoreboard() + "."), false);
        return 1;
    }

    public static int executeDisable(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (!manaPreference.getEnabled()) {
            throw OPTION_ALREADY_DISABLED_EXCEPTION.create();
        }

        manaPreference.setEnabled(false);

        source.sendFeedback(() -> Text.literal("Disabled mana for player " + player.getNameForScoreboard() + "."), false);
        if (Pentamana.forceManaEnabled) {
            source.sendFeedback(() -> Text.literal("Mana calculation will continue due to the force enabled mode is turned on in server."), false);
        }

        return 1;
    }

    public static int executeSetDisplay(ServerCommandSource source, boolean display) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.getDisplay() == display) {
            throw OPTION_DISPLAY_UNCHANGED_EXCEPTION.create(display);
        }

        manaPreference.setDisplay(display);

        source.sendFeedback(() -> Text.literal("Updated the mana display for player " + player.getNameForScoreboard() + " to " + display + "."), false);
        return 1;
    }

    public static int executeSetManaRenderTypeFlexSize(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.getManaRenderType() == Pentamana.ManaRenderType.FLEX_SIZE.getIndex()) {
            throw OPTION_RENDER_TYPE_ALREADY_FLEX_SIZE_EXCEPTION.create();
        }

        manaPreference.setManaRenderType(Pentamana.ManaRenderType.FLEX_SIZE.getIndex());

        source.sendFeedback(() -> Text.literal("Updated the mana render type for player " + player.getNameForScoreboard() + " to flex size."), false);
        return 1;
    }

    public static int executeSetManaRenderTypeFixedSize(ServerCommandSource source) throws CommandSyntaxException {
        return executeSetManaRenderTypeFixedSize(source, Pentamana.manaFixedSize);
    }

    public static int executeSetManaRenderTypeFixedSize(ServerCommandSource source, int fixedSize) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.getManaRenderType() == Pentamana.ManaRenderType.FIXED_SIZE.getIndex() && manaPreference.getManaFixedSize() == fixedSize) {
            throw OPTION_RENDER_TYPE_ALREADY_FIXED_SIZE_EXCEPTION.create(fixedSize);
        }

        manaPreference.setManaRenderType(Pentamana.ManaRenderType.FIXED_SIZE.getIndex());
        manaPreference.setManaFixedSize(fixedSize);

        source.sendFeedback(() -> Text.literal("Updated the mana render type for player " + player.getNameForScoreboard() + " to fixed size " + fixedSize + "."), false);
        return 1;
    }

    public static int executeSetManaRenderTypeNumberic(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.getManaRenderType() == Pentamana.ManaRenderType.NUMBERIC.getIndex()) {
            throw OPTION_RENDER_TYPE_ALREADY_NUMBERIC_EXCEPTION.create();
        }

        manaPreference.setManaRenderType(Pentamana.ManaRenderType.NUMBERIC.getIndex());

        source.sendFeedback(() -> Text.literal("Updated the mana render type for player " + player.getNameForScoreboard() + " to numberic."), false);
        return 1;
    }

    public static int executeSetManaRenderTypePercentage(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.getManaRenderType() == Pentamana.ManaRenderType.PERCENTAGE.getIndex()) {
            throw OPTION_RENDER_TYPE_ALREADY_PERCENTAGE_EXCEPTION.create();
        }

        manaPreference.setManaRenderType(Pentamana.ManaRenderType.PERCENTAGE.getIndex());

        source.sendFeedback(() -> Text.literal("Updated the mana render type for player " + player.getNameForScoreboard() + " to percentage."), false);
        return 1;
    }

    public static int executeSetPointsPerCharacter(ServerCommandSource source, int pointsPerCharacter) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.getPointsPerCharacter() == pointsPerCharacter) {
            throw OPTION_POINTS_PER_CHARACTER_UNCHANGED_EXCEPTION.create(pointsPerCharacter);
        }

        manaPreference.setPointsPerCharacter(pointsPerCharacter);

        source.sendFeedback(() -> Text.literal("Updated the points per character for player " + player.getNameForScoreboard() + " to " + pointsPerCharacter + "."), false);
        return 1;
    }

    public static int executeSetCharacter(ServerCommandSource source, Text manaCharacter) throws CommandSyntaxException {
        return executeSetCharacter(source, manaCharacter, -1);
    }

    public static int executeSetCharacter(ServerCommandSource source, Text manaCharacter, int manaCharacterTypeIndex) throws CommandSyntaxException {
        return executeSetCharacter(source, manaCharacter, manaCharacterTypeIndex, -1);
    }

    public static int executeSetCharacter(ServerCommandSource source, Text manaCharacter, int manaCharacterTypeIndex, int manaCharacterIndex) throws CommandSyntaxException {
        if (manaCharacterTypeIndex > Pentamana.MANA_CHARACTER_TYPE_INDEX_LIMIT) {
            throw NOT_CUSTOMIZABLE_CHARACTER_TYPE_INDEX.create();
        }

        if (manaCharacterIndex > Pentamana.MANA_CHARACTER_INDEX_LIMIT) {
            throw NOT_CUSTOMIZABLE_CHARACTER_INDEX.create();
        }

        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        List<List<Text>> manaCharacters = manaPreference.getManaCharacters();

        int startManaCharTypeIndex = manaCharacterTypeIndex == -1 ? 0 : manaCharacterTypeIndex;
        int endManaCharTypeIndex = manaCharacterTypeIndex == -1 ? Pentamana.MANA_CHARACTER_TYPE_INDEX_LIMIT : manaCharacterTypeIndex;
        int startManaCharIndex = manaCharacterIndex == -1 ? 0 : manaCharacterIndex;
        int endManaCharIndex = manaCharacterIndex == -1 ? Pentamana.MANA_CHARACTER_INDEX_LIMIT : manaCharacterIndex;

        MutableInt miss = new MutableInt(0);
        IntStream.rangeClosed(startManaCharTypeIndex, endManaCharTypeIndex)
            .forEach(cti -> IntStream.rangeClosed(startManaCharIndex, endManaCharIndex)
                .forEach(ci -> {
                    if (!manaCharacters.get(cti).get(ci).equals(manaCharacter)) {
                        miss.increment();
                        manaCharacters.get(cti).set(ci, manaCharacter);
                    }
                })
            );

        if (miss.intValue() == 0) {
            throw OPTION_CHARACTER_UNCHANGED_EXCEPTION.create(manaCharacterTypeIndex, manaCharacterIndex);
        }

        source.sendFeedback(() -> Text.literal("Updated the" + (manaCharacterIndex == -1 ? "" : (" #" + manaCharacterIndex)) + (manaCharacterTypeIndex == -1 ? "" : (" " + manaCharacterTypeIndex + " point")) + " mana character for player " + player.getNameForScoreboard() + " to " + manaCharacter.getLiteralString() + "."), false);
        return 1;
    }

    public static int executeReset(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        manaPreference.setDisplay(Pentamana.display);
        manaPreference.setManaRenderType(Pentamana.manaRenderType);
        manaPreference.setPointsPerCharacter(Pentamana.pointsPerCharacter);
        manaPreference.setManaCharacters(
            Pentamana.manaCharacters.stream()
                .map(ArrayList::new)
                .collect(Collectors.toList())
        );

        source.sendFeedback(() -> Text.literal("Reset mana options for player " + player.getNameForScoreboard() + "."), false);
        return 0;
    }

    public static int executeResetDisplay(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        manaPreference.setDisplay(Pentamana.display);

        source.sendFeedback(() -> Text.literal("Reset mana display for player " + player.getNameForScoreboard() + "."), false);
        return 0;
    }

    public static int executeResetManaRenderType(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        manaPreference.setManaRenderType(Pentamana.manaRenderType);
        manaPreference.setManaFixedSize(Pentamana.manaFixedSize);

        source.sendFeedback(() -> Text.literal("Reset mana render type for player " + player.getNameForScoreboard() + "."), false);
        return 0;
    }

    public static int executeResetPointsPerCharacter(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        manaPreference.setPointsPerCharacter(Pentamana.pointsPerCharacter);

        source.sendFeedback(() -> Text.literal("Reset points per character for player " + player.getNameForScoreboard() + "."), false);
        return 0;
    }

    public static int executeResetCharacters(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        manaPreference.setManaCharacters(
            Pentamana.manaCharacters.stream()
                .map(ArrayList::new)
                .collect(Collectors.toList())
        );

        source.sendFeedback(() -> Text.literal("Reset mana characters for player " + player.getNameForScoreboard() + "."), false);
        return 0;
    }

    public static int executeReload(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("Reloading Pentamana!"), true);
        return Pentamana.reload();
	}
}

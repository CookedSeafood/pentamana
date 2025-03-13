package net.cookedseafood.pentamana.command;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.mutable.MutableInt;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.component.ManaPreference;
import net.cookedseafood.pentamana.render.ManabarPositions;
import net.cookedseafood.pentamana.render.ManabarTypes;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ManabarCommand {
    private static final DynamicCommandExceptionType OPTION_VISIBILITY_UNCHANGED_EXCEPTION =
        new DynamicCommandExceptionType(isVisible -> Text.literal("Nothing changed. That player already has " + (boolean)isVisible + " for manabar visibility."));
    private static final DynamicCommandExceptionType OPTION_COMPRESSION_UNCHANGED_EXCEPTION =
        new DynamicCommandExceptionType(isCompression -> Text.literal("Nothing changed. That player already has " + (boolean)isCompression + " for manabar compression."));
    private static final DynamicCommandExceptionType OPTION_COMPRESSION_SIZE_UNCHANGED_EXCEPTION =
        new DynamicCommandExceptionType(compressionSize -> Text.literal("Nothing changed. That player already has " + (boolean)compressionSize + " for manabar compression size."));
    private static final DynamicCommandExceptionType OPTION_PATTERN_UNCHANGED_EXCEPTION =
        new DynamicCommandExceptionType(pattern -> Text.literal("Nothing changed. That player already has " + (String)pattern + " for manabar pattern."));
    private static final DynamicCommandExceptionType OPTION_TYPE_UNCHANGED_EXCEPTION =
        new DynamicCommandExceptionType(type -> Text.literal("Nothing changed. That player already has " + (String)type + " for manabar type."));
    private static final DynamicCommandExceptionType OPTION_POSITION_UNCHANGED_EXCEPTION =
        new DynamicCommandExceptionType(position -> Text.literal("Nothing changed. That player already has " + (String)position + " for manabar position."));
    private static final DynamicCommandExceptionType OPTION_COLOR_UNCHANGED_EXCEPTION =
        new DynamicCommandExceptionType(color -> Text.literal("Nothing changed. That player already has " + (String)color + " for manabar color."));
    private static final DynamicCommandExceptionType OPTION_STYLE_UNCHANGED_EXCEPTION =
        new DynamicCommandExceptionType(style -> Text.literal("Nothing changed. That player already has " + (String)style + " for manabar style."));
    private static final DynamicCommandExceptionType OPTION_POINTS_PER_CHARACTER_UNCHANGED_EXCEPTION =
        new DynamicCommandExceptionType(pointsPerCharacter -> Text.literal("Nothing changed. That player already has " + (int)pointsPerCharacter + " for points per character."));
    private static final Dynamic3CommandExceptionType OPTION_MANA_CHARACTER_UNCHANGED_EXCEPTION =
        new Dynamic3CommandExceptionType((manaCharacter, manaCharacterTypeIndex, manaCharacterIndex) -> Text.literal("Nothing changed. That player already has " + (String)manaCharacter + " for" + ((int)manaCharacterIndex == -1 ? "" : (" #" + (int)manaCharacterIndex)) + ((int)manaCharacterTypeIndex == -1 ? "" : (" " + (int)manaCharacterTypeIndex + " point")) + " mana character."));

    public ManabarCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(
            CommandManager.literal("manabar")
            .then(
                CommandManager.literal("set")
                .then(
                    CommandManager.literal("visibility")
                    .then(
                        CommandManager.literal("false")
                        .executes(context -> executeSetVisibility(context.getSource(), false))
                    )
                    .then(
                        CommandManager.literal("true")
                        .executes(context -> executeSetVisibility(context.getSource(), true))
                    )
                )
                .then(
                    CommandManager.literal("pattern")
                    .then(
                        CommandManager.argument("text", TextArgumentType.text(registryAccess))
                        .executes(context -> executeSetPattern(context.getSource(), TextArgumentType.getTextArgument(context, "text")))
                    )
                )
                .then(
                    CommandManager.literal("type")
                    .then(
                        CommandManager.literal("character")
                        .executes(context -> executeSetType(context.getSource(), ManabarTypes.CHARACTER))
                    )
                    .then(
                        CommandManager.literal("numeric")
                        .executes(context -> executeSetType(context.getSource(), ManabarTypes.NUMERIC))
                    )
                    .then(
                        CommandManager.literal("percentage")
                        .executes(context -> executeSetType(context.getSource(), ManabarTypes.PERCENTAGE))
                    )
                    .then(
                        CommandManager.literal("none")
                        .executes(context -> executeSetType(context.getSource(), ManabarTypes.NONE))
                    )
                )
                .then(
                    CommandManager.literal("position")
                    .then(
                        CommandManager.literal("actionbar")
                        .executes(context -> executeSetPosition(context.getSource(), ManabarPositions.ACTIONBAR))
                    )
                    .then(
                        CommandManager.literal("bossbar")
                        .executes(context -> executeSetPosition(context.getSource(), ManabarPositions.BOSSBAR))
                    )
                )
                .then(
                    CommandManager.literal("compression")
                    .then(
                        CommandManager.literal("false")
                        .executes(context -> executeSetCompression(context.getSource(), false))
                    )
                    .then(
                        CommandManager.literal("true")
                        .executes(context -> executeSetCompression(context.getSource(), true))
                    )
                )
                .then(
                    CommandManager.literal("compression_size")
                    .then(
                        CommandManager.argument("size", IntegerArgumentType.integer(1, Pentamana.MANA_CHARACTER_INDEX_LIMIT + 1))
                        .executes(context -> executeSetCompressionSize(context.getSource(), (byte)IntegerArgumentType.getInteger(context, "size")))
                    )
                )
                .then(
                    CommandManager.literal("points_per_character")
                    .then(
                        CommandManager.argument("value", IntegerArgumentType.integer(1))
                        .executes(context -> executeSetPointsPerCharacter(context.getSource(), IntegerArgumentType.getInteger(context, "value")))
                    )
                )
                .then(
                    CommandManager.literal("character")
                    .then(
                        CommandManager.argument("text", TextArgumentType.text(registryAccess))
                        .executes(context -> executeSetCharacter(context.getSource(), TextArgumentType.getTextArgument(context, "text")))
                        .then(
                            CommandManager.argument("character_type_index", IntegerArgumentType.integer(0, Pentamana.MANA_CHARACTER_TYPE_INDEX_LIMIT))
                            .executes(context -> executeSetCharacter(context.getSource(), TextArgumentType.getTextArgument(context, "text"), IntegerArgumentType.getInteger(context, "type_index")))
                            .then(
                                CommandManager.argument("character_index", IntegerArgumentType.integer(0, Pentamana.MANA_CHARACTER_INDEX_LIMIT))
                                .executes(context -> executeSetCharacter(context.getSource(), TextArgumentType.getTextArgument(context, "text"), IntegerArgumentType.getInteger(context, "type_index"), IntegerArgumentType.getInteger(context, "character_index")))
                            )
                        )
                    )
                )
                .then(
                    CommandManager.literal("color")
                    .then(
                        CommandManager.literal("pink")
                        .executes(context -> executeSetColor(context.getSource(), BossBar.Color.PINK))
                    )
                    .then(
                        CommandManager.literal("blue")
                        .executes(context -> executeSetColor(context.getSource(), BossBar.Color.BLUE))
                    )
                    .then(
                        CommandManager.literal("red")
                        .executes(context -> executeSetColor(context.getSource(), BossBar.Color.RED))
                    )
                    .then(
                        CommandManager.literal("green")
                        .executes(context -> executeSetColor(context.getSource(), BossBar.Color.GREEN))
                    )
                    .then(
                        CommandManager.literal("yellow")
                        .executes(context -> executeSetColor(context.getSource(), BossBar.Color.YELLOW))
                    )
                    .then(
                        CommandManager.literal("purple")
                        .executes(context -> executeSetColor(context.getSource(), BossBar.Color.PURPLE))
                    )
                    .then(
                        CommandManager.literal("white")
                        .executes(context -> executeSetColor(context.getSource(), BossBar.Color.WHITE))
                    )
                )
                .then(
                    CommandManager.literal("style")
                    .then(
                        CommandManager.literal("progress")
                        .executes(context -> executeSetStyle(context.getSource(), BossBar.Style.PROGRESS))
                    )
                    .then(
                        CommandManager.literal("notched_6")
                        .executes(context -> executeSetStyle(context.getSource(), BossBar.Style.NOTCHED_6))
                    )
                    .then(
                        CommandManager.literal("notched_10")
                        .executes(context -> executeSetStyle(context.getSource(), BossBar.Style.NOTCHED_10))
                    )
                    .then(
                        CommandManager.literal("notched_12")
                        .executes(context -> executeSetStyle(context.getSource(), BossBar.Style.NOTCHED_12))
                    )
                    .then(
                        CommandManager.literal("notched_20")
                        .executes(context -> executeSetStyle(context.getSource(), BossBar.Style.NOTCHED_20))
                    )
                )
            )
            .then(
                CommandManager.literal("reset")
                .executes(context -> executeReset(context.getSource()))
                .then(
                    CommandManager.literal("visibility")
                    .executes(context -> executeResetVisibility(context.getSource()))
                )
                .then(
                    CommandManager.literal("compression")
                    .executes(context -> executeResetCompression(context.getSource()))
                )
                .then(
                    CommandManager.literal("compression_size")
                    .executes(context -> executeResetCompressionSize(context.getSource()))
                )
                .then(
                    CommandManager.literal("pattern")
                    .executes(context -> executeResetPattern(context.getSource()))
                )
                .then(
                    CommandManager.literal("type")
                    .executes(context -> executeResetType(context.getSource()))
                )
                .then(
                    CommandManager.literal("position")
                    .executes(context -> executeResetPosition(context.getSource()))
                )
                .then(
                    CommandManager.literal("color")
                    .executes(context -> executeResetColor(context.getSource()))
                )
                .then(
                    CommandManager.literal("style")
                    .executes(context -> executeResetStyle(context.getSource()))
                )
                .then(
                    CommandManager.literal("points_per_character")
                    .executes(context -> executeResetPointsPerCharacter(context.getSource()))
                )
                .then(
                    CommandManager.literal("character")
                    .executes(context -> executeResetCharacter(context.getSource()))
                )
            )
        );
    }

    public static int executeSetVisibility(ServerCommandSource source, boolean isVisible) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.isVisible() == isVisible) {
            throw OPTION_VISIBILITY_UNCHANGED_EXCEPTION.create(isVisible);
        }

        manaPreference.setIsVisible(isVisible);

        source.sendFeedback(() -> Text.literal("Updated manabar visibility for player " + player.getNameForScoreboard() + " to " + isVisible + "."), false);
        return 1;
    }

    public static int executeSetCompression(ServerCommandSource source, boolean isCompression) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.isCompression() == isCompression) {
            throw OPTION_COMPRESSION_UNCHANGED_EXCEPTION.create(isCompression);
        }

        manaPreference.setIsCompression(isCompression);

        source.sendFeedback(() -> Text.literal("Updated manabar compression for player " + player.getNameForScoreboard() + " to " + isCompression + "."), false);
        return 1;
    }

    public static int executeSetCompressionSize(ServerCommandSource source, byte compressionSize) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.getCompressionSize() == compressionSize) {
            throw OPTION_COMPRESSION_SIZE_UNCHANGED_EXCEPTION.create(compressionSize);
        }

        manaPreference.setCompressionSize(compressionSize);

        source.sendFeedback(() -> Text.literal("Updated manabar compression size for player " + player.getNameForScoreboard() + " to " + compressionSize + "."), false);
        return 1;
    }

    public static int executeSetPattern(ServerCommandSource source, Text manabarPattern) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.getManabarPattern().equals(manabarPattern)) {
            throw OPTION_PATTERN_UNCHANGED_EXCEPTION.create(manabarPattern.getString());
        }

        manaPreference.setManabarPattern(manabarPattern);

        source.sendFeedback(() -> Text.literal("Updated manabar pattern for player" + player.getNameForScoreboard() + " to " + manabarPattern.getString() + "."), false);
        return 1;
    }

    public static int executeSetType(ServerCommandSource source, ManabarTypes manabarType) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.getManabarType() == manabarType.getIndex()) {
            throw OPTION_TYPE_UNCHANGED_EXCEPTION.create(manabarType.getName());
        }

        manaPreference.setManabarType(manabarType.getIndex());

        source.sendFeedback(() -> Text.literal("Updated manabar type for player " + player.getNameForScoreboard() + " to " + manabarType.getName() + "."), false);
        return 1;
    }

    public static int executeSetPosition(ServerCommandSource source, ManabarPositions manabarPosition) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.getManabarPosition() == manabarPosition.getIndex()) {
            throw OPTION_POSITION_UNCHANGED_EXCEPTION.create(manabarPosition.getName());
        }

        manaPreference.setManabarPosition(manabarPosition.getIndex());

        source.sendFeedback(() -> Text.literal("Updated manabar position for player " + player.getNameForScoreboard() + " to " + manabarPosition.getName() + "."), false);
        return 1;
    }

    public static int executeSetColor(ServerCommandSource source, BossBar.Color manabarColor) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.getManabarColor().equals(manabarColor)) {
            throw OPTION_COLOR_UNCHANGED_EXCEPTION.create(manabarColor.getName());
        }

        manaPreference.setManabarColor(manabarColor);

        source.sendFeedback(() -> Text.literal("Updated manabar color for player " + player.getNameForScoreboard() + " to " + manabarColor.getName() + "."), false);
        return 1;
    }

    public static int executeSetStyle(ServerCommandSource source, BossBar.Style manabarStyle) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.getManabarStyle().equals(manabarStyle)) {
            throw OPTION_STYLE_UNCHANGED_EXCEPTION.create(manabarStyle.getName());
        }

        manaPreference.setManabarStyle(manabarStyle);

        source.sendFeedback(() -> Text.literal("Updated manabar style for player " + player.getNameForScoreboard() + " to " + manabarStyle.getName() + "."), false);
        return 1;
    }

    public static int executeSetPointsPerCharacter(ServerCommandSource source, int pointsPerCharacter) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.getPointsPerCharacter() == pointsPerCharacter) {
            throw OPTION_POINTS_PER_CHARACTER_UNCHANGED_EXCEPTION.create(pointsPerCharacter);
        }

        manaPreference.setPointsPerCharacter(pointsPerCharacter);

        source.sendFeedback(() -> Text.literal("Updated points per character for player " + player.getNameForScoreboard() + " to " + pointsPerCharacter + "."), false);
        return 1;
    }

    public static int executeSetCharacter(ServerCommandSource source, Text targetManaCharacter) throws CommandSyntaxException {
        return executeSetCharacter(source, targetManaCharacter, -1);
    }

    public static int executeSetCharacter(ServerCommandSource source, Text targetManaCharacter, int manaCharacterTypeIndex) throws CommandSyntaxException {
        return executeSetCharacter(source, targetManaCharacter, manaCharacterTypeIndex, -1);
    }

    public static int executeSetCharacter(ServerCommandSource source, Text targetManaCharacter, int manaCharacterTypeIndex, int manaCharacterIndex) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        List<List<Text>> manaCharacter = manaPreference.getManaCharacter();

        int startManaCharTypeIndex = manaCharacterTypeIndex == -1 ? 0 : manaCharacterTypeIndex;
        int endManaCharTypeIndex = manaCharacterTypeIndex == -1 ? Pentamana.MANA_CHARACTER_TYPE_INDEX_LIMIT : manaCharacterTypeIndex;
        int startManaCharIndex = manaCharacterIndex == -1 ? 0 : manaCharacterIndex;
        int endManaCharIndex = manaCharacterIndex == -1 ? Pentamana.MANA_CHARACTER_INDEX_LIMIT : manaCharacterIndex;

        MutableInt miss = new MutableInt(0);
        IntStream.rangeClosed(startManaCharTypeIndex, endManaCharTypeIndex)
            .forEach(cti -> IntStream.rangeClosed(startManaCharIndex, endManaCharIndex)
                .forEach(ci -> {
                    if (!manaCharacter.get(cti).get(ci).equals(targetManaCharacter)) {
                        miss.increment();
                        manaCharacter.get(cti).set(ci, targetManaCharacter);
                    }
                })
            );

        if (miss.intValue() == 0) {
            throw OPTION_MANA_CHARACTER_UNCHANGED_EXCEPTION.create(targetManaCharacter, manaCharacterTypeIndex, manaCharacterIndex);
        }

        source.sendFeedback(() -> Text.literal("Updated " + (manaCharacterIndex == -1 ? "" : (" #" + manaCharacterIndex)) + (manaCharacterTypeIndex == -1 ? "" : (" " + manaCharacterTypeIndex + " point")) + " mana character for player " + player.getNameForScoreboard() + " to " + targetManaCharacter.getString() + "."), false);
        return 1;
    }

    public static int executeReset(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        manaPreference.setIsVisible(Pentamana.isVisible);
        manaPreference.setIsCompression(Pentamana.isCompression);
        manaPreference.setCompressionSize(Pentamana.compressionSize);
        manaPreference.setManabarPattern(Pentamana.manabarPattern);
        manaPreference.setManabarType(Pentamana.manabarType);
        manaPreference.setManabarPosition(Pentamana.manabarPosition);
        manaPreference.setManabarColor(Pentamana.manabarColor);
        manaPreference.setManabarStyle(Pentamana.manabarStyle);
        manaPreference.setPointsPerCharacter(Pentamana.pointsPerCharacter);
        manaPreference.setManaCharacter(new ArrayList<>(Pentamana.manaCharacter));

        source.sendFeedback(() -> Text.literal("Reset manabar options for player " + player.getNameForScoreboard() + "."), false);
        return 0;
    }

    public static int executeResetVisibility(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        manaPreference.setIsVisible(Pentamana.isVisible);

        source.sendFeedback(() -> Text.literal("Reset manabar visibility for player " + player.getNameForScoreboard() + "."), false);
        return 0;
    }

    public static int executeResetCompression(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        manaPreference.setIsCompression(Pentamana.isCompression);

        source.sendFeedback(() -> Text.literal("Reset manabar compression for player " + player.getNameForScoreboard() + "."), false);
        return 0;
    }

    public static int executeResetCompressionSize(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        manaPreference.setCompressionSize(Pentamana.compressionSize);

        source.sendFeedback(() -> Text.literal("Reset manabar compression size for player " + player.getNameForScoreboard() + "."), false);
        return 0;
    }

    public static int executeResetPattern(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        manaPreference.setManabarPattern(Pentamana.manabarPattern);

        source.sendFeedback(() -> Text.literal("Reset manabar pattern for player " + player.getNameForScoreboard() + "."), false);
        return 0;
    }

    public static int executeResetType(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        manaPreference.setManabarType(Pentamana.manabarType);

        source.sendFeedback(() -> Text.literal("Reset manabar type for player " + player.getNameForScoreboard() + "."), false);
        return 0;
    }

    public static int executeResetPosition(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        manaPreference.setManabarPosition(Pentamana.manabarPosition);

        source.sendFeedback(() -> Text.literal("Reset manabar position for player " + player.getNameForScoreboard() + "."), false);
        return 0;
    }

    public static int executeResetColor(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        manaPreference.setManabarColor(Pentamana.manabarColor);

        source.sendFeedback(() -> Text.literal("Reset manabar color for player " + player.getNameForScoreboard() + "."), false);
        return 0;
    }

    public static int executeResetStyle(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        manaPreference.setManabarStyle(Pentamana.manabarStyle);

        source.sendFeedback(() -> Text.literal("Reset manabar style for player " + player.getNameForScoreboard() + "."), false);
        return 0;
    }

    public static int executeResetPointsPerCharacter(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        manaPreference.setPointsPerCharacter(Pentamana.pointsPerCharacter);

        source.sendFeedback(() -> Text.literal("Reset points per character for player " + player.getNameForScoreboard() + "."), false);
        return 0;
    }

    public static int executeResetCharacter(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        manaPreference.setManaCharacter(new ArrayList<>(Pentamana.manaCharacter));

        source.sendFeedback(() -> Text.literal("Reset mana character for player " + player.getNameForScoreboard() + "."), false);
        return 0;
    }
}

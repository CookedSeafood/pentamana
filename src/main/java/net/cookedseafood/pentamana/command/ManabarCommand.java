package net.cookedseafood.pentamana.command;

import java.util.List;
import java.util.stream.IntStream;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.component.ServerManaBarComponentInstance;
import net.cookedseafood.pentamana.mana.ManaBar;
import net.cookedseafood.pentamana.mana.ManaCharset;
import net.cookedseafood.pentamana.mana.ManaPattern;
import net.cookedseafood.pentamana.mana.ManaRender;
import net.cookedseafood.pentamana.mana.ManaTextual;
import net.cookedseafood.pentamana.mana.ServerManaBar;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.commons.lang3.mutable.MutableInt;

public class ManaBarCommand {
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
                    CommandManager.literal("position")
                    .then(
                        CommandManager.literal("actionbar")
                        .executes(context -> executeSetPosition(context.getSource(), ManaBar.Position.ACTIONBAR))
                    )
                    .then(
                        CommandManager.literal("bossbar")
                        .executes(context -> executeSetPosition(context.getSource(), ManaBar.Position.BOSSBAR))
                    )
                    .then(
                        CommandManager.literal("siderbar")
                        .executes(context -> executeSetPosition(context.getSource(), ManaBar.Position.SIDERBAR))
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
                        .executes(context -> executeSetType(context.getSource(), ManaRender.Type.CHARACTER))
                    )
                    .then(
                        CommandManager.literal("numeric")
                        .executes(context -> executeSetType(context.getSource(), ManaRender.Type.NUMERIC))
                    )
                    .then(
                        CommandManager.literal("percentage")
                        .executes(context -> executeSetType(context.getSource(), ManaRender.Type.PERCENTAGE))
                    )
                    .then(
                        CommandManager.literal("none")
                        .executes(context -> executeSetType(context.getSource(), ManaRender.Type.NONE))
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
        ServerManaBar serverManaBar = ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar();
        if (serverManaBar.isVisible() == isVisible) {
            throw OPTION_VISIBILITY_UNCHANGED_EXCEPTION.create(isVisible);
        }

        serverManaBar.setIsVisible(isVisible);

        source.sendFeedback(() -> Text.literal("Updated manabar visibility for player ").append(player.getDisplayName()).append(" to " + isVisible + "."), false);
        return 1;
    }

    public static int executeSetPosition(ServerCommandSource source, ManaBar.Position manaBarPosition) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ServerManaBar serverManaBar = ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar();
        if (serverManaBar.getPosition() == manaBarPosition) {
            throw OPTION_POSITION_UNCHANGED_EXCEPTION.create(manaBarPosition.getName());
        }

        serverManaBar.setPosition(manaBarPosition);

        source.sendFeedback(() -> Text.literal("Updated manabar position for player ").append(player.getDisplayName()).append(" to " + manaBarPosition.getName() + "."), false);
        return 1;
    }

    public static int executeSetColor(ServerCommandSource source, BossBar.Color manaBarColor) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ServerManaBar serverManaBar = ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar();
        if (serverManaBar.getColor() == manaBarColor) {
            throw OPTION_COLOR_UNCHANGED_EXCEPTION.create(manaBarColor.getName());
        }

        serverManaBar.setColor(manaBarColor);

        source.sendFeedback(() -> Text.literal("Updated manabar color for player ").append(player.getDisplayName()).append(" to " + manaBarColor.getName() + "."), false);
        return 1;
    }

    public static int executeSetStyle(ServerCommandSource source, BossBar.Style manaBarStyle) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ServerManaBar serverManaBar = ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar();
        if (serverManaBar.getStyle() == manaBarStyle) {
            throw OPTION_STYLE_UNCHANGED_EXCEPTION.create(manaBarStyle.getName());
        }

        serverManaBar.setStyle(manaBarStyle);

        source.sendFeedback(() -> Text.literal("Updated manabar style for player ").append(player.getDisplayName()).append(" to " + manaBarStyle.getName() + "."), false);
        return 1;
    }

    public static int executeSetPattern(ServerCommandSource source, Text manaBarPattern) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPattern manaPattern = ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar().getTextual().getPattern();
        List<Text> targetManabarPattern = manaBarPattern.getSiblings();
        if (manaPattern.getPattern().equals(targetManabarPattern)) {
            throw OPTION_PATTERN_UNCHANGED_EXCEPTION.create(manaBarPattern.getString());
        }

        manaPattern.setPattern(targetManabarPattern);

        source.sendFeedback(() -> Text.literal("Updated manabar pattern for player").append(player.getDisplayName()).append(" to " + manaBarPattern.getString() + "."), false);
        return 1;
    }

    public static int executeSetType(ServerCommandSource source, ManaRender.Type manaBarType) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaRender manaRender = ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar().getTextual().getRender();
        if (manaRender.getType() == manaBarType) {
            throw OPTION_TYPE_UNCHANGED_EXCEPTION.create(manaBarType.getName());
        }

        manaRender.setType(manaBarType);

        source.sendFeedback(() -> Text.literal("Updated manabar type for player ").append(player.getDisplayName()).append(" to " + manaBarType.getName() + "."), false);
        return 1;
    }

    public static int executeSetPointsPerCharacter(ServerCommandSource source, int pointsPerCharacter) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaRender manaRender = ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar().getTextual().getRender();
        if (manaRender.getPointsPerCharacter() == pointsPerCharacter) {
            throw OPTION_POINTS_PER_CHARACTER_UNCHANGED_EXCEPTION.create(pointsPerCharacter);
        }

        manaRender.setPointsPerCharacter(pointsPerCharacter);

        source.sendFeedback(() -> Text.literal("Updated points per character for player ").append(player.getDisplayName()).append(" to " + pointsPerCharacter + "."), false);
        return 1;
    }

    public static int executeSetCompression(ServerCommandSource source, boolean isCompression) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaRender manaRender = ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar().getTextual().getRender();
        if (manaRender.isCompression() == isCompression) {
            throw OPTION_COMPRESSION_UNCHANGED_EXCEPTION.create(isCompression);
        }

        manaRender.setIsCompression(isCompression);

        source.sendFeedback(() -> Text.literal("Updated manabar compression for player ").append(player.getDisplayName()).append(" to " + isCompression + "."), false);
        return 1;
    }

    public static int executeSetCompressionSize(ServerCommandSource source, byte compressionSize) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaRender manaRender = ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar().getTextual().getRender();
        if (manaRender.getCompressionSize() == compressionSize) {
            throw OPTION_COMPRESSION_SIZE_UNCHANGED_EXCEPTION.create(compressionSize);
        }

        manaRender.setCompressionSize(compressionSize);

        source.sendFeedback(() -> Text.literal("Updated manabar compression size for player ").append(player.getDisplayName()).append(" to " + compressionSize + "."), false);
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
        ManaCharset manaCharset = ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar().getTextual().getRender().getCharset();
        List<List<Text>> manaCharacter = manaCharset.getCharset();

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

        source.sendFeedback(() -> Text.literal("Updated " + (manaCharacterIndex == -1 ? "" : (" #" + manaCharacterIndex)) + (manaCharacterTypeIndex == -1 ? "" : (" " + manaCharacterTypeIndex + " point")) + " mana character for player ").append(player.getDisplayName()).append(" to " + targetManaCharacter.getString() + "."), false);
        return 1;
    }

    public static int executeReset(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ServerManaBar serverManaBar = ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar();
        ManaTextual textual = serverManaBar.getTextual();
        ManaPattern pattern = textual.getPattern();
        ManaRender render = textual.getRender();
        ManaCharset charset = render.getCharset();
        serverManaBar.setIsVisible(Pentamana.isVisible);
        serverManaBar.setPosition(Pentamana.manaBarPosition);
        serverManaBar.setColor(Pentamana.manaBarColor);
        serverManaBar.setStyle(Pentamana.manaBarStyle);
        pattern.setPattern(Pentamana.manaPattern.deepCopy().getPattern());
        render.setType(Pentamana.manaRenderType);
        render.setPointsPerCharacter(Pentamana.pointsPerCharacter);
        render.setIsCompression(Pentamana.isCompression);
        render.setCompressionSize(Pentamana.compressionSize);
        charset.setCharset(Pentamana.manaCharset.deepCopy().getCharset());

        source.sendFeedback(() -> Text.literal("Reset manabar options for player ").append(player.getDisplayName()).append("."), false);
        return 0;
    }

    public static int executeResetVisibility(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar().setIsVisible(Pentamana.isVisible);

        source.sendFeedback(() -> Text.literal("Reset manabar visibility for player ").append(player.getDisplayName()).append("."), false);
        return 0;
    }

    public static int executeResetPosition(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar().setPosition(Pentamana.manaBarPosition);

        source.sendFeedback(() -> Text.literal("Reset manabar position for player ").append(player.getDisplayName()).append("."), false);
        return 0;
    }

    public static int executeResetColor(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar().setColor(Pentamana.manaBarColor);

        source.sendFeedback(() -> Text.literal("Reset manabar color for player ").append(player.getDisplayName()).append("."), false);
        return 0;
    }

    public static int executeResetStyle(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar().setStyle(Pentamana.manaBarStyle);

        source.sendFeedback(() -> Text.literal("Reset manabar style for player ").append(player.getDisplayName()).append("."), false);
        return 0;
    }

    public static int executeResetPattern(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar().getTextual().setPattern(Pentamana.manaPattern);

        source.sendFeedback(() -> Text.literal("Reset manabar pattern for player ").append(player.getDisplayName()).append("."), false);
        return 0;
    }

    public static int executeResetType(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar().getTextual().getRender().setType(Pentamana.manaRenderType);

        source.sendFeedback(() -> Text.literal("Reset manabar type for player ").append(player.getDisplayName()).append("."), false);
        return 0;
    }

    public static int executeResetPointsPerCharacter(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar().getTextual().getRender().setPointsPerCharacter(Pentamana.pointsPerCharacter);

        source.sendFeedback(() -> Text.literal("Reset points per character for player ").append(player.getDisplayName()).append("."), false);
        return 0;
    }

    public static int executeResetCompression(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar().getTextual().getRender().setIsCompression(Pentamana.isCompression);

        source.sendFeedback(() -> Text.literal("Reset manabar compression for player ").append(player.getDisplayName()).append("."), false);
        return 0;
    }

    public static int executeResetCompressionSize(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar().getTextual().getRender().setCompressionSize(Pentamana.compressionSize);

        source.sendFeedback(() -> Text.literal("Reset manabar compression size for player ").append(player.getDisplayName()).append("."), false);
        return 0;
    }

    public static int executeResetCharacter(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar().getTextual().getRender().getCharset().setCharset(Pentamana.manaCharset.deepCopy().getCharset());

        source.sendFeedback(() -> Text.literal("Reset mana character for player ").append(player.getDisplayName()).append("."), false);
        return 0;
    }
}

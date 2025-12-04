package net.hederamc.pentamana.command;

import java.util.List;
import java.util.stream.IntStream;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.hederamc.pentamana.Pentamana;
import net.hederamc.pentamana.data.PentamanaConfig;
import net.hederamc.pentamana.render.ManaBar;
import net.hederamc.pentamana.render.ManaCharset;
import net.hederamc.pentamana.render.ManaPattern;
import net.hederamc.pentamana.render.ManaRender;
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
    private static final DynamicCommandExceptionType OPTION_SUPPRESSION_UNCHANGED_EXCEPTION =
        new DynamicCommandExceptionType(isSuppressed -> Text.literal("Nothing changed. That player already has " + (boolean)isSuppressed + " for manabar suppression."));
    private static final DynamicCommandExceptionType OPTION_COMPRESSION_UNCHANGED_EXCEPTION =
        new DynamicCommandExceptionType(isCompressed -> Text.literal("Nothing changed. That player already has " + (boolean)isCompressed + " for manabar compression."));
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
        new DynamicCommandExceptionType(pointsPerCharacter -> Text.literal("Nothing changed. That player already has " + (int)pointsPerCharacter + " for mana points per character."));
    private static final Dynamic3CommandExceptionType OPTION_MANA_CHARACTER_UNCHANGED_EXCEPTION =
        new Dynamic3CommandExceptionType((manaCharacter, charTypeIndex, charIndex) -> Text.literal("Nothing changed. That player already has " + (String)manaCharacter + " for" + ((int)charIndex == -1 ? "" : (" #" + (int)charIndex)) + ((int)charTypeIndex == -1 ? "" : (" " + (int)charTypeIndex + " point")) + " mana character."));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(
            CommandManager.literal("manabar")
            .then(
                CommandManager.literal("set")
                .then(
                    CommandManager.literal("visibility")
                    .then(
                        CommandManager.argument("visibility", BoolArgumentType.bool())
                        .executes(context -> executeSetVisibility(context.getSource(), BoolArgumentType.getBool(context, "visibility")))
                    )
                )
                .then(
                    CommandManager.literal("suppression")
                    .then(
                        CommandManager.argument("suppression", BoolArgumentType.bool())
                        .executes(context -> executeSetSuppression(context.getSource(), BoolArgumentType.getBool(context, "suppression")))
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
                    CommandManager.literal("pattern")
                    .then(
                        CommandManager.argument("text", TextArgumentType.text(registryAccess))
                        .executes(context -> executeSetPattern(context.getSource(), TextArgumentType.getTextArgument(context, "text")))
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
                        CommandManager.argument("compression", BoolArgumentType.bool())
                        .executes(context -> executeSetCompression(context.getSource(), BoolArgumentType.getBool(context, "compression")))
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
                    CommandManager.literal("suppression")
                    .executes(context -> executeResetSuppression(context.getSource()))
                )
                .then(
                    CommandManager.literal("position")
                    .executes(context -> executeResetPosition(context.getSource()))
                )
                .then(
                    CommandManager.literal("type")
                    .executes(context -> executeResetType(context.getSource()))
                )
                .then(
                    CommandManager.literal("pattern")
                    .executes(context -> executeResetPattern(context.getSource()))
                )
                .then(
                    CommandManager.literal("points_per_character")
                    .executes(context -> executeResetPointsPerCharacter(context.getSource()))
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
                    CommandManager.literal("character")
                    .executes(context -> executeResetCharacter(context.getSource()))
                )
                .then(
                    CommandManager.literal("color")
                    .executes(context -> executeResetColor(context.getSource()))
                )
                .then(
                    CommandManager.literal("style")
                    .executes(context -> executeResetStyle(context.getSource()))
                )
            )
        );
    }

    public static int executeSetVisibility(ServerCommandSource source, boolean isVisible) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        if (player.isManaBarVisible() == isVisible) {
            throw OPTION_VISIBILITY_UNCHANGED_EXCEPTION.create(isVisible);
        }

        player.setManaBarVisibility(isVisible);

        if (!player.isManaBarSuppressed()) {
            if (isVisible) {
                player.putManaBarDisplay();
            } else {
                player.removeManaBarDisplay();
            }
        }

        source.sendFeedback(() -> Text.literal("Updated manabar visibility for player ").append(player.getDisplayName()).append(" to " + isVisible + "."), false);
        return 1;
    }

    public static int executeSetSuppression(ServerCommandSource source, boolean isSuppressed) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        if (player.isManaBarSuppressed() == isSuppressed) {
            throw OPTION_SUPPRESSION_UNCHANGED_EXCEPTION.create(isSuppressed);
        }

        player.setManaBarSuppression(isSuppressed);

        if (player.isManaBarVisible()) {
            if (isSuppressed) {
                player.removeManaBarDisplay();
            } else {
                player.putManaBarDisplay();
            }
        }

        source.sendFeedback(() -> Text.literal("Updated manabar suppression for player ").append(player.getDisplayName()).append(" to " + isSuppressed + "."), false);
        return 1;
    }

    public static int executeSetPosition(ServerCommandSource source, ManaBar.Position position) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        if (player.getManaBarPosition() == position) {
            throw OPTION_POSITION_UNCHANGED_EXCEPTION.create(position.getName());
        }

        if (player.isManaBarVisible() && !player.isManaBarSuppressed()) {
            player.removeManaBarDisplay();
            player.putManaBarDisplay(position);
        }

        player.setManaBarPosition(position);

        source.sendFeedback(() -> Text.literal("Updated manabar position for player ").append(player.getDisplayName()).append(" to " + position.getName() + "."), false);
        return 1;
    }

    public static int executeSetType(ServerCommandSource source, ManaRender.Type type) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        if (player.getManaRenderType() == type) {
            throw OPTION_TYPE_UNCHANGED_EXCEPTION.create(type.getName());
        }

        player.setManaRenderType(type);

        if (player.isManaBarVisible() && !player.isManaBarSuppressed()) {
            player.putManaBarDisplay();
        }

        source.sendFeedback(() -> Text.literal("Updated manabar type for player ").append(player.getDisplayName()).append(" to " + type.getName() + "."), false);
        return 1;
    }

    public static int executeSetPattern(ServerCommandSource source, Text wrappedPattern) throws CommandSyntaxException {
        return executeSetPattern(source, wrappedPattern.getSiblings());
    }

    public static int executeSetPattern(ServerCommandSource source, List<Text> pattern) throws CommandSyntaxException {
        return executeSetPattern(source, new ManaPattern(pattern));
    }

    public static int executeSetPattern(ServerCommandSource source, ManaPattern pattern) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        if (player.getManaPattern().equals(pattern)) {
            throw OPTION_PATTERN_UNCHANGED_EXCEPTION.create(pattern.toText().getString());
        }

        player.setManaPattern(pattern);

        if (player.isManaBarVisible() && !player.isManaBarSuppressed()) {
            player.putManaBarDisplay();
        }

        source.sendFeedback(() -> Text.literal("Updated manabar pattern for player").append(player.getDisplayName()).append(" to " + pattern.toText().getString() + "."), false);
        return 1;
    }

    public static int executeSetPointsPerCharacter(ServerCommandSource source, int pointsPerCharacter) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        if (player.getManaPointsPerCharacter() == pointsPerCharacter) {
            throw OPTION_POINTS_PER_CHARACTER_UNCHANGED_EXCEPTION.create(pointsPerCharacter);
        }

        player.setManaPointsPerCharacter(pointsPerCharacter);

        if (player.isManaBarVisible() && !player.isManaBarSuppressed()) {
            player.putManaBarDisplay();
        }

        source.sendFeedback(() -> Text.literal("Updated mana points per character for player ").append(player.getDisplayName()).append(" to " + pointsPerCharacter + "."), false);
        return 1;
    }

    public static int executeSetCompression(ServerCommandSource source, boolean isCompressed) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        if (player.isManaRenderCompressed() == isCompressed) {
            throw OPTION_COMPRESSION_UNCHANGED_EXCEPTION.create(isCompressed);
        }

        player.setManaRenderCompression(isCompressed);

        if (player.isManaBarVisible() && !player.isManaBarSuppressed()) {
            player.putManaBarDisplay();
        }

        source.sendFeedback(() -> Text.literal("Updated manabar compression for player ").append(player.getDisplayName()).append(" to " + isCompressed + "."), false);
        return 1;
    }

    public static int executeSetCompressionSize(ServerCommandSource source, byte compressionSize) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        if (player.getManaRenderCompressionSize() == compressionSize) {
            throw OPTION_COMPRESSION_SIZE_UNCHANGED_EXCEPTION.create(compressionSize);
        }

        player.setManaRenderCompressionSize(compressionSize);

        if (player.isManaBarVisible() && !player.isManaBarSuppressed()) {
            player.putManaBarDisplay();
        }

        source.sendFeedback(() -> Text.literal("Updated manabar compression size for player ").append(player.getDisplayName()).append(" to " + compressionSize + "."), false);
        return 1;
    }

    public static int executeSetCharacter(ServerCommandSource source, Text targetChar) throws CommandSyntaxException {
        return executeSetCharacter(source, targetChar, -1);
    }

    public static int executeSetCharacter(ServerCommandSource source, Text targetChar, int charTypeIndex) throws CommandSyntaxException {
        return executeSetCharacter(source, targetChar, charTypeIndex, -1);
    }

    public static int executeSetCharacter(ServerCommandSource source, Text targetChar, int charTypeIndex, int charIndex) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaCharset manaCharset = player.getManaCharset();

        int startCharTypeIndex = charTypeIndex == -1 ? 0 : charTypeIndex;
        int endCharTypeIndex = charTypeIndex == -1 ? Pentamana.MANA_CHARACTER_TYPE_INDEX_LIMIT : charTypeIndex;
        int startCharIndex = charIndex == -1 ? 0 : charIndex;
        int endCharIndex = charIndex == -1 ? Pentamana.MANA_CHARACTER_INDEX_LIMIT : charIndex;

        MutableInt miss = new MutableInt(0);
        IntStream.rangeClosed(startCharTypeIndex, endCharTypeIndex)
            .forEach(cti -> IntStream.rangeClosed(startCharIndex, endCharIndex)
                .forEach(ci -> {
                    if (!manaCharset.get(cti).get(ci).equals(targetChar)) {
                        miss.increment();
                        manaCharset.get(cti).set(ci, targetChar);
                    }
                })
            );

        if (miss.intValue() == 0) {
            throw OPTION_MANA_CHARACTER_UNCHANGED_EXCEPTION.create(targetChar, charTypeIndex, charIndex);
        }

        player.setManaCharset(manaCharset);

        if (player.isManaBarVisible() && !player.isManaBarSuppressed()) {
            player.putManaBarDisplay();
        }

        source.sendFeedback(() -> Text.literal("Updated " + (charIndex == -1 ? "" : (" #" + charIndex)) + (charTypeIndex == -1 ? "" : (" " + charTypeIndex + " point")) + " mana character for player ").append(player.getDisplayName()).append(" to " + targetChar.getString() + "."), false);
        return 1;
    }

    public static int executeSetColor(ServerCommandSource source, BossBar.Color color) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        if (player.getManaBarColor() == color) {
            throw OPTION_COLOR_UNCHANGED_EXCEPTION.create(color.getName());
        }

        player.setManaBarColor(color);

        if (player.isManaBarVisible() && !player.isManaBarSuppressed()) {
            player.putManaBarDisplay();
        }

        source.sendFeedback(() -> Text.literal("Updated manabar color for player ").append(player.getDisplayName()).append(" to " + color.getName() + "."), false);
        return 1;
    }

    public static int executeSetStyle(ServerCommandSource source, BossBar.Style style) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        if (player.getManaBarStyle() == style) {
            throw OPTION_STYLE_UNCHANGED_EXCEPTION.create(style.getName());
        }

        player.setManaBarStyle(style);

        if (player.isManaBarVisible() && !player.isManaBarSuppressed()) {
            player.putManaBarDisplay();
        }

        source.sendFeedback(() -> Text.literal("Updated manabar style for player ").append(player.getDisplayName()).append(" to " + style.getName() + "."), false);
        return 1;
    }

    public static int executeReset(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();

        if (player.isManaBarVisible() != PentamanaConfig.DefaultPreference.isVisible) {
            player.setManaBarVisibility(PentamanaConfig.DefaultPreference.isVisible);

            if (!player.isManaBarSuppressed()) {
                if (PentamanaConfig.DefaultPreference.isVisible) {
                    player.putManaBarDisplay();
                } else {
                    player.removeManaBarDisplay();
                }
            }
        }

        if (player.isManaBarSuppressed() != PentamanaConfig.DefaultPreference.isSuppressed) {
            player.setManaBarSuppression(PentamanaConfig.DefaultPreference.isSuppressed);

            if (player.isManaBarVisible()) {
                if (PentamanaConfig.DefaultPreference.isSuppressed) {
                    player.removeManaBarDisplay();
                } else {
                    player.putManaBarDisplay();
                }
            }
        }

        if (player.getManaBarPosition() != PentamanaConfig.DefaultPreference.position) {
            if (player.isManaBarVisible() && !player.isManaBarSuppressed()) {
                player.removeManaBarDisplay();
                player.putManaBarDisplay(PentamanaConfig.DefaultPreference.position);
            }

            executeSetPosition(source, PentamanaConfig.DefaultPreference.position);
        }

        boolean different = false;

        if (player.getManaRenderType() != PentamanaConfig.DefaultPreference.type) {
            player.setManaRenderType(PentamanaConfig.DefaultPreference.type);
            different = true;
        }

        if (!player.getManaPattern().equals(PentamanaConfig.DefaultPreference.pattern)) {
            player.setManaPattern(PentamanaConfig.DefaultPreference.pattern.deepCopy());
            different = true;
        }

        if (player.getManaPointsPerCharacter() != PentamanaConfig.DefaultPreference.pointsPerCharacter) {
            player.setManaPointsPerCharacter(PentamanaConfig.DefaultPreference.pointsPerCharacter);
            different = true;
        }

        if (player.isManaRenderCompressed() != PentamanaConfig.DefaultPreference.isCompressed) {
            player.setManaRenderCompression(PentamanaConfig.DefaultPreference.isCompressed);
            different = true;
        }

        if (player.getManaRenderCompressionSize() != PentamanaConfig.DefaultPreference.compressionSize) {
            player.setManaRenderCompressionSize(PentamanaConfig.DefaultPreference.compressionSize);
            different = true;
        }

        if (!player.getManaCharset().equals(PentamanaConfig.DefaultPreference.charset)) {
            player.setManaCharset(PentamanaConfig.DefaultPreference.charset.deepCopy());
            different = true;
        }

        if (player.getManaBarColor() != PentamanaConfig.DefaultPreference.color) {
            player.setManaBarColor(PentamanaConfig.DefaultPreference.color);
            different = true;
        }

        if (player.getManaBarStyle() != PentamanaConfig.DefaultPreference.style) {
            player.setManaBarStyle(PentamanaConfig.DefaultPreference.style);
            different = true;
        }

        if (different && player.isManaBarVisible() && !player.isManaBarSuppressed()) {
            player.putManaBarDisplay();
        }

        source.sendFeedback(() -> Text.literal("Reset manabar options for player ").append(player.getDisplayName()).append("."), false);
        return 0;
    }

    public static int executeResetVisibility(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();

        if (player.isManaBarVisible() != PentamanaConfig.DefaultPreference.isVisible) {
            player.setManaBarVisibility(PentamanaConfig.DefaultPreference.isVisible);

            if (!player.isManaBarSuppressed()) {
                if (PentamanaConfig.DefaultPreference.isVisible) {
                    player.putManaBarDisplay();
                } else {
                    player.removeManaBarDisplay();
                }
            }
        }

        source.sendFeedback(() -> Text.literal("Reset manabar visibility for player ").append(player.getDisplayName()).append("."), false);
        return 0;
    }

    public static int executeResetSuppression(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();

        if (player.isManaBarSuppressed() != PentamanaConfig.DefaultPreference.isSuppressed) {
            player.setManaBarSuppression(PentamanaConfig.DefaultPreference.isSuppressed);

            if (player.isManaBarVisible()) {
                if (PentamanaConfig.DefaultPreference.isSuppressed) {
                    player.removeManaBarDisplay();
                } else {
                    player.putManaBarDisplay();
                }
            }
        }

        source.sendFeedback(() -> Text.literal("Reset manabar suppression for player ").append(player.getDisplayName()).append("."), false);
        return 1;
    }

    public static int executeResetPosition(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();

        if (player.getManaBarPosition() != PentamanaConfig.DefaultPreference.position) {
            if (player.isManaBarVisible() && !player.isManaBarSuppressed()) {
                player.removeManaBarDisplay();
                player.putManaBarDisplay(PentamanaConfig.DefaultPreference.position);
            }

            executeSetPosition(source, PentamanaConfig.DefaultPreference.position);
        }

        source.sendFeedback(() -> Text.literal("Reset manabar position for player ").append(player.getDisplayName()).append("."), false);
        return 0;
    }

    public static int executeResetType(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();

        if (player.getManaRenderType() != PentamanaConfig.DefaultPreference.type) {
            player.setManaRenderType(PentamanaConfig.DefaultPreference.type);

            if (player.isManaBarVisible() && !player.isManaBarSuppressed()) {
                player.putManaBarDisplay();
            }
        }

        source.sendFeedback(() -> Text.literal("Reset manabar type for player ").append(player.getDisplayName()).append("."), false);
        return 0;
    }

    public static int executeResetPattern(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();

        if (!player.getManaPattern().equals(PentamanaConfig.DefaultPreference.pattern)) {
            player.setManaPattern(PentamanaConfig.DefaultPreference.pattern.deepCopy());

            if (player.isManaBarVisible() && !player.isManaBarSuppressed()) {
                player.putManaBarDisplay();
            }
        }

        source.sendFeedback(() -> Text.literal("Reset manabar pattern for player ").append(player.getDisplayName()).append("."), false);
        return 0;
    }

    public static int executeResetPointsPerCharacter(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();

        if (player.getManaPointsPerCharacter() != PentamanaConfig.DefaultPreference.pointsPerCharacter) {
            player.setManaPointsPerCharacter(PentamanaConfig.DefaultPreference.pointsPerCharacter);

            if (player.isManaBarVisible() && !player.isManaBarSuppressed()) {
                player.putManaBarDisplay();
            }
        }

        source.sendFeedback(() -> Text.literal("Reset points per character for player ").append(player.getDisplayName()).append("."), false);
        return 0;
    }

    public static int executeResetCompression(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();

        if (player.isManaRenderCompressed() != PentamanaConfig.DefaultPreference.isCompressed) {
            player.setManaRenderCompression(PentamanaConfig.DefaultPreference.isCompressed);

            if (player.isManaBarVisible() && !player.isManaBarSuppressed()) {
                player.putManaBarDisplay();
            }
        }

        source.sendFeedback(() -> Text.literal("Reset manabar compression for player ").append(player.getDisplayName()).append("."), false);
        return 0;
    }

    public static int executeResetCompressionSize(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();

        if (player.getManaRenderCompressionSize() != PentamanaConfig.DefaultPreference.compressionSize) {
            player.setManaRenderCompressionSize(PentamanaConfig.DefaultPreference.compressionSize);

            if (player.isManaBarVisible() && !player.isManaBarSuppressed()) {
                player.putManaBarDisplay();
            }
        }

        source.sendFeedback(() -> Text.literal("Reset manabar compression size for player ").append(player.getDisplayName()).append("."), false);
        return 0;
    }

    public static int executeResetCharacter(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();

        if (!player.getManaCharset().equals(PentamanaConfig.DefaultPreference.charset)) {
            player.setManaCharset(PentamanaConfig.DefaultPreference.charset.deepCopy());

            if (player.isManaBarVisible() && !player.isManaBarSuppressed()) {
                player.putManaBarDisplay();
            }
        }

        source.sendFeedback(() -> Text.literal("Reset mana character for player ").append(player.getDisplayName()).append("."), false);
        return 0;
    }

    public static int executeResetColor(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();

        if (player.getManaBarColor() != PentamanaConfig.DefaultPreference.color) {
            player.setManaBarColor(PentamanaConfig.DefaultPreference.color);

            if (player.isManaBarVisible() && !player.isManaBarSuppressed()) {
                player.putManaBarDisplay();
            }
        }

        source.sendFeedback(() -> Text.literal("Reset manabar color for player ").append(player.getDisplayName()).append("."), false);
        return 0;
    }

    public static int executeResetStyle(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();

        if (player.getManaBarStyle() != PentamanaConfig.DefaultPreference.style) {
            player.setManaBarStyle(PentamanaConfig.DefaultPreference.style);

            if (player.isManaBarVisible() && !player.isManaBarSuppressed()) {
                player.putManaBarDisplay();
            }
        }

        source.sendFeedback(() -> Text.literal("Reset manabar style for player ").append(player.getDisplayName()).append("."), false);
        return 0;
    }
}

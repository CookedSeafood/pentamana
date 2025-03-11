package net.cookedseafood.pentamana.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
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
import org.apache.commons.lang3.mutable.MutableInt;

public class ManaCommand {
    private static final SimpleCommandExceptionType OPTION_ALREADY_ENABLED_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Nothing changed. Mana is already enabled for that player."));
    private static final SimpleCommandExceptionType OPTION_ALREADY_DISABLED_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Nothing changed. Mana is already disbaled for that player."));
    private static final DynamicCommandExceptionType OPTION_VISIBILITY_UNCHANGED_EXCEPTION =
        new DynamicCommandExceptionType(isVisible -> Text.literal("Nothing changed. That player already has " + (boolean)isVisible + " for mana visibility."));
    private static final DynamicCommandExceptionType OPTION_COMPRESSION_UNCHANGED_EXCEPTION =
        new DynamicCommandExceptionType(isCompression -> Text.literal("Nothing changed. That player already has " + (boolean)isCompression + " for mana compression."));
    private static final DynamicCommandExceptionType OPTION_COMPRESSION_SIZE_UNCHANGED_EXCEPTION =
        new DynamicCommandExceptionType(compressionSize -> Text.literal("Nothing changed. That player already has " + (boolean)compressionSize + " for mana compression size."));
    private static final DynamicCommandExceptionType OPTION_RENDER_TYPE_UNCHANGED_EXCEPTION =
        new DynamicCommandExceptionType(renderType -> Text.literal("Nothing changed. That player already has " + (String)renderType + " for mana render type."));
    private static final DynamicCommandExceptionType OPTION_RENDER_POSITION_UNCHANGED_EXCEPTION =
        new DynamicCommandExceptionType(renderPosition -> Text.literal("Nothing changed. That player already has " + (String)renderPosition + " for mana render position."));
    private static final DynamicCommandExceptionType OPTION_POINTS_PER_CHARACTER_UNCHANGED_EXCEPTION =
        new DynamicCommandExceptionType(pointsPerCharacter -> Text.literal("Nothing changed. That player already has " + (int)pointsPerCharacter + " for points per character."));
    private static final Dynamic2CommandExceptionType OPTION_CHARACTER_UNCHANGED_EXCEPTION =
        new Dynamic2CommandExceptionType((manaCharacterTypeIndex, manaCharacterIndex) -> Text.literal("Nothing changed. That player already has that" + ((int)manaCharacterIndex == -1 ? "" : (" #" + (int)manaCharacterIndex)) + ((int)manaCharacterTypeIndex == -1 ? "" : (" " + (int)manaCharacterTypeIndex + " point")) + " mana character."));
    private static final DynamicCommandExceptionType OPTION_BOSSBAR_COLOR_UNCHANGED_EXCEPTION =
        new DynamicCommandExceptionType(bossbarColor -> Text.literal("Nothing changed. That player already has " + ((BossBar.Color)bossbarColor).getName() + " for mana bossbar color."));
    private static final DynamicCommandExceptionType OPTION_BOSSBAR_STYLE_UNCHANGED_EXCEPTION =
        new DynamicCommandExceptionType(bossbarStyle -> Text.literal("Nothing changed. That player already has " + ((BossBar.Color)bossbarStyle).getName() + " for mana bossbar style."));

    public ManaCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(
            CommandManager.literal("mana")
            .then(
                CommandManager.literal("disable")
                .executes(context -> executeDisable(context.getSource()))
            )
            .then(
                CommandManager.literal("enable")
                .executes(context -> executeEnable(context.getSource()))
            )
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
                    CommandManager.literal("render_type")
                    .then(
                        CommandManager.literal("none")
                        .executes(context -> executeSetRenderType(context.getSource(), ManabarTypes.NONE))
                    )
                    .then(
                        CommandManager.literal("character")
                        .executes(context -> executeSetRenderType(context.getSource(), ManabarTypes.CHARACTER))
                    )
                    .then(
                        CommandManager.literal("numeric")
                        .executes(context -> executeSetRenderType(context.getSource(), ManabarTypes.NUMERIC))
                    )
                    .then(
                        CommandManager.literal("percentage")
                        .executes(context -> executeSetRenderType(context.getSource(), ManabarTypes.PERCENTAGE))                   )
                )
                .then(
                    CommandManager.literal("render_position")
                    .then(
                        CommandManager.literal("actionbar")
                        .executes(context -> executeSetRenderPosition(context.getSource(), ManabarPositions.ACTIONBAR))
                    )
                    .then(
                        CommandManager.literal("bossbar")
                        .executes(context -> executeSetRenderPosition(context.getSource(), ManabarPositions.BOSSBAR))
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
                            CommandManager.argument("type_index", IntegerArgumentType.integer(0, Pentamana.MANA_CHARACTER_TYPE_INDEX_LIMIT))
                            .executes(context -> executeSetCharacter(context.getSource(), TextArgumentType.getTextArgument(context, "text"), IntegerArgumentType.getInteger(context, "type_index")))
                            .then(
                                CommandManager.argument("character_index", IntegerArgumentType.integer(0, Pentamana.MANA_CHARACTER_INDEX_LIMIT))
                                .executes(context -> executeSetCharacter(context.getSource(), TextArgumentType.getTextArgument(context, "text"), IntegerArgumentType.getInteger(context, "type_index"), IntegerArgumentType.getInteger(context, "character_index")))
                            )
                        )
                    )
                )
                .then(
                    CommandManager.literal("bossbar_color")
                    .then(
                        CommandManager.literal("pink")
                        .executes(context -> executeSetBossBarColor(context.getSource(), BossBar.Color.PINK))
                    )
                    .then(
                        CommandManager.literal("blue")
                        .executes(context -> executeSetBossBarColor(context.getSource(), BossBar.Color.BLUE))
                    )
                    .then(
                        CommandManager.literal("red")
                        .executes(context -> executeSetBossBarColor(context.getSource(), BossBar.Color.RED))
                    )
                    .then(
                        CommandManager.literal("green")
                        .executes(context -> executeSetBossBarColor(context.getSource(), BossBar.Color.GREEN))
                    )
                    .then(
                        CommandManager.literal("yellow")
                        .executes(context -> executeSetBossBarColor(context.getSource(), BossBar.Color.YELLOW))
                    )
                    .then(
                        CommandManager.literal("purple")
                        .executes(context -> executeSetBossBarColor(context.getSource(), BossBar.Color.PURPLE))
                    )
                    .then(
                        CommandManager.literal("white")
                        .executes(context -> executeSetBossBarColor(context.getSource(), BossBar.Color.WHITE))
                    )
                )
                .then(
                    CommandManager.literal("bossbar_style")
                    .then(
                        CommandManager.literal("progress")
                        .executes(context -> executeSetBossBarStyle(context.getSource(), BossBar.Style.PROGRESS))
                    )
                    .then(
                        CommandManager.literal("notched_6")
                        .executes(context -> executeSetBossBarStyle(context.getSource(), BossBar.Style.NOTCHED_6))
                    )
                    .then(
                        CommandManager.literal("notched_10")
                        .executes(context -> executeSetBossBarStyle(context.getSource(), BossBar.Style.NOTCHED_10))
                    )
                    .then(
                        CommandManager.literal("notched_12")
                        .executes(context -> executeSetBossBarStyle(context.getSource(), BossBar.Style.NOTCHED_12))
                    )
                    .then(
                        CommandManager.literal("notched_20")
                        .executes(context -> executeSetBossBarStyle(context.getSource(), BossBar.Style.NOTCHED_20))
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
                    CommandManager.literal("render_type")
                    .executes(context -> executeResetManaRenderType(context.getSource()))
                )
                .then(
                    CommandManager.literal("render_position")
                    .executes(context -> executeResetManaRenderPosition(context.getSource()))
                )
                .then(
                    CommandManager.literal("points_per_character")
                    .executes(context -> executeResetPointsPerCharacter(context.getSource()))
                )
                .then(
                    CommandManager.literal("characters")
                    .executes(context -> executeResetCharacters(context.getSource()))
                )
                .then(
                    CommandManager.literal("bossbar_color")
                    .executes(context -> executeResetBossBarColor(context.getSource()))
                )
                .then(
                    CommandManager.literal("bossbar_style")
                    .executes(context -> executeResetBossBarStyle(context.getSource()))
                )
            )
            .then(
                CommandManager.literal("reload")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> executeReload(context.getSource()))
            )
        );
    }

    public static int executeEnable(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.isEnabled()) {
            throw OPTION_ALREADY_ENABLED_EXCEPTION.create();
        }

        manaPreference.setIsEnabled(true);

        source.sendFeedback(() -> Text.literal("Enabled mana for player " + player.getNameForScoreboard() + "."), false);
        return 1;
    }

    public static int executeDisable(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (!manaPreference.isEnabled()) {
            throw OPTION_ALREADY_DISABLED_EXCEPTION.create();
        }

        manaPreference.setIsEnabled(false);

        source.sendFeedback(() -> Text.literal("Disabled mana for player " + player.getNameForScoreboard() + "."), false);
        if (Pentamana.isForceEnabled) {
            source.sendFeedback(() -> Text.literal("Mana calculation will continue due to the force enabled mode is turned on in server."), false);
        }

        return 1;
    }

    public static int executeSetVisibility(ServerCommandSource source, boolean isVisible) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.isVisible() == isVisible) {
            throw OPTION_VISIBILITY_UNCHANGED_EXCEPTION.create(isVisible);
        }

        manaPreference.setIsVisible(isVisible);

        source.sendFeedback(() -> Text.literal("Updated mana visibility for player " + player.getNameForScoreboard() + " to " + isVisible + "."), false);
        return 1;
    }

    public static int executeSetCompression(ServerCommandSource source, boolean isCompression) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.isCompression() == isCompression) {
            throw OPTION_COMPRESSION_UNCHANGED_EXCEPTION.create(isCompression);
        }

        manaPreference.setIsCompression(isCompression);

        source.sendFeedback(() -> Text.literal("Updated mana compression for player " + player.getNameForScoreboard() + " to " + isCompression + "."), false);
        return 1;
    }

    public static int executeSetCompressionSize(ServerCommandSource source, byte compressionSize) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.getCompressionSize() == compressionSize) {
            throw OPTION_COMPRESSION_SIZE_UNCHANGED_EXCEPTION.create(compressionSize);
        }

        manaPreference.setCompressionSize(compressionSize);

        source.sendFeedback(() -> Text.literal("Updated mana compression size for player " + player.getNameForScoreboard() + " to " + compressionSize + "."), false);
        return 1;
    }

    public static int executeSetRenderType(ServerCommandSource source, ManabarTypes manabarType) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.getManabarType() == manabarType.getIndex()) {
            throw OPTION_RENDER_TYPE_UNCHANGED_EXCEPTION.create(manabarType.getName());
        }

        manaPreference.setManabarType(manabarType.getIndex());

        source.sendFeedback(() -> Text.literal("Updated mana render type for player " + player.getNameForScoreboard() + " to " + manabarType.getName() + "."), false);
        return 1;
    }

    public static int executeSetRenderPosition(ServerCommandSource source, ManabarPositions manabarPosition) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.getManabarPosition() == manabarPosition.getIndex()) {
            throw OPTION_RENDER_POSITION_UNCHANGED_EXCEPTION.create(manabarPosition.getName());
        }

        manaPreference.setManabarPosition(manabarPosition.getIndex());

        source.sendFeedback(() -> Text.literal("Updated mana render position for player " + player.getNameForScoreboard() + " to " + manabarPosition.getName() + "."), false);
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

    public static int executeSetCharacter(ServerCommandSource source, Text manaCharacter) throws CommandSyntaxException {
        return executeSetCharacter(source, manaCharacter, -1);
    }

    public static int executeSetCharacter(ServerCommandSource source, Text manaCharacter, int manaCharacterTypeIndex) throws CommandSyntaxException {
        return executeSetCharacter(source, manaCharacter, manaCharacterTypeIndex, -1);
    }

    public static int executeSetCharacter(ServerCommandSource source, Text manaCharacter, int manaCharacterTypeIndex, int manaCharacterIndex) throws CommandSyntaxException {
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

        source.sendFeedback(() -> Text.literal("Updated " + (manaCharacterIndex == -1 ? "" : (" #" + manaCharacterIndex)) + (manaCharacterTypeIndex == -1 ? "" : (" " + manaCharacterTypeIndex + " point")) + " mana character for player " + player.getNameForScoreboard() + " to " + manaCharacter.getLiteralString() + "."), false);
        return 1;
    }

    public static int executeSetBossBarColor(ServerCommandSource source, BossBar.Color bossbarColor) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.getBossBarColor().equals(bossbarColor)) {
            throw OPTION_BOSSBAR_COLOR_UNCHANGED_EXCEPTION.create(bossbarColor);
        }

        manaPreference.setBossBarColor(bossbarColor);

        source.sendFeedback(() -> Text.literal("Updated mana bossbar color for player " + player.getNameForScoreboard() + " to " + bossbarColor.getName() + "."), false);
        return 1;
    }

    public static int executeSetBossBarStyle(ServerCommandSource source, BossBar.Style bossbarStyle) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.getBossBarStyle().equals(bossbarStyle)) {
            throw OPTION_BOSSBAR_STYLE_UNCHANGED_EXCEPTION.create(bossbarStyle);
        }

        manaPreference.setBossBarStyle(bossbarStyle);

        source.sendFeedback(() -> Text.literal("Updated mana bossbar style for player " + player.getNameForScoreboard() + " to " + bossbarStyle.getName() + "."), false);
        return 1;
    }

    public static int executeReset(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        manaPreference.setIsVisible(Pentamana.isVisible);
        manaPreference.setIsCompression(Pentamana.isCompression);
        manaPreference.setCompressionSize(Pentamana.compressionSize);
        manaPreference.setManabarType(Pentamana.manabarType);
        manaPreference.setPointsPerCharacter(Pentamana.pointsPerCharacter);
        manaPreference.setManaCharacters(new ArrayList<>(Pentamana.manaCharacters));

        source.sendFeedback(() -> Text.literal("Reset mana options for player " + player.getNameForScoreboard() + "."), false);
        return 0;
    }

    public static int executeResetVisibility(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        manaPreference.setIsVisible(Pentamana.isVisible);

        source.sendFeedback(() -> Text.literal("Reset mana visibility for player " + player.getNameForScoreboard() + "."), false);
        return 0;
    }

    public static int executeResetCompression(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        manaPreference.setIsCompression(Pentamana.isCompression);

        source.sendFeedback(() -> Text.literal("Reset mana compression for player " + player.getNameForScoreboard() + "."), false);
        return 0;
    }

    public static int executeResetCompressionSize(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        manaPreference.setCompressionSize(Pentamana.compressionSize);

        source.sendFeedback(() -> Text.literal("Reset mana compression size for player " + player.getNameForScoreboard() + "."), false);
        return 0;
    }

    public static int executeResetManaRenderType(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        manaPreference.setManabarType(Pentamana.manabarType);

        source.sendFeedback(() -> Text.literal("Reset mana render type for player " + player.getNameForScoreboard() + "."), false);
        return 0;
    }

    public static int executeResetManaRenderPosition(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        manaPreference.setManabarPosition(Pentamana.manabarPosition);

        source.sendFeedback(() -> Text.literal("Reset mana render position for player " + player.getNameForScoreboard() + "."), false);
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
        manaPreference.setManaCharacters(new ArrayList<>(Pentamana.manaCharacters));

        source.sendFeedback(() -> Text.literal("Reset mana characters for player " + player.getNameForScoreboard() + "."), false);
        return 0;
    }

    public static int executeResetBossBarColor(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        manaPreference.setBossBarColor(Pentamana.bossbarColor);

        source.sendFeedback(() -> Text.literal("Reset mana bossbar color for player " + player.getNameForScoreboard() + "."), false);
        return 0;
    }

    public static int executeResetBossBarStyle(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        manaPreference.setBossBarStyle(Pentamana.bossbarStyle);

        source.sendFeedback(() -> Text.literal("Reset mana bossbar style for player " + player.getNameForScoreboard() + "."), false);
        return 0;
    }

    public static int executeReload(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("Reloading Pentamana!"), true);
        return Pentamana.reload(source.getServer());
	}
}

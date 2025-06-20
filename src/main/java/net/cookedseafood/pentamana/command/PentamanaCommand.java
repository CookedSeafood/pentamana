package net.cookedseafood.pentamana.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.data.PentamanaConfig;
import net.cookedseafood.pentamana.data.PentamanaPreference;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PentamanaCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(
            CommandManager.literal(Pentamana.MOD_ID)
            .then(
                CommandManager.literal("debug")
                .then(
                    CommandManager.literal("preference")
                    .executes(context -> executeDebugPreference(context.getSource()))
                    .then(
                        CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> executeDebugPreference(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                    )
                )
            )
            .then(
                CommandManager.literal("version")
                .executes(context -> executeVersion(context.getSource()))
            )
            .then(
                CommandManager.literal("reload")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> executeReload(context.getSource()))
            )
        );
    }

    public static int executeDebugPreference(ServerCommandSource source) throws CommandSyntaxException {
        return executeDebugPreference(source, source.getPlayerOrThrow());
    }

    public static int executeDebugPreference(ServerCommandSource source, ServerPlayerEntity player) throws CommandSyntaxException {
        return executeDebugPreference(source, player, player.getPentamanaPreference());
    }

    public static int executeDebugPreference(ServerCommandSource source, ServerPlayerEntity player, PentamanaPreference preference) throws CommandSyntaxException {
        MutableText feedback = MutableText.of(PlainTextContent.EMPTY);
        feedback.append(Text.literal("\n" + player.getMana() + "/" + player.getManaCapacity()).formatted(Formatting.AQUA));
        feedback.append(Text.literal("\n- visibility "));
        feedback.append(preference.isVisible ? Text.literal("true").formatted(Formatting.GREEN) : Text.literal("false").formatted(Formatting.RED));
        feedback.append(Text.literal("\n- suppression "));
        feedback.append(preference.isSuppressed ? Text.literal("true").formatted(Formatting.GREEN) : Text.literal("false").formatted(Formatting.RED));
        feedback.append(Text.literal("\n- position "));
        feedback.append(Text.literal(preference.position.getName()).formatted(Formatting.YELLOW));
        feedback.append(Text.literal("\n- pattern "));
        feedback.append(Text.literal(preference.pattern.toText().toString()).formatted(Formatting.YELLOW));
        feedback.append(Text.literal("\n- renderType "));
        feedback.append(Text.literal(preference.type.getName()).formatted(Formatting.YELLOW));
        feedback.append(Text.literal("\n- charset "));
        for (int i = 0; i < 10; i++) {
            feedback.append(preference.charset.get(i).get(i));
        };
        feedback.append(Text.literal("\n- pointsPerCharacter "));
        feedback.append(Text.literal("" + preference.pointsPerCharacter).formatted(Formatting.GREEN));
        feedback.append(Text.literal("\n- compression "));
        feedback.append(preference.isCompressed ? Text.literal("true").formatted(Formatting.GREEN) : Text.literal("false").formatted(Formatting.RED));
        feedback.append(Text.literal("\n- compressionSize "));
        feedback.append(Text.literal("" + preference.compressionSize).formatted(Formatting.GREEN));
        feedback.append(Text.literal("\n- color "));
        feedback.append(Text.literal("" + preference.color.getName()).formatted(Formatting.YELLOW));
        feedback.append(Text.literal("\n- style "));
        feedback.append(Text.literal("" + preference.style.getName()).formatted(Formatting.YELLOW));

        source.sendFeedback(() -> feedback, false);
        return 0;
    }

    public static int executeVersion(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("Pentamana " + Pentamana.VERSION_MAJOR + "." + Pentamana.VERSION_MINOR + "." + Pentamana.VERSION_PATCH), false);
        return 0;
    }

    public static int executeReload(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("Reloading Pentamana!"), true);
        return PentamanaConfig.reload(source.getServer());
    }
}

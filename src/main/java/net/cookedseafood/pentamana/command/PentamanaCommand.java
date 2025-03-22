package net.cookedseafood.pentamana.command;

import java.util.UUID;
import java.util.stream.IntStream;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.component.ManaPreferenceComponentImpl;
import net.cookedseafood.pentamana.component.ManaStatusEffectManagerComponentImpl;
import net.cookedseafood.pentamana.component.ServerManaBarComponentImpl;
import net.cookedseafood.pentamana.mana.ManaCharset;
import net.cookedseafood.pentamana.mana.ManaPattern;
import net.cookedseafood.pentamana.mana.ManaRender;
import net.cookedseafood.pentamana.mana.ManaStatusEffectManager;
import net.cookedseafood.pentamana.mana.ManaTextual;
import net.cookedseafood.pentamana.mana.ServerManaBar;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PentamanaCommand {
    public PentamanaCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(
            CommandManager.literal(Pentamana.MOD_ID)
            .then(
                CommandManager.literal("debug")
                .then(
                    CommandManager.literal("profile")
                    .executes(context -> executeDebugProfile((ServerCommandSource)context.getSource()))
                )
            )
            .then(
                CommandManager.literal("version")
                .executes(context -> executeVersion((ServerCommandSource)context.getSource()))
            )
        );
    }

    public static int executeDebugProfile(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreferenceComponentImpl manaPreference = ManaPreferenceComponentImpl.MANA_PREFERENCE.get(player);
        ManaStatusEffectManager statusEffectManager = ManaStatusEffectManagerComponentImpl.MANA_STATUS_EFFECT.get(player).getStatusEffectManager();
        ServerManaBar serverManaBar = ServerManaBarComponentImpl.SERVER_MANA_BAR.get(player).getServerManaBar();
        ManaTextual textual = serverManaBar.getTextual();
        ManaPattern pattern = textual.getPattern();
        ManaRender render = textual.getRender();
        ManaCharset charset = render.getCharset();
        UUID uuid = serverManaBar.getUuid();
        ServerPlayerEntity playerInServerManaBar = serverManaBar.getPlayer();

        MutableText profile = MutableText.of(PlainTextContent.EMPTY);
        IntStream.range(0, 10).forEach(i -> profile.append(charset.get(i).get(i)));
        profile.append(Text.literal("\n" + serverManaBar.getSupply() + "/" + serverManaBar.getCapacity()).formatted(Formatting.AQUA));
        profile.append(Text.literal(" " + serverManaBar.getLife()).formatted(Formatting.GRAY));
        statusEffectManager.keySet().stream()
            .filter(id -> statusEffectManager.has(id))
            .forEach(id -> {
                int amplifier = statusEffectManager.getActiveStatusEffectAmplifier(id);
                profile.append(Text.literal("\n" + id).formatted(Formatting.GRAY));
                profile.append(Text.literal(" " + amplifier));
                profile.append(Text.literal(" " + statusEffectManager.getDuration(id, amplifier)));
            });
        profile.append(manaPreference.isEnabled() ? Text.literal("\nT").formatted(Formatting.GREEN) : Text.literal("\nF").formatted(Formatting.RED));
        profile.append(serverManaBar.isVisible() ? Text.literal(" T").formatted(Formatting.GREEN) : Text.literal(" F").formatted(Formatting.RED));
        profile.append(render.isCompression() ? Text.literal(" T").formatted(Formatting.GREEN) : Text.literal(" F").formatted(Formatting.RED));
        profile.append(Text.literal(" " + render.getCompressionSize()).formatted(Formatting.YELLOW));
        profile.append(Text.literal(" " + render.getPointsPerCharacter()).formatted(Formatting.AQUA));
        profile.append(Text.literal(" " + serverManaBar.getPosition().getName()).formatted(Formatting.YELLOW));
        profile.append(Text.literal(" " + render.getType().getName()).formatted(Formatting.YELLOW));
        profile.append(Text.literal("\n" + pattern.toText().toString()));
        profile.append(uuid != null ? Text.literal("\n" + uuid.toString()).formatted(Formatting.DARK_GRAY) : Text.literal("\nNull").formatted(Formatting.RED));
        profile.append(playerInServerManaBar != null ? Text.literal("\n" + playerInServerManaBar.toString()).formatted(Formatting.DARK_GRAY) : Text.literal("\nNull").formatted(Formatting.RED));

        source.sendFeedback(() -> profile, false);
        return 0;
    }

    public static int executeVersion(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("Pentamana " + Pentamana.VERSION_MAJOR + "." + Pentamana.VERSION_MINOR + "." + Pentamana.VERSION_PATCH + (Pentamana.isForceEnabled ? " (Force Enabled Mode)" : "")), false);
        return 0;
    }
}

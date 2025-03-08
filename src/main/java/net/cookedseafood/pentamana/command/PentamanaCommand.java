package net.cookedseafood.pentamana.command;

import java.util.List;
import java.util.stream.IntStream;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.component.ManaDisplay;
import net.cookedseafood.pentamana.component.ManaPreference;
import net.cookedseafood.pentamana.component.ManaStatus;
import net.cookedseafood.pentamana.component.ManaStatusEffect;
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
            CommandManager.literal("pentamana")
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
        ManaStatus manaStatus = ManaStatus.MANA_STATUS.get(player);
        ManaDisplay manaDisplay = ManaDisplay.MANA_DISPLAY.get(player);
        ManaStatusEffect manaStatusEffect = ManaStatusEffect.MANA_STATUS_EFFECT.get(player);
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        boolean enabled = manaPreference.getEnabled();
        boolean display = manaPreference.getVisibility();
        List<List<Text>> manaCharacters = manaPreference.getManaCharacters();

        MutableText profile = MutableText.of(PlainTextContent.EMPTY);
        IntStream.range(0, 10).forEach(i -> profile.append(manaCharacters.get(i).get(i)));
        profile.append(Text.literal("\n" + manaStatus.getManaSupply() + "/" + manaStatus.getManaCapacity()).formatted(Formatting.AQUA));
        profile.append(Text.literal(" " + manaDisplay.getManaSupplyPoint() + "/" + manaDisplay.getManaCapacityPoint()).formatted(Formatting.YELLOW));
        profile.append(Text.literal(" " + manaDisplay.getManaSupplyPercent() + "%").formatted(Formatting.YELLOW));
        profile.append(Text.literal(" " + manaDisplay.getManabarLife()).formatted(Formatting.GRAY));
        manaStatusEffect.getStatusEffects().keySet().stream()
            .filter(id -> manaStatusEffect.hasStatusEffect(id))
            .forEach(id -> {
                int amplifier = manaStatusEffect.getActiveStatusEffectAmplifier(id);
                profile.append(Text.literal("\n" + id).formatted(Formatting.GRAY));
                profile.append(Text.literal(" " + amplifier));
                profile.append(Text.literal(" " + manaStatusEffect.getStatusEffectDuration(id, amplifier)));
            });
        profile.append(Text.literal("\n" + (enabled ? "T" : "F")).formatted(enabled ? Formatting.GREEN : Formatting.RED));
        profile.append(Text.literal(" " + (display ? "T" : "F")).formatted(display ? Formatting.GREEN : Formatting.RED));
        profile.append(Text.literal(" " + manaPreference.getPointsPerCharacter()).formatted(Formatting.AQUA));
        profile.append(Text.literal(" " + Pentamana.ManaRenderType.getName((byte)manaPreference.getManaRenderType())).formatted(Formatting.YELLOW));
        profile.append(Text.literal(" " + manaPreference.getManaFixedSize()).formatted(Formatting.YELLOW));

        source.sendFeedback(() -> profile, false);
        return 0;
    }

    public static int executeVersion(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("Pentamana " + Pentamana.VERSION_MAJOR + "." + Pentamana.VERSION_MINOR + "." + Pentamana.VERSION_PATCH + (Pentamana.isForceEnabled ? " (Force Enabled Mode)" : "")), false);
        return 0;
    }
}

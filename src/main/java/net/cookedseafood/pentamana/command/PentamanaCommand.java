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
import net.cookedseafood.pentamana.render.ManabarPositions;
import net.cookedseafood.pentamana.render.ManabarTypes;
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
        ManaStatus manaStatus = ManaStatus.MANA_STATUS.get(player);
        ManaDisplay manaDisplay = ManaDisplay.MANA_DISPLAY.get(player);
        ManaStatusEffect manaStatusEffect = ManaStatusEffect.MANA_STATUS_EFFECT.get(player);
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        List<List<Text>> manaCharacter = manaPreference.getManaCharacter();

        MutableText profile = MutableText.of(PlainTextContent.EMPTY);
        IntStream.range(0, 10).forEach(i -> profile.append(manaCharacter.get(i).get(i)));
        profile.append(Text.literal("\n" + manaStatus.getManaSupply() + "/" + manaStatus.getManaCapacity()).formatted(Formatting.AQUA));
        profile.append(Text.literal(" " + manaDisplay.getLastManaSupplyPoint() + "/" + manaDisplay.getLastManaCapacityPoint()).formatted(Formatting.YELLOW));
        profile.append(Text.literal(" " + manaDisplay.getManabarLife()).formatted(Formatting.GRAY));
        manaStatusEffect.getStatusEffects().keySet().stream()
            .filter(id -> manaStatusEffect.hasStatusEffect(id))
            .forEach(id -> {
                int amplifier = manaStatusEffect.getActiveStatusEffectAmplifier(id);
                profile.append(Text.literal("\n" + id).formatted(Formatting.GRAY));
                profile.append(Text.literal(" " + amplifier));
                profile.append(Text.literal(" " + manaStatusEffect.getStatusEffectDuration(id, amplifier)));
            });
        profile.append(manaPreference.isEnabled() ? Text.literal("\nT").formatted(Formatting.GREEN) : Text.literal("\nF").formatted(Formatting.RED));
        profile.append(manaPreference.isVisible() ? Text.literal(" T").formatted(Formatting.GREEN) : Text.literal(" F").formatted(Formatting.RED));
        profile.append(manaPreference.isCompression() ? Text.literal(" T").formatted(Formatting.GREEN) : Text.literal(" F").formatted(Formatting.RED));
        profile.append(Text.literal(" " + manaPreference.getCompressionSize()).formatted(Formatting.YELLOW));
        profile.append(Text.literal(" " + manaPreference.getPointsPerCharacter()).formatted(Formatting.AQUA));
        profile.append(Text.literal(" " + manaPreference.getManabarPattern().toString()));
        profile.append(Text.literal(" " + ManabarTypes.getName((byte)manaPreference.getManabarType())).formatted(Formatting.YELLOW));
        profile.append(Text.literal(" " + ManabarPositions.getName((byte)manaPreference.getManabarPosition())).formatted(Formatting.YELLOW));

        source.sendFeedback(() -> profile, false);
        return 0;
    }

    public static int executeVersion(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("Pentamana " + Pentamana.VERSION_MAJOR + "." + Pentamana.VERSION_MINOR + "." + Pentamana.VERSION_PATCH + (Pentamana.isForceEnabled ? " (Force Enabled Mode)" : "")), false);
        return 0;
    }
}

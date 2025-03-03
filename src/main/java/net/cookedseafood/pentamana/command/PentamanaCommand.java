package net.cookedseafood.pentamana.command;

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
import net.minecraft.text.Style;
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
        boolean display = manaPreference.getDisplay();

        MutableText profile = MutableText.of(PlainTextContent.EMPTY);
        profile.append(Text.literal(manaStatus.getManaSupply() + "/" + manaStatus.getManaCapacity()).formatted(Formatting.AQUA));
        profile.append(Text.literal(" " + manaDisplay.getManaSupplyPoint() + "/" + manaDisplay.getManaCapacityPoint()).formatted(Formatting.YELLOW));
        profile.append(Text.literal(" " + manaDisplay.getManaSupplyPercent() + "%").formatted(Formatting.YELLOW));
        profile.append(Text.literal(" " + manaDisplay.getManabarLife()).formatted(Formatting.GRAY));
        manaStatusEffect.getStatusEffect().keySet().stream()
            .filter(id -> manaStatusEffect.hasStatusEffect(id))
            .forEach(id -> {
                int amplifier = manaStatusEffect.getActiveStatusEffectAmplifier(id);
                profile.append(Text.literal("\n" + id).formatted(Formatting.GOLD));
                profile.append(Text.literal(" " + amplifier).formatted(Formatting.AQUA));
                profile.append(Text.literal(" " + manaStatusEffect.getStatusEffectDuration(id, amplifier)).formatted(Formatting.YELLOW));
            });
        profile.append(Text.literal("\nEnabled"));
        profile.append(Text.literal(" " + enabled).setStyle(Style.EMPTY.withColor(enabled ? Formatting.GREEN : Formatting.RED)));
        profile.append(Text.literal("\nDisplay"));
        profile.append(Text.literal(" " + display).setStyle(Style.EMPTY.withColor(display ? Formatting.GREEN : Formatting.RED)));
        profile.append(Text.literal("\nRenderType"));
        profile.append(Text.literal(" " + Pentamana.ManaRenderType.getName((byte)manaPreference.getManaRenderType())).setStyle(Style.EMPTY.withColor(Formatting.AQUA)));
        profile.append(Text.literal("\nPointsPerCharacter"));
        profile.append(Text.literal(" " + manaPreference.getPointsPerCharacter()).setStyle(Style.EMPTY.withColor(Formatting.AQUA)));

        source.sendFeedback(() -> profile, false);
        return 0;
    }

    public static int executeVersion(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("Pentamana " + Pentamana.VERSION_MAJOR + "." + Pentamana.VERSION_MINOR + "." + Pentamana.VERSION_PATCH + (Pentamana.forceManaEnabled ? " (Force Enabled Mode)" : "")), false);
        return 0;
    }
}

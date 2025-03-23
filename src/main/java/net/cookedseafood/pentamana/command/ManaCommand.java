package net.cookedseafood.pentamana.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.component.ManaPreferenceComponentImpl;
import net.cookedseafood.pentamana.component.ServerManaBarComponentImpl;
import net.cookedseafood.pentamana.mana.ServerManaBar;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ManaCommand {
    private static final SimpleCommandExceptionType OPTION_ALREADY_ENABLED_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Nothing changed. Mana is already enabled for that player."));
    private static final SimpleCommandExceptionType OPTION_ALREADY_DISABLED_EXCEPTION =
        new SimpleCommandExceptionType(Text.literal("Nothing changed. Mana is already disbaled for that player."));

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
                CommandManager.literal("get")
                .requires(source -> source.hasPermissionLevel(2))
                .then(
                    CommandManager.argument("amount", FloatArgumentType.floatArg())
                    .executes(context -> executeSet(context.getSource(), FloatArgumentType.getFloat(context, "amount")))
                )
            )
            .then(
                CommandManager.literal("set")
                .requires(source -> source.hasPermissionLevel(2))
                .then(
                    CommandManager.argument("amount", FloatArgumentType.floatArg())
                    .executes(context -> executeSet(context.getSource(), FloatArgumentType.getFloat(context, "amount")))
                )
            )
            .then(
                CommandManager.literal("add")
                .requires(source -> source.hasPermissionLevel(2))
                .then(
                    CommandManager.argument("amount", FloatArgumentType.floatArg())
                    .executes(context -> executeAdd(context.getSource(), FloatArgumentType.getFloat(context, "amount")))
                )
            )
            .then(
                CommandManager.literal("subtract")
                .requires(source -> source.hasPermissionLevel(2))
                .then(
                    CommandManager.argument("amount", FloatArgumentType.floatArg())
                    .executes(context -> executeSubtract(context.getSource(), FloatArgumentType.getFloat(context, "amount")))
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
        ManaPreferenceComponentImpl manaPreference = ManaPreferenceComponentImpl.MANA_PREFERENCE.get(player);
        if (manaPreference.isEnabled()) {
            throw OPTION_ALREADY_ENABLED_EXCEPTION.create();
        }

        manaPreference.setIsEnabled(true);

        source.sendFeedback(() -> Text.literal("Enabled mana for player " + player.getNameForScoreboard() + "."), false);
        return 1;
    }

    public static int executeDisable(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreferenceComponentImpl manaPreference = ManaPreferenceComponentImpl.MANA_PREFERENCE.get(player);
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

    public static int executeGet(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ServerManaBar serverManaBar = ServerManaBarComponentImpl.SERVER_MANA_BAR.get(player).getServerManaBar();
        float supply = serverManaBar.getSupply();
        source.sendFeedback(() -> Text.literal(player.getNameForScoreboard() + " has " + supply + " mana."), false);
        return (int)(supply / Pentamana.manaPerPoint);
    }

    public static int executeSet(ServerCommandSource source, float amount) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ServerManaBar serverManaBar = ServerManaBarComponentImpl.SERVER_MANA_BAR.get(player).getServerManaBar();
        serverManaBar.setSupply(amount);
        source.sendFeedback(() -> Text.literal("Set mana for player " + player.getNameForScoreboard() + " to " + amount + "."), false);
        return (int)(amount / Pentamana.manaPerPoint);
    }

    public static int executeAdd(ServerCommandSource source, float amount) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ServerManaBar serverManaBar = ServerManaBarComponentImpl.SERVER_MANA_BAR.get(player).getServerManaBar();
        float targetSupply = serverManaBar.getSupply() + amount;
        serverManaBar.setSupply(targetSupply);
        source.sendFeedback(() -> Text.literal("Added " + amount + " mana for player " + player.getNameForScoreboard() + "."), false);
        return (int)(targetSupply / Pentamana.manaPerPoint);
    }

    public static int executeSubtract(ServerCommandSource source, float amount) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ServerManaBar serverManaBar = ServerManaBarComponentImpl.SERVER_MANA_BAR.get(player).getServerManaBar();
        float targetSupply = serverManaBar.getSupply() - amount;
        serverManaBar.setSupply(targetSupply);
        source.sendFeedback(() -> Text.literal("Subtracted " + amount + " mana for player " + player.getNameForScoreboard() + "."), false);
        return (int)(targetSupply / Pentamana.manaPerPoint);
    }

    public static int executeReload(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("Reloading Pentamana!"), true);
        return Pentamana.reload(source.getServer());
	}
}

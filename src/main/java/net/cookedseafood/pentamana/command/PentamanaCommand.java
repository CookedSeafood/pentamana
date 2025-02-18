package net.cookedseafood.pentamana.command;

import com.mojang.brigadier.CommandDispatcher;
import net.cookedseafood.pentamana.Pentamana;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class PentamanaCommand {
    public PentamanaCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(
            CommandManager.literal("pentamana")
            .then(
                CommandManager.literal("version")
                .executes(context -> executeVersion((ServerCommandSource)context.getSource()))
            )
        );
    }

    public static int executeVersion(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("Pentamana " + Pentamana.VERSION_MAJOR + "." + Pentamana.VERSION_MINOR + "." + Pentamana.VERSION_PATCH + (Pentamana.forceEnabled ? " (Force Enabled Mode)" : "")), false);
        return 0;
    }
}

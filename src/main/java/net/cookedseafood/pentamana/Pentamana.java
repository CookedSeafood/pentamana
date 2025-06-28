package net.cookedseafood.pentamana;

import net.cookedseafood.pentamana.command.ManaBarCommand;
import net.cookedseafood.pentamana.command.ManaCommand;
import net.cookedseafood.pentamana.command.PentamanaCommand;
import net.cookedseafood.pentamana.data.PentamanaConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pentamana implements ModInitializer {
    public static final String MOD_ID = "pentamana";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final byte VERSION_MAJOR = 0;
    public static final byte VERSION_MINOR = 8;
    public static final byte VERSION_PATCH = 2;

    public static final String MOD_NAMESPACE = "pentamana";
    public static final byte MANA_CHARACTER_TYPE_INDEX_LIMIT = Byte.MAX_VALUE;
    public static final byte MANA_CHARACTER_INDEX_LIMIT = Byte.MAX_VALUE;
    public static final Text MANA_PATTERN_MATCHER = Text.of("$");

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> PentamanaCommand.register(dispatcher, registryAccess));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ManaCommand.register(dispatcher, registryAccess));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ManaBarCommand.register(dispatcher, registryAccess));

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            PentamanaConfig.reload(server);
        });
    }
}

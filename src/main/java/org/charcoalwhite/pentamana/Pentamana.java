package org.charcoalwhite.pentamana;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.util.Formatting;
import org.charcoalwhite.pentamana.command.ManaCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pentamana implements ModInitializer {
	public static final String MOD_ID = "pentamana";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static int manaScale;
	public static int manaCapacityBase;
	public static int manaCapacityIncrementBase;
	public static int manaRegenBase;
	public static int manaRegenIncrementBase;
	public static int maxManabarLife;
	public static char manaCharFull;
	public static char manaCharHalf;
	public static char manaCharZero;
	public static Formatting manaColor;
	public static boolean forceEnabled;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ManaCommand.register(dispatcher, registryAccess));

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			LOGGER.info("[Pentamana] Loaded " + ManaCommand.executeReload(server.getCommandSource()) + " changes from config.");
		});
	}
}

package net.cookedseafood.pentamana;

import net.cookedseafood.pentamana.command.ManaCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pentamana implements ModInitializer {
	public static final String MOD_ID = "pentamana";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static int manaScale = 16777216/* 2^24 */;
	public static int manaCapacityBase = 33554431/* 2^24*2-1 */;
	public static int manaCapacityIncrementBase = 33554432/* 2^24*2 */;
	public static int manaRegenBase = 1048576/* 2^20 */;
	public static int manaRegenIncrementBase = 65536/* 2^16 */;
	public static int maxManabarLife = 40/* 20*2 */;
	public static char manaCharFull = '\u2605';
	public static char manaCharHalf = '\u2bea';
	public static char manaCharZero = '\u2606';
	public static Formatting manaColor = Formatting.AQUA;
	public static boolean forceEnabled = false;
	public static final byte versionMajor = 0;
	public static final byte versionMinor = 2;
	public static final byte versionPatch = 9;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ManaCommand.register(dispatcher, registryAccess));

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			ManaCommand.executeReload(server.getCommandSource());
		});
	}
}

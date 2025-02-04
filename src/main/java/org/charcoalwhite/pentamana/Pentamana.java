package org.charcoalwhite.pentamana;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.util.Formatting;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pentamana implements ModInitializer {
	public static final String MOD_ID = "pentamana";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// It is 2^24.
	public static int manaScale = 16777216;
	
	// It is 2^24*2-1.
	public static int manaCapacityBase = 33554431;

	// It is 2^24*2.
	public static int manaCapacityIncrementBase = 33554432;

	// It is 2^20.
	public static int manaRegenBase = 1048576;

	// It is 2^16
	public static int manaRegenIncrementBase = 65536;
	public static int maxManabarLife = 40;
	public static char manaCharFull = '\u2605';
	public static char manaCharHalf = '\u2bea';
	public static char manaCharZero = '\u2606';
	public static Formatting manaColor = Formatting.AQUA;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("[Pentamana] Mana can be everything.");

		LOGGER.info(new File("./").getAbsolutePath());

		loadConfig();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ManaCommand.register(dispatcher, registryAccess));
	}
	
	public void loadConfig() {
		String configString = null;
		try {
			configString = FileUtils.readFileToString(new File("./config/pentamana.json"), StandardCharsets.UTF_8);
		} catch (IOException e) {
			return;
		}

		JsonObject configObject  = new Gson().fromJson(configString, JsonObject.class);
		if (configObject.has("manaScale")) {
			Pentamana.manaScale = configObject.get("manaScale").getAsInt();
		}
		
		if (configObject.has("manaCapacityBase")) {
			Pentamana.manaCapacityBase = configObject.get("manaCapacityBase").getAsInt();
		}

		if (configObject.has("manaCapacityIncrementBase")) {
			Pentamana.manaCapacityIncrementBase = configObject.get("manaCapacityIncrementBase").getAsInt();
		}

		if (configObject.has("manaRegenBase")) {
			Pentamana.manaRegenBase = configObject.get("manaRegenBase").getAsInt();
		}

		if (configObject.has("manaRegenIncrementBase")) {
			Pentamana.manaRegenIncrementBase = configObject.get("manaRegenIncrementBase").getAsInt();
		}

		if (configObject.has("maxManabarLife")) {
			Pentamana.maxManabarLife = configObject.get("maxManabarLife").getAsInt();
		}

		if (configObject.has("manaCharFull")) {
			Pentamana.manaCharFull = configObject.get("manaCharFull").getAsString().charAt(0);
		}

		if (configObject.has("manaCharHalf")) {
			Pentamana.manaCharHalf = configObject.get("manaCharHalf").getAsString().charAt(0);
		}

		if (configObject.has("manaCharZero")) {
			Pentamana.manaCharZero = configObject.get("manaCharZero").getAsString().charAt(0);
		}

		if (configObject.has("manaColor")) {
			Pentamana.manaColor = Formatting.byName(configObject.get("manaColor").getAsString());
		}
	}
}

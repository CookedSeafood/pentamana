package net.cookedseafood.pentamana;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import net.cookedseafood.pentamana.command.ManaCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
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

	public static final byte VERSION_MAJOR = 0;
	public static final byte VERSION_MINOR = 2;
	public static final byte VERSION_PATCH = 17;

	public static final int MANA_SCALE = 16777216/* 2^24 */;
	public static final int MANA_CAPACITY_BASE = 33554431/* 2^24*2-1 */;
	public static final int MANA_CAPACITY_INCREMENT_BASE = 33554432/* 2^24*2 */;
	public static final int MANA_REGEN_BASE = 1048576/* 2^20 */;
	public static final int MANA_REGEN_INCREMENT_BASE = 65536/* 2^16 */;
	public static final int MAX_MANABAR_LIFE = 40/* 20*2 */;
	public static final char[] MANA_CHAR_FULL = {'\u2605'};
	public static final char[] MANA_CHAR_HALF = {'\u2bea'};
	public static final char[] MANA_CHAR_ZERO = {'\u2606'};
	public static final Formatting MANA_COLOR_FULL = Formatting.AQUA;
	public static final Formatting MANA_COLOR_HALF = Formatting.AQUA;
	public static final Formatting MANA_COLOR_ZERO = Formatting.BLACK;
	public static final boolean FORCE_ENABLED = false;

	public static int manaScale;
	public static int manaCapacityBase;
	public static int manaCapacityIncrementBase;
	public static int manaRegenBase;
	public static int manaRegenIncrementBase;
	public static int maxManabarLife;
	public static char[] manaCharFull;
	public static char[] manaCharHalf;
	public static char[] manaCharZero;
	public static Formatting manaColorFull;
	public static Formatting manaColorHalf;
	public static Formatting manaColorZero;
	public static boolean forceEnabled;

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

	public static int reload() {
		String configString;
		try {
			configString = FileUtils.readFileToString(new File("./config/pentamana.json"), StandardCharsets.UTF_8);
		} catch (IOException e) {
            reset();
			return 1;
		}

		JsonObject config = new Gson().fromJson(configString, JsonObject.class);

		manaScale =
            config.has("manaScale") ? 
            config.get("manaScale").getAsInt() :
            MANA_SCALE;
        manaCapacityBase =
            config.has("manaCapacityBase") ?
            config.get("manaCapacityBase").getAsInt() :
            MANA_CAPACITY_BASE;
        manaCapacityIncrementBase =
            config.has("manaCapacityIncrementBase") ?
            config.get("manaCapacityIncrementBase").getAsInt() :
            MANA_CAPACITY_INCREMENT_BASE;
        manaRegenBase =
            config.has("manaRegenBase") ?
            config.get("manaRegenBase").getAsInt() :
            MANA_REGEN_BASE;
        manaRegenIncrementBase =
            config.has("manaRegenIncrementBase") ?
            config.get("manaRegenIncrementBase").getAsInt() :
            MANA_REGEN_INCREMENT_BASE;
        maxManabarLife =
            config.has("maxManabarLife") ?
            config.get("maxManabarLife").getAsInt() :
            MAX_MANABAR_LIFE;
        manaCharFull =
            config.has("manaCharFull") ?
            Character.toChars(config.get("manaCharFull").getAsString().codePointAt(0)) :
            MANA_CHAR_FULL;
        manaCharHalf =
            config.has("manaCharHalf") ?
            Character.toChars(config.get("manaCharHalf").getAsString().codePointAt(0)) :
            MANA_CHAR_HALF;
        manaCharZero =
            config.has("manaCharZero") ?
            Character.toChars(config.get("manaCharZero").getAsString().codePointAt(0)) :
            MANA_CHAR_ZERO;
        manaColorFull =
            config.has("manaColorFull") ?
            Formatting.byName(config.get("manaColorFull").getAsString()) :
            MANA_COLOR_FULL;
        manaColorHalf =
            config.has("manaColorHalf") ?
            Formatting.byName(config.get("manaColorHalf").getAsString()) :
            MANA_COLOR_HALF;
        manaColorZero =
            config.has("manaColorZero") ?
            Formatting.byName(config.get("manaColorZero").getAsString()) :
            MANA_COLOR_ZERO;
        forceEnabled =
            config.has("forceEnabled") ?
            config.get("forceEnabled").getAsBoolean() :
            FORCE_ENABLED;
		return 2;
	}

	public static void reset() {
        manaScale = MANA_SCALE;
        manaCapacityBase = MANA_CAPACITY_BASE;
        manaCapacityIncrementBase = MANA_CAPACITY_INCREMENT_BASE;
        manaRegenBase = MANA_REGEN_BASE;
        manaRegenIncrementBase = MANA_REGEN_INCREMENT_BASE;
        maxManabarLife = MAX_MANABAR_LIFE;
        manaCharFull = MANA_CHAR_FULL;
        manaCharHalf = MANA_CHAR_HALF;
        manaCharZero = MANA_CHAR_ZERO;
        manaColorFull = MANA_COLOR_FULL;
        manaColorHalf = MANA_COLOR_HALF;
        manaColorZero = MANA_COLOR_ZERO;
        forceEnabled = FORCE_ENABLED;
	}
}

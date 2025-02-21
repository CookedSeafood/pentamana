package net.cookedseafood.pentamana;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.cookedseafood.pentamana.command.ManaCommand;
import net.cookedseafood.pentamana.command.PentamanaCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.text.TextColor;
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
	public static final byte VERSION_MINOR = 3;
	public static final byte VERSION_PATCH = 10;

	public static final int MANA_PER_POINT = 0x1_0000/* 2^16 */;
	public static final int MANA_CAPACITY_BASE = 0x1_ffff/* 2^16*2-1 */;
	public static final int MANA_REGEN_BASE = 0x1000/* 2^12 */;
    public static final int ENCHANTMENT_CAPACITY_BASE = 0x2_0000/* 2^16*2 */;
	public static final int ENCHANTMENT_STREAM_BASE = 0x100/* 2^8 */;
    public static final int ENCHANTMENT_UTILIZATION_BASE = 0xccc_cccc/* (2^31-1)/10 */;
    public static final int ENCHANTMENT_POTENCY_BASE = 0x3fff_ffff/* 2^30 */;
    public static final int STATUS_EFFECT_MANA_BOOST_BASE = 0x4_0000;/* 2^16*4 */
    public static final int STATUS_EFFECT_MANA_REDUCTION_BASE = 0x4_0000;/* 2^16*4 */
    public static final int STATUS_EFFECT_INSTANT_MANA_BASE = 0x4_000;/* 2^16*4 */
    public static final int STATUS_EFFECT_INSTANT_DEPLETE_BASE = 0x6_000;/* 2^16*6 */
    public static final int STATUS_EFFECT_MANA_REGEN_BASE = 0x32/* 50 */;
    public static final int STATUS_EFFECT_MANA_INHIBITION_BASE = 0x28/* 40 */;
    public static final int STATUS_EFFECT_MANA_POWER_BASE = 0x3;
    public static final int STATUS_EFFECT_MANA_SICKNESS_BASE = 0x4;
    public static final int DISPLAY_IDLE_INTERVAL = 40/* 20*2 */;
	public static final int DISPLAY_SUPPRESSION_INTERVAL = 40/* 20*2 */;
    public static final boolean FORCE_ENABLED = false;
    public static final boolean ENABLED = true;
    public static final boolean DISPLAY = true;
    public static final int RENDER_TYPE = 1;
    public static final int MAX_MANA_CHAR_INDEX_FOR_DISPLAY = 127;
    public static final int FIXED_SIZE = 20;
    public static final List<Integer> MANA_CHARS = Stream.of(0x2605, 0x2bea, 0x2606).collect(Collectors.toUnmodifiableList());
    public static final List<TextColor> MANA_COLORS = Stream.of(TextColor.fromRgb(0x55ffff), TextColor.fromRgb(0x55ffff), TextColor.fromRgb(0x0)).collect(Collectors.toUnmodifiableList());
    public static final List<Boolean> MANA_BOLDS = Stream.of(false, false, false).collect(Collectors.toUnmodifiableList());
    public static final List<Boolean> MANA_ITALICS = Stream.of(false, false, false).collect(Collectors.toUnmodifiableList());
    public static final List<Boolean> MANA_UNDERLINEDS = Stream.of(false, false, false).collect(Collectors.toUnmodifiableList());
    public static final List<Boolean> MANA_STRIKETHROUGHS = Stream.of(false, false, false).collect(Collectors.toUnmodifiableList());
    public static final List<Boolean> MANA_OBFUSCATEDS = Stream.of(false, false, false).collect(Collectors.toUnmodifiableList());

    public static final int RENDER_TYPE_FLEX_SIZE_INDEX = 1;
    public static final int RENDER_TYPE_FIXED_SIZE_INDEX = 2;
    public static final int RENDER_TYPE_NUMBERIC_INDEX = 3;

	public static int manaPerPoint;
	public static int manaCapacityBase;
    public static int manaRegenBase;
	public static int enchantmentCapacityBase;
    public static int enchantmentStreamBase;
    public static int enchantmentUtilizationBase;
    public static int enchantmentPotencyBase;
    public static int statusEffectManaBoostBase;
    public static int statusEffectManaReductionBase;
    public static int statusEffectInstantManaBase;
    public static int statusEffectInstantDepleteBase;
    public static int statusEffectManaRegenBase;
    public static int statusEffectManaInhibitionBase;
    public static int statusEffectManaPowerBase;
    public static int statusEffectManaSicknessBase;
    public static int displayIdleInterval;
	public static int displaySuppressionInterval;
	public static boolean forceEnabled;
    public static boolean enabled;
    public static boolean display;
    public static int renderType;
    public static int maxManaCharIndexForDisplay;
    public static int fixedSize;
	public static List<Integer> manaChars;
    public static List<TextColor> manaColors;
    public static List<Boolean> manaBolds;
    public static List<Boolean> manaItalics;
    public static List<Boolean> manaUnderlineds;
    public static List<Boolean> manaStrikethroughs;
    public static List<Boolean> manaObfuscateds;

    public static int manaCharTypes;
    public static int maxManaCharTypeIndex;
    public static int pointsPerChar;
    public static int maxManaPointIndex;
    public static int maxManaCharIndex;
    public static int maxFlexSize;
    public static int maxManaCapacityPointTrimmed;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> PentamanaCommand.register(dispatcher, registryAccess));
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
            reCalc();
			return 1;
		}

		JsonObject config = new Gson().fromJson(configString, JsonObject.class);

		manaPerPoint =
            config.has("manaPerPoint") ? 
            config.get("manaPerPoint").getAsInt() :
            MANA_PER_POINT;
        manaCapacityBase =
            config.has("manaCapacityBase") ?
            config.get("manaCapacityBase").getAsInt() :
            MANA_CAPACITY_BASE;
        manaRegenBase =
            config.has("manaRegenBase") ?
            config.get("manaRegenBase").getAsInt() :
            MANA_REGEN_BASE;
        enchantmentCapacityBase =
            config.has("enchantmentCapacityBase") ?
            config.get("enchantmentCapacityBase").getAsInt() :
            ENCHANTMENT_CAPACITY_BASE;
        enchantmentStreamBase =
            config.has("enchantmentStreamBase") ?
            config.get("enchantmentStreamBase").getAsInt() :
            ENCHANTMENT_STREAM_BASE;
        enchantmentUtilizationBase =
            config.has("enchantmentUtilizationBase") ?
            config.get("enchantmentUtilizationBase").getAsInt() :
            ENCHANTMENT_UTILIZATION_BASE;
        enchantmentPotencyBase =
            config.has("enchantmentPotencyBase") ?
            config.get("enchantmentPotencyBase").getAsInt() :
            ENCHANTMENT_POTENCY_BASE;
        statusEffectManaBoostBase =
            config.has("statusEffectManaBoostBase") ?
            config.get("statusEffectManaBoostBase").getAsInt() :
            STATUS_EFFECT_MANA_BOOST_BASE;
        statusEffectManaReductionBase =
            config.has("statusEffectManaReductionBase") ?
            config.get("statusEffectManaReductionBase").getAsInt() :
            STATUS_EFFECT_MANA_REDUCTION_BASE;
        statusEffectInstantManaBase =
            config.has("statusEffectInstantManaBase") ?
            config.get("statusEffectInstantManaBase").getAsInt() :
            STATUS_EFFECT_INSTANT_MANA_BASE;
        statusEffectInstantDepleteBase =
            config.has("statusEffectInstantDepleteBase") ?
            config.get("statusEffectInstantDepleteBase").getAsInt() :
            STATUS_EFFECT_INSTANT_DEPLETE_BASE;
        statusEffectManaRegenBase =
            config.has("statusEffectManaRegenBase") ?
            config.get("statusEffectManaRegenBase").getAsInt() :
            STATUS_EFFECT_MANA_REGEN_BASE;
        statusEffectManaInhibitionBase =
            config.has("statusEffectManaInhibitionBase") ?
            config.get("statusEffectManaInhibitionBase").getAsInt() :
            STATUS_EFFECT_MANA_INHIBITION_BASE;
        statusEffectManaPowerBase =
            config.has("statusEffectManaPowerBase") ?
            config.get("statusEffectManaPowerBase").getAsInt() :
            STATUS_EFFECT_MANA_POWER_BASE;
        statusEffectManaSicknessBase =
            config.has("statusEffectManaSicknessBase") ?
            config.get("statusEffectManaSicknessBase").getAsInt() :
            STATUS_EFFECT_MANA_SICKNESS_BASE;
        displayIdleInterval =
            config.has("displayIdleInterval") ?
            config.get("displayIdleInterval").getAsInt() :
            DISPLAY_IDLE_INTERVAL;
        displaySuppressionInterval =
            config.has("displaySuppressionInterval") ?
            config.get("displaySuppressionInterval").getAsInt() :
            DISPLAY_SUPPRESSION_INTERVAL;
        forceEnabled =
            config.has("forceEnabled") ?
            config.get("forceEnabled").getAsBoolean() :
            FORCE_ENABLED;
        enabled =
            config.has("enabled") ?
            config.get("enabled").getAsBoolean() :
            ENABLED;
        display =
            config.has("display") ?
            config.get("display").getAsBoolean() :
            DISPLAY;
        renderType =
            config.has("renderType") ?
            config.get("renderType").getAsInt() :
            RENDER_TYPE;
        maxManaCharIndexForDisplay =
            config.has("maxManaCharIndexForDisplay") ?
            config.get("maxManaCharIndexForDisplay").getAsInt() :
            MAX_MANA_CHAR_INDEX_FOR_DISPLAY;
        fixedSize =
            config.has("fixedSize") ?
            config.get("fixedSize").getAsInt() :
            FIXED_SIZE;
        manaChars =
            config.has("manaChars") ?
            config.get("manaChars").getAsJsonArray().asList().stream().map(jsonElement -> jsonElement.getAsString().codePointAt(0)).collect(Collectors.toUnmodifiableList()) :
            MANA_CHARS;
        manaColors =
            config.has("manaColors") ?
            config.get("manaColors").getAsJsonArray().asList().stream().map(jsonElement -> TextColor.fromRgb(jsonElement.getAsInt())).collect(Collectors.toUnmodifiableList()) :
            MANA_COLORS;
        manaBolds =
            config.has("manaBolds") ?
            config.get("manaBolds").getAsJsonArray().asList().stream().map(jsonElement -> jsonElement.getAsBoolean()).collect(Collectors.toUnmodifiableList()) :
            MANA_BOLDS;
        manaItalics =
            config.has("manaItalics") ?
            config.get("manaItalics").getAsJsonArray().asList().stream().map(jsonElement -> jsonElement.getAsBoolean()).collect(Collectors.toUnmodifiableList()) :
            MANA_ITALICS;
        manaUnderlineds =
            config.has("manaUnderlineds") ?
            config.get("manaUnderlineds").getAsJsonArray().asList().stream().map(jsonElement -> jsonElement.getAsBoolean()).collect(Collectors.toUnmodifiableList()) :
            MANA_UNDERLINEDS;
        manaStrikethroughs =
            config.has("manaStrikethroughs") ?
            config.get("manaStrikethroughs").getAsJsonArray().asList().stream().map(jsonElement -> jsonElement.getAsBoolean()).collect(Collectors.toUnmodifiableList()) :
            MANA_STRIKETHROUGHS;
        manaObfuscateds =
            config.has("manaObfuscateds") ?
            config.get("manaObfuscateds").getAsJsonArray().asList().stream().map(jsonElement -> jsonElement.getAsBoolean()).collect(Collectors.toUnmodifiableList()) :
            MANA_OBFUSCATEDS;
        
        reCalc();
		return 2;
	}

	public static void reset() {
        manaPerPoint                        = MANA_PER_POINT;
        manaCapacityBase                    = MANA_CAPACITY_BASE;
        manaRegenBase                       = MANA_REGEN_BASE;
        enchantmentCapacityBase             = ENCHANTMENT_CAPACITY_BASE;
        enchantmentStreamBase               = ENCHANTMENT_STREAM_BASE;
        enchantmentUtilizationBase          = ENCHANTMENT_UTILIZATION_BASE;
        enchantmentPotencyBase              = ENCHANTMENT_POTENCY_BASE;
        statusEffectManaBoostBase           = STATUS_EFFECT_MANA_BOOST_BASE;
        statusEffectManaReductionBase       = STATUS_EFFECT_MANA_REDUCTION_BASE;
        statusEffectInstantManaBase         = STATUS_EFFECT_INSTANT_MANA_BASE;
        statusEffectInstantDepleteBase      = STATUS_EFFECT_INSTANT_DEPLETE_BASE;
        statusEffectManaRegenBase           = STATUS_EFFECT_MANA_REGEN_BASE;
        statusEffectManaInhibitionBase      = STATUS_EFFECT_MANA_INHIBITION_BASE;
        statusEffectManaPowerBase           = STATUS_EFFECT_MANA_POWER_BASE;
        statusEffectManaSicknessBase        = STATUS_EFFECT_MANA_SICKNESS_BASE;
        displayIdleInterval                 = DISPLAY_IDLE_INTERVAL;
        displaySuppressionInterval          = DISPLAY_SUPPRESSION_INTERVAL;
        forceEnabled                        = FORCE_ENABLED;
        enabled                             = ENABLED;
        display                             = DISPLAY;
        renderType                          = RENDER_TYPE;
        maxManaCharIndexForDisplay          = MAX_MANA_CHAR_INDEX_FOR_DISPLAY;
        fixedSize                           = FIXED_SIZE;
        manaChars                           = MANA_CHARS;
        manaColors                          = MANA_COLORS;
        manaBolds                           = MANA_BOLDS;
        manaItalics                         = MANA_ITALICS;
        manaUnderlineds                     = MANA_UNDERLINEDS;
        manaStrikethroughs                  = MANA_STRIKETHROUGHS;
        manaObfuscateds                     = MANA_OBFUSCATEDS;
	}

    public static void reCalc() {
        manaCharTypes = manaChars.size();
        maxManaCharTypeIndex = manaCharTypes - 1;
        pointsPerChar = maxManaCharTypeIndex;
        maxManaPointIndex = Integer.MIN_VALUE / -manaPerPoint;
        maxManaCharIndex = -maxManaPointIndex - 1 / -pointsPerChar;
        maxFlexSize = maxManaCharIndexForDisplay + 1;
        maxManaCapacityPointTrimmed = maxFlexSize * manaPerPoint;
    }
}

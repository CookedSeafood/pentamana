package net.cookedseafood.pentamana;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.cookedseafood.pentamana.command.ManaCommand;
import net.cookedseafood.pentamana.command.PentamanaCommand;
import net.cookedseafood.pentamana.render.ManabarPositions;
import net.cookedseafood.pentamana.render.ManabarTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
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
	public static final byte VERSION_MINOR = 5;
	public static final byte VERSION_PATCH = 0;

    public static final byte MANA_CHARACTER_TYPE_INDEX_LIMIT = Byte.MAX_VALUE;
    public static final byte MANA_CHARACTER_INDEX_LIMIT = Byte.MAX_VALUE;
    public static final int MANA_STATUS_EFFECT_AMPLIFIER_LIMIT = 255;

	public static final int MANA_PER_POINT = 1;
    public static final int POINTS_PER_CHARACTER = 2;
	public static final float MANA_CAPACITY_BASE = 2.0f;
	public static final float MANA_REGEN_BASE = 0.0625f;
    public static final float ENCHANTMENT_CAPACITY_BASE = 2.0f;
	public static final float ENCHANTMENT_STREAM_BASE = 0.03125f;
    public static final float ENCHANTMENT_UTILIZATION_BASE = 0.1f;
    public static final float ENCHANTMENT_POTENCY_BASE = 0.5f;
    public static final float STATUS_EFFECT_MANA_BOOST_BASE = 4.0f;
    public static final float STATUS_EFFECT_MANA_REDUCTION_BASE = 4.0f;
    public static final float STATUS_EFFECT_INSTANT_MANA_BASE = 4.0f;
    public static final float STATUS_EFFECT_INSTANT_DEPLETE_BASE = 6.0f;
    public static final float STATUS_EFFECT_MANA_POWER_BASE = 3.0f;
    public static final float STATUS_EFFECT_MANA_SICKNESS_BASE = 4.0f;
    public static final int STATUS_EFFECT_MANA_REGEN_BASE = 50;
    public static final int STATUS_EFFECT_MANA_INHIBITION_BASE = 40;
    public static final byte DISPLAY_IDLE_INTERVAL = 40/* 20*2 */;
	public static final byte DISPLAY_SUPPRESSION_INTERVAL = 40/* 20*2 */;
    public static final boolean IS_FORCE_ENABLED = false;
    public static final boolean IS_ENABLED = true;
    public static final boolean IS_VISIBLE = true;
    public static final boolean IS_COMPRESSION = false;
    public static final byte COMPRESSION_SIZE = 20;
    public static final byte MANA_BAR_TYPE = ManabarTypes.CHARACTER.getIndex();
    public static final byte MANA_BAR_POSITION = ManabarPositions.ACTIONBAR.getIndex();
    public static final List<List<Text>> MANA_CHARACTERS = Stream.concat(
        Stream.of(
            Collections.nCopies(MANA_CHARACTER_INDEX_LIMIT + 1, (Text)Text.literal("\u2605").formatted(Formatting.AQUA)),
            Collections.nCopies(MANA_CHARACTER_INDEX_LIMIT + 1, (Text)Text.literal("\u2bea").formatted(Formatting.AQUA)),
            Collections.nCopies(MANA_CHARACTER_INDEX_LIMIT + 1, (Text)Text.literal("\u2606").formatted(Formatting.BLACK))
        ),
        Collections.nCopies(125, Collections.nCopies(MANA_CHARACTER_INDEX_LIMIT + 1, (Text)Text.literal("\ufffd"))).stream()
    )
    .map(ArrayList::new)
    .collect(Collectors.toList());
    public static final BossBar.Color BOSS_BAR_COLOR = BossBar.Color.BLUE;
    public static final BossBar.Style BOSS_BAR_STYLE = BossBar.Style.PROGRESS;

	public static int manaPerPoint;
    public static int pointsPerCharacter;
	public static float manaCapacityBase;
    public static float manaRegenBase;
	public static float enchantmentCapacityBase;
    public static float enchantmentStreamBase;
    public static float enchantmentUtilizationBase;
    public static float enchantmentPotencyBase;
    public static float statusEffectManaBoostBase;
    public static float statusEffectManaReductionBase;
    public static float statusEffectInstantManaBase;
    public static float statusEffectInstantDepleteBase;
    public static float statusEffectManaPowerBase;
    public static float statusEffectManaSicknessBase;
    public static int statusEffectManaRegenBase;
    public static int statusEffectManaInhibitionBase;
    public static byte displayIdleInterval;
	public static byte displaySuppressionInterval;
	public static boolean isForceEnabled;
    public static boolean isEnabled;
    public static boolean isVisible;
    public static boolean isCompression;
    public static byte compressionSize;
    public static byte manabarType;
    public static byte manabarPosition;
	public static List<List<Text>> manaCharacters;
    public static BossBar.Color bossbarColor;
    public static BossBar.Style bossbarStyle;

    public static int manaPointLimit;
    public static boolean isLoaded;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> PentamanaCommand.register(dispatcher, registryAccess));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ManaCommand.register(dispatcher, registryAccess));

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            reload(server);
        });
	}

	public static int reload(MinecraftServer server) {
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
        pointsPerCharacter =
            config.has("pointsPerCharacter") ? 
            config.get("pointsPerCharacter").getAsInt() :
            POINTS_PER_CHARACTER;
        manaCapacityBase =
            config.has("manaCapacityBase") ?
            config.get("manaCapacityBase").getAsFloat() :
            MANA_CAPACITY_BASE;
        manaRegenBase =
            config.has("manaRegenBase") ?
            config.get("manaRegenBase").getAsFloat() :
            MANA_REGEN_BASE;
        enchantmentCapacityBase =
            config.has("enchantmentCapacityBase") ?
            config.get("enchantmentCapacityBase").getAsFloat() :
            ENCHANTMENT_CAPACITY_BASE;
        enchantmentStreamBase =
            config.has("enchantmentStreamBase") ?
            config.get("enchantmentStreamBase").getAsFloat() :
            ENCHANTMENT_STREAM_BASE;
        enchantmentUtilizationBase =
            config.has("enchantmentUtilizationBase") ?
            config.get("enchantmentUtilizationBase").getAsFloat() :
            ENCHANTMENT_UTILIZATION_BASE;
        enchantmentPotencyBase =
            config.has("enchantmentPotencyBase") ?
            config.get("enchantmentPotencyBase").getAsFloat() :
            ENCHANTMENT_POTENCY_BASE;
        statusEffectManaBoostBase =
            config.has("statusEffectManaBoostBase") ?
            config.get("statusEffectManaBoostBase").getAsFloat() :
            STATUS_EFFECT_MANA_BOOST_BASE;
        statusEffectManaReductionBase =
            config.has("statusEffectManaReductionBase") ?
            config.get("statusEffectManaReductionBase").getAsFloat() :
            STATUS_EFFECT_MANA_REDUCTION_BASE;
        statusEffectInstantManaBase =
            config.has("statusEffectInstantManaBase") ?
            config.get("statusEffectInstantManaBase").getAsFloat() :
            STATUS_EFFECT_INSTANT_MANA_BASE;
        statusEffectInstantDepleteBase =
            config.has("statusEffectInstantDepleteBase") ?
            config.get("statusEffectInstantDepleteBase").getAsFloat() :
            STATUS_EFFECT_INSTANT_DEPLETE_BASE;
        statusEffectManaPowerBase =
            config.has("statusEffectManaPowerBase") ?
            config.get("statusEffectManaPowerBase").getAsFloat() :
            STATUS_EFFECT_MANA_POWER_BASE;
        statusEffectManaSicknessBase =
            config.has("statusEffectManaSicknessBase") ?
            config.get("statusEffectManaSicknessBase").getAsFloat() :
            STATUS_EFFECT_MANA_SICKNESS_BASE;
        statusEffectManaRegenBase =
            config.has("statusEffectManaRegenBase") ?
            config.get("statusEffectManaRegenBase").getAsInt() :
            STATUS_EFFECT_MANA_REGEN_BASE;
        statusEffectManaInhibitionBase =
            config.has("statusEffectManaInhibitionBase") ?
            config.get("statusEffectManaInhibitionBase").getAsInt() :
            STATUS_EFFECT_MANA_INHIBITION_BASE;
        displayIdleInterval =
            config.has("displayIdleInterval") ?
            config.get("displayIdleInterval").getAsByte() :
            DISPLAY_IDLE_INTERVAL;
        displaySuppressionInterval =
            config.has("displaySuppressionInterval") ?
            config.get("displaySuppressionInterval").getAsByte() :
            DISPLAY_SUPPRESSION_INTERVAL;
        isForceEnabled =
            config.has("isForceEnabled") ?
            config.get("isForceEnabled").getAsBoolean() :
            IS_FORCE_ENABLED;
        isEnabled =
            config.has("isEnabled") ?
            config.get("isEnabled").getAsBoolean() :
            IS_ENABLED;
        isVisible =
            config.has("isVisible") ?
            config.get("isVisible").getAsBoolean() :
            IS_VISIBLE;
        isCompression =
            config.has("isCompression") ?
            config.get("isCompression").getAsBoolean() :
            IS_COMPRESSION;
        compressionSize =
            config.has("compressionSize") ?
            config.get("compressionSize").getAsByte() :
            COMPRESSION_SIZE;
        manabarType =
            config.has("manabarType") ?
            ManabarTypes.getIndex(config.get("manabarType").getAsString()) :
            MANA_BAR_TYPE;
        manabarPosition =
            config.has("manabarPosition") ?
            ManabarPositions.getIndex(config.get("manabarPosition").getAsString()) :
            MANA_BAR_POSITION;
        manaCharacters =
            config.has("manaCharacters") ?
            Stream.of(
                config.get("manaCharacters").getAsJsonArray().asList().stream()
                    .map(incompleteManaCharacterType -> incompleteManaCharacterType.getAsJsonArray().asList().stream()
                        .map(manaCharacter -> Text.Serialization.fromJsonTree(manaCharacter, server.getRegistryManager()))
                        .map(Text.class::cast)
                        .collect(Collectors.toList())
                    )
                    .map(incompleteManaCharacterType -> incompleteManaCharacterType.size() <= MANA_CHARACTER_INDEX_LIMIT ?
                        Stream.concat(
                            incompleteManaCharacterType.stream(),
                            Collections.nCopies(MANA_CHARACTER_INDEX_LIMIT + 1 - incompleteManaCharacterType.size(), incompleteManaCharacterType.getFirst()).stream()
                        )
                        .collect(Collectors.toList()) :
                        incompleteManaCharacterType
                    )
                    .collect(Collectors.toList())
            )
            .map(incompleteManaCharacters -> incompleteManaCharacters.size() <= MANA_CHARACTER_TYPE_INDEX_LIMIT ?
                Stream.concat(
                    incompleteManaCharacters.stream(),
                    Collections.nCopies(MANA_CHARACTER_TYPE_INDEX_LIMIT + 1 - incompleteManaCharacters.size(), Collections.nCopies(MANA_CHARACTER_INDEX_LIMIT + 1, (Text)Text.literal("�"))).stream()
                )
                .collect(Collectors.toList()) :
                incompleteManaCharacters
            )
            .findAny()
            .orElse(MANA_CHARACTERS) :
            MANA_CHARACTERS;
        bossbarColor =
            config.has("bossbarColor") ?
            BossBar.Color.byName(config.get("bossbarColor").getAsString()) :
            BOSS_BAR_COLOR;
        bossbarStyle =
            config.has("bossbarStyle") ?
            BossBar.Style.byName(config.get("bossbarStyle").getAsString()) :
            BOSS_BAR_STYLE;

        reCalc();
        isLoaded = true;
		return 2;
	}

	public static void reset() {
        manaPerPoint                        = MANA_PER_POINT;
        pointsPerCharacter                  = POINTS_PER_CHARACTER;
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
        isForceEnabled                      = IS_FORCE_ENABLED;
        isEnabled                           = IS_ENABLED;
        isVisible                           = IS_VISIBLE;
        isCompression                       = IS_COMPRESSION;
        compressionSize                     = COMPRESSION_SIZE;
        manabarType                         = MANA_BAR_TYPE;
        manabarPosition                     = MANA_BAR_POSITION;
        manaCharacters                      = MANA_CHARACTERS;
        bossbarColor                        = BOSS_BAR_COLOR;
        bossbarStyle                        = BOSS_BAR_STYLE;
	}

    public static void reCalc() {
        manaPointLimit = MANA_CHARACTER_INDEX_LIMIT * manaPerPoint;
    }
}

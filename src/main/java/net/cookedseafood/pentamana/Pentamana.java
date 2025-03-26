package net.cookedseafood.pentamana;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.cookedseafood.pentamana.command.ManaBarCommand;
import net.cookedseafood.pentamana.command.ManaCommand;
import net.cookedseafood.pentamana.command.PentamanaCommand;
import net.cookedseafood.pentamana.mana.ManaBar;
import net.cookedseafood.pentamana.mana.ManaCharset;
import net.cookedseafood.pentamana.mana.ManaPattern;
import net.cookedseafood.pentamana.mana.ManaRender;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pentamana implements ModInitializer {
    public static final String MOD_ID = "pentamana";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final byte VERSION_MAJOR = 0;
    public static final byte VERSION_MINOR = 6;
    public static final byte VERSION_PATCH = 4;

    public static final byte MANA_CHARACTER_TYPE_INDEX_LIMIT = Byte.MAX_VALUE;
    public static final byte MANA_CHARACTER_INDEX_LIMIT = Byte.MAX_VALUE;
    public static final int MANA_STATUS_EFFECT_AMPLIFIER_LIMIT = 255;
    public static final Text MANA_PATTERN_MATCHER = Text.of("$");
    public static final String MANA_BAR_NAME_PREFIX = "manabar.";

    public static final int MANA_PER_POINT = 1;
    public static final float MANA_CAPACITY_BASE = 2.0f;
    public static final float MANA_REGENERATION_BASE = 0.0625f;
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
    public static final int STATUS_EFFECT_MANA_REGENERATION_BASE = 50;
    public static final int STATUS_EFFECT_MANA_INHIBITION_BASE = 40;
    public static final boolean IS_CONVERSION_EXPERIENCE_LEVEL = false;
    public static final float CONVERSION_EXPERIENCE_LEVEL_BASE = 0.5f;
    public static final byte DISPLAY_IDLE_INTERVAL = 40/* 20*2 */;
    public static final byte DISPLAY_SUPPRESSION_INTERVAL = 40/* 20*2 */;
    public static final boolean IS_FORCE_ENABLED = false;
    public static final boolean IS_ENABLED = true;
    public static final ManaBar.Position MANA_BAR_POSITION = ManaBar.Position.ACTIONBAR;
    public static final ManaPattern MANA_PATTERN = new ManaPattern(Stream.of(Text.literal("$")).collect(Collectors.toList()));
    public static final ManaRender.Type MANA_RENDER_TYPE = ManaRender.Type.CHARACTER;
    public static final ManaCharset MANA_CHARSET = new ManaCharset(
        Stream.concat(
            Stream.of(
                Collections.nCopies(MANA_CHARACTER_INDEX_LIMIT + 1, (Text)Text.literal("\u2605").formatted(Formatting.AQUA)),
                Collections.nCopies(MANA_CHARACTER_INDEX_LIMIT + 1, (Text)Text.literal("\u2bea").formatted(Formatting.AQUA)),
                Collections.nCopies(MANA_CHARACTER_INDEX_LIMIT + 1, (Text)Text.literal("\u2606").formatted(Formatting.BLACK))
            ),
            Collections.nCopies(125, Collections.nCopies(MANA_CHARACTER_INDEX_LIMIT + 1, (Text)Text.literal("\ufffd"))).stream()
        )
        .map(ArrayList::new)
        .collect(Collectors.toList())
    );
    public static final int POINTS_PER_CHARACTER = 2;
    public static final boolean IS_COMPRESSION = false;
    public static final byte COMPRESSION_SIZE = 20;
    public static final boolean IS_VISIBLE = true;
    public static final BossBar.Color MANA_BAR_COLOR = BossBar.Color.BLUE;
    public static final BossBar.Style MANA_BAR_STYLE = BossBar.Style.PROGRESS;

    public static int manaPerPoint;
    public static float manaCapacityBase;
    public static float manaRegenerationBase;
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
    public static int statusEffectManaRegenerationBase;
    public static int statusEffectManaInhibitionBase;
    public static boolean isConversionExperienceLevel;
    public static float conversionExperienceLevelBase;
    public static byte displayIdleInterval;
    public static byte displaySuppressionInterval;
    public static boolean isForceEnabled;
    public static boolean isEnabled;
    public static ManaBar.Position manaBarPosition;
    public static ManaPattern manaPattern;
    public static ManaRender.Type manaRenderType;
    public static ManaCharset manaCharset;
    public static int pointsPerCharacter;
    public static boolean isCompression;
    public static byte compressionSize;
    public static boolean isVisible;
    public static BossBar.Color manaBarColor;
    public static BossBar.Style manaBarStyle;

    public static int manaPointLimit;

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> PentamanaCommand.register(dispatcher, registryAccess));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ManaCommand.register(dispatcher, registryAccess));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ManaBarCommand.register(dispatcher, registryAccess));

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
        MutableInt counter = new MutableInt(0);

        if (config.has("manaPerPoint")) {
            manaPerPoint = config.get("manaPerPoint").getAsInt();
            counter.increment();
        } else {
            manaPerPoint = MANA_PER_POINT;
        }

        if (config.has("manaCapacityBase")) {
            manaCapacityBase = config.get("manaCapacityBase").getAsFloat();
            counter.increment();
        } else {
            manaCapacityBase = MANA_CAPACITY_BASE;
        }

        if (config.has("manaRegenerationBase")) {
            manaRegenerationBase = config.get("manaRegenerationBase").getAsFloat();
            counter.increment();
        } else {
            manaRegenerationBase = MANA_REGENERATION_BASE;
        }

        if (config.has("enchantmentCapacityBase")) {
            enchantmentCapacityBase = config.get("enchantmentCapacityBase").getAsFloat();
            counter.increment();
        } else {
            enchantmentCapacityBase = ENCHANTMENT_CAPACITY_BASE;
        }

        if (config.has("enchantmentStreamBase")) {
            enchantmentStreamBase = config.get("enchantmentStreamBase").getAsFloat();
            counter.increment();
        } else {
            enchantmentStreamBase = ENCHANTMENT_STREAM_BASE;
        }

        if (config.has("enchantmentUtilizationBase")) {
            enchantmentUtilizationBase = config.get("enchantmentUtilizationBase").getAsFloat();
            counter.increment();
        } else {
            enchantmentUtilizationBase = ENCHANTMENT_UTILIZATION_BASE;
        }

        if (config.has("enchantmentPotencyBase")) {
            enchantmentPotencyBase = config.get("enchantmentPotencyBase").getAsFloat();
            counter.increment();
        } else {
            enchantmentPotencyBase = ENCHANTMENT_POTENCY_BASE;
        }

        if (config.has("statusEffectManaBoostBase")) {
            statusEffectManaBoostBase = config.get("statusEffectManaBoostBase").getAsFloat();
            counter.increment();
        } else {
            statusEffectManaBoostBase = STATUS_EFFECT_MANA_BOOST_BASE;
        }

        if (config.has("statusEffectManaReductionBase")) {
            statusEffectManaReductionBase = config.get("statusEffectManaReductionBase").getAsFloat();
            counter.increment();
        } else {
            statusEffectManaReductionBase = STATUS_EFFECT_MANA_REDUCTION_BASE;
        }

        if (config.has("statusEffectInstantManaBase")) {
            statusEffectInstantManaBase = config.get("statusEffectInstantManaBase").getAsFloat();
            counter.increment();
        } else {
            statusEffectInstantManaBase = STATUS_EFFECT_INSTANT_MANA_BASE;
        }

        if (config.has("statusEffectInstantDepleteBase")) {
            statusEffectInstantDepleteBase = config.get("statusEffectInstantDepleteBase").getAsFloat();
            counter.increment();
        } else {
            statusEffectInstantDepleteBase = STATUS_EFFECT_INSTANT_DEPLETE_BASE;
        }

        if (config.has("statusEffectManaPowerBase")) {
            statusEffectManaPowerBase = config.get("statusEffectManaPowerBase").getAsFloat();
            counter.increment();
        } else {
            statusEffectManaPowerBase = STATUS_EFFECT_MANA_POWER_BASE;
        }

        if (config.has("statusEffectManaSicknessBase")) {
            statusEffectManaSicknessBase = config.get("statusEffectManaSicknessBase").getAsFloat();
            counter.increment();
        } else {
            statusEffectManaSicknessBase = STATUS_EFFECT_MANA_SICKNESS_BASE;
        }

        if (config.has("statusEffectManaRegenerationBase")) {
            statusEffectManaRegenerationBase = config.get("statusEffectManaRegenerationBase").getAsInt();
            counter.increment();
        } else {
            statusEffectManaRegenerationBase = STATUS_EFFECT_MANA_REGENERATION_BASE;
        }

        if (config.has("statusEffectManaInhibitionBase")) {
            statusEffectManaInhibitionBase = config.get("statusEffectManaInhibitionBase").getAsInt();
            counter.increment();
        } else {
            statusEffectManaInhibitionBase = STATUS_EFFECT_MANA_INHIBITION_BASE;
        }

        if (config.has("isConversionExperienceLevel")) {
            isConversionExperienceLevel = config.get("isConversionExperienceLevel").getAsBoolean();
            counter.increment();
        } else {
            isConversionExperienceLevel = IS_CONVERSION_EXPERIENCE_LEVEL;
        }

        if (config.has("conversionExperienceLevelBase")) {
            conversionExperienceLevelBase = config.get("conversionExperienceLevelBase").getAsFloat();
            counter.increment();
        } else {
            conversionExperienceLevelBase = CONVERSION_EXPERIENCE_LEVEL_BASE;
        }

        if (config.has("displayIdleInterval")) {
            displayIdleInterval = config.get("displayIdleInterval").getAsByte();
            counter.increment();
        } else {
            displayIdleInterval = DISPLAY_IDLE_INTERVAL;
        }

        if (config.has("displaySuppressionInterval")) {
            displaySuppressionInterval = config.get("displaySuppressionInterval").getAsByte();
            counter.increment();
        } else {
            displaySuppressionInterval = DISPLAY_SUPPRESSION_INTERVAL;
        }

        if (config.has("isForceEnabled")) {
            isForceEnabled = config.get("isForceEnabled").getAsBoolean();
            counter.increment();
        } else {
            isForceEnabled = IS_FORCE_ENABLED;
        }

        if (config.has("isEnabled")) {
            isEnabled = config.get("isEnabled").getAsBoolean();
            counter.increment();
        } else {
            isEnabled = IS_ENABLED;
        }

        if (config.has("manaBarPosition")) {
            manaBarPosition = ManaBar.Position.byName(config.get("manaBarPosition").getAsString());
            counter.increment();
        } else {
            manaBarPosition = MANA_BAR_POSITION;
        }

        if (config.has("manaPattern")) {
            manaPattern = new ManaPattern(
                config.get("pattern").getAsJsonArray().asList().stream()
                    .map(partialPattern -> Text.Serialization.fromJsonTree(partialPattern, server.getRegistryManager()))
                    .map(Text.class::cast)
                    .collect(Collectors.toList())
            );
            counter.increment();
        } else {
            manaPattern = MANA_PATTERN;
        }

        if (config.has("manaRenderType")) {
            manaRenderType = ManaRender.Type.byName(config.get("manaRenderType").getAsString());
            counter.increment();
        } else {
            manaRenderType = MANA_RENDER_TYPE;
        }

        if (config.has("manaCharset")) {
            manaCharset = new ManaCharset(
                Stream.of(
                    config.get("charset").getAsJsonArray().asList().stream()
                        .map(JsonElement::getAsJsonArray)
                        .map(JsonArray::asList)
                        .map(charsetType -> charsetType.stream()
                            .map(character -> Text.Serialization.fromJsonTree(character, server.getRegistryManager()))
                            .map(Text.class::cast)
                            .collect(Collectors.toList())
                        )
                        .map(charsetType -> charsetType.size() <= MANA_CHARACTER_INDEX_LIMIT ?
                            Stream.concat(
                                charsetType.stream(),
                                Collections.nCopies(MANA_CHARACTER_INDEX_LIMIT + 1 - charsetType.size(), charsetType.getFirst()).stream()
                            )
                            .collect(Collectors.toList()) :
                            charsetType
                        )
                        .collect(Collectors.toList())
                )
                .map(charset -> charset.size() <= MANA_CHARACTER_TYPE_INDEX_LIMIT ?
                    Stream.concat(
                        charset.stream(),
                        Collections.nCopies(MANA_CHARACTER_TYPE_INDEX_LIMIT + 1 - charset.size(), Collections.nCopies(MANA_CHARACTER_INDEX_LIMIT + 1, (Text)Text.literal("�"))).stream()
                    )
                    .collect(Collectors.toList()) :
                    charset
                )
                .findAny()
                .get()
            );
        } else {
            manaCharset = MANA_CHARSET;
        }

        if (config.has("pointsPerCharacter")) {
            pointsPerCharacter = config.get("pointsPerCharacter").getAsInt();
            counter.increment();
        } else {
            pointsPerCharacter = POINTS_PER_CHARACTER;
        }

        if (config.has("isCompression")) {
            isCompression = config.get("isCompression").getAsBoolean();
            counter.increment();
        } else {
            isCompression = IS_COMPRESSION;
        }

        if (config.has("compressionSize")) {
            compressionSize = config.get("compressionSize").getAsByte();
            counter.increment();
        } else {
            compressionSize = COMPRESSION_SIZE;
        }

        if (config.has("isVisible")) {
            isVisible = config.get("isVisible").getAsBoolean();
            counter.increment();
        } else {
            isVisible = IS_VISIBLE;
        }

        if (config.has("manaBarColor")) {
            manaBarColor = BossBar.Color.byName(config.get("manaBarColor").getAsString());
            counter.increment();
        } else {
            manaBarColor = MANA_BAR_COLOR;
        }

        if (config.has("manaBarStyle")) {
            manaBarStyle = BossBar.Style.byName(config.get("manaBarStyle").getAsString());
            counter.increment();
        } else {
            manaBarStyle = MANA_BAR_STYLE;
        }

        reCalc();
        return counter.intValue();
    }

    public static void reset() {
        manaPerPoint                        = MANA_PER_POINT;
        manaCapacityBase                    = MANA_CAPACITY_BASE;
        manaRegenerationBase                = MANA_REGENERATION_BASE;
        enchantmentCapacityBase             = ENCHANTMENT_CAPACITY_BASE;
        enchantmentStreamBase               = ENCHANTMENT_STREAM_BASE;
        enchantmentUtilizationBase          = ENCHANTMENT_UTILIZATION_BASE;
        enchantmentPotencyBase              = ENCHANTMENT_POTENCY_BASE;
        statusEffectManaBoostBase           = STATUS_EFFECT_MANA_BOOST_BASE;
        statusEffectManaReductionBase       = STATUS_EFFECT_MANA_REDUCTION_BASE;
        statusEffectInstantManaBase         = STATUS_EFFECT_INSTANT_MANA_BASE;
        statusEffectInstantDepleteBase      = STATUS_EFFECT_INSTANT_DEPLETE_BASE;
        statusEffectManaRegenerationBase           = STATUS_EFFECT_MANA_REGENERATION_BASE;
        statusEffectManaInhibitionBase      = STATUS_EFFECT_MANA_INHIBITION_BASE;
        statusEffectManaPowerBase           = STATUS_EFFECT_MANA_POWER_BASE;
        statusEffectManaSicknessBase        = STATUS_EFFECT_MANA_SICKNESS_BASE;
        isConversionExperienceLevel         = IS_CONVERSION_EXPERIENCE_LEVEL;
        conversionExperienceLevelBase       = CONVERSION_EXPERIENCE_LEVEL_BASE;
        displayIdleInterval                 = DISPLAY_IDLE_INTERVAL;
        displaySuppressionInterval          = DISPLAY_SUPPRESSION_INTERVAL;
        isForceEnabled                      = IS_FORCE_ENABLED;
        isEnabled                           = IS_ENABLED;
        manaBarPosition                     = MANA_BAR_POSITION;
        manaPattern                         = MANA_PATTERN;
        manaRenderType                      = MANA_RENDER_TYPE;
        manaCharset                         = MANA_CHARSET;
        pointsPerCharacter                  = POINTS_PER_CHARACTER;
        isCompression                       = IS_COMPRESSION;
        compressionSize                     = COMPRESSION_SIZE;
        isVisible                           = IS_VISIBLE;
        manaBarColor                        = MANA_BAR_COLOR;
        manaBarStyle                        = MANA_BAR_STYLE;
    }

    public static void reCalc() {
        manaPointLimit = MANA_CHARACTER_INDEX_LIMIT * manaPerPoint;
    }
}

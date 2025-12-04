package net.hederamc.pentamana.data;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.hederamc.cw.util.BossBars;
import net.hederamc.pentamana.Pentamana;
import net.hederamc.pentamana.render.ManaBar;
import net.hederamc.pentamana.render.ManaCharset;
import net.hederamc.pentamana.render.ManaPattern;
import net.hederamc.pentamana.render.ManaRender;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Formatting;

public abstract class PentamanaConfig {
    public static final int MANA_PER_POINT = 1;
    public static final float MANA_CAPACITY_BASE = 2.0f;
    public static final float MANA_REGENERATION_BASE = 0.0625f;
    public static final float ENCHANTMENT_CAPACITY_BASE = 2.0f;
    public static final float ENCHANTMENT_STREAM_BASE = 0.015625f;
    public static final float ENCHANTMENT_MANA_EFFICIENCY_BASE = 0.1f;
    public static final float ENCHANTMENT_POTENCY_BASE = 0.5f;
    public static final float STATUS_EFFECT_MANA_BOOST_BASE = 4.0f;
    public static final float STATUS_EFFECT_MANA_REDUCTION_BASE = 4.0f;
    public static final float STATUS_EFFECT_INSTANT_MANA_BASE = 4.0f;
    public static final float STATUS_EFFECT_INSTANT_DEPLETE_BASE = 6.0f;
    public static final float STATUS_EFFECT_MANA_POWER_BASE = 3.0f;
    public static final float STATUS_EFFECT_MANA_SICKNESS_BASE = 4.0f;
    public static final int STATUS_EFFECT_MANA_REGENERATION_BASE = 50;
    public static final int STATUS_EFFECT_MANA_INHIBITION_BASE = 40;
    public static final boolean SHOULD_CONVERT_EXPERIENCE_LEVEL = false;
    public static final float EXPERIENCE_LEVEL_CONVERSION_BASE = 0.5f;
    public static int manaPerPoint;
    public static float manaCapacityBase;
    public static float manaRegenerationBase;
    public static float enchantmentCapacityBase;
    public static float enchantmentStreamBase;
    public static float enchantmentManaEfficiencyBase;
    public static float enchantmentPotencyBase;
    public static float statusEffectManaBoostBase;
    public static float statusEffectManaReductionBase;
    public static float statusEffectInstantManaBase;
    public static float statusEffectInstantDepleteBase;
    public static float statusEffectManaPowerBase;
    public static float statusEffectManaSicknessBase;
    public static int statusEffectManaRegenerationBase;
    public static int statusEffectManaInhibitionBase;
    public static boolean shouldConvertExperienceLevel;
    public static float experienceLevelConversionBase;

    public static int manaPointLimit;

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
        if (config == null) {
            reset();
            reCalc();
            return 1;
        }

        return reload(config);
    }

    public static int reload(JsonObject config) {
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

        if (config.has("enchantmentManaEfficiencyBase")) {
            enchantmentManaEfficiencyBase = config.get("enchantmentManaEfficiencyBase").getAsFloat();
            counter.increment();
        } else {
            enchantmentManaEfficiencyBase = ENCHANTMENT_MANA_EFFICIENCY_BASE;
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

        if (config.has("shouldConvertExperienceLevel")) {
            shouldConvertExperienceLevel = config.get("shouldConvertExperienceLevel").getAsBoolean();
            counter.increment();
        } else {
            shouldConvertExperienceLevel = SHOULD_CONVERT_EXPERIENCE_LEVEL;
        }

        if (config.has("experienceLevelConversionBase")) {
            experienceLevelConversionBase = config.get("experienceLevelConversionBase").getAsFloat();
            counter.increment();
        } else {
            experienceLevelConversionBase = EXPERIENCE_LEVEL_CONVERSION_BASE;
        }

        if (config.has("default_preference")) {
            JsonObject defaultPreference = config.get("default_preference").getAsJsonObject();
            counter.add(DefaultPreference.reload(defaultPreference));
        } else {
            DefaultPreference.reset();
        }

        reCalc();
        return counter.intValue();
    }

    public static void reset() {
        manaPerPoint = MANA_PER_POINT;
        manaCapacityBase = MANA_CAPACITY_BASE;
        manaRegenerationBase = MANA_REGENERATION_BASE;
        enchantmentCapacityBase = ENCHANTMENT_CAPACITY_BASE;
        enchantmentStreamBase = ENCHANTMENT_STREAM_BASE;
        enchantmentManaEfficiencyBase = ENCHANTMENT_MANA_EFFICIENCY_BASE;
        enchantmentPotencyBase = ENCHANTMENT_POTENCY_BASE;
        statusEffectManaBoostBase = STATUS_EFFECT_MANA_BOOST_BASE;
        statusEffectManaReductionBase = STATUS_EFFECT_MANA_REDUCTION_BASE;
        statusEffectInstantManaBase = STATUS_EFFECT_INSTANT_MANA_BASE;
        statusEffectInstantDepleteBase = STATUS_EFFECT_INSTANT_DEPLETE_BASE;
        statusEffectManaRegenerationBase = STATUS_EFFECT_MANA_REGENERATION_BASE;
        statusEffectManaInhibitionBase = STATUS_EFFECT_MANA_INHIBITION_BASE;
        statusEffectManaPowerBase = STATUS_EFFECT_MANA_POWER_BASE;
        statusEffectManaSicknessBase = STATUS_EFFECT_MANA_SICKNESS_BASE;
        shouldConvertExperienceLevel = SHOULD_CONVERT_EXPERIENCE_LEVEL;
        experienceLevelConversionBase = EXPERIENCE_LEVEL_CONVERSION_BASE;
        DefaultPreference.reset();
    }

    public static void reCalc() {
        manaPointLimit = Pentamana.MANA_CHARACTER_INDEX_LIMIT * manaPerPoint;
    }

    public abstract class DefaultPreference {
        public static final boolean IS_VISIBLE = true;
        public static final boolean IS_SUPPRESSED = false;
        public static final ManaBar.Position POSITION = ManaBar.Position.ACTIONBAR;
        public static final ManaPattern PATTERN = new ManaPattern(Stream.of(Text.literal("$")).collect(Collectors.toList()));
        public static final ManaRender.Type TYPE = ManaRender.Type.CHARACTER;
        public static final ManaCharset CHARSET = new ManaCharset(
            Stream.concat(
                Stream.of(
                    Collections.nCopies(Pentamana.MANA_CHARACTER_INDEX_LIMIT + 1, (Text)Text.literal("\u2605").formatted(Formatting.AQUA)),
                    Collections.nCopies(Pentamana.MANA_CHARACTER_INDEX_LIMIT + 1, (Text)Text.literal("\u2bea").formatted(Formatting.AQUA)),
                    Collections.nCopies(Pentamana.MANA_CHARACTER_INDEX_LIMIT + 1, (Text)Text.literal("\u2606").formatted(Formatting.BLACK))
                ),
                Collections.nCopies(125, Collections.nCopies(Pentamana.MANA_CHARACTER_INDEX_LIMIT + 1, (Text)Text.literal("\ufffd"))).stream()
            )
            .map(ArrayList::new)
            .collect(Collectors.toList())
        );
        public static final int POINTS_PER_CHARACTER = 2;
        public static final boolean IS_COMPRESSED = false;
        public static final byte COMPRESSION_SIZE = 20;
        public static final BossBar.Color COLOR = BossBar.Color.BLUE;
        public static final BossBar.Style STYLE = BossBar.Style.PROGRESS;
        public static boolean isVisible;
        public static boolean isSuppressed;
        public static ManaBar.Position position;
        public static ManaRender.Type type;
        public static ManaPattern pattern;
        public static int pointsPerCharacter;
        public static boolean isCompressed;
        public static byte compressionSize;
        public static ManaCharset charset;
        public static BossBar.Color color;
        public static BossBar.Style style;

        public static int reload() {
            String configString;
            try {
                configString = FileUtils.readFileToString(new File("./config/pentamana.json"), StandardCharsets.UTF_8);
            } catch (IOException e) {
                reset();
                return 1;
            }

            JsonObject config = new Gson().fromJson(configString, JsonObject.class);
            if (config == null || !config.has("default_preference")) {
                reset();
                return 1;
            }

            JsonObject defaultPreference = config.get("default_preference").getAsJsonObject();
            if (defaultPreference == null) {
                reset();
                return 1;
            }

            return reload(defaultPreference);
        }

        public static int reload(JsonObject defaultPreference) {
            MutableInt counter = new MutableInt(0);

            if (defaultPreference.has("isVisible")) {
                isVisible = defaultPreference.get("isVisible").getAsBoolean();
                counter.increment();
            } else {
                isVisible = IS_VISIBLE;
            }

            if (defaultPreference.has("isSuppressed")) {
                isSuppressed = defaultPreference.get("isSuppressed").getAsBoolean();
                counter.increment();
            } else {
                isSuppressed = IS_VISIBLE;
            }

            if (defaultPreference.has("position")) {
                position = ManaBar.Position.byName(defaultPreference.get("position").getAsString());
                counter.increment();
            } else {
                position = POSITION;
            }

            if (defaultPreference.has("type")) {
                type = ManaRender.Type.byName(defaultPreference.get("type").getAsString());
                counter.increment();
            } else {
                type = TYPE;
            }

            if (defaultPreference.has("pattern")) {
                pattern = new ManaPattern(
                    defaultPreference.get("pattern").getAsJsonArray().asList().stream()
                        .map(text -> TextCodecs.CODEC.parse(JsonOps.INSTANCE, text))
                        .map(DataResult::result)
                        .map(Optional::get)
                        .map(Text.class::cast)
                        .collect(Collectors.toList())
                );
                counter.increment();
            } else {
                pattern = PATTERN;
            }

            if (defaultPreference.has("charset")) {
                charset = new ManaCharset(
                    Stream.of(
                        defaultPreference.get("charset").getAsJsonArray().asList().stream()
                            .map(JsonElement::getAsJsonArray)
                            .map(JsonArray::asList)
                            .map(charsetType -> charsetType.stream()
                                .map(text -> TextCodecs.CODEC.parse(JsonOps.INSTANCE, text))
                                .map(DataResult::result)
                                .map(Optional::get)
                                .collect(Collectors.toList())
                            )
                            .map(charsetType -> charsetType.size() <= Pentamana.MANA_CHARACTER_INDEX_LIMIT ?
                                Stream.concat(
                                    charsetType.stream(),
                                    Collections.nCopies(Pentamana.MANA_CHARACTER_INDEX_LIMIT + 1 - charsetType.size(), charsetType.getFirst()).stream()
                                )
                                .collect(Collectors.toList()) :
                                charsetType
                            )
                            .collect(Collectors.toList())
                    )
                    .map(charset -> charset.size() <= Pentamana.MANA_CHARACTER_TYPE_INDEX_LIMIT ?
                        Stream.concat(
                            charset.stream(),
                            Collections.nCopies(Pentamana.MANA_CHARACTER_TYPE_INDEX_LIMIT + 1 - charset.size(), Collections.nCopies(Pentamana.MANA_CHARACTER_INDEX_LIMIT + 1, (Text)Text.literal("�"))).stream()
                        )
                        .collect(Collectors.toList()) :
                        charset
                    )
                    .findAny()
                    .get()
                );
            } else {
                charset = CHARSET;
            }

            if (defaultPreference.has("pointsPerCharacter")) {
                pointsPerCharacter = defaultPreference.get("pointsPerCharacter").getAsInt();
                counter.increment();
            } else {
                pointsPerCharacter = POINTS_PER_CHARACTER;
            }

            if (defaultPreference.has("isCompressed")) {
                isCompressed = defaultPreference.get("isCompressed").getAsBoolean();
                counter.increment();
            } else {
                isCompressed = IS_COMPRESSED;
            }

            if (defaultPreference.has("compressionSize")) {
                compressionSize = defaultPreference.get("compressionSize").getAsByte();
                counter.increment();
            } else {
                compressionSize = COMPRESSION_SIZE;
            }

            if (defaultPreference.has("color")) {
                color = BossBars.Colors.byName(defaultPreference.get("color").getAsString());
                counter.increment();
            } else {
                color = COLOR;
            }

            if (defaultPreference.has("style")) {
                style = BossBars.Styles.byName(defaultPreference.get("style").getAsString());
                counter.increment();
            } else {
                style = STYLE;
            }

            reCalc();
            return counter.intValue();
        }

        public static void reset() {
            position = POSITION;
            pattern = PATTERN;
            type = TYPE;
            charset = CHARSET;
            pointsPerCharacter = POINTS_PER_CHARACTER;
            isCompressed = IS_COMPRESSED;
            compressionSize = COMPRESSION_SIZE;
            isVisible = IS_VISIBLE;
            isSuppressed = IS_SUPPRESSED;
            color = COLOR;
            style = STYLE;
        }
    }
}

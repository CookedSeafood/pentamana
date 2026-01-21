package net.hederamc.pentamanaextra.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.hederamc.pentamana.Pentamana;
import net.minecraft.resources.Identifier;

public final class PentamanaExtraConfig {
    public static ConfigClassHandler<PentamanaExtraConfig> HANDLER = ConfigClassHandler.createBuilder(PentamanaExtraConfig.class)
        .id(Identifier.fromNamespaceAndPath(Pentamana.MOD_NAMESPACE, "server_config"))
        .serializer(config -> GsonConfigSerializerBuilder.create(config)
            .setPath(FabricLoader.getInstance().getConfigDir().resolve("pentamana-extra.json"))
            .build()
        )
        .build();
    @SerialEntry public float enchantmentCapacityBase = 2.0f;
    @SerialEntry public float enchantmentStreamBase = 0.015625f;
    @SerialEntry public float enchantmentManaEfficiencyBase = 0.1f;
    @SerialEntry public float enchantmentPotencyBase = 0.5f;
    @SerialEntry public float statusEffectManaBoostBase = 4.0f;
    @SerialEntry public float statusEffectManaReductionBase = 4.0f;
    @SerialEntry public float statusEffectInstantManaBase = 4.0f;
    @SerialEntry public float statusEffectInstantDepleteBase = 6.0f;
    @SerialEntry public float statusEffectManaPowerBase = 3.0f;
    @SerialEntry public float statusEffectManaSicknessBase = 4.0f;
    @SerialEntry public int statusEffectManaRegenerationBase = 50;
    @SerialEntry public int statusEffectManaInhibitionBase = 40;
    @SerialEntry public boolean shouldConvertExperienceLevel = false;
    @SerialEntry public float experienceLevelConversionBase = 0.5f;
}

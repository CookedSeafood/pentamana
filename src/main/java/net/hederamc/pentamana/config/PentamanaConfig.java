package net.hederamc.pentamana.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.hederamc.pentamana.Pentamana;
import net.minecraft.resources.Identifier;

public final class PentamanaConfig {
    public static ConfigClassHandler<PentamanaConfig> HANDLER = ConfigClassHandler.createBuilder(PentamanaConfig.class)
        .id(Identifier.fromNamespaceAndPath(Pentamana.MOD_NAMESPACE, "server_config"))
        .serializer(config -> GsonConfigSerializerBuilder.create(config)
            .setPath(FabricLoader.getInstance().getConfigDir().resolve("pentamana/server.json"))
            .build()
        )
        .build();
    @SerialEntry public float manaCapacityBase = 20.0f;
    @SerialEntry public float manaRegenerationBase = 0.0625f;
}

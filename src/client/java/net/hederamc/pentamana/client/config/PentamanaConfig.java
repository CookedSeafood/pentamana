package net.hederamc.pentamana.client.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.hederamc.pentamana.Pentamana;
import net.minecraft.resources.Identifier;

public final class PentamanaConfig {
    public static ConfigClassHandler<PentamanaConfig> HANDLER = ConfigClassHandler.createBuilder(PentamanaConfig.class)
        .id(Identifier.fromNamespaceAndPath(Pentamana.MOD_NAMESPACE, "client_config"))
        .serializer(config -> GsonConfigSerializerBuilder.create(config)
            .setPath(FabricLoader.getInstance().getConfigDir().resolve("pentamana/client.json"))
            .build()
        )
        .build();
    @SerialEntry public int manabarMaxStars = 20;
}

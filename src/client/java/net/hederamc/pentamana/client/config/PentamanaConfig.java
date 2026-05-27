package net.hederamc.pentamana.client.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.hederamc.pentamana.Pentamana;
import net.hederamc.pentamana.client.gui.ManaBarAlignment;
import net.hederamc.pentamana.math.Direction2D;
import net.minecraft.resources.Identifier;

public final class PentamanaConfig {
    public static ConfigClassHandler<PentamanaConfig> HANDLER = ConfigClassHandler.createBuilder(PentamanaConfig.class)
            .id(Identifier.fromNamespaceAndPath(Pentamana.MOD_NAMESPACE, "client_config"))
            .serializer(
                    config -> GsonConfigSerializerBuilder.create(config)
                            .setPath(FabricLoader.getInstance().getConfigDir().resolve("pentamana/client.json"))
                            .build()
            )
            .build();
    @SerialEntry public int manaBarOffsetX = 0;
    @SerialEntry public int manaBarOffsetY = -69;
    @SerialEntry public Direction2D manaBarDirection = Direction2D.RIGHT;
    @SerialEntry public ManaBarAlignment manaBarAlignment = ManaBarAlignment.MIDDLE;
    @SerialEntry public int manaBarMaxStars = 20;
}

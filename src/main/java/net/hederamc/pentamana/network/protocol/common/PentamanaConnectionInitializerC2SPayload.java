package net.hederamc.pentamana.network.protocol.common;

import net.hederamc.pentamana.Pentamana;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PentamanaConnectionInitializerC2SPayload() implements CustomPacketPayload {
    public static final Identifier CONNECTION_INITIALIZER_ID = Identifier.fromNamespaceAndPath(Pentamana.MOD_ID, "connection_initializer");
    public static final CustomPacketPayload.Type<PentamanaConnectionInitializerC2SPayload> ID = new CustomPacketPayload.Type<>(CONNECTION_INITIALIZER_ID);
    public static final PentamanaConnectionInitializerC2SPayload INSTANCE = new PentamanaConnectionInitializerC2SPayload();
    public static final StreamCodec<RegistryFriendlyByteBuf, PentamanaConnectionInitializerC2SPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

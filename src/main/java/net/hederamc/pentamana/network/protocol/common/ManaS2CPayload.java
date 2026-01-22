package net.hederamc.pentamana.network.protocol.common;

import net.hederamc.pentamana.Pentamana;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ManaS2CPayload(float mana, float capacity) implements CustomPacketPayload {
    public static final Identifier MANA_PAYLOAD_ID = Identifier.fromNamespaceAndPath(Pentamana.MOD_ID, "mana");
    public static final CustomPacketPayload.Type<ManaS2CPayload> ID = new CustomPacketPayload.Type<>(MANA_PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ManaS2CPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT,
        ManaS2CPayload::mana,
        ByteBufCodecs.FLOAT,
        ManaS2CPayload::capacity,
        ManaS2CPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

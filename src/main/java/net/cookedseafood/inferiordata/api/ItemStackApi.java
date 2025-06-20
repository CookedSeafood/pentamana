package net.cookedseafood.inferiordata.api;

import net.minecraft.nbt.NbtList;

public interface ItemStackApi {
    default String getCustomId() {
        return null;
    }

    default String getCustomIdOrId() {
        return null;
    }

    default NbtList getCustomModifiers() {
        return null;
    }

    default NbtList getCustomStatusEffects() {
        return null;
    }
}

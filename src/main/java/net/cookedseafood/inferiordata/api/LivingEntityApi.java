package net.cookedseafood.inferiordata.api;

import java.util.function.Consumer;
import java.util.function.Function;
import net.cookedseafood.inferiordata.effect.CustomStatusEffect;
import net.cookedseafood.inferiordata.effect.CustomStatusEffectIdentifier;
import net.cookedseafood.inferiordata.effect.CustomStatusEffectPlaylist;
import net.cookedseafood.inferiordata.effect.ServerCustomStatusEffectManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public interface LivingEntityApi {
    default NbtCompound getCustomStatusEffects() {
        return null;
    }

    default void setCustomStatusEffects(NbtCompound customStatusEffects) {
    }

    default ServerCustomStatusEffectManager getCustomStatusEffectManager() {
        return null;
    }

    default boolean hasCustomStatusEffect(CustomStatusEffectIdentifier id) {
        return false;
    }

    default <T> T modifyCustomStatusEffectManager(Function<ServerCustomStatusEffectManager, T> action) {
        return null;
    }

    default void modifyCustomStatusEffectManager(Consumer<ServerCustomStatusEffectManager> action) {
    }

    default boolean addCustomStatusEffect(CustomStatusEffect statusEffect) {
        return false;
    }

    default boolean setCustomStatusEffect(CustomStatusEffect statusEffect) {
        return false;
    }

    default CustomStatusEffectPlaylist removeCustomStatusEffect(CustomStatusEffectIdentifier id) {
        return null;
    }

    default void clearCustomStatusEffect() {
    }

    default void tickCustomStatusEffect() {
    }

    default double getCustomModifiedValue(String attribute, double base) {
        return 0.0d;
    }

    default NbtList getCustomModifiers(String attribute) {
        return null;
    }

    default NbtList getCustomModifiers() {
        return null;
    }
}

package net.cookedseafood.pentamana.api;

import net.minecraft.entity.Entity;

public interface LivingEntityApi {
    default void tickMana() {
    }

    default boolean regenMana(float amount) {
        return false;
    }

    default boolean regenMana() {
        return false;
    }

    default boolean consumMana(float amount) {
        return false;
    }

    default float getMana() {
        return 0f;
    }

    default void setMana(float value) {
    }

    default float getManaCapacity() {
        return 0f;
    }

    default void setManaCapacity(float value) {
    }

    default float getCastingDamageAgainst(Entity entity, float baseDamage) {
        return 0;
    }
}

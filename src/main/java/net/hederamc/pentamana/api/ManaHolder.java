package net.hederamc.pentamana.api;

import net.minecraft.world.entity.Entity;

public interface ManaHolder {
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

    default float getModifiedManaCapacityBase(float base) {
        return 0.0f;
    }

    default float getModifiedManaRegenerationBase(float base) {
        return 0.0f;
    }

    default float getMana() {
        return 0.0f;
    }

    default void setMana(float value) {
    }

    default float getManaCapacity() {
        return 0.0f;
    }

    default void setManaCapacity(float value) {
    }

    default float getCastingDamageAgainst(Entity entity, float baseDamage) {
        return 0.0f;
    }
}

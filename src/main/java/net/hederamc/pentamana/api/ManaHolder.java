package net.hederamc.pentamana.api;

import net.minecraft.world.entity.Entity;

public interface ManaHolder {
    default void tickMana() {
        throw new UnsupportedOperationException();
    }

    default boolean regenMana(float amount) {
        throw new UnsupportedOperationException();
    }

    default boolean regenMana() {
        throw new UnsupportedOperationException();
    }

    default boolean consumeMana(float amount) {
        throw new UnsupportedOperationException();
    }

    default float getModifiedManaCapacityBase(float base) {
        throw new UnsupportedOperationException();
    }

    default float getModifiedManaRegenerationBase(float base) {
        throw new UnsupportedOperationException();
    }

    default float getMana() {
        throw new UnsupportedOperationException();
    }

    default void setMana(float value) {
        throw new UnsupportedOperationException();
    }

    default float getManaCapacity() {
        throw new UnsupportedOperationException();
    }

    default void setManaCapacity(float value) {
        throw new UnsupportedOperationException();
    }

    default float getCastingDamageAgainst(Entity entity, float baseDamage) {
        throw new UnsupportedOperationException();
    }
}

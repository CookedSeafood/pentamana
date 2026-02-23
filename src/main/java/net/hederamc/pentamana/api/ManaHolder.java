package net.hederamc.pentamana.api;

import net.hederamc.fishbonetrehalose.api.CustomDataHolder;
import net.hederamc.pentamana.Pentamana;
import net.hederamc.pentamana.api.event.ManaEvents;
import net.minecraft.nbt.FloatTag;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jspecify.annotations.Nullable;

public interface ManaHolder extends CustomDataHolder {
    default float getMana() {
        return this.getCustomDataOrEmpty().getTag().getFloatOr("mana", 0.0f);
    }

    default void setMana(float value) {
        this.getOrCreateCustomData().getTag().putFloat("mana", value);
    }

    @Nullable
    default FloatTag removeMana() {
        return (FloatTag)this.getCustomDataOrEmpty().getTag().remove("mana");
    }

    default float getManaCapacity() {
        return this.getCustomDataOrEmpty().getTag().getFloatOr("mana_capacity", 0.0f);
    }

    default void setManaCapacity(float value) {
        this.getOrCreateCustomData().getTag().putFloat("mana_capacity", value);
    }

    @Nullable
    default FloatTag removeManaCapacity() {
        return (FloatTag)this.getCustomDataOrEmpty().getTag().remove("mana_capacity");
    }

    default void tickMana() {
        float capacity = this.getModifiedManaCapacityBase(Pentamana.CONFIG.manaCapacityBase);

        if (this.getManaCapacity() != capacity) {
            this.setManaCapacity(capacity);
        }

        float mana = this.getMana();

        if (mana < capacity && mana >= 0.0f) {
            this.regenMana();
        } else if (mana > capacity) {
            this.setMana(capacity);
        } else if (mana < 0) {
            this.setMana(0.0f);
        }
    }

    default boolean regenMana(float amount) {
        float presentedSupply = this.getMana();
        float targetSupply = presentedSupply + amount;
        targetSupply = Math.min(targetSupply, this.getManaCapacity());
        targetSupply = Math.max(targetSupply, 0.0f);

        this.setMana(targetSupply);
        return targetSupply != presentedSupply;
    }

    default boolean regenMana() {
        return this.regenMana(this.getModifiedManaRegenerationBase(Pentamana.CONFIG.manaRegenerationBase));
    }

    default boolean consumeMana(float amount) {
        float targetSupply = this.getMana() - this.getModifiedManaConsumptionAmount(amount);
        if (targetSupply < 0.0f) {
            return false;
        }

        this.setMana(targetSupply);
        return true;
    }

    default float getModifiedManaCapacityBase(float base) {
        MutableFloat capacity = new MutableFloat(base);
        ManaEvents.CALCULATE_CAPACITY.invoker().calculateCapacity(this, capacity);
        return capacity.floatValue();
    }

    default float getModifiedManaRegenerationBase(float base) {
        MutableFloat regeneration = new MutableFloat(base);
        ManaEvents.CALCULATE_REGENERATION.invoker().calculateRegeneration(this, regeneration);
        return regeneration.floatValue();
    }

    default float getModifiedManaConsumptionAmount(float amount) {
        MutableFloat consumption = new MutableFloat(amount);
        ManaEvents.CALCULATE_CONSUMPTION.invoker().calculateConsumption(this, consumption);
        return consumption.floatValue();
    }

    default float getCastingDamageAgainst(Entity entity, float baseDamage) {
        MutableFloat damage = new MutableFloat(baseDamage);
        ManaEvents.CALCULATE_DAMAGE.invoker().calculateDamage(this, entity, damage);
        return this.getManaCapacity() / Pentamana.CONFIG.manaCapacityBase * damage.floatValue();
    }
}

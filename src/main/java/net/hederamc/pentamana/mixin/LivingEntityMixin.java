package net.hederamc.pentamana.mixin;

import java.util.HashMap;
import java.util.Map;
import net.hederamc.pentamana.Pentamana;
import net.hederamc.pentamana.api.ManaHolder;
import net.hederamc.pentamana.api.event.ManaEvents;
import net.hederamc.pentamana.config.PentamanaConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.CustomData;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements ManaHolder {
    @Inject(
        method = "tick()V",
        at = @At("TAIL")
    )
    private void tickMana(CallbackInfo info) {
        this.tickMana();
    }

    @Override
    public void tickMana() {
        float capacity = this.getModifiedManaCapacityBase(Pentamana.CONFIG.manaCapacityBase);

        if (this.getManaCapacity() != capacity) {
            this.setManaCapacity(capacity);
        }

        float supply = this.getMana();

        if (supply < capacity && supply >= 0.0f) {
            this.regenMana();
        } else if (supply > capacity) {
            this.setMana(capacity);
        } else if (supply < 0) {
            this.setMana(0.0f);
        }
    }

    /**
     * Add {@code amount} to mana. Then cap mana at capacity and 0.
     *
     * @param amount of regeneration
     * @return {@code true} if supply changed
     *
     * @see #regenMana()
     */
    @Override
    public boolean regenMana(float amount) {
        float presentedSupply = this.getMana();
        float targetSupply = presentedSupply + amount;
        targetSupply = Math.min(targetSupply, this.getManaCapacity());
        targetSupply = Math.max(targetSupply, 0.0f);

        this.setMana(targetSupply);
        return targetSupply != presentedSupply;
    }

    /**
     * Add modified {@link PentamanaConfig#manaRegenerationBase} to mana.
     * Then cap mana at capacity and 0.
     *
     * @return {@code true} if supply changed
     *
     * @see #regenMana(float)
     */
    @Override
    public boolean regenMana() {
        return this.regenMana(this.getModifiedManaRegenerationBase(Pentamana.CONFIG.manaRegenerationBase));
    }

    /**
     * Substract modified {@code amount} from mana if {@code amount <= mana}.
     *
     * @param amount of consumption
     * @return true if successful
     */
    @Override
    public boolean consumeMana(float amount) {
        MutableFloat consumption = new MutableFloat(amount);
        ManaEvents.CALCULATE_CAPACITY.invoker().calculateCapacity((LivingEntity)(Object)this, consumption);
        float targetSupply = this.getMana() - consumption.floatValue();
        if (targetSupply < 0.0f) {
            return false;
        }

        this.setMana(targetSupply);
        return true;
    }

    @Override
    public float getModifiedManaCapacityBase(float base) {
        MutableFloat capacity = new MutableFloat(base);
        ManaEvents.CALCULATE_CAPACITY.invoker().calculateCapacity((LivingEntity)(Object)this, capacity);
        return capacity.floatValue();
    }

    @Override
    public float getModifiedManaRegenerationBase(float base) {
        MutableFloat regeneration = new MutableFloat(base);
        ManaEvents.CALCULATE_REGENERATION.invoker().calculateRegeneration((LivingEntity)(Object)this, regeneration);
        return regeneration.floatValue();
    }

    @Override
    public float getMana() {
        return ((LivingEntity)(Object)this).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getFloatOr("mana", 0.0f);
    }

    @Override
    public void setMana(float value) {
        ((LivingEntity)(Object)this).setComponent(DataComponents.CUSTOM_DATA, CustomData.of(
            ((LivingEntity)(Object)this).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().merge(
                new CompoundTag(
                    new HashMap<>(
                        Map.<String, Tag>of(
                            "mana",
                            FloatTag.valueOf(value)
                        )
                    )
                )
            )
        ));
    }

    @Override
    public float getManaCapacity() {
        return ((LivingEntity)(Object)this).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getFloatOr("mana_capacity", 0.0f);
    }

    @Override
    public void setManaCapacity(float value) {
        ((LivingEntity)(Object)this).setComponent(DataComponents.CUSTOM_DATA, CustomData.of(
            ((LivingEntity)(Object)this).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().merge(
                new CompoundTag(
                    new HashMap<>(
                        Map.<String, Tag>of(
                            "mana_capacity",
                            FloatTag.valueOf(value)
                        )
                    )
                )
            )
        ));
    }

    @Override
    public float getCastingDamageAgainst(Entity entity, float baseDamage) {
        MutableFloat damage = new MutableFloat(baseDamage);
        ManaEvents.CALCULATE_DAMAGE.invoker().calculateDamage((LivingEntity)(Object)this, damage);
        return this.getManaCapacity() / Pentamana.CONFIG.manaCapacityBase * damage.floatValue();
    }
}

package net.hederamc.pentamana.mixin;

import java.util.HashMap;
import java.util.Map;
import net.hederamc.gcd.effect.CustomStatusEffectManager;
import net.hederamc.pentamana.api.LivingEntityApi;
import net.hederamc.pentamana.api.event.ConsumManaCallback;
import net.hederamc.pentamana.api.event.RegenManaCallback;
import net.hederamc.pentamana.api.event.TickManaCallback;
import net.hederamc.pentamana.attribute.PentamanaAttributeIdentifiers;
import net.hederamc.pentamana.data.PentamanaConfig;
import net.hederamc.pentamana.effect.PentamanaStatusEffectIdentifiers;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.server.network.ServerPlayerEntity;

import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements LivingEntityApi {
    @Inject(
        method = "tick()V",
        at = @At("RETURN")
    )
    private void tickMana(CallbackInfo info) {
        this.tickMana();
    }

    @Override
    public void tickMana() {
        LivingEntity livingEntity = (LivingEntity)(Object)this;
        TickManaCallback.EVENT.invoker().interact(livingEntity);
        boolean isValueChanged = false;

        float capacity = this.getModifiedManaCapacityBase(PentamanaConfig.manaCapacityBase);

        if (this.getManaCapacity() != capacity) {
            this.setManaCapacity(capacity);
            isValueChanged = true;
        }

        float supply = this.getMana();

        if (supply < capacity && supply >= 0.0f) {
            isValueChanged |= this.regenMana();
        } else if (supply > capacity) {
            this.setMana(capacity);
            isValueChanged = true;
        } else if (supply < 0) {
            this.setMana(0.0f);
            isValueChanged = true;
        }

        if (livingEntity instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity)livingEntity;
            if (player.isManaBarDisplayOutdate(isValueChanged)) {
                player.putManaBarDisplay();
            }
        }
    }

    /**
     * Add {@code amount} to supply, and cap supply at capacity and 0.
     * 
     * @param amount regeneration amount
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
     * Add {@link PentamanaConfig#manaRegenerationBase} to supply with custom modifiers
     * and enchantments applied, and cap supply at capacity and 0.
     * 
     * @return {@code true} if supply changed
     * 
     * @see #regenMana(float)
     */
    @Override
    public boolean regenMana() {
        RegenManaCallback.EVENT.invoker().interact((LivingEntity)(Object)this);
        return this.regenMana(this.getModifiedManaRegenerationBase(PentamanaConfig.manaRegenerationBase));
    }

    /**
     * Substract {@code amount} from supply with custom modifiers and enchantments
     * applied if supply >= {@code consum}.
     * 
     * @param amount consumption amount
     * @return true if successful
     */
    @Override
    public boolean consumMana(float amount) {
        LivingEntity livingEntity = (LivingEntity)(Object)this;
        ConsumManaCallback.EVENT.invoker().interact(livingEntity);

        MutableFloat consum = new MutableFloat((float)livingEntity.getCustomModifiedValue(PentamanaAttributeIdentifiers.MANA_CONSUMPTION, amount));
        livingEntity.getEnchantments(Enchantments.MANA_EFFICIENCY).forEach(entry -> consum.setValue(consum.floatValue() * (1 - PentamanaConfig.enchantmentManaEfficiencyBase * (entry.getIntValue() + 1))));
        float targetSupply = this.getMana() - consum.floatValue();
        if (targetSupply < 0.0f) {
            return false;
        }

        this.setMana(targetSupply);
        return true;
    }

    @Override
    public float getModifiedManaCapacityBase(float base) {
        LivingEntity livingEntity = (LivingEntity)(Object)this;
        CustomStatusEffectManager statusEffectManager = livingEntity.getCustomStatusEffectManager();

        MutableFloat capacity = new MutableFloat((float)livingEntity.getCustomModifiedValue(PentamanaAttributeIdentifiers.MANA_CAPACITY, base));
        livingEntity.getEnchantments(Enchantments.CAPACITY).forEach(entry -> capacity.setValue(capacity.floatValue() + PentamanaConfig.enchantmentCapacityBase * (entry.getIntValue() + 1)));
        return Math.max(
            capacity.floatValue()
                + (statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_BOOST) ? PentamanaConfig.statusEffectManaBoostBase * (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_BOOST) + 1) : 0)
                - (statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_REDUCTION) ? PentamanaConfig.statusEffectManaReductionBase * (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_REDUCTION) + 1) : 0)
                + (PentamanaConfig.shouldConvertExperienceLevel && livingEntity instanceof ServerPlayerEntity ? PentamanaConfig.experienceLevelConversionBase * ((ServerPlayerEntity)livingEntity).experienceLevel : 0),
            0.0f
        );
    }

    @Override
    public float getModifiedManaRegenerationBase(float base) {
        LivingEntity livingEntity = (LivingEntity)(Object)this;
        CustomStatusEffectManager statusEffectManager = livingEntity.getCustomStatusEffectManager();

        MutableFloat regen = new MutableFloat((float)livingEntity.getCustomModifiedValue(PentamanaAttributeIdentifiers.MANA_REGENERATION, base));
        livingEntity.getEnchantments(Enchantments.STREAM).forEach(entry -> regen.setValue(regen.floatValue() + PentamanaConfig.enchantmentStreamBase * (entry.getIntValue() + 1)));
        return regen.floatValue()
            + (statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.INSTANT_MANA) ? PentamanaConfig.statusEffectInstantManaBase * (float)Math.pow(2.0, statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.INSTANT_MANA)) : 0)
            - (statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.INSTANT_DEPLETE) ? PentamanaConfig.statusEffectInstantDepleteBase * (float)Math.pow(2.0, statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.INSTANT_DEPLETE)) : 0)
            + (statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_REGENERATION) ? PentamanaConfig.manaPerPoint / Math.max(1, PentamanaConfig.statusEffectManaRegenerationBase >> statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_REGENERATION)) : 0)
            - (statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_INHIBITION) ? PentamanaConfig.manaPerPoint / Math.max(1, PentamanaConfig.statusEffectManaInhibitionBase >> statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_INHIBITION)) : 0);
    }

    @Override
    public float getMana() {
        return ((LivingEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getFloat("mana", 0f);
    }

    @Override
    public void setMana(float value) {
        ((LivingEntity)(Object)this).setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(
            ((LivingEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().copyFrom(
                new NbtCompound(
                    new HashMap<>(
                        Map.<String, NbtElement>of(
                            "mana",
                            NbtFloat.of(value)
                        )
                    )
                )
            )
        ));
    }

    @Override
    public float getManaCapacity() {
        return ((LivingEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getFloat("mana_capacity", 0f);
    }

    @Override
    public void setManaCapacity(float value) {
        ((LivingEntity)(Object)this).setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(
            ((LivingEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().copyFrom(
                new NbtCompound(
                    new HashMap<>(
                        Map.<String, NbtElement>of(
                            "mana_capacity",
                            NbtFloat.of(value)
                        )
                    )
                )
            )
        ));
    }

    @Override
    public float getCastingDamageAgainst(Entity entity, float baseDamage) {
        LivingEntity livingEntity = (LivingEntity)(Object)this;
        CustomStatusEffectManager statusEffectManager = livingEntity.getCustomStatusEffectManager();

        MutableFloat damage = new MutableFloat(this.getManaCapacity() / PentamanaConfig.manaCapacityBase * (float)livingEntity.getCustomModifiedValue(PentamanaAttributeIdentifiers.CASTING_DAMAGE, baseDamage));
        livingEntity.getEnchantments(Enchantments.POTENCY).forEach(entry -> damage.setValue(damage.floatValue() + PentamanaConfig.enchantmentPotencyBase * (entry.getIntValue() + 1)));
        return Math.max(
            (
                damage.floatValue()
                    + (statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_POWER) ? (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_POWER) + 1) * PentamanaConfig.statusEffectManaPowerBase : 0)
                    - (statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_SICKNESS) ? (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_SICKNESS) + 1) * PentamanaConfig.statusEffectManaSicknessBase : 0)
            )
            * (entity instanceof WitchEntity ? 0.15f : 1),
            0.0f
        );
    }
}

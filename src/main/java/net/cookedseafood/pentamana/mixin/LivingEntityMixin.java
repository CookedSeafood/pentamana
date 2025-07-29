package net.cookedseafood.pentamana.mixin;

import java.util.HashMap;
import java.util.Map;
import net.cookedseafood.generalcustomdata.effect.CustomStatusEffectManager;
import net.cookedseafood.pentamana.api.LivingEntityApi;
import net.cookedseafood.pentamana.api.event.ConsumManaCallback;
import net.cookedseafood.pentamana.api.event.RegenManaCallback;
import net.cookedseafood.pentamana.api.event.TickManaCallback;
import net.cookedseafood.pentamana.attribute.PentamanaAttributeIdentifiers;
import net.cookedseafood.pentamana.data.PentamanaConfig;
import net.cookedseafood.pentamana.effect.PentamanaStatusEffectIdentifiers;
import net.cookedseafood.pentamana.enchantment.PentamanaEnchantmentIdentifiers;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.server.network.ServerPlayerEntity;
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
        LivingEntity entity = (LivingEntity)(Object)this;
        TickManaCallback.EVENT.invoker().interact(entity);
        CustomStatusEffectManager statusEffectManager = entity.getCustomStatusEffectManager();
        boolean isValueChanged = false;

        float capacity = (float)entity.getCustomModifiedValue(PentamanaAttributeIdentifiers.MANA_CAPACITY, PentamanaConfig.manaCapacityBase);
        capacity += PentamanaConfig.enchantmentCapacityBase * entity.getWeaponStack().getEnchantments().getLevel(PentamanaEnchantmentIdentifiers.CAPACITY);
        capacity += statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_BOOST) ? PentamanaConfig.statusEffectManaBoostBase * (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_BOOST) + 1) : 0;
        capacity -= statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_REDUCTION) ? PentamanaConfig.statusEffectManaReductionBase * (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_REDUCTION) + 1) : 0;
        capacity += PentamanaConfig.shouldConvertExperienceLevel && entity instanceof ServerPlayerEntity ? PentamanaConfig.experienceLevelConversionBase * ((ServerPlayerEntity)entity).experienceLevel : 0;
        capacity = Math.max(capacity, 0.0f);

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

        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity)entity;
            if (player.isManaBarDisplayOutdate(isValueChanged)) {
                player.putManaBarDisplay();
            }
        }
    }

    /**
     * Add {@code amount} to supply and cap it at capacity and 0.
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
     * and enchantments applied.
     * 
     * @return {@code true} if supply changed
     * 
     * @see #regenMana(float)
     */
    @Override
    public boolean regenMana() {
        LivingEntity entity = (LivingEntity)(Object)this;
        RegenManaCallback.EVENT.invoker().interact(entity);
        CustomStatusEffectManager statusEffectManager = entity.getCustomStatusEffectManager();

        float regen = (float)entity.getCustomModifiedValue(PentamanaAttributeIdentifiers.MANA_REGENERATION, PentamanaConfig.manaRegenerationBase);
        regen += PentamanaConfig.enchantmentStreamBase * entity.getWeaponStack().getEnchantments().getLevel(PentamanaEnchantmentIdentifiers.STREAM);
        regen += statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.INSTANT_MANA) ? PentamanaConfig.statusEffectInstantManaBase * Math.pow(2, statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.INSTANT_MANA)) : 0;
        regen -= statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.INSTANT_DEPLETE) ? PentamanaConfig.statusEffectInstantDepleteBase * Math.pow(2, statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.INSTANT_DEPLETE)) : 0;
        regen += statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_REGENERATION) ? PentamanaConfig.manaPerPoint / (float)Math.max(1, PentamanaConfig.statusEffectManaRegenerationBase >> statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_REGENERATION)) : 0;
        regen -= statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_INHIBITION) ? PentamanaConfig.manaPerPoint / (float)Math.max(1, PentamanaConfig.statusEffectManaInhibitionBase >> statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_INHIBITION)) : 0;

        return this.regenMana(regen);
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
        LivingEntity entity = (LivingEntity)(Object)this;
        ConsumManaCallback.EVENT.invoker().interact(entity);

        float targetConsum = (float)entity.getCustomModifiedValue(PentamanaAttributeIdentifiers.MANA_CONSUMPTION, amount);
        targetConsum *= 1 - PentamanaConfig.enchantmentManaEfficiencyBase * entity.getWeaponStack().getEnchantments().getLevel(PentamanaEnchantmentIdentifiers.MANA_EFFICIENCY);

        float targetSupply = this.getMana() - targetConsum;
        if (targetSupply < 0.0f) {
            return false;
        }

        this.setMana(targetSupply);
        return true;
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

        float manaCapacity = livingEntity.getManaCapacity();
        int potencyLevel = livingEntity.getWeaponStack().getEnchantments().getLevel(PentamanaEnchantmentIdentifiers.POTENCY);

        float castingDamage = manaCapacity;
        castingDamage /= PentamanaConfig.manaCapacityBase;
        castingDamage *= (float)livingEntity.getCustomModifiedValue(PentamanaAttributeIdentifiers.CASTING_DAMAGE, baseDamage);
        castingDamage += potencyLevel != 0 ? ++potencyLevel * PentamanaConfig.enchantmentPotencyBase / Integer.MAX_VALUE : 0;
        castingDamage += statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_POWER) ? (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_POWER) + 1) * PentamanaConfig.statusEffectManaPowerBase : 0;
        castingDamage -= statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_SICKNESS) ? (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_SICKNESS) + 1) * PentamanaConfig.statusEffectManaSicknessBase : 0;
        castingDamage = Math.max(castingDamage, 0.0f);
        castingDamage *= entity instanceof WitchEntity ? 0.15f : 1;
        return castingDamage;
    }
}

package net.hederamc.pentamana.mixin;

import java.util.HashMap;
import java.util.Map;
import net.hederamc.generalcustomdata.effect.CustomStatusEffectManager;
import net.hederamc.pentamana.api.ManaHolder;
import net.hederamc.pentamana.attribute.PentamanaAttributeIdentifiers;
import net.hederamc.pentamana.config.PentamanaConfig;
import net.hederamc.pentamana.effect.PentamanaStatusEffectIdentifiers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantments;
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
        float capacity = this.getModifiedManaCapacityBase(PentamanaConfig.HANDLER.instance().manaCapacityBase);

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
     * Add {@link PentamanaConfig.HANDLER.instance()#manaRegenerationBase} to supply with custom modifiers
     * and enchantments applied, and cap supply at capacity and 0.
     *
     * @return {@code true} if supply changed
     *
     * @see #regenMana(float)
     */
    @Override
    public boolean regenMana() {
        return this.regenMana(this.getModifiedManaRegenerationBase(PentamanaConfig.HANDLER.instance().manaRegenerationBase));
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

        MutableFloat consum = new MutableFloat((float)livingEntity.getCustomModifiedValue(PentamanaAttributeIdentifiers.MANA_CONSUMPTION, amount));
        livingEntity.getEnchantments(Enchantments.MANA_EFFICIENCY).forEach(entry -> consum.setValue(consum.floatValue() * (1 - PentamanaConfig.HANDLER.instance().enchantmentManaEfficiencyBase * (entry.getIntValue() + 1))));
        float targetSupply = this.getMana() - consum.floatValue();
        if (targetSupply < 0.0f) {
            return false;
        }

        this.setMana(targetSupply);
        return true;
    }

    @Override
    public float getModifiedManaCapacityBase(float base) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        LivingEntity livingEntity = (LivingEntity)(Object)this;
        CustomStatusEffectManager statusEffectManager = livingEntity.getCustomStatusEffectManager();

        MutableFloat capacity = new MutableFloat((float)livingEntity.getCustomModifiedValue(PentamanaAttributeIdentifiers.MANA_CAPACITY, base));
        livingEntity.getEnchantments(Enchantments.CAPACITY).forEach(entry -> capacity.setValue(capacity.floatValue() + config.enchantmentCapacityBase * (entry.getIntValue() + 1)));
        return Math.max(
            capacity.floatValue()
                + (statusEffectManager.contains(PentamanaStatusEffectIdentifiers.MANA_BOOST) ? config.statusEffectManaBoostBase * (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_BOOST) + 1) : 0)
                - (statusEffectManager.contains(PentamanaStatusEffectIdentifiers.MANA_REDUCTION) ? config.statusEffectManaReductionBase * (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_REDUCTION) + 1) : 0)
                + (config.shouldConvertExperienceLevel && livingEntity instanceof ServerPlayer ? config.experienceLevelConversionBase * ((ServerPlayer)livingEntity).experienceLevel : 0),
            0.0f
        );
    }

    @Override
    public float getModifiedManaRegenerationBase(float base) {
        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        LivingEntity livingEntity = (LivingEntity)(Object)this;
        CustomStatusEffectManager statusEffectManager = livingEntity.getCustomStatusEffectManager();

        MutableFloat regen = new MutableFloat((float)livingEntity.getCustomModifiedValue(PentamanaAttributeIdentifiers.MANA_REGENERATION, base));
        livingEntity.getEnchantments(Enchantments.STREAM).forEach(entry -> regen.setValue(regen.floatValue() + config.enchantmentStreamBase * (entry.getIntValue() + 1)));
        return regen.floatValue()
            + (statusEffectManager.contains(PentamanaStatusEffectIdentifiers.INSTANT_MANA) ? config.statusEffectInstantManaBase * (float)Math.pow(2.0, statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.INSTANT_MANA)) : 0)
            - (statusEffectManager.contains(PentamanaStatusEffectIdentifiers.INSTANT_DEPLETE) ? config.statusEffectInstantDepleteBase * (float)Math.pow(2.0, statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.INSTANT_DEPLETE)) : 0)
            + (statusEffectManager.contains(PentamanaStatusEffectIdentifiers.MANA_REGENERATION) ? 1.0f / Math.max(1, config.statusEffectManaRegenerationBase >> statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_REGENERATION)) : 0)
            - (statusEffectManager.contains(PentamanaStatusEffectIdentifiers.MANA_INHIBITION) ? 1.0f / Math.max(1, config.statusEffectManaInhibitionBase >> statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_INHIBITION)) : 0);
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
        LivingEntity livingEntity = (LivingEntity)(Object)this;
        CustomStatusEffectManager statusEffectManager = livingEntity.getCustomStatusEffectManager();

        MutableFloat damage = new MutableFloat(this.getManaCapacity() / PentamanaConfig.HANDLER.instance().manaCapacityBase * (float)livingEntity.getCustomModifiedValue(PentamanaAttributeIdentifiers.CASTING_DAMAGE, baseDamage));
        livingEntity.getEnchantments(Enchantments.POTENCY).forEach(entry -> damage.setValue(damage.floatValue() + PentamanaConfig.HANDLER.instance().enchantmentPotencyBase * (entry.getIntValue() + 1)));
        return Math.max(
            (
                damage.floatValue()
                    + (statusEffectManager.contains(PentamanaStatusEffectIdentifiers.MANA_POWER) ? (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_POWER) + 1) * PentamanaConfig.HANDLER.instance().statusEffectManaPowerBase : 0)
                    - (statusEffectManager.contains(PentamanaStatusEffectIdentifiers.MANA_SICKNESS) ? (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_SICKNESS) + 1) * PentamanaConfig.HANDLER.instance().statusEffectManaSicknessBase : 0)
            )
            * (entity instanceof Witch ? 0.15f : 1),
            0.0f
        );
    }
}

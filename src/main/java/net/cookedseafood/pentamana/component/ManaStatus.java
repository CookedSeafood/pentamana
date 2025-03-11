package net.cookedseafood.pentamana.component;

import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.api.component.ManaStatusComponent;
import net.cookedseafood.pentamana.api.event.ConsumManaCallback;
import net.cookedseafood.pentamana.api.event.RegenManaCallback;
import net.cookedseafood.pentamana.api.event.TickManaCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnableComponent;

public class ManaStatus implements ManaStatusComponent, EntityComponentInitializer, RespawnableComponent<ManaStatus> {
    public static final ComponentKey<ManaStatus> MANA_STATUS =
        ComponentRegistry.getOrCreate(Identifier.of(Pentamana.MOD_ID, "mana_status"), ManaStatus.class);
    private float manaSupply;
    private float manaCapacity;

    public ManaStatus() {
    }

    @Override
    public float tick(PlayerEntity player) {
        ManaStatusEffect manaStatusEffect = ManaStatusEffect.MANA_STATUS_EFFECT.get(player);

        float manaCapacity = (float)player.getCustomModifiedValue("pentamana:mana_capacity", Pentamana.manaCapacityBase);
        manaCapacity += Pentamana.enchantmentCapacityBase * player.getWeaponStack().getEnchantments().getLevel("pentamana:capacity");
        manaCapacity += manaStatusEffect.hasStatusEffect("pentamana:mana_boost") ?
            Pentamana.statusEffectManaBoostBase * (manaStatusEffect.getActiveStatusEffectAmplifier("pentamana:mana_boost") + 1) :
            0;
        manaCapacity -= manaStatusEffect.hasStatusEffect("pentamana:mana_reduction") ?
            Pentamana.statusEffectManaReductionBase * (manaStatusEffect.getActiveStatusEffectAmplifier("pentamana:mana_reduction") + 1) :
            0;
        manaCapacity = Math.max(manaCapacity, 0.0f);

        TickManaCallback.EVENT.invoker().interact(player);

        this.setManaCapacity(manaCapacity);

        if (this.manaSupply == this.manaCapacity) {
            return 0.0f;
        }

		if (this.manaSupply < this.manaCapacity && this.manaSupply >= 0.0f) {
			return this.regen(player);
		}

        if (this.manaSupply > this.manaCapacity) {
            return this.setManaSupply(this.manaCapacity);
        }

        if (this.manaSupply < 0) {
            return this.setManaSupply(0.0f);
        }

		return 0.0f;
    }

    @Override
    public float regen(PlayerEntity player) {
        ManaStatusEffect manaStatusEffect = ManaStatusEffect.MANA_STATUS_EFFECT.get(player);

        float manaRegen = (float)player.getCustomModifiedValue("pentamana:mana_regeneration", Pentamana.manaRegenBase);
        manaRegen += Pentamana.enchantmentStreamBase * player.getWeaponStack().getEnchantments().getLevel("pentamana:stream");
        manaRegen += manaStatusEffect.hasStatusEffect("pentamana:instant_mana") ? Pentamana.statusEffectInstantManaBase * Math.pow(2, manaStatusEffect.getActiveStatusEffectAmplifier("pentamana:instant_mana")) : 0;
        manaRegen -= manaStatusEffect.hasStatusEffect("pentamana:instant_deplete") ? Pentamana.statusEffectInstantDepleteBase * Math.pow(2, manaStatusEffect.getActiveStatusEffectAmplifier("pentamana:instant_deplete")) : 0;
        manaRegen += manaStatusEffect.hasStatusEffect("pentamana:mana_regeneration") ? Pentamana.manaPerPoint / (float)Math.max(1, Pentamana.statusEffectManaRegenBase >> manaStatusEffect.getActiveStatusEffectAmplifier("pentamana:mana_regeneration")) : 0;
        manaRegen -= manaStatusEffect.hasStatusEffect("pentamana:mana_inhibition") ? Pentamana.manaPerPoint / (float)Math.max(1, Pentamana.statusEffectManaInhibitionBase >> manaStatusEffect.getActiveStatusEffectAmplifier("pentamana:mana_inhibition")) : 0;

        RegenManaCallback.EVENT.invoker().interact(player);

        return regen(player, manaRegen);
    }

    @Override
    public float regen(PlayerEntity player, float manaRegen) {
        float targetManaSupply = this.manaSupply + manaRegen;
        targetManaSupply = Math.min(targetManaSupply, manaCapacity);
        targetManaSupply = Math.max(targetManaSupply, 0.0f);
        return this.setManaSupply(targetManaSupply);
    }

    @Override
    public float consum(PlayerEntity player, float manaConsume) {
        float targetManaConsume = (float)player.getCustomModifiedValue("pentamana:mana_consumption", manaConsume);
        targetManaConsume *= 1 - Pentamana.enchantmentUtilizationBase * player.getWeaponStack().getEnchantments().getLevel("pentamana:utilization");

        ConsumManaCallback.EVENT.invoker().interact(player);

        float targetManaSupply = this.manaSupply - targetManaConsume;
		if (targetManaSupply > 0.0f) {
			return this.setManaSupply(targetManaSupply);
		}

        return 0.0f;
    }

    @Override
    public float getManaSupply() {
        return manaSupply;
    }

    @Override
    public float setManaSupply(float manaSupply) {
        return this.manaSupply = manaSupply;
    }

    @Override
    public float getManaCapacity() {
        return manaCapacity;
    }

    @Override
    public float setManaCapacity(float manaCapacity) {
        return this.manaCapacity = manaCapacity;
    }

    @Override
    public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        this.manaSupply = nbtCompound.getFloat("manaSupply");
        this.manaCapacity = nbtCompound.getFloat("manaCapacity");
    }

    @Override
    public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        nbtCompound.putFloat("manaSupply", manaSupply);
        nbtCompound.putFloat("manaCapacity", manaCapacity);
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(MANA_STATUS, player -> new ManaStatus());
    }
}

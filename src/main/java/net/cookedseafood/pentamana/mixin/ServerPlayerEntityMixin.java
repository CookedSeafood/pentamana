package net.cookedseafood.pentamana.mixin;

import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.api.ServerPlayerEntityApi;
import net.cookedseafood.pentamana.component.ManaDisplay;
import net.cookedseafood.pentamana.component.ManaPreference;
import net.cookedseafood.pentamana.component.ManaStatus;
import net.cookedseafood.pentamana.component.ManaStatusEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements ServerPlayerEntityApi {
	@Inject(
		method = "tick()V",
		at = @At("RETURN")
	)
	private void tickMana(CallbackInfo info) {
		ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
		ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);

        if (manaPreference.getEnabled() == false && !Pentamana.isForceEnabled) {
            return;
        }

		ManaStatusEffect.MANA_STATUS_EFFECT.get(player).tick();
		ManaStatus.MANA_STATUS.get(player).tick(player);
		ManaDisplay.MANA_DISPLAY.get(player).tick(player);
	}

	@Inject(
		method = "consumeItem()V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/item/ItemStack;isEmpty()Z",
			shift = At.Shift.AFTER
		)
	)
	private void applyManaStatusEffects(CallbackInfo info) {
		ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
		ManaStatusEffect manaStatusEffect = ManaStatusEffect.MANA_STATUS_EFFECT.get(player);

		NbtList presentedManaStatusEffects = ((ServerPlayerEntity)(Object)this).getActiveItem().getCustomStatusEffects();
		presentedManaStatusEffects.stream()
			.map(NbtCompound.class::cast)
			.forEach(statusEffect -> manaStatusEffect.addStatusEffect(statusEffect.getString("id"), statusEffect.getInt("duration"), statusEffect.getInt("amplifier")));
	}

	@Override
	public float getCastingDamageAgainst(Entity entity, float baseDamage) {
		ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
		ManaStatusEffect manaStatusEffect = ManaStatusEffect.MANA_STATUS_EFFECT.get(player);

		float manaCapacity = ManaStatus.MANA_STATUS.get(player).getManaCapacity();
		int potencyLevel = player.getWeaponStack().getEnchantments().getLevel("pentamana:potency");

		float castingDamage = manaCapacity;
		castingDamage /= Pentamana.manaCapacityBase;
		castingDamage *= (float)player.getCustomModifiedValue("pentamana:casting_damage", baseDamage);
		castingDamage += potencyLevel != 0 ?
			++potencyLevel * Pentamana.enchantmentPotencyBase / Integer.MAX_VALUE :
			0;
		castingDamage += manaStatusEffect.hasStatusEffect("pentamana:mana_power") ?
			(manaStatusEffect.getActiveStatusEffectAmplifier("pentamana:mana_power") + 1) * Pentamana.statusEffectManaPowerBase :
			0;
		castingDamage -= manaStatusEffect.hasStatusEffect("pentamana:mana_sickness") ?
			(manaStatusEffect.getActiveStatusEffectAmplifier("pentamana:mana_sickness") + 1) * Pentamana.statusEffectManaSicknessBase :
			0;
		castingDamage = Math.max(castingDamage, 0.0f);
		castingDamage *= entity instanceof WitchEntity ? 0.15f : 1;
		return castingDamage;
	}

	@Shadow
	public abstract ServerCommandSource getCommandSource();
}

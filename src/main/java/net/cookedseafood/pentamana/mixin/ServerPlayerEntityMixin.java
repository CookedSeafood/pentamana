package net.cookedseafood.pentamana.mixin;

import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.api.ServerPlayerEntityApi;
import net.cookedseafood.pentamana.component.ManaPreferenceComponentImpl;
import net.cookedseafood.pentamana.component.ManaStatusEffectManagerComponentImpl;
import net.cookedseafood.pentamana.component.ServerManaBarComponentImpl;
import net.cookedseafood.pentamana.mana.ManaStatusEffectManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.nbt.NbtCompound;
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

        if (ManaPreferenceComponentImpl.MANA_PREFERENCE.get(player).isEnabled() == false && !Pentamana.isForceEnabled) {
            return;
        }

		ManaStatusEffectManagerComponentImpl.MANA_STATUS_EFFECT.get(player).getStatusEffectManager().tick();;
		ServerManaBarComponentImpl.SERVER_MANA_BAR.get(player).getServerManaBar().tick(player);
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
		ManaStatusEffectManager statusEffectManager = ManaStatusEffectManagerComponentImpl.MANA_STATUS_EFFECT.get((ServerPlayerEntity)(Object)this).getStatusEffectManager();
		((ServerPlayerEntity)(Object)this).getActiveItem().getCustomStatusEffects().stream()
			.map(NbtCompound.class::cast)
			.forEach(presentedStatusEffect -> statusEffectManager.add(presentedStatusEffect.getString("id"), presentedStatusEffect.getInt("duration"), presentedStatusEffect.getInt("amplifier")));
	}

	@Override
	public float getCastingDamageAgainst(Entity entity, float baseDamage) {
		ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
		ManaStatusEffectManager statusEffectManager = ManaStatusEffectManagerComponentImpl.MANA_STATUS_EFFECT.get(player).getStatusEffectManager();

		float manaCapacity = ServerManaBarComponentImpl.SERVER_MANA_BAR.get(player).getServerManaBar().getCapacity();
		int potencyLevel = player.getWeaponStack().getEnchantments().getLevel("pentamana:potency");

		float castingDamage = manaCapacity;
		castingDamage /= Pentamana.manaCapacityBase;
		castingDamage *= (float)player.getCustomModifiedValue("pentamana:casting_damage", baseDamage);
		castingDamage += potencyLevel != 0 ? ++potencyLevel * Pentamana.enchantmentPotencyBase / Integer.MAX_VALUE : 0;
		castingDamage += statusEffectManager.has("pentamana:mana_power") ? (statusEffectManager.getActiveStatusEffectAmplifier("pentamana:mana_power") + 1) * Pentamana.statusEffectManaPowerBase : 0;
		castingDamage -= statusEffectManager.has("pentamana:mana_sickness") ? (statusEffectManager.getActiveStatusEffectAmplifier("pentamana:mana_sickness") + 1) * Pentamana.statusEffectManaSicknessBase : 0;
		castingDamage = Math.max(castingDamage, 0.0f);
		castingDamage *= entity instanceof WitchEntity ? 0.15f : 1;
		return castingDamage;
	}

	@Shadow
	public abstract ServerCommandSource getCommandSource();
}

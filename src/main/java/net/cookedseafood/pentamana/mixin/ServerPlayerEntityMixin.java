package net.cookedseafood.pentamana.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.api.ServerPlayerEntityApi;
import net.cookedseafood.pentamana.command.ManaCommand;
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
		try {
			ManaCommand.executeTick(this.getCommandSource());
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}
	}

	@Inject(
		method = "consumeItem()V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/item/ItemStack;isEmpty()Z",
			shift = At.Shift.AFTER
		)
	)
	private void applyCustomStatusEffects(CallbackInfo info) {
		NbtList statusEffects = ((ServerPlayerEntity)(Object)this).getActiveItem().getCustomStatusEffects();
		statusEffects.stream()
			.map(nbtElement -> (NbtCompound)nbtElement)
			.forEach(statusEffect -> {
				((ServerPlayerEntity)(Object)this).addCustomStatusEffect(statusEffect.getString("id"), statusEffect.getInt("duration"), statusEffect.getInt("amplifier"));
			});
	}

	@Override
	public float getCastingDamageAgainst(Entity entity, float baseDamage) {
		ServerCommandSource source = this.getCommandSource();
		int potencyLevel = ((ServerPlayerEntity)(Object)this).getWeaponStack().getEnchantments().getLevel("pentamana:potency");
		int manaCapacity = Pentamana.manaCapacityBase;
		try {
			manaCapacity = ManaCommand.executeGetManaCapacity(source);
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}

		float castingDamage = manaCapacity;
		castingDamage /= Pentamana.manaCapacityBase;
		castingDamage *= (float)((ServerPlayerEntity)(Object)this).getCustomModifiedValue("pentamana:casting_damage", baseDamage);
		castingDamage += potencyLevel != 0 ? ++potencyLevel * (float)Pentamana.enchantmentPotencyBase / Integer.MAX_VALUE : 0;
		castingDamage += ((ServerPlayerEntity)(Object)this).hasCustomStatusEffect("pentamana:mana_power") ? (((ServerPlayerEntity)(Object)this).getActiveCustomStatusEffect("pentamana:mana_power").getInt("amplifier") + 1) * Pentamana.statusEffectManaPowerBase : 0;
		castingDamage -= ((ServerPlayerEntity)(Object)this).hasCustomStatusEffect("pentamana:mana_sickness") ? (((ServerPlayerEntity)(Object)this).getActiveCustomStatusEffect("pentamana:mana_sickness").getInt("amplifier") + 1) * Pentamana.statusEffectManaSicknessBase : 0;
		castingDamage = Math.max(castingDamage, 0);
		castingDamage *= entity instanceof WitchEntity ? (float)0.15 : 1;
		return castingDamage;
	}

	@Shadow
	public abstract ServerCommandSource getCommandSource();
}

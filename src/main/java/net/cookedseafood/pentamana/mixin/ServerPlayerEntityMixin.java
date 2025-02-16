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
			.filter(statusEffect -> "pentamana:instant_mana".equals(statusEffect.getString("id")))
			.forEach(statusEffect -> {
				try {
					ManaCommand.executeIncrementMana(this.getCommandSource(), (int)Math.pow(2, statusEffect.getInt("amplifier") + 3) * Pentamana.manaPerPoint);
					ManaCommand.executeIncrementManaSicknessAmplifier(getCommandSource());
					ManaCommand.executeSetManaSicknessDuration(getCommandSource(), 100);
				} catch (CommandSyntaxException e) {
					e.printStackTrace();
				}
			});
	}

	@Override
	public float getCastingDamageAgainst(Entity entity, float baseDamage) {
		ServerCommandSource source = this.getCommandSource();
		int potencyLevel = ((ServerPlayerEntity)(Object)this).getWeaponStack().getEnchantments().getLevel("pentamana:potency");
		try {
			return Math.max(0, ((float)ManaCommand.executeGetManaCapacity(source) / Pentamana.manaCapacityBase * (float)((ServerPlayerEntity)(Object)this).getCustomModifiedValue("pentamana:casting_damage", baseDamage) + potencyLevel == 0 ? 0 : ++potencyLevel * (float)0.5 + ManaCommand.executeGetManaPowerDuration(source) == 0 ? 0 : (ManaCommand.executeGetManaPowerAmplifier(source) + 1) * 3 - ManaCommand.executeGetManaSicknessDuration(source) == 0 ? 0 : (ManaCommand.executeGetManaSicknessAmplifier(source) + 1) * 4)) * (entity instanceof WitchEntity ? (float)0.15 : 1);
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}

		return 0;
	}

	@Shadow
	public abstract ServerCommandSource getCommandSource();
}

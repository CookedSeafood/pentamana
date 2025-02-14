package net.cookedseafood.pentamana.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.api.ServerPlayerEntityApi;
import net.cookedseafood.pentamana.command.ManaCommand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.WitchEntity;
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

	@Override
	public float getCastingDamageAgainst(Entity entity, float baseDamage) {
		ServerPlayerEntity player = ((ServerPlayerEntity)(Object)this);
		int level = player.getWeaponStack().getEnchantments().getLevel("pentamana:potency");
		try {
			return (ManaCommand.executeGetManaCapacity(player.getCommandSource()) / Pentamana.manaPerPoint * baseDamage + level > 0 ? ++level * (float)0.5 : 0) * (entity instanceof WitchEntity ? (float)0.15 : 1);
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Shadow
	public abstract ServerCommandSource getCommandSource();
}

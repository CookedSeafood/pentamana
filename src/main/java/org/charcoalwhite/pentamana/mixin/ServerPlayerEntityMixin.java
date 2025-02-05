package org.charcoalwhite.pentamana.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.charcoalwhite.pentamana.command.ManaCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
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

	@Shadow
	public abstract ServerCommandSource getCommandSource();
}

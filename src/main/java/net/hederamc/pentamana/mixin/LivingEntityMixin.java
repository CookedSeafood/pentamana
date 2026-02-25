package net.hederamc.pentamana.mixin;

import net.hederamc.pentamana.api.ManaHolder;
import net.minecraft.world.entity.LivingEntity;
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
        if (((LivingEntity)(Object)this).level().isClientSide()) {
            return;
        }

        this.tickMana();
    }
}

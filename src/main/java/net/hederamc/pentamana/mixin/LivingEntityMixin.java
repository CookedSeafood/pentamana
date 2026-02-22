package net.hederamc.pentamana.mixin;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hederamc.pentamana.Pentamana;
import net.hederamc.pentamana.api.ManaHolder;
import net.hederamc.pentamana.network.protocol.common.ManaS2CPayload;
import net.minecraft.server.level.ServerPlayer;
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

    @Override
    public void tickMana() {
        boolean changed = false;
        float capacity = this.getModifiedManaCapacityBase(Pentamana.CONFIG.manaCapacityBase);

        if (this.getManaCapacity() != capacity) {
            this.setManaCapacity(capacity);
            changed = true;
        }

        float mana = this.getMana();

        if (mana < capacity && mana >= 0.0f) {
            changed = this.regenMana();
        } else if (mana > capacity) {
            this.setMana(capacity);
            changed = true;
        } else if (mana < 0) {
            this.setMana(0.0f);
            changed = true;
        }

        if (changed) {
            LivingEntity livingEntity = (LivingEntity)(Object)this;

            if (livingEntity instanceof ServerPlayer) {
                ServerPlayer player = (ServerPlayer)livingEntity;

                if (player.connection.canConnectPentamana()) {
                    ServerPlayNetworking.send(player, new ManaS2CPayload(this.getMana(), capacity));
                }
            }
        }
    }
}

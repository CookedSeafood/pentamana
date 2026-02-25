package net.hederamc.pentamana.mixin;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hederamc.pentamana.Pentamana;
import net.hederamc.pentamana.network.protocol.common.ManaS2CPayload;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends LivingEntityMixin {
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
            changed |= this.regenMana();
        } else if (mana > capacity) {
            this.setMana(capacity);
            changed = true;
        } else if (mana < 0.0f) {
            this.setMana(0.0f);
            changed = true;
        }

        if (changed) {
            ServerPlayer player = (ServerPlayer)(Object)this;

            if (player.connection.canConnectPentamana()) {
                ServerPlayNetworking.send(player, new ManaS2CPayload(this.getMana(), capacity));
            }
        }
    }
}

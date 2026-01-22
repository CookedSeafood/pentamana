package net.hederamc.pentamana.mixin;

import net.hederamc.pentamana.api.PentamanaConnection;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin implements PentamanaConnection {
    @Unique
    private boolean canConnectPentamana;

    @Override
    public boolean canConnectPentamana() {
        return this.canConnectPentamana;
    }

    @Override
    public void setCanConnectPentamana(boolean bool) {
        this.canConnectPentamana = bool;
    }
}

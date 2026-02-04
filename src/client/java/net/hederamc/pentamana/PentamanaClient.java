package net.hederamc.pentamana;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.hederamc.pentamana.client.command.ManaBarCommand;
import net.hederamc.pentamana.client.config.PentamanaConfig;
import net.hederamc.pentamana.network.protocol.common.ManaS2CPayload;
import net.hederamc.pentamana.network.protocol.common.PentamanaConnectionInitializerC2SPayload;
import net.minecraft.client.player.LocalPlayer;

public class PentamanaClient implements ClientModInitializer {
    public static final PentamanaConfig CONFIG = PentamanaConfig.HANDLER.instance();
    public static final PentamanaConfig DEFAULTS = PentamanaConfig.HANDLER.defaults();

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        ClientPlayNetworking.registerGlobalReceiver(ManaS2CPayload.ID, (payload, context) -> {
            LocalPlayer player = context.player();
            if (player == null) {
                return;
            }

            player.setMana(payload.mana());
            player.setManaCapacity(payload.capacity());
        });
        ClientPlayConnectionEvents.JOIN.register((listener, sender, client) -> ClientPlayNetworking.send(PentamanaConnectionInitializerC2SPayload.INSTANCE));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ManaBarCommand.register(dispatcher, registryAccess));
    }
}

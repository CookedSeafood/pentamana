package net.hederamc.pentamana;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hederamc.pentamana.command.ManaCommand;
import net.hederamc.pentamana.command.PentamanaCommand;
import net.hederamc.pentamana.config.PentamanaConfig;
import net.hederamc.pentamana.network.protocol.common.ManaS2CPayload;
import net.hederamc.pentamana.network.protocol.common.PentamanaConnectionInitializerC2SPayload;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Pentamana implements ModInitializer {
    public static final String MOD_ID = "pentamana";
    public static final String MOD_NAMESPACE = "pentamana";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final PentamanaConfig CONFIG = PentamanaConfig.HANDLER.instance();
    public static final PentamanaConfig DEFAULTS = PentamanaConfig.HANDLER.defaults();

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        PayloadTypeRegistry.clientboundPlay().register(ManaS2CPayload.ID, ManaS2CPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PentamanaConnectionInitializerC2SPayload.ID, PentamanaConnectionInitializerC2SPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PentamanaConnectionInitializerC2SPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player == null) {
                return;
            }

            player.connection.setCanConnectPentamana(true);
            ServerPlayNetworking.send(player, new ManaS2CPayload(player.getMana(), player.getManaCapacity()));
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ManaCommand.register(dispatcher, registryAccess));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> PentamanaCommand.register(dispatcher, registryAccess));
    }
}

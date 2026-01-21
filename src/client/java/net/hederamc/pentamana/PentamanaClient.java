package net.hederamc.pentamana;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.hederamc.pentamana.client.command.ManaBarCommand;
import net.hederamc.pentamana.client.config.PentamanaConfig;

public class PentamanaClient implements ClientModInitializer {
    public static final PentamanaConfig CONFIG = PentamanaConfig.HANDLER.instance();
    public static final PentamanaConfig DEFAULTS = PentamanaConfig.HANDLER.defaults();

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ManaBarCommand.register(dispatcher, registryAccess));
    }
}

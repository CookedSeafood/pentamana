package net.cookedseafood.pentamana.component;

import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.entity.RespawnableComponent;
import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.api.component.ServerManaBarComponent;
import net.cookedseafood.pentamana.mana.ServerManaBar;
import net.cookedseafood.pentamana.mana.ManaTextual;
import net.cookedseafood.pentamana.mana.ManaRender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

public class ServerManaBarComponentInstance implements ServerManaBarComponent, EntityComponentInitializer, RespawnableComponent<ServerManaBarComponentInstance> {
    public static final ComponentKey<ServerManaBarComponentInstance> SERVER_MANA_BAR =
        ComponentRegistry.getOrCreate(Identifier.of(Pentamana.MOD_NAMESPACE, "server_mana_bar"), ServerManaBarComponentInstance.class);
    private ServerManaBar serverManaBar;

    public ServerManaBarComponentInstance() {
    }

    public ServerManaBarComponentInstance(PlayerEntity player) {
        this.serverManaBar = new ServerManaBar(
            player.getServer(),
            null,
            null,
            null,
            null,
            0.0f,
            0.0f,
            Pentamana.manaBarPosition,
            new ManaTextual(
                Pentamana.manaPattern,
                new ManaRender(
                    Pentamana.manaRenderType,
                    Pentamana.manaCharset,
                    Pentamana.pointsPerCharacter,
                    Pentamana.isCompression,
                    Pentamana.compressionSize
                )
            ),
            Pentamana.isVisible,
            Pentamana.manaBarColor,
            Pentamana.manaBarStyle
        );
    }

    @Override
    public ServerManaBar getServerManaBar() {
        return this.serverManaBar;
    }

    @Override
    public void setServerManaBar(ServerManaBar serverManaBar) {
        this.serverManaBar = serverManaBar;
    }

    @Override
    public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        if (!nbtCompound.isEmpty()) {
            this.serverManaBar = ServerManaBar.fromNbt(nbtCompound, wrapperLookup)
                .withServer(this.serverManaBar.getServer());
        }
    }

    @Override
    public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        nbtCompound.copyFrom(this.serverManaBar.toNbt(wrapperLookup));
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(SERVER_MANA_BAR, ServerManaBarComponentInstance::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}

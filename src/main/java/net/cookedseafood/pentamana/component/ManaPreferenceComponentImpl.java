package net.cookedseafood.pentamana.component;

import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.api.component.ManaPreferenceComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.entity.RespawnableComponent;

public class ManaPreferenceComponentImpl implements ManaPreferenceComponent, EntityComponentInitializer, RespawnableComponent<ManaPreferenceComponentImpl> {
    public static final ComponentKey<ManaPreferenceComponentImpl> MANA_PREFERENCE =
        ComponentRegistry.getOrCreate(Identifier.of(Pentamana.MOD_ID, "mana_preference"), ManaPreferenceComponentImpl.class);
    private boolean isEnbaled;

    public ManaPreferenceComponentImpl() {
    }

    public ManaPreferenceComponentImpl(PlayerEntity player) {
        this.isEnbaled = Pentamana.isEnabled;
    }

    @Override
    public boolean isEnabled() {
        return this.isEnbaled;
    }

    @Override
    public void setIsEnabled(boolean isEnabled) {
        this.isEnbaled = isEnabled;
    }

    @Override
    public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        if (!nbtCompound.isEmpty()) {
            this.isEnbaled = nbtCompound.getBoolean("isEnbaled");
        }
    }

    @Override
    public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        nbtCompound.putBoolean("isEnbaled", this.isEnbaled);
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(MANA_PREFERENCE, ManaPreferenceComponentImpl::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}

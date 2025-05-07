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

public class ManaPreferenceComponentInstance implements ManaPreferenceComponent, EntityComponentInitializer, RespawnableComponent<ManaPreferenceComponentInstance> {
    public static final ComponentKey<ManaPreferenceComponentInstance> MANA_PREFERENCE =
        ComponentRegistry.getOrCreate(Identifier.of(Pentamana.MOD_NAMESPACE, "mana_preference"), ManaPreferenceComponentInstance.class);
    private boolean isEnbaled;

    public ManaPreferenceComponentInstance() {
    }

    public ManaPreferenceComponentInstance(PlayerEntity player) {
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
    public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        if (!nbtCompound.isEmpty()) {
            this.isEnbaled = nbtCompound.getBoolean("isEnbaled");
        }
    }

    @Override
    public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        nbtCompound.putBoolean("isEnbaled", this.isEnbaled);
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(MANA_PREFERENCE, ManaPreferenceComponentInstance::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}

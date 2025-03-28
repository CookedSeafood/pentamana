package net.cookedseafood.pentamana.component;

import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.api.component.CustomStatusEffectManagerComponent;
import net.cookedseafood.pentamana.effect.CustomStatusEffectManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnableComponent;

public class CustomStatusEffectManagerComponentInstance implements CustomStatusEffectManagerComponent, EntityComponentInitializer, RespawnableComponent<CustomStatusEffectManagerComponentInstance> {
    public static final ComponentKey<CustomStatusEffectManagerComponentInstance> CUSTOM_STATUS_EFFECT_MANAGER =
        ComponentRegistry.getOrCreate(Identifier.of(Pentamana.MOD_ID, "custom_status_effect_manager"), CustomStatusEffectManagerComponentInstance.class);
    private CustomStatusEffectManager statusEffectManager;

    public CustomStatusEffectManagerComponentInstance() {
    }

    public CustomStatusEffectManagerComponentInstance(PlayerEntity player) {
        this.statusEffectManager = new CustomStatusEffectManager();
    }

    @Override
    public CustomStatusEffectManager getStatusEffectManager() {
        return this.statusEffectManager;
    }

    @Override
    public void setStatusEffectManager(CustomStatusEffectManager statusEffectManager) {
        this.statusEffectManager = statusEffectManager;
    }

    @Override
    public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        if (!nbtCompound.isEmpty()) {
            this.statusEffectManager = CustomStatusEffectManager.fromNbt(nbtCompound, registryLookup);
        }
    }

    @Override
    public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        nbtCompound.copyFrom(this.statusEffectManager.toNbt(registryLookup));
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
    }
}

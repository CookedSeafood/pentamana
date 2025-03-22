package net.cookedseafood.pentamana.component;

import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.api.component.ManaStatusEffectManagerComponent;
import net.cookedseafood.pentamana.mana.ManaStatusEffectManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnableComponent;

public class ManaStatusEffectManagerComponentImpl implements ManaStatusEffectManagerComponent, EntityComponentInitializer, RespawnableComponent<ManaStatusEffectManagerComponentImpl> {
    public static final ComponentKey<ManaStatusEffectManagerComponentImpl> MANA_STATUS_EFFECT =
        ComponentRegistry.getOrCreate(Identifier.of(Pentamana.MOD_ID, "mana_status_effect_manager"), ManaStatusEffectManagerComponentImpl.class);
    private ManaStatusEffectManager statusEffectManager;

    public ManaStatusEffectManagerComponentImpl() {
    }

    public ManaStatusEffectManagerComponentImpl(PlayerEntity player) {
        this.statusEffectManager = new ManaStatusEffectManager();
    }

    @Override
    public ManaStatusEffectManager getStatusEffectManager() {
        return this.statusEffectManager;
    }

    @Override
    public void setStatusEffectManager(ManaStatusEffectManager statusEffectManager) {
        this.statusEffectManager = statusEffectManager;
    }

    @Override
    public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        if (!nbtCompound.isEmpty()) {
            this.statusEffectManager = ManaStatusEffectManager.fromNbt(nbtCompound, registryLookup);
        }
    }

    @Override
    public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        nbtCompound.copyFrom(this.statusEffectManager.toNbt(registryLookup));
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(MANA_STATUS_EFFECT, ManaStatusEffectManagerComponentImpl::new);
    }
}

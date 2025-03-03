package net.cookedseafood.pentamana.component;

import net.cookedseafood.pentamana.api.component.ManaDisplayComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnableComponent;

public class ManaDisplay implements ManaDisplayComponent, EntityComponentInitializer, RespawnableComponent<ManaDisplay> {
    public static final ComponentKey<ManaDisplay> MANA_DISPLAY =
        ComponentRegistry.getOrCreate(Identifier.of("pentamana", "mana_display"), ManaDisplay.class);
    private byte manabarLife;
    private int manaSupplyPoint;
    private int manaCapacityPoint;
    private byte manaSupplyPercent;

    public ManaDisplay() {
    }

    @Override
    public byte getManabarLife() {
        return manabarLife;
    }

    @Override
    public byte setManabarLife(byte manabarLife) {
        return this.manabarLife = manabarLife;
    }

    @Override
    public int getManaSupplyPoint() {
        return manaSupplyPoint;
    }

    @Override
    public int setManaSupplyPoint(int manaSupplyPoint) {
        return this.manaSupplyPoint = manaSupplyPoint;
    }

    @Override
    public int getManaCapacityPoint() {
        return manaCapacityPoint;
    }

    @Override
    public int setManaCapacityPoint(int manaCapacityPoint) {
        return this.manaCapacityPoint = manaCapacityPoint;
    }

    @Override
    public byte getManaSupplyPercent() {
        return manaSupplyPercent;
    }

    @Override
    public byte setManaSupplyPercent(byte manaSupplyPercent) {
        return this.manaSupplyPercent = manaSupplyPercent;
    }

    @Override
    public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
    }

    @Override
    public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(MANA_DISPLAY, player -> new ManaDisplay());
    }
}

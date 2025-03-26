package net.cookedseafood.pentamana.mana;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

public class ManaStatusEffect {
    private Identifier id;
    private int duration;
    private int amplifier;

    public ManaStatusEffect(Identifier id, int duration, int amplifier) {
        this.id = id;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    public static ManaStatusEffect of(Identifier id) {
        return new ManaStatusEffect(id, 0, 0);
    }

    /**
     * A shadow copy.
     * 
     * @return a new ManaStatusEffect
     * 
     * @see #deepCopy()
     */
    public ManaStatusEffect copy() {
        return new ManaStatusEffect(this.id, this.duration, this.amplifier);
    }

    /**
     * A deep copy.
     * 
     * @return a new ManaStatusEffect
     * 
     * @see #copy()
     */
    public ManaStatusEffect deepCopy() {
        return new ManaStatusEffect(Identifier.of(this.id.getNamespace(), this.id.getPath()), this.duration, this.amplifier);
    }

    public Identifier getId() {
        return this.id;
    }

    public void setId(Identifier id) {
        this.id = id;
    }

    public ManaStatusEffect withId(Identifier id) {
        this.id = id;
        return this;
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int incrementDuration() {
        return this.incrementDuration(1);
    }

    public int incrementDuration(int duration) {
        this.duration += duration;
        return this.duration;
    }

    public ManaStatusEffect withDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public int getAmplifier() {
        return this.amplifier;
    }

    public void setAmplifier(int amplifier) {
        this.amplifier = amplifier;
    }

    public int incrementAmplifier() {
        return this.incrementAmplifier(1);
    }

    public int incrementAmplifier(int amplifier) {
        this.amplifier += amplifier;
        return this.amplifier;
    }

    public ManaStatusEffect withAmplifier(int amplifier) {
        this.amplifier = amplifier;
        return this;
    }

    public static ManaStatusEffect fromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        return new ManaStatusEffect(
            Identifier.of(nbtCompound.getString("id")),
            nbtCompound.getInt("duration"),
            nbtCompound.getInt("amplifier")
        );
    }

    public NbtCompound toNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return new NbtCompound(
            new HashMap<>(
                Map.<String, NbtElement>of(
                    "id",
                    NbtString.of(this.id.toString()),
                    "duration",
                    NbtInt.of(this.duration),
                    "amplifier",
                    NbtInt.of(this.amplifier)
                )
            )
        );
    }
}

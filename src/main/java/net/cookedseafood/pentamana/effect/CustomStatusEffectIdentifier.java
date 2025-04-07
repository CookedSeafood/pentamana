package net.cookedseafood.pentamana.effect;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

public class CustomStatusEffectIdentifier {
    private Identifier id;
    private int color;

    public CustomStatusEffectIdentifier(Identifier id, int color) {
        this.id = id;
        this.color = color;
    }

    public Identifier getId() {
        return this.id;
    }

    public void setId(Identifier id) {
        this.id = id;
    }

    public CustomStatusEffectIdentifier withId(Identifier id) {
        this.id = id;
        return this;
    }

    public int getColor() {
        return this.color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int incrementColor() {
        return this.incrementColor(1);
    }

    public int incrementColor(int value) {
        this.setColor(this.color + value);
        return this.color;
    }

    public CustomStatusEffectIdentifier withColor(int color) {
        this.color = color;
        return this;
    }

    public boolean equals(CustomStatusEffectIdentifier id) {
        return this.id.equals(id.id) && this.color == id.color;
    }

    /**
     * A shadow copy.
     * 
     * @return a new ManaStatusEffectIdentifier
     * 
     * @see #deepCopy()
     */
    public CustomStatusEffectIdentifier copy() {
        return new CustomStatusEffectIdentifier(this.id, this.color);
    }

    /**
     * A deep copy.
     * 
     * @return a new ManaStatusEffectIdentifier
     * 
     * @see #copy()
     */
    public CustomStatusEffectIdentifier deepCopy() {
        return new CustomStatusEffectIdentifier(Identifier.of(this.id.getNamespace(), this.id.getPath()), this.color);
    }

    public static CustomStatusEffectIdentifier fromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        return new CustomStatusEffectIdentifier(
            Identifier.of(nbtCompound.getString("id")),
            nbtCompound.getInt("color")
        );
    }

    public NbtCompound toNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return new NbtCompound(
            new HashMap<>(
                Map.<String, NbtElement>of(
                    "id",
                    NbtString.of(this.id.toString()),
                    "color",
                    NbtInt.of(this.color)
                )
            )
        );
    }
}

package net.cookedseafood.pentamana.mana;

import java.util.HashMap;
import java.util.Map;
import net.cookedseafood.pentamana.Pentamana;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class ManaTextual {
    private ManaPattern pattern;
    private ManaRender render;

    public ManaTextual(ManaPattern pattern, ManaRender render) {
        this.pattern = pattern;
        this.render = render;
    }

    public Text toText(float manaCapacity, float manaSupply) {
        if (!this.pattern.stream()
            .anyMatch(Pentamana.MANA_PATTERN_MATCHER::equals)) {
            return this.pattern.toText();
        }

        Text renderText = this.render.toText(manaCapacity, manaSupply);
        MutableText text = Text.empty();
        this.pattern.stream()
            .map(p -> Pentamana.MANA_PATTERN_MATCHER.equals(p) ? renderText : p)
            .forEach(text::append);
        return text;
    }

    public ManaPattern getPattern() {
        return this.pattern;
    }

    public void setPattern(ManaPattern pattern) {
        this.pattern = pattern;
    }

    public ManaRender getRender() {
        return render;
    }

    public void setRender(ManaRender render) {
        this.render = render;
    }

    /**
     * A shadow copy.
     * 
     * @return a new ManaTextual
     * 
     * @see #deepCopy()
     */
    public ManaTextual copy() {
        return new ManaTextual(this.pattern, this.render);
    }

    /**
     * A deep copy.
     * 
     * @return a new ManaTextual
     * 
     * @see #copy()
     */
    public ManaTextual deepCopy() {
        return new ManaTextual(this.pattern.deepCopy(), this.render.deepCopy());
    }

    public static ManaTextual fromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        return new ManaTextual(
            ManaPattern.fromNbt(nbtCompound.getList("pattern", NbtElement.STRING_TYPE), wrapperLookup),
            ManaRender.fromNbt(nbtCompound.getCompound("render"), wrapperLookup)
        );
    }

    public NbtCompound toNbt(RegistryWrapper.WrapperLookup wrapperLookup) {
        return new NbtCompound(
            new HashMap<>(
                Map.<String,NbtElement>of(
                    "pattern",
                    this.pattern.toNbt(wrapperLookup),
                    "render",
                    this.render.toNbt(wrapperLookup)
                )
            )
        );
    }
}

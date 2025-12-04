package net.hederamc.pentamana.render;

import java.util.HashMap;
import java.util.Map;
import net.hederamc.pentamana.Pentamana;
import net.hederamc.pentamana.data.PentamanaPreference;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
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
        if (!this.pattern.stream().anyMatch(Pentamana.MANA_PATTERN_MATCHER::equals)) {
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

    public ManaTextual withPattern(ManaPattern pattern) {
        this.setPattern(pattern);
        return this;
    }

    public ManaRender getRender() {
        return render;
    }

    public void setRender(ManaRender render) {
        this.render = render;
    }

    public ManaTextual withRender(ManaRender render) {
        this.setRender(render);
        return this;
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

    public static ManaTextual fromPreference(PentamanaPreference preference) {
        return new ManaTextual(
            preference.pattern,
            ManaRender.fromPreference(preference)
        );
    }

    public static ManaTextual fromNbt(NbtCompound nbtCompound) {
        return new ManaTextual(
            ManaPattern.fromNbt(nbtCompound.getListOrEmpty("pattern")),
            ManaRender.fromNbt(nbtCompound.getCompoundOrEmpty("render"))
        );
    }

    public NbtCompound toNbt() {
        return new NbtCompound(
            new HashMap<>(
                Map.<String,NbtElement>ofEntries(
                    Map.entry("pattern", this.pattern.toNbt()),
                    Map.entry("render", this.render.toNbt())
                )
            )
        );
    }
}

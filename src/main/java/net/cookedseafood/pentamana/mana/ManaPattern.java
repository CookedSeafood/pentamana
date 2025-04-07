package net.cookedseafood.pentamana.mana;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class ManaPattern {
    private List<Text> pattern;

    public ManaPattern(List<Text> pattern) {
        this.pattern = pattern;
    }

    public Text toText() {
        MutableText text = Text.empty();
        this.forEach(text::append);
        return text;
    }

    public List<Text> getPattern() {
        return this.pattern;
    }

    public void setPattern(List<Text> pattern) {
        this.pattern = pattern;
    }

    public Text get(int index) {
        return this.pattern.get(index);
    }

    public boolean add(Text text) {
        return this.pattern.add(text);
    }

    public boolean addAll(Collection<Text> texts) {
        return this.pattern.addAll(texts);
    }

    public boolean remove(Text text) {
        return this.pattern.remove(text);
    }

    public void clear() {
        this.pattern.clear();
    }

    public boolean contains(Text text) {
        return this.pattern.contains(text);
    }

    public boolean containsAll(Collection<Text> texts) {
        return this.pattern.containsAll(texts);
    }

    public boolean isEmpty() {
        return this.pattern.isEmpty();
    }

    public void forEach(Consumer<? super Text> action) {
        this.pattern.forEach(action);
    }

    public Iterator<Text> iterator() {
        return this.pattern.iterator();
    }

    public Stream<Text> stream() {
        return this.pattern.stream();
    }

    /**
     * A shadow copy.
     * 
     * @return a new ManaPattern
     * 
     * @see #deepCopy()
     */
    public ManaPattern copy() {
        return new ManaPattern(this.pattern);
    }

    /**
     * A deep copy.
     * 
     * @return a new ManaPattern
     * 
     * @see #copy()
     */
    public ManaPattern deepCopy() {
        return new ManaPattern(this.stream()
            .map(MutableText.class::cast)
            .map(MutableText::deepCopy)
            .collect(Collectors.toList()));
    }

    public static ManaPattern fromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        return new ManaPattern(
            nbtCompound.getList("pattern", NbtList.STRING_TYPE).stream()
                .map(NbtString.class::cast)
                .map(NbtString::asString)
                .map(text -> Text.Serialization.fromJson(text, registryLookup))
                .map(Text.class::cast)
                .collect(Collectors.toList())
        );
    }

    public NbtCompound toNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return new NbtCompound(
            new HashMap<>(
                Map.<String,NbtElement>of(
                    "pattern",
                    this.stream()
                        .map(text -> Text.Serialization.toJsonString(text, registryLookup))
                        .map(NbtString::of)
                        .collect(NbtList::new, NbtList::add, NbtList::addAll)
                )
            )
        );
    }
}

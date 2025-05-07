package net.cookedseafood.pentamana.mana;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    public int size() {
        return this.pattern.size();
    }

    public boolean isEmpty() {
        return this.pattern.isEmpty();
    }

    public boolean contains(Text text) {
        return this.pattern.contains(text);
    }

    public boolean containsAll(Collection<Text> texts) {
        return this.pattern.containsAll(texts);
    }

    public Text get(int index) {
        return this.pattern.get(index);
    }

    public boolean add(Text text) {
        return this.pattern.add(text);
    }

    public void add(int i, Text text) {
        this.pattern.add(i, text);
    }

    public boolean addAll(Collection<Text> texts) {
        return this.pattern.addAll(texts);
    }

    public boolean addAll(int i, Collection<Text> texts) {
        return this.pattern.addAll(i, texts);
    }

    public boolean remove(Text text) {
        return this.pattern.remove(text);
    }

    public boolean removeAll(Collection<Text> texts) {
        return this.pattern.removeAll(texts);
    }

    public boolean removeIf(Predicate<? super Text> filter) {
        return this.pattern.removeIf(filter);
    }

    public void clear() {
        this.pattern.clear();
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

    public void sort(Comparator<? super Text> c) {
        this.pattern.sort(c);
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
        return new ManaPattern(
            this.stream()
                .map(MutableText.class::cast)
                .map(MutableText::deepCopy)
                .collect(Collectors.toList())
        );
    }

    public static ManaPattern fromNbt(NbtList nbtList, RegistryWrapper.WrapperLookup wrapperLookup) {
        return new ManaPattern(
            nbtList.stream()
                .map(NbtString.class::cast)
                .map(NbtString::asString)
                .map(text -> Text.Serialization.fromJson(text, wrapperLookup))
                .map(Text.class::cast)
                .collect(Collectors.toList())
        );
    }

    public NbtList toNbt(RegistryWrapper.WrapperLookup wrapperLookup) {
        return this.stream()
            .map(text -> Text.Serialization.toJsonString(text, wrapperLookup))
            .map(NbtString::of)
            .collect(NbtList::new, NbtList::add, NbtList::addAll);
    }
}

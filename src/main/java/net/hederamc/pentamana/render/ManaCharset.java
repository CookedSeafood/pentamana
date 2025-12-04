package net.hederamc.pentamana.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public class ManaCharset {
    private List<List<Text>> charset;

    public ManaCharset(List<List<Text>> charset) {
        this.charset = charset;
    }

    public Text toText() {
        MutableText text = Text.empty();
        this.forEach(charType -> 
            charType.forEach(text::append)
        );
        return text;
    }

    public List<List<Text>> getCharset() {
        return charset;
    }

    public void setCharset(List<List<Text>> charset) {
        this.charset = charset;
    }

    public int size() {
        return this.charset.size();
    }

    public boolean isEmpty() {
        return this.charset.isEmpty();
    }

    public boolean contains(List<Text> charType) {
        return this.charset.contains(charType);
    }

    public boolean containsAll(Collection<List<Text>> charTypes) {
        return this.containsAll(charTypes);
    }

    public List<Text> get(int index) {
        return this.charset.get(index);
    }

    public boolean add(List<Text> charType) {
        return this.charset.add(charType);
    }

    public void add(int i, List<Text> charType) {
        this.charset.add(i, charType);
    }

    public boolean addAll(Collection<List<Text>> charTypes) {
        return this.charset.addAll(charTypes);
    }

    public boolean addAll(int i, Collection<List<Text>> charTypes) {
        return this.charset.addAll(i, charTypes);
    }

    public boolean remove(List<Text> charType) {
        return this.charset.remove(charType);
    }

    public boolean removeAll(Collection<List<Text>> charTypes) {
        return this.charset.removeAll(charTypes);
    }

    public boolean removeIf(Predicate<? super List<Text>> filter) {
        return this.charset.removeIf(filter);
    }

    public void clear() {
        this.charset.clear();
    }

    public void forEach(Consumer<? super List<Text>> action) {
        this.charset.forEach(action);
    }

    public Iterator<List<Text>> iterator() {
        return this.charset.iterator();
    }

    public Stream<List<Text>> stream() {
        return this.charset.stream();
    }

    public void sort(Comparator<? super List<Text>> c) {
        this.charset.sort(c);
    }

    /**
     * A shadow copy.
     * 
     * @return a new ManaCharset
     * 
     * @see #deepCopy()
     */
    public ManaCharset copy() {
        return new ManaCharset(this.charset);
    }

    /**
     * A deep copy.
     * 
     * @return a new ManaCharset
     * 
     * @see #copy()
     */
    public ManaCharset deepCopy() {
        return new ManaCharset(this.stream()
            .map(type -> type.stream()
                .map(MutableText.class::cast)
                .map(MutableText::deepCopy)
                .collect(Collectors.toList())
            )
            .collect(Collectors.toList())
        );
    }

    public static ManaCharset fromNbt(NbtList nbtList) {
        return new ManaCharset(
            nbtList.stream()
                .map(NbtList.class::cast)
                .map(manaCharacterType -> manaCharacterType.stream()
                    .map(NbtString.class::cast)
                    .map(NbtString::asString)
                    .map(Optional::get)
                    .map(JsonParser::parseString)
                    .map(text -> TextCodecs.CODEC.parse(JsonOps.INSTANCE, text))
                    .map(DataResult::result)
                    .map(Optional::get)
                    .collect(Collectors.toList())
                )
                .collect(Collectors.toList())
        );
    }

    public NbtList toNbt() {
        return this.stream()
            .map(type -> type.stream()
                .map(text -> TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, text))
                .map(DataResult::result)
                .map(Optional::get)
                .map(JsonElement::toString)
                .map(NbtString::of)
                .collect(NbtList::new, NbtList::add, NbtList::addAll)
            )
            .collect(NbtList::new, NbtList::add, NbtList::addAll);
    }
}

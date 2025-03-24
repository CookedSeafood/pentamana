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

public class ManaCharset {
    private List<List<Text>> charset;

    public ManaCharset(List<List<Text>> charset) {
        this.charset = charset;
    }

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

    public List<Text> get(int index) {
        return this.charset.get(index);
    }

    public boolean contains(List<Text> text) {
        return this.charset.contains(text);
    }

    public boolean containsAll(Collection<List<Text>> collection) {
        return this.containsAll(collection);
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

    public List<List<Text>> getCharset() {
        return charset;
    }

    public void setCharset(List<List<Text>> charset) {
        this.charset = charset;
    }

    public static ManaCharset fromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        return new ManaCharset(
            nbtCompound.getList("charset", NbtList.LIST_TYPE).stream()
                .map(NbtList.class::cast)
                .map(manaCharacterType -> manaCharacterType.stream()
                    .map(NbtString.class::cast)
                    .map(NbtString::asString)
                    .map(manaCharacter -> Text.Serialization.fromJson(manaCharacter, registryLookup))
                    .map(Text.class::cast)
                    .collect(Collectors.toList())
                )
                .collect(Collectors.toList())
        );
    }

    public NbtCompound toNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return new NbtCompound(
            new HashMap<>(
                Map.<String,NbtElement>of(
                    "charset",
                    this.stream()
                        .map(type -> type.stream()
                            .map(character -> Text.Serialization.toJsonString(character, registryLookup))
                            .map(NbtString::of)
                            .collect(NbtList::new, NbtList::add, NbtList::addAll)
                        )
                        .collect(NbtList::new, NbtList::add, NbtList::addAll)
                )
            )
        );
    }
}

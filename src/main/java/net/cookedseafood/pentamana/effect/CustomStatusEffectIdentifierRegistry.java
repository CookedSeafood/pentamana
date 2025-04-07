package net.cookedseafood.pentamana.effect;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class CustomStatusEffectIdentifierRegistry {
    private final Set<CustomStatusEffectIdentifier> ids;

    public CustomStatusEffectIdentifierRegistry() {
        ids = Sets.newHashSet();
    }

    @Nullable
    public CustomStatusEffectIdentifier get(Identifier id) {
        return this.stream()
            .filter(statusEffectId -> statusEffectId.getId().equals(id))
            .findAny()
            .orElse(null);
    }

    public Set<CustomStatusEffectIdentifier> getIds() {
        return this.ids;
    }

    public boolean add(CustomStatusEffectIdentifier id) {
        return this.ids.add(id);
    }

    public boolean addAll(Collection<CustomStatusEffectIdentifier> ids) {
        return this.ids.addAll(ids);
    }

    public boolean remove(CustomStatusEffectIdentifier id) {
        return this.ids.remove(id);
    }

    public boolean contains(CustomStatusEffectIdentifier id) {
        return this.ids.contains(id);
    }

    public boolean containsAll(Collection<CustomStatusEffectIdentifier> ids) {
        return this.ids.containsAll(ids);
    }

    public boolean isEmpty() {
        return this.ids.isEmpty();
    }

    public void forEach(Consumer<? super CustomStatusEffectIdentifier> action) {
        this.ids.forEach(action);
    }

    public Iterator<CustomStatusEffectIdentifier> iterator() {
        return this.ids.iterator();
    }

    public Stream<CustomStatusEffectIdentifier> stream() {
        return this.ids.stream();
    }
}

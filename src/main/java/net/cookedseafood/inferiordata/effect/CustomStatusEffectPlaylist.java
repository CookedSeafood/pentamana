package net.cookedseafood.inferiordata.effect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;

/**
 * Tickable single-id auto-ordering status effect container.
 * 
 * @see #add(CustomStatusEffect)
 */
public class CustomStatusEffectPlaylist {
    private final List<CustomStatusEffect> playlist;

    public CustomStatusEffectPlaylist(List<CustomStatusEffect> playlist) {
        this.playlist = playlist;
    }

    public CustomStatusEffectPlaylist() {
        this.playlist = new ArrayList<>();
    }

    public CustomStatusEffectIdentifier getId() {
        CustomStatusEffect statusEffect = this.get(0);

        if (statusEffect == null) {
            return null;
        }

        return statusEffect.getId();
    }

    public int getActiveAmplifier() {
        CustomStatusEffect statusEffect = this.get(0);

        if (statusEffect == null) {
            return -1;
        }

        return statusEffect.getAmplifier();
    }

    public int getActiveDuration() {
        CustomStatusEffect statusEffect = this.get(0);

        if (statusEffect == null) {
            return -1;
        }

        return statusEffect.getDuration();
    }

    public void tick() {
        Iterator<CustomStatusEffect> iterator = this.iterator();

        while (iterator.hasNext()) {
            CustomStatusEffect statusEffect = iterator.next();

            if (statusEffect.getDuration() == 0) {
                iterator.remove();
                continue;
            }

            statusEffect.tick();
        }
    }

    public List<CustomStatusEffect> getPlaylist() {
        return this.playlist;
    }

    public int size() {
        return this.playlist.size();
    }

    public boolean isEmpty() {
        return this.playlist.isEmpty();
    }

    public boolean contains(CustomStatusEffect statusEffect) {
        return this.playlist.contains(statusEffect);
    }

    public boolean containsAll(Collection<CustomStatusEffect> statusEffects) {
        return this.playlist.containsAll(statusEffects);
    }

    public CustomStatusEffect get(int index) {
        return this.playlist.get(index);
    }

    /**
     * Add the status effect in descending order of amplifier.
     * 
     * @param statusEffect
     * @return {@code true}
     */
    public boolean add(CustomStatusEffect statusEffect) {
        int amplifier = statusEffect.getAmplifier();
        int size = this.size();

        for (int i = 0; i < size; ++i) {
            if (this.get(i).getAmplifier() < amplifier) {
                this.add(i, statusEffect);
                return true;
            }
        }

        return this.playlist.add(statusEffect);
    }

    public void add(int i, CustomStatusEffect statusEffect) {
        this.playlist.add(i, statusEffect);
    }

    /**
     * Add every status effect in descending order of amplifier.
     * 
     * @param statusEffect
     * @return {@code true}
     */
    public boolean addAll(Collection<CustomStatusEffect> statusEffects) {
        statusEffects.forEach(this::add);
        return true;
    }

    public boolean addAll(int i, Collection<CustomStatusEffect> statusEffects) {
        return this.playlist.addAll(i, statusEffects);
    }

    public boolean remove(CustomStatusEffect statusEffect) {
        return this.playlist.remove(statusEffect);
    }

    public boolean removeAll(Collection<CustomStatusEffect> statusEffects) {
        return this.playlist.removeAll(statusEffects);
    }

    public boolean removeIf(Predicate<? super CustomStatusEffect> filter) {
        return this.playlist.removeIf(filter);
    }

    public void clear() {
        this.playlist.clear();
    }

    public void forEach(Consumer<? super CustomStatusEffect> action) {
        this.playlist.forEach(action);
    }

    public Iterator<CustomStatusEffect> iterator() {
        return this.playlist.iterator();
    }

    public Stream<CustomStatusEffect> stream() {
        return this.playlist.stream();
    }

    public void sort(Comparator<? super CustomStatusEffect> c) {
        this.playlist.sort(c);
    }

    /**
     * A shadow copy.
     * 
     * @return a new CustomStatusEffectPlaylist
     * 
     * @see #deepCopy()
     */
    public CustomStatusEffectPlaylist copy() {
        return new CustomStatusEffectPlaylist(this.playlist);
    }

    /**
     * A deep copy.
     * 
     * @return a new CustomStatusEffectPlaylist
     * 
     * @see #copy()
     */
    public CustomStatusEffectPlaylist deepCopy() {
        return new CustomStatusEffectPlaylist(
            this.stream()
                .map(CustomStatusEffect::deepCopy)
                .collect(Collectors.toList())
        );
    }

    public static CustomStatusEffectPlaylist fromNbt(NbtList nbtList, RegistryWrapper.WrapperLookup wrapperLookup) {
        return new CustomStatusEffectPlaylist(
            nbtList.stream()
                .map(NbtCompound.class::cast)
                .map(statusEffect -> CustomStatusEffect.fromNbt(statusEffect, wrapperLookup))
                .collect(Collectors.toList())
        );
    }

    public NbtList toNbt(RegistryWrapper.WrapperLookup wrapperLookup) {
        return this.stream()
            .map(statusEffect -> statusEffect.toNbt(wrapperLookup))
            .collect(NbtList::new, NbtList::add, NbtList::addAll);
    }
}

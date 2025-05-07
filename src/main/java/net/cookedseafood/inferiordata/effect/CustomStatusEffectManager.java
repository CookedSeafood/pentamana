package net.cookedseafood.inferiordata.effect;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import net.cookedseafood.genericregistry.registry.Registries;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Tickable multi-id status effect container.
 */
public class CustomStatusEffectManager {
    private final Map<CustomStatusEffectIdentifier, CustomStatusEffectPlaylist> statusEffects;

    public CustomStatusEffectManager(Map<CustomStatusEffectIdentifier, CustomStatusEffectPlaylist> statusEffects) {
        this.statusEffects = statusEffects;
    }

    public CustomStatusEffectManager() {
        this.statusEffects = new HashMap<>();
    }

    /**
     * Get the highest presented amplifier of the status effect with the id.
     * 
     * @param id
     * @return {@code -1} if there is no status effect with the id.
     */
    public int getActiveAmplifier(CustomStatusEffectIdentifier id) {
        CustomStatusEffectPlaylist playlist = this.get(id);

        if (playlist == null) {
            return -1;
        }

        return playlist.getActiveAmplifier();
    }

    /**
     * Get the duration of the presented status effect with the id and the highest amplifier.
     * 
     * @param id
     * @return {@code -1} if there is no status effect with the id.
     */
    public int getActiveDuration(CustomStatusEffectIdentifier id) {
        CustomStatusEffectPlaylist playlist = this.get(id);

        if (playlist == null) {
            return -1;
        }

        return playlist.getActiveDuration();
    }

    public void tick() {
        Iterator<CustomStatusEffectPlaylist> iterator = this.values().iterator();

        while (iterator.hasNext()) {
            CustomStatusEffectPlaylist playlist = iterator.next();
            playlist.tick();

            if (playlist.isEmpty()) {
                iterator.remove();
            }
        }
    }

    public boolean add(CustomStatusEffect statusEffect) {
        return this.getOrPut(statusEffect.getId().deepCopy()).add(statusEffect);
    }

    public Map<CustomStatusEffectIdentifier, CustomStatusEffectPlaylist> getStatusEffects() {
        return this.statusEffects;
    }

    public int size() {
        return this.statusEffects.size();
    }

    public boolean isEmpty() {
        return this.statusEffects.isEmpty();
    }

    public boolean containsKey(CustomStatusEffectIdentifier id) {
        return this.statusEffects.containsKey(id);
    }

    public boolean containsValue(CustomStatusEffectPlaylist playlist) {
        return this.statusEffects.containsValue(playlist);
    }

    public Set<Map.Entry<CustomStatusEffectIdentifier, CustomStatusEffectPlaylist>> entrySet() {
        return this.statusEffects.entrySet();
    }

    public Set<CustomStatusEffectIdentifier> keySet() {
        return this.statusEffects.keySet();
    }

    public Collection<CustomStatusEffectPlaylist> values() {
        return this.statusEffects.values();
    }

    @Nullable
    public CustomStatusEffectPlaylist get(CustomStatusEffectIdentifier id) {
        return this.statusEffects.get(id);
    }

    public CustomStatusEffectPlaylist getOrPut(CustomStatusEffectIdentifier id, CustomStatusEffectPlaylist playlist) {
        CustomStatusEffectPlaylist playlist2 = this.get(id);

        if (playlist2 != null) {
            return playlist2;
        }

        this.put(id, playlist);
        return playlist;
    }

    public CustomStatusEffectPlaylist getOrPut(CustomStatusEffectIdentifier id) {
        return this.getOrPut(id, new CustomStatusEffectPlaylist());
    }

    public CustomStatusEffectPlaylist put(CustomStatusEffectIdentifier id, CustomStatusEffectPlaylist playlist) {
        return this.statusEffects.put(id, playlist);
    }

    public void putAll(Map<CustomStatusEffectIdentifier, CustomStatusEffectPlaylist> playlists) {
        this.statusEffects.putAll(playlists);
    }

    public CustomStatusEffectPlaylist remove(CustomStatusEffectIdentifier id) {
        return this.statusEffects.remove(id);
    }

    public boolean remove(CustomStatusEffectIdentifier id, CustomStatusEffectPlaylist playlist) {
        return this.statusEffects.remove(id, playlist);
    }

    public void clear() {
        this.statusEffects.clear();
    }

    public CustomStatusEffectPlaylist replace(CustomStatusEffectIdentifier id, CustomStatusEffectPlaylist playlist) {
        return this.statusEffects.replace(id, playlist);
    }

    public boolean replace(CustomStatusEffectIdentifier id, CustomStatusEffectPlaylist oldPlaylist, CustomStatusEffectPlaylist newPlaylist) {
        return this.statusEffects.replace(id, oldPlaylist, newPlaylist);
    }

    public void replaceAll(BiFunction<CustomStatusEffectIdentifier, CustomStatusEffectPlaylist, CustomStatusEffectPlaylist> function) {
        this.statusEffects.replaceAll(function);
    }

    /**
     * A shadow copy.
     * 
     * @return a new CustomStatusEffectManager
     * 
     * @see #deepCopy()
     */
    public CustomStatusEffectManager copy() {
        return new CustomStatusEffectManager(this.statusEffects);
    }

    /**
     * A deep copy.
     * 
     * @return a new CustomStatusEffectManager
     * 
     * @see #copy()
     */
    public CustomStatusEffectManager deepCopy() {
        return new CustomStatusEffectManager(
            this.entrySet().stream()
                .map(entry -> Map.entry(
                    entry.getKey().deepCopy(),
                    entry.getValue().deepCopy()
                ))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue))
        );
    }

    public static CustomStatusEffectManager fromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        return new CustomStatusEffectManager(
            nbtCompound.entrySet().stream()
                .map(entry -> Map.entry(
                    Registries.get(CustomStatusEffectIdentifier.class, Identifier.of(entry.getKey())),
                    CustomStatusEffectPlaylist.fromNbt((NbtList)entry.getValue(), wrapperLookup)
                ))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue))
        );
    }

    public NbtCompound toNbt(RegistryWrapper.WrapperLookup wrapperLookup) {
        return this.entrySet().stream()
            .map(entry -> Map.entry(
                entry.getKey().getId().toString(),
                entry.getValue().toNbt(wrapperLookup)
            ))
        .<NbtCompound>collect(NbtCompound::new, (nbtCompound, entry) -> nbtCompound.put(entry.getKey(), entry.getValue()), (left, right) -> left.putAll(right));
    }
}

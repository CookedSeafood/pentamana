package net.cookedseafood.pentamana.mana;

import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.cookedseafood.pentamana.Pentamana;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.Nullable;

public class ManaStatusEffectManager {
    private Map<String, List<Integer>> statusEffects;

    public ManaStatusEffectManager(Map<String, List<Integer>> statusEffects) {
        this.statusEffects = statusEffects;
    }

    public ManaStatusEffectManager() {
        this.statusEffects = Maps.<String, List<Integer>>newHashMap();
    }

    public void tick() {
        statusEffects.forEach((id, statusEffect) -> IntStream.range(0, statusEffect.size())
            .filter(amplifier -> statusEffect.get(amplifier) > 0)
            .forEach(amplifier -> statusEffect.set(amplifier, statusEffect.get(amplifier) - 1))
        );
    }

    @Nullable
    public List<Integer> get(String id) {
        return this.statusEffects.get(id);
    }

    public boolean has(String id) {
        return this.statusEffects.containsKey(id) ?
            this.get(id).stream().anyMatch(duration -> duration > 0) :
            false;
    }

    public int getActiveStatusEffectAmplifier(String id) {
        if (!this.has(id)) {
            return -1;
        }

        List<Integer> statusEffect = this.get(id);
        return IntStream.iterate(Pentamana.MANA_STATUS_EFFECT_AMPLIFIER_LIMIT, amplifier -> amplifier >= 0, amplifier -> --amplifier)
            .filter(amplifier -> statusEffect.get(amplifier) > 0)
            .findFirst()
            .orElse(-1);
    }

    public int getDuration(String id, int amplifier) {
        return this.has(id) ? this.get(id).get(amplifier) : 0;
    }

    /**
     * Add a status effect with 1 tick duration and 0 amplifier.
     * 
     * @param id
     * @return {@code true} if the player has less duration than {@code duration},
     * otherwise {@code false}.
     */
    public boolean add(String id) {
        return this.add(id, 1);
    }

    /**
     * Add a status effect with 0 amplifier.
     * 
     * @param id
     * @param duration In ticks.
     * @return {@code true} if the player has less duration than {@code duration},
     * otherwise {@code false}.
     */
    public boolean add(String id, int duration) {
        return this.add(id, duration, 0);
    }

    /**
     * Add a status effect.
     * 
     * @param id
     * @param duration In ticks.
     * @param amplifier
     * @return {@code true} if the player has less duration than {@code duration},
     * otherwise {@code false}.
     */
    public boolean add(String id, int duration, int amplifier) {
        if (!this.has(id)) {
            this.statusEffects.put(id, new ArrayList<>(Collections.nCopies(Pentamana.MANA_STATUS_EFFECT_AMPLIFIER_LIMIT + 1, 0)));
        }

        List<Integer> statusEffect = this.get(id);
        if (statusEffect.get(amplifier) < duration) {
            statusEffect.set(amplifier, duration);
            return true;
        }

        return false;
    }

    public void set(String id) {
        this.set(id, 1);
    }

    public void set(String id, int duration) {
        this.set(id, duration, 0);
    }

    public void set(String id, int duration, int amplifier) {
        if (!this.has(id)) {
            this.statusEffects.put(id, new ArrayList<>(Collections.nCopies(Pentamana.MANA_STATUS_EFFECT_AMPLIFIER_LIMIT + 1, 0)));
        }

        this.get(id).set(amplifier, duration);
    }

    public Set<Map.Entry<String, List<Integer>>> entrySet() {
        return this.statusEffects.entrySet();
    }

    public void forEach(BiConsumer<? super String, ? super List<Integer>> action) {
        this.statusEffects.forEach(action);
    }

    public Set<String> keySet() {
        return this.statusEffects.keySet();
    }

    public Collection<List<Integer>> values() {
        return this.statusEffects.values();
    }

    public Map<String, List<Integer>> getStatusEffects() {
        return this.statusEffects;
    }

    public void setStatusEffects(Map<String, List<Integer>> statusEffects) {
        this.statusEffects = statusEffects;
    }

    public static ManaStatusEffectManager fromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        return new ManaStatusEffectManager(
            nbtCompound.getCompound("statusEffects").getKeys().stream()
                .collect(Collectors.toMap(
                    id -> id,
                    id -> nbtCompound.getCompound("statusEffects").getList(id, NbtElement.INT_ARRAY_TYPE).stream()
                        .map(NbtInt.class::cast)
                        .map(NbtInt::intValue)
                        .collect(Collectors.toList())
                ))
        );
    }

    public NbtCompound toNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return new NbtCompound(
            Map.<String,NbtElement>of(
                "statusEffects",
                this.entrySet().stream()
                    .collect(
                        NbtCompound::new,
                        (statusEffectsnbtCompound, entry) -> statusEffectsnbtCompound.put(
                            entry.getKey(),
                            entry.getValue().stream()
                                .map(NbtInt::of)
                                .collect(NbtList::new, NbtList::add, (left, right) -> left.addAll(right))
                        ),
                        (left, right) -> right.getKeys().forEach(key -> left.put(key, right.get(key)))
                    )
            )
        );
    }
}

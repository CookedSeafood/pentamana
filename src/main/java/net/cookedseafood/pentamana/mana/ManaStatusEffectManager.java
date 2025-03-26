package net.cookedseafood.pentamana.mana;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class ManaStatusEffectManager {
    private Set<ManaStatusEffect> statusEffects;

    public ManaStatusEffectManager(Set<ManaStatusEffect> statusEffects) {
        this.statusEffects = statusEffects;
    }

    public ManaStatusEffectManager() {
        this.statusEffects = Sets.<ManaStatusEffect>newHashSet();
    }

    public void tick() {
        this.forEach(statusEffect -> {
            if (statusEffect.getDuration() > 0) {
                statusEffect.incrementDuration(-1);
            } else {
                this.remove(statusEffect);
            }
        });
    }

    /**
     * Get a status effect based on the {@code id}.
     * 
     * @param id
     * @return {@code null} if there is no status effect with the {@code id}
     */
    @Nullable
    public ManaStatusEffect get(Identifier id) {
        return this.stream()
            .filter(statusEffect -> id.equals(statusEffect.getId()))
            .findAny()
            .orElse(null);
    }

    /**
     * Get a status effect based on the {@code id} and {@code amplifier}.
     * 
     * @param id
     * @return {@code null} if there is no status effect with the {@code id} and {@code amplifier}
     */
    @Nullable
    public ManaStatusEffect get(Identifier id, int amplifier) {
        return this.stream()
            .filter(statusEffect -> id.equals(statusEffect.getId()))
            .filter(statusEffect -> amplifier == statusEffect.getAmplifier())
            .findAny()
            .orElse(null);
    }

    /**
     * Check if there is any status effect in this manager has an id {@code i} satisfies
     * {@code statusEffect.getId().equals(i)}.
     * 
     * @param statusEffect
     * @return true if there is any status effect in this manager has an id {@code i} satisfies
     * {@code statusEffect.getId().equals(i)}
     * 
     * @see #has(Identifier)
     */
    public boolean has(ManaStatusEffect statusEffect) {
        return this.has(statusEffect.getId());
    }

    /**
     * Check if there is any status effect in this manager has an id {@code i} satisfies
     * {@code id.equals(i)}.
     * 
     * @param id
     * @return true if there is any status effect in this manager has an id {@code i} satisfies
     * {@code id.equals(i)}
     * 
     * @see #has(ManaStatusEffect)
     */
    public boolean has(Identifier id) {
        return this.stream()
            .anyMatch(statusEffect -> id.equals(statusEffect.getId()));
    }

    /**
     * Check if there is any status effect in this manager has an id {@code i} satisfies
     * {@code id.equals(i)} and an amplifier {@code a} satisfies {@code amplifier.equals(a)}.
     * 
     * @param id
     * @return true if there is any status effect in this manager has an id {@code i} satisfies
     * {@code id.equals(i)} and an amplifier {@code a} satisfies {@code amplifier.equals(a)}
     */
    public boolean has(Identifier id, int amplifier) {
        return this.stream()
            .filter(statusEffect -> id.equals(statusEffect.getId()))
            .anyMatch(statusEffect -> amplifier == statusEffect.getAmplifier());
    }

    /**
     * Get the largest amplifier from status effects in this manager has an id {@code i} satisfies
     * {@code statusEffect.get(id).equals(i)}.
     * 
     * @param statusEffect
     * @return -1 if there is no status effect in this manager has an id {@code i} satisfies
     * {@code statusEffect.get(id).equals(i)}
     */
    public int getActiveAmplifier(ManaStatusEffect statusEffect) {
        return this.getActiveAmplifier(statusEffect.getId());
    }

    /**
     * Get the largest amplifier from status effects in this manager has an id {@code i} satisfies
     * {@code id.equals(i)}.
     * 
     * @param id
     * @return -1 if there is no status effect in this manager has an id {@code i} satisfies
     * {@code id.equals(i)}
     */
    public int getActiveAmplifier(Identifier id) {
        return this.stream()
            .filter(statusEffect -> id.equals(statusEffect.getId()))
            .map(ManaStatusEffect::getAmplifier)
            .max(Integer::compare)
            .orElse(-1);
    }

    /**
     * Add a status effect to this manager if there is no status effect with the same id and amplifier,
     * or set the duration to {@code duration} if the duration is less than {@code duration}.
     * 
     * <p>Duration is 1. Amplifier is 0.
     * 
     * @param id
     * @return {@code true} if modified something
     * 
     * @see #add(Identifier, int)
     * @see #add(Identifier, int, int)
     */
    public boolean add(Identifier id) {
        return this.add(id, 1);
    }

    /**
     * Add a status effect to this manager if there is no status effect with the same id and amplifier,
     * or set the duration to {@code duration} if the duration is less than {@code duration}.
     * 
     * <p>Amplifier is 0.
     * 
     * @param id
     * @param duration in ticks
     * @return {@code true} if modified something
     * 
     * @see #add(Identifier, int, int)
     */
    public boolean add(Identifier id, int duration) {
        return this.add(id, duration, 0);
    }

    /**
     * Add a status effect to this manager if there is no status effect with the same id and amplifier,
     * or set the duration to {@code duration} if the duration is less than {@code duration}.
     * 
     * @param id
     * @param duration in ticks
     * @param amplifier
     * @return {@code true} if modified something
     */
    public boolean add(Identifier id, int duration, int amplifier) {
        ManaStatusEffect statusEffect = this.get(id, amplifier);
        if (statusEffect == null) {
            return this.add(new ManaStatusEffect(id, duration, amplifier));
        } else if (duration > statusEffect.getDuration()) {
            statusEffect.setDuration(duration);
            return true;
        }

        return false;
    }

    public Set<ManaStatusEffect> getStatusEffects() {
        return this.statusEffects;
    }

    public void setStatusEffects(Set<ManaStatusEffect> statusEffects) {
        this.statusEffects = statusEffects;
    }

    public boolean add(ManaStatusEffect statusEffect) {
        return this.statusEffects.add(statusEffect);
    }

    public boolean addAll(Collection<ManaStatusEffect> statusEffects) {
        return this.statusEffects.addAll(statusEffects);
    }

    public boolean remove(ManaStatusEffect statusEffect) {
        return this.statusEffects.remove(statusEffect);
    }

    public boolean contains(ManaStatusEffect statusEffect) {
        return this.statusEffects.contains(statusEffect);
    }

    public boolean containsAll(Collection<ManaStatusEffect> statusEffects) {
        return this.statusEffects.containsAll(statusEffects);
    }

    public void forEach(Consumer<? super ManaStatusEffect> action) {
        this.statusEffects.forEach(action);
    }

    public Iterator<ManaStatusEffect> iterator() {
        return this.statusEffects.iterator();
    }

    public Stream<ManaStatusEffect> stream() {
        return this.statusEffects.stream();
    }

    public static ManaStatusEffectManager fromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        return new ManaStatusEffectManager(
            nbtCompound.getList("statusEffects", NbtElement.COMPOUND_TYPE).stream()
                .map(NbtCompound.class::cast)
                .map(statusEffect -> ManaStatusEffect.fromNbt(nbtCompound, registryLookup))
                .collect(Collectors.toSet())
        );
    }

    public NbtCompound toNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return new NbtCompound(
            new HashMap<>(
                Map.<String,NbtElement>of(
                    "statusEffects",
                    this.stream()
                        .map(statusEffects -> statusEffects.toNbt(registryLookup))
                        .collect(NbtList::new, NbtList::add, NbtList::addAll)
                )
            )
        );
    }
}

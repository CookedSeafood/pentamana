package net.cookedseafood.pentamana.effect;

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
import org.jetbrains.annotations.Nullable;

/**
 * Tickable status effect container.
 * 
 * <p>Can only contain one status effect with the same id and amplifier.
 */
public class CustomStatusEffectManager {
    private Set<CustomStatusEffect> statusEffects;

    public CustomStatusEffectManager(Set<CustomStatusEffect> statusEffects) {
        this.statusEffects = statusEffects;
    }

    public CustomStatusEffectManager() {
        this.statusEffects = Sets.<CustomStatusEffect>newHashSet();
    }

    public void tick() {
        Iterator<CustomStatusEffect> iterator = this.iterator();

        while (iterator.hasNext()) {
            CustomStatusEffect statusEffect = iterator.next();
            if (statusEffect.getDuration() > 0) {
                statusEffect.tick();
            } else {
                iterator.remove();
            }
        }
    }

    /**
     * Get status effects with the same id
     * 
     * @param id
     * @return status effects with the same id
     */
    public Set<CustomStatusEffect> get(CustomStatusEffectIdentifier id) {
        return this.stream()
            .filter(statusEffect -> statusEffect.getId().equals(id))
            .collect(Collectors.toSet());
    }

    /**
     * Get the <i>only</i> status effect with the same id and amplifier.
     * 
     * @param statusEffect
     * @return {@code null} if there is no status effect with the same id and amplifier
     */
    @Nullable
    public CustomStatusEffect get(CustomStatusEffect statusEffect) {
        CustomStatusEffectIdentifier id = statusEffect.getId();
        int amplifier = statusEffect.getAmplifier();

        return this.stream()
            .filter(statusEffect2 -> statusEffect2.getId().equals(id))
            .filter(statusEffect2 -> statusEffect2.getAmplifier() == amplifier)
            .findAny()
            .orElse(null);
    }

    /**
     * Check if there is any status effect with the same id.
     * 
     * @param id
     * @return true if there is any status effect with the same id
     */
    public boolean has(CustomStatusEffectIdentifier id) {
        return this.stream()
            .anyMatch(statusEffect -> statusEffect.getId().equals(id));
    }

    /**
     * Check if there is any status effect with the same id and amplifier.
     * 
     * @param statusEffect
     * @return true if there is any status effect with the same id and amplifier
     */
    public boolean has(CustomStatusEffect statusEffect) {
        CustomStatusEffectIdentifier id = statusEffect.getId();
        int amplifier = statusEffect.getAmplifier();

        return this.stream()
            .filter(statusEffect2 -> statusEffect2.getId().equals(id))
            .anyMatch(statusEffect2 -> statusEffect2.getAmplifier() == amplifier);
    }

    /**
     * Get the largest amplifier from status effects with the same id.
     * 
     * @param id
     * @return -1 if there is no status effect with the same id
     */
    public int getActiveAmplifier(CustomStatusEffectIdentifier id) {
        return this.stream()
            .filter(statusEffect -> statusEffect.getId().equals(id))
            .map(CustomStatusEffect::getAmplifier)
            .max(Integer::compare)
            .orElse(-1);
    }

    public Set<CustomStatusEffect> getStatusEffects() {
        return this.statusEffects;
    }

    public void setStatusEffects(Set<CustomStatusEffect> statusEffects) {
        this.statusEffects = statusEffects;
    }

    /**
     * Add a status effect to this manager if there is no status effect with the same id and amplifier,
     * or set the duration to {@code duration} if the duration is less than {@code duration}.
     * 
     * <p>To ensure there is <i>only</i> one status effects with the same id and amplifier.
     * 
     * @param statusEffect
     * @return {@code true} if modified something
     */
    public boolean add(CustomStatusEffect statusEffect) {
        CustomStatusEffect presentedStatusEffect = this.get(statusEffect);
        if (presentedStatusEffect == null) {
            return this.statusEffects.add(statusEffect);
        }

        int duration = statusEffect.getDuration();
        if (duration > presentedStatusEffect.getDuration()) {
            presentedStatusEffect.setDuration(duration);
            return true;
        }

        return false;
    }

    /**
     * For each status effect {@code s} in the collection, add {@code s} to this manager if there is no
     * status effect with the same id and amplifier, or set the duration to {@code duration} if the duration
     * is less than {@code duration}.
     * 
     * @param statusEffects
     * 
     * @see #add(CustomStatusEffect)
     */
    public void addAll(Collection<CustomStatusEffect> statusEffects) {
        statusEffects.forEach(this.statusEffects::add);
    }

    public boolean remove(CustomStatusEffect statusEffect) {
        return this.statusEffects.remove(statusEffect);
    }

    public boolean contains(CustomStatusEffect statusEffect) {
        return this.statusEffects.contains(statusEffect);
    }

    public boolean containsAll(Collection<CustomStatusEffect> statusEffects) {
        return this.statusEffects.containsAll(statusEffects);
    }

    public void forEach(Consumer<? super CustomStatusEffect> action) {
        this.statusEffects.forEach(action);
    }

    public Iterator<CustomStatusEffect> iterator() {
        return this.statusEffects.iterator();
    }

    public Stream<CustomStatusEffect> stream() {
        return this.statusEffects.stream();
    }

    public static CustomStatusEffectManager fromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        return new CustomStatusEffectManager(
            nbtCompound.getList("statusEffects", NbtElement.COMPOUND_TYPE).stream()
                .map(NbtCompound.class::cast)
                .map(statusEffect -> CustomStatusEffect.fromNbt(nbtCompound, registryLookup))
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

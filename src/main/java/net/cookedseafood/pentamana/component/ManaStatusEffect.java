package net.cookedseafood.pentamana.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.cookedseafood.pentamana.api.component.ManaStatusEffectComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnableComponent;

public class ManaStatusEffect implements ManaStatusEffectComponent, EntityComponentInitializer, RespawnableComponent<ManaStatusEffect> {
    public static final ComponentKey<ManaStatusEffect> MANA_STATUS_EFFECT =
        ComponentRegistry.getOrCreate(Identifier.of("pentamana", "mana_status_effect"), ManaStatusEffect.class);
    private Map<String, List<Integer>> statusEffects;

    public ManaStatusEffect() {
        this.statusEffects = new HashMap<>();
    }

    @Override
    public void tick() {
        statusEffects.forEach((id, statusEffect) -> IntStream.range(0, statusEffect.size())
            .filter(amplifier -> statusEffect.get(amplifier) > 0)
            .forEach(amplifier -> statusEffect.set(amplifier, statusEffect.get(amplifier) - 1))
        );
    }

    @Override
    @NotNull
    public Map<String, List<Integer>> getStatusEffects() {
        return this.statusEffects;
    }

    @Override
    public void setStatusEffect(Map<String, List<Integer>> statusEffects) {
        this.statusEffects = statusEffects;
    }

    @Override
    public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        this.statusEffects = nbtCompound.contains("statusEffects", NbtElement.COMPOUND_TYPE) ?
            nbtCompound.getCompound("statusEffects").getKeys().stream()
                .collect(Collectors.toMap(
                    id -> id,
                    id -> nbtCompound.getCompound("statusEffects").getList(id, NbtElement.INT_ARRAY_TYPE).stream()
                        .map(NbtInt.class::cast)
                        .map(NbtInt::intValue)
                        .collect(Collectors.toList())
                )) :
                new HashMap<>();
    }

    @Override
    public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        nbtCompound.put("statusEffects", statusEffects.keySet().stream()
            .collect(
                NbtCompound::new,
                (statusEffectsnbtCompound, id) -> statusEffectsnbtCompound.put(
                    id,
                    statusEffects.get(id).stream()
                        .map(NbtInt::of)
                        .collect(NbtList::new, NbtList::add, (left, right) -> left.addAll(right))
                ),
                (left, right) -> right.getKeys().forEach(key -> left.put(key, right.get(key)))
            )
        );
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(MANA_STATUS_EFFECT, player -> new ManaStatusEffect());
    }
}

package net.cookedseafood.pentamana.api.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.Component;

import net.cookedseafood.pentamana.Pentamana;

public interface ManaStatusEffectComponent extends Component {
    void tick();

    @NotNull
    Map<String, List<Integer>> getStatusEffects();

    @NotNull
    default List<Integer> getStatusEffect(String id) {
        List<Integer> statusEffect = this.getStatusEffects().get(id);
        return statusEffect != null ? statusEffect : new ArrayList<>(Collections.nCopies(Pentamana.MANA_STATUS_EFFECT_AMPLIFIER_LIMIT + 1, 0));
    }

    default boolean hasStatusEffect(String id) {
        return this.getStatusEffects().containsKey(id) ?
            this.getStatusEffect(id).stream().anyMatch(duration -> duration > 0) :
            false;
    }

    default int getActiveStatusEffectAmplifier(String id) {
        if (!this.hasStatusEffect(id)) {
            return -1;
        }

        List<Integer> statusEffect = this.getStatusEffect(id);
        return IntStream.iterate(statusEffect.size() - 1, amplifier -> amplifier >= 0, amplifier -> --amplifier)
            .filter(amplifier -> statusEffect.get(amplifier) > 0)
            .findFirst()
            .orElse(-1);
    }

    default int getStatusEffectDuration(String id, int amplifier) {
        if (!this.hasStatusEffect(id)) {
            return 0;
        }

        return this.getStatusEffect(id).get(amplifier);
    }

    default void setStatusEffect(String id) {
        this.setStatusEffect(id, 1);
    }

    default void setStatusEffect(String id, int duration) {
        this.setStatusEffect(id, duration, 0);
    }

    default void setStatusEffect(String id, int duration, int amplifier) {
        if (!this.hasStatusEffect(id)) {
            this.getStatusEffects().put(id, new ArrayList<>(Collections.nCopies(Pentamana.MANA_STATUS_EFFECT_AMPLIFIER_LIMIT + 1, 0)));
        }

        this.getStatusEffect(id).set(amplifier, duration);
    }

    default boolean addStatusEffect(String id) {
        return this.addStatusEffect(id, 1);
    }

    default boolean addStatusEffect(String id, int duration) {
        return this.addStatusEffect(id, duration, 0);
    }

    default boolean addStatusEffect(String id, int duration, int amplifier) {
        if (!this.hasStatusEffect(id)) {
            this.getStatusEffects().put(id, new ArrayList<>(Collections.nCopies(Pentamana.MANA_STATUS_EFFECT_AMPLIFIER_LIMIT + 1, 0)));
        }

        List<Integer> statusEffect = this.getStatusEffect(id);
        if (statusEffect.get(amplifier) < duration) {
            statusEffect.set(amplifier, duration);
            return true;
        }

        return false;
    }

    void setStatusEffect(Map<String, List<Integer>> statusEffects);
}

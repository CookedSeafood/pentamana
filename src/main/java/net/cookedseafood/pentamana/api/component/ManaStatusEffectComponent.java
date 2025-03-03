package net.cookedseafood.pentamana.api.component;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.ladysnake.cca.api.v3.component.Component;

public interface ManaStatusEffectComponent extends Component {
    void tick();

    Map<String, List<Integer>> getStatusEffect();

    default List<Integer> getStatusEffect(String id) {
        return this.getStatusEffect().get(id);
    }

    default boolean hasStatusEffect(String id) {
        List<Integer> statusEffect = this.getStatusEffect(id);
        return statusEffect != null ? statusEffect.stream().anyMatch(duration -> duration > 0) : false;
    }

    default int getActiveStatusEffectAmplifier(String id) {
        List<Integer> statusEffect = this.getStatusEffect(id);

        return IntStream.iterate(statusEffect.size() - 1, amplifier -> amplifier >= 0, amplifier -> --amplifier)
            .filter(amplifier -> statusEffect.get(amplifier) > 0)
            .findFirst()
            .orElse(-1);
    }

    default int getStatusEffectDuration(String id, int amplifier) {
        return this.getStatusEffect(id).get(amplifier);
    }

    default void setStatusEffect(String id) {
        this.setStatusEffect(id, 1);
    }

    default void setStatusEffect(String id, int duration) {
        this.setStatusEffect(id, duration, 0);
    }

    default void setStatusEffect(String id, int duration, int amplifier) {
        this.getStatusEffect(id).set(amplifier, duration);
    }

    default boolean addStatusEffect(String id) {
        return this.addStatusEffect(id, 1);
    }

    default boolean addStatusEffect(String id, int duration) {
        return this.addStatusEffect(id, duration, 0);
    }

    default boolean addStatusEffect(String id, int duration, int amplifier) {
        List<Integer> statusEffect = this.getStatusEffect(id);
        if (statusEffect.get(amplifier) < duration) {
            statusEffect.set(amplifier, duration);
            return true;
        }

        return false;
    }

    void setStatusEffect(Map<String, List<Integer>> statusEffects);
}

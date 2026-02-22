package net.hederamc.pentamana.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.hederamc.pentamana.api.ManaHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jspecify.annotations.Nullable;

public interface ManaEvents {
    Event<CalculateCapacityCallback> CALCULATE_CAPACITY = EventFactory.createArrayBacked(CalculateCapacityCallback.class,
        (listeners) -> (holder, capacity) -> {
            for (CalculateCapacityCallback listener : listeners) {
                InteractionResult result = listener.calculateCapacity(holder, capacity);

                if (result != InteractionResult.PASS) {
                    return result;
                }
            }

            return InteractionResult.PASS;
        }
    );

    Event<CalculateRegenerationCallback> CALCULATE_REGENERATION = EventFactory.createArrayBacked(CalculateRegenerationCallback.class,
        (listeners) -> (holder, regeneration) -> {
            for (CalculateRegenerationCallback listener : listeners) {
                InteractionResult result = listener.calculateRegeneration(holder, regeneration);

                if (result != InteractionResult.PASS) {
                    return result;
                }
            }

            return InteractionResult.PASS;
        }
    );

    Event<CalculateConsumptionCallback> CALCULATE_CONSUMPTION = EventFactory.createArrayBacked(CalculateConsumptionCallback.class,
        (listeners) -> (holder, consumption) -> {
            for (CalculateConsumptionCallback listener : listeners) {
                InteractionResult result = listener.calculateConsumption(holder, consumption);

                if (result != InteractionResult.PASS) {
                    return result;
                }
            }

            return InteractionResult.PASS;
        }
    );

    Event<CalculateDamageCallback> CALCULATE_DAMAGE = EventFactory.createArrayBacked(CalculateDamageCallback.class,
        (listeners) -> (holder, entity, damage) -> {
            for (CalculateDamageCallback listener : listeners) {
                InteractionResult result = listener.calculateDamage(holder, entity, damage);

                if (result != InteractionResult.PASS) {
                    return result;
                }
            }

            return InteractionResult.PASS;
        }
    );

    @FunctionalInterface
    public interface CalculateCapacityCallback {
        @Nullable
        InteractionResult calculateCapacity(ManaHolder holder, MutableFloat capacity);
    }

    @FunctionalInterface
    public interface CalculateRegenerationCallback {
        @Nullable
        InteractionResult calculateRegeneration(ManaHolder holder, MutableFloat regeneration);
    }

    @FunctionalInterface
    public interface CalculateConsumptionCallback {
        @Nullable
        InteractionResult calculateConsumption(ManaHolder holder, MutableFloat mana);
    }

    @FunctionalInterface
    public interface CalculateDamageCallback {
        @Nullable
        InteractionResult calculateDamage(ManaHolder holder, Entity entity, MutableFloat mana);
    }
}

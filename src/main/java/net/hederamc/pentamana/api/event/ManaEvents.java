package net.hederamc.pentamana.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.Nullable;

public interface ManaEvents {
    Event<CalculateCapacityCallback> CALCULATE_CAPACITY = EventFactory.createArrayBacked(CalculateCapacityCallback.class,
        (listeners) -> (livingEntity, capacity) -> {
            for (CalculateCapacityCallback listener : listeners) {
                InteractionResult result = listener.calculateCapacity(livingEntity, capacity);

                if (result != InteractionResult.PASS) {
                    return result;
                }
            }

            return InteractionResult.PASS;
        }
    );

    Event<CalculateRegenerationCallback> CALCULATE_REGENERATION = EventFactory.createArrayBacked(CalculateRegenerationCallback.class,
        (listeners) -> (livingEntity, regeneration) -> {
            for (CalculateRegenerationCallback listener : listeners) {
                InteractionResult result = listener.calculateRegeneration(livingEntity, regeneration);

                if (result != InteractionResult.PASS) {
                    return result;
                }
            }

            return InteractionResult.PASS;
        }
    );

    Event<CalculateConsumptionCallback> CALCULATE_CONSUMPTION = EventFactory.createArrayBacked(CalculateConsumptionCallback.class,
        (listeners) -> (livingEntity, consumption) -> {
            for (CalculateConsumptionCallback listener : listeners) {
                InteractionResult result = listener.calculateConsumption(livingEntity, consumption);

                if (result != InteractionResult.PASS) {
                    return result;
                }
            }

            return InteractionResult.PASS;
        }
    );

    Event<CalculateDamageCallback> CALCULATE_DAMAGE = EventFactory.createArrayBacked(CalculateDamageCallback.class,
        (listeners) -> (livingEntity, damage) -> {
            for (CalculateDamageCallback listener : listeners) {
                InteractionResult result = listener.calculateDamage(livingEntity, damage);

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
        InteractionResult calculateCapacity(LivingEntity livingEntity, MutableFloat capacity);
    }

    @FunctionalInterface
    public interface CalculateRegenerationCallback {
        @Nullable
        InteractionResult calculateRegeneration(LivingEntity livingEntity, MutableFloat regeneration);
    }

    @FunctionalInterface
    public interface CalculateConsumptionCallback {
        @Nullable
        InteractionResult calculateConsumption(LivingEntity livingEntity, MutableFloat mana);
    }

    @FunctionalInterface
    public interface CalculateDamageCallback {
        @Nullable
        InteractionResult calculateDamage(LivingEntity livingEntity, MutableFloat mana);
    }
}

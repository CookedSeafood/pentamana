package net.hederamc.pentamana.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;

public interface ConsumManaCallback {
    Event<ConsumManaCallback> EVENT = EventFactory.createArrayBacked(ConsumManaCallback.class,
        (callbacks) -> (entity) -> {
            for (ConsumManaCallback callback : callbacks) {
                ActionResult result = callback.interact(entity);

                if(result != ActionResult.PASS) {
                    return result;
                }
            }

        return ActionResult.PASS;
    });

    ActionResult interact(LivingEntity entity);
}

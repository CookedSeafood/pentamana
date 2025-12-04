package net.hederamc.pentamana.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;

public interface RegenManaCallback {
    Event<RegenManaCallback> EVENT = EventFactory.createArrayBacked(RegenManaCallback.class,
        (callbacks) -> (livingEntity) -> {
            for (RegenManaCallback callback : callbacks) {
                ActionResult result = callback.interact(livingEntity);

                if(result != ActionResult.PASS) {
                    return result;
                }
            }

        return ActionResult.PASS;
    });

    ActionResult interact(LivingEntity livingEntity);
}

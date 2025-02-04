package org.charcoalwhite.pentamana.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public interface RegenManaCallback {
    Event<RegenManaCallback> EVENT = EventFactory.createArrayBacked(RegenManaCallback.class,
        (callbacks) -> (player) -> {
            for (RegenManaCallback callback : callbacks) {
                ActionResult result = callback.interact(player);
 
                if(result != ActionResult.PASS) {
                    return result;
                }
            }
 
        return ActionResult.PASS;
    });
 
    ActionResult interact(PlayerEntity player);
}

package net.cookedseafood.pentamana.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public interface TickManaCallback {
    Event<TickManaCallback> EVENT = EventFactory.createArrayBacked(TickManaCallback.class,
        (callbacks) -> (player) -> {
            for (TickManaCallback callback : callbacks) {
                ActionResult result = callback.interact(player);
 
                if(result != ActionResult.PASS) {
                    return result;
                }
            }
 
        return ActionResult.PASS;
    });
 
    ActionResult interact(PlayerEntity player);
}

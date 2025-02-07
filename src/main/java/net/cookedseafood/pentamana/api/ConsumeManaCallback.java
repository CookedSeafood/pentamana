package net.cookedseafood.pentamana.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public interface ConsumeManaCallback {
    Event<ConsumeManaCallback> EVENT = EventFactory.createArrayBacked(ConsumeManaCallback.class,
        (callbacks) -> (player) -> {
            for (ConsumeManaCallback callback : callbacks) {
                ActionResult result = callback.interact(player);
 
                if(result != ActionResult.PASS) {
                    return result;
                }
            }
 
        return ActionResult.PASS;
    });
 
    ActionResult interact(PlayerEntity player);
}

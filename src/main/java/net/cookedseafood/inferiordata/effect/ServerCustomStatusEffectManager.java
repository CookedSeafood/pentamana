package net.cookedseafood.inferiordata.effect;

import java.util.Map;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;

/**
 * Tickable multi-id status effect container, with owner living entity.
 */
public final class ServerCustomStatusEffectManager extends CustomStatusEffectManager {
    private LivingEntity entity;

    public ServerCustomStatusEffectManager(LivingEntity entity, Map<CustomStatusEffectIdentifier, CustomStatusEffectPlaylist> statusEffects) {
        super(statusEffects);
        this.entity = entity;
    }

    public ServerCustomStatusEffectManager(LivingEntity entity, CustomStatusEffectManager manager) {
        this(entity, manager.statusEffects);
    }

    public ServerCustomStatusEffectManager(LivingEntity entity, NbtCompound nbtCompound) {
        this(entity, CustomStatusEffectManager.fromNbt(nbtCompound));
    }

    public ServerCustomStatusEffectManager(LivingEntity entity) {
        this(entity, entity.getCustomStatusEffects());
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    public void setEntity(LivingEntity entity) {
        this.entity = entity;
    }

    public ServerCustomStatusEffectManager withEntity(LivingEntity entity) {
        this.setEntity(entity);
        return this;
    }

    /**
     * A shadow copy.
     * 
     * @return a new ServerCustomStatusEffectManager
     * 
     * @see #deepCopy()
     */
    @Override
    public ServerCustomStatusEffectManager copy() {
        return new ServerCustomStatusEffectManager(this.entity, super.copy());
    }

    /**
     * A deep copy.
     * 
     * @return a new ServerCustomStatusEffectManager
     * 
     * @see #copy()
     */
    @Override
    public ServerCustomStatusEffectManager deepCopy() {
        return new ServerCustomStatusEffectManager(this.entity, super.deepCopy());
    }
}

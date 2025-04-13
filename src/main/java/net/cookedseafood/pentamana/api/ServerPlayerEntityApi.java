package net.cookedseafood.pentamana.api;

import net.minecraft.entity.Entity;

public interface ServerPlayerEntityApi {
	default float getCastingDamageAgainst(Entity entity, float baseDamage) {
		return 0;
	}
}

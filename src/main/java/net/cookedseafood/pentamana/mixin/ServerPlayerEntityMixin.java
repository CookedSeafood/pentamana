package net.cookedseafood.pentamana.mixin;

import net.cookedseafood.inferiordata.component.CustomStatusEffectManagerComponentInstance;
import net.cookedseafood.inferiordata.effect.CustomStatusEffectManager;
import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.api.ServerPlayerEntityApi;
import net.cookedseafood.pentamana.attribute.PentamanaAttributeIdentifiers;
import net.cookedseafood.pentamana.component.ManaPreferenceComponentInstance;
import net.cookedseafood.pentamana.component.ServerManaBarComponentInstance;
import net.cookedseafood.pentamana.effect.PentamanaStatusEffectIdentifiers;
import net.cookedseafood.pentamana.enchantment.PentamanaEnchantmentIdentifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements ServerPlayerEntityApi {
    @Inject(
        method = "tick()V",
        at = @At("RETURN")
    )
    private void tickMana(CallbackInfo info) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;

        if (ManaPreferenceComponentInstance.MANA_PREFERENCE.get(player).isEnabled() == false && !Pentamana.isForceEnabled) {
            return;
        }

        CustomStatusEffectManagerComponentInstance.CUSTOM_STATUS_EFFECT_MANAGER.get(player).getStatusEffectManager().tick();
        ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar().tick(player);
    }

    @Override
    public float getCastingDamageAgainst(Entity entity, float baseDamage) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        CustomStatusEffectManager statusEffectManager = CustomStatusEffectManagerComponentInstance.CUSTOM_STATUS_EFFECT_MANAGER.get(player).getStatusEffectManager();

        float manaCapacity = ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar().getCapacity();
        int potencyLevel = player.getWeaponStack().getEnchantments().getLevel(PentamanaEnchantmentIdentifiers.POTENCY);

        float castingDamage = manaCapacity;
        castingDamage /= Pentamana.manaCapacityBase;
        castingDamage *= (float)player.getCustomModifiedValue(PentamanaAttributeIdentifiers.CASTING_DAMAGE, baseDamage);
        castingDamage += potencyLevel != 0 ? ++potencyLevel * Pentamana.enchantmentPotencyBase / Integer.MAX_VALUE : 0;
        castingDamage += statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_POWER) ? (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_POWER) + 1) * Pentamana.statusEffectManaPowerBase : 0;
        castingDamage -= statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_SICKNESS) ? (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_SICKNESS) + 1) * Pentamana.statusEffectManaSicknessBase : 0;
        castingDamage = Math.max(castingDamage, 0.0f);
        castingDamage *= entity instanceof WitchEntity ? 0.15f : 1;
        return castingDamage;
    }

    @Shadow
    public abstract ServerCommandSource getCommandSource();
}

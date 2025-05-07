package net.cookedseafood.inferiordata.mixin;

import net.cookedseafood.genericregistry.registry.Registries;
import net.cookedseafood.inferiordata.component.CustomStatusEffectManagerComponentInstance;
import net.cookedseafood.inferiordata.effect.CustomStatusEffect;
import net.cookedseafood.inferiordata.effect.CustomStatusEffectIdentifier;
import net.cookedseafood.inferiordata.effect.CustomStatusEffectManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Inject(
        method = "consumeItem()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;isEmpty()Z",
            shift = At.Shift.AFTER
        )
    )
    private void applyPentamanaStatusEffects(CallbackInfo info) {
        CustomStatusEffectManager statusEffectManager = CustomStatusEffectManagerComponentInstance.CUSTOM_STATUS_EFFECT_MANAGER.get((ServerPlayerEntity)(Object)this).getStatusEffectManager();
        ((ServerPlayerEntity)(Object)this).getActiveItem().getCustomStatusEffects().stream()
            .map(NbtCompound.class::cast)
            .forEach(presentedStatusEffect -> statusEffectManager.add(
                new CustomStatusEffect(
                    Registries.get(CustomStatusEffectIdentifier.class, Identifier.of(presentedStatusEffect.getString("id"))),
                    presentedStatusEffect.getInt("duration"),
                    presentedStatusEffect.getInt("amplifier")
                )
            ));
    }
}

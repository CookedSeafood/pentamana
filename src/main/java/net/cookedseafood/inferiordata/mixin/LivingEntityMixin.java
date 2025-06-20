package net.cookedseafood.inferiordata.mixin;

import java.util.function.Consumer;
import java.util.function.Function;
import net.cookedseafood.inferiordata.api.LivingEntityApi;
import net.cookedseafood.inferiordata.effect.CustomStatusEffect;
import net.cookedseafood.inferiordata.effect.CustomStatusEffectIdentifier;
import net.cookedseafood.inferiordata.effect.CustomStatusEffectPlaylist;
import net.cookedseafood.inferiordata.effect.ServerCustomStatusEffectManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements LivingEntityApi {
    @Inject(
        method = "tick()V",
        at = @At("RETURN")
    )
    private void tickCustomStatusEffect(CallbackInfo info) {
        ((LivingEntity)(Object)this).tickCustomStatusEffect();
    }

    @Inject(
        method = "consumeItem()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;finishUsing(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;",
            shift = At.Shift.AFTER
        )
    )
    private void applyCustomStatusEffects(CallbackInfo info) {
        ((LivingEntity)(Object)this).getActiveItem().getCustomStatusEffects().stream()
            .map(NbtCompound.class::cast)
            .map(CustomStatusEffect::fromNbt)
            .forEach(this::addCustomStatusEffect);
    }

    @Override
    public NbtCompound getCustomStatusEffects() {
        return ((LivingEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getCompoundOrEmpty("status_effect");
    }

    @Override
    public void setCustomStatusEffects(NbtCompound customStatusEffects) {
        ((LivingEntity)(Object)this).setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(
            ((LivingEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().copyFrom(customStatusEffects)
        ));
    }

    @Override
    public ServerCustomStatusEffectManager getCustomStatusEffectManager() {
        return new ServerCustomStatusEffectManager((LivingEntity)(Object)this);
    }

    @Override
    public boolean hasCustomStatusEffect(CustomStatusEffectIdentifier id) {
        return this.getCustomStatusEffectManager().containsKey(id);
    }

    @Override
    public <T> T modifyCustomStatusEffectManager(Function<ServerCustomStatusEffectManager, T> action) {
        ServerCustomStatusEffectManager manager = this.getCustomStatusEffectManager();

        T result = action.apply(manager);

        this.setCustomStatusEffects(manager.toNbt());
        return result;
    }

    @Override
    public void modifyCustomStatusEffectManager(Consumer<ServerCustomStatusEffectManager> action) {
        ServerCustomStatusEffectManager manager = this.getCustomStatusEffectManager();

        action.accept(manager);

        this.setCustomStatusEffects(manager.toNbt());
    }

    @Override
    public boolean addCustomStatusEffect(CustomStatusEffect statusEffect) {
        return this.modifyCustomStatusEffectManager((Function<ServerCustomStatusEffectManager, Boolean>)statusEffect::addTo);
    }

    @Override
    public boolean setCustomStatusEffect(CustomStatusEffect statusEffect) {
        return this.modifyCustomStatusEffectManager((Function<ServerCustomStatusEffectManager, Boolean>)statusEffect::setTo);
    }

    @Override
    public CustomStatusEffectPlaylist removeCustomStatusEffect(CustomStatusEffectIdentifier id) {
        return this.modifyCustomStatusEffectManager((Function<ServerCustomStatusEffectManager, CustomStatusEffectPlaylist>)id::removeFrom);
    }

    @Override
    public void clearCustomStatusEffect() {
        this.modifyCustomStatusEffectManager(ServerCustomStatusEffectManager::clear);
    }

    @Override
    public void tickCustomStatusEffect() {
        this.modifyCustomStatusEffectManager(ServerCustomStatusEffectManager::tick);
    }

    @Override
    public double getCustomModifiedValue(String attribute, double base) {
        NbtList modifiers = this.getCustomModifiers(attribute);
        MutableDouble modified = new MutableDouble(base);

        modifiers.stream()
            .map(nbtElement -> (NbtCompound)nbtElement)
            .filter(modifier -> "add_value".equals(modifier.getString("operation", "add_value")))
            .forEach(modifier -> modified.add(modifier.getDouble("base", 0d)));

        MutableDouble multiplier = new MutableDouble(1);

        modifiers.stream()
            .map(nbtElement -> (NbtCompound)nbtElement)
            .filter(modifier -> "add_multiplied_base".equals(modifier.getString("operation", "add_value")))
            .forEach(modifier -> multiplier.add(modifier.getDouble("base", 0d)));

        modified.setValue(modified.getValue() * multiplier.getValue());

        modifiers.stream()
            .map(nbtElement -> (NbtCompound)nbtElement)
            .filter(modifier -> "add_multiplied_total".equals(modifier.getString("operation", "add_value")))
            .forEach(modifier -> modified.setValue((1 + modifier.getDouble("base", 0d)) * modified.getValue()));

        return modified.doubleValue();
    }

    @Override
    public NbtList getCustomModifiers(String attribute) {
        NbtList modifiers = new NbtList();
        this.getCustomModifiers().stream().map(nbtElement -> (NbtCompound)nbtElement).filter(modifier -> attribute.equals(modifier.getString("attribute", ""))).forEach(modifier -> modifiers.add(modifier));
        return modifiers;
    }

    @Override
    public NbtList getCustomModifiers() {
        NbtList modifiers = new NbtList();
        this.getEquippedStack(EquipmentSlot.MAINHAND)   .getCustomModifiers().stream().map(nbtElement -> (NbtCompound)nbtElement).filter(modifier -> "mainhand" .equals(modifier.getString("slot", "mainhand"))).forEach(modifier -> modifiers.add(modifier));
        this.getEquippedStack(EquipmentSlot.OFFHAND)    .getCustomModifiers().stream().map(nbtElement -> (NbtCompound)nbtElement).filter(modifier -> "offhand"  .equals(modifier.getString("slot", "mainhand"))).forEach(modifier -> modifiers.add(modifier));
        this.getEquippedStack(EquipmentSlot.FEET)       .getCustomModifiers().stream().map(nbtElement -> (NbtCompound)nbtElement).filter(modifier -> "feet"     .equals(modifier.getString("slot", "mainhand"))).forEach(modifier -> modifiers.add(modifier));
        this.getEquippedStack(EquipmentSlot.LEGS)       .getCustomModifiers().stream().map(nbtElement -> (NbtCompound)nbtElement).filter(modifier -> "legs"     .equals(modifier.getString("slot", "mainhand"))).forEach(modifier -> modifiers.add(modifier));
        this.getEquippedStack(EquipmentSlot.CHEST)      .getCustomModifiers().stream().map(nbtElement -> (NbtCompound)nbtElement).filter(modifier -> "chest"    .equals(modifier.getString("slot", "mainhand"))).forEach(modifier -> modifiers.add(modifier));
        this.getEquippedStack(EquipmentSlot.HEAD)       .getCustomModifiers().stream().map(nbtElement -> (NbtCompound)nbtElement).filter(modifier -> "head"     .equals(modifier.getString("slot", "mainhand"))).forEach(modifier -> modifiers.add(modifier));
        return modifiers;
    }

    @Shadow
    public abstract ItemStack getEquippedStack(EquipmentSlot slot);
}

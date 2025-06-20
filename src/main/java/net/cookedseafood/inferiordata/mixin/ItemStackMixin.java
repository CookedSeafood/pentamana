package net.cookedseafood.inferiordata.mixin;

import net.cookedseafood.inferiordata.api.ItemStackApi;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackApi {
    @Override
    public String getCustomId() {
        return ((ItemStack)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getString("id", "");
    }

    @Override
    public String getCustomIdOrId() {
        String customId = this.getCustomId();
        return customId == "" ? ((ItemStack)(Object)this).getIdAsString() : customId;
    }

    @Override
    public NbtList getCustomModifiers() {
        return ((ItemStack)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getListOrEmpty("modifiers");
    }

    @Override
    public NbtList getCustomStatusEffects() {
        return ((ItemStack)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getListOrEmpty("status_effects");
    }
}

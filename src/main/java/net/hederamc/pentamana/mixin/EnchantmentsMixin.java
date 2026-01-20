package net.hederamc.pentamana.mixin;

import net.hederamc.pentamana.api.PentamanaEnchantments;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Enchantments.class)
public abstract class EnchantmentsMixin implements PentamanaEnchantments {
}

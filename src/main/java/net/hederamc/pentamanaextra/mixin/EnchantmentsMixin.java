package net.hederamc.pentamanaextra.mixin;

import net.hederamc.pentamanaextra.api.PentamanaEnchantments;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Enchantments.class)
public abstract class EnchantmentsMixin implements PentamanaEnchantments {
}

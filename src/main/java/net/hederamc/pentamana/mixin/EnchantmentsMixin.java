package net.hederamc.pentamana.mixin;

import net.hederamc.pentamana.api.EnchantmentsApi;
import net.minecraft.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Enchantments.class)
public abstract class EnchantmentsMixin implements EnchantmentsApi {
}

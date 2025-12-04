package net.hederamc.pentamana.api;

import net.hederamc.pentamana.Pentamana;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public interface EnchantmentsApi {
    public static final RegistryKey<Enchantment> CAPACITY = of("capacity");
    public static final RegistryKey<Enchantment> POTENCY = of("potency");
    public static final RegistryKey<Enchantment> STREAM = of("stream");
    public static final RegistryKey<Enchantment> MANA_EFFICIENCY = of("mana_efficiency");

    private static RegistryKey<Enchantment> of(String id) {
        return RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(Pentamana.MOD_NAMESPACE, id));
    }
}

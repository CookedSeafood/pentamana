package net.hederamc.pentamanaextra.api;

import net.hederamc.pentamana.Pentamana;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

public interface PentamanaEnchantments {
    ResourceKey<Enchantment> CAPACITY = key("capacity");
    ResourceKey<Enchantment> POTENCY = key("potency");
    ResourceKey<Enchantment> STREAM = key("stream");
    ResourceKey<Enchantment> MANA_EFFICIENCY = key("mana_efficiency");

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(Pentamana.MOD_NAMESPACE, id));
    }
}

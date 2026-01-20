package net.hederamc.pentamana.effect;

import net.hederamc.generalcustomdata.effect.CustomStatusEffectIdentifier;
import net.hederamc.genericregistry.registry.Registries;
import net.hederamc.pentamana.Pentamana;
import net.minecraft.resources.Identifier;

public abstract class PentamanaStatusEffectIdentifiers {
    public static final CustomStatusEffectIdentifier MANA_BOOST = Registries.register(
            Identifier.fromNamespaceAndPath(Pentamana.MOD_NAMESPACE, "mana_boost"),
            new CustomStatusEffectIdentifier(
                    Identifier.fromNamespaceAndPath(Pentamana.MOD_NAMESPACE, "mana_boost"),
                    13195725));
    public static final CustomStatusEffectIdentifier MANA_REDUCTION = Registries.register(
            Identifier.fromNamespaceAndPath(Pentamana.MOD_NAMESPACE, "mana_reduction"),
            new CustomStatusEffectIdentifier(
                    Identifier.fromNamespaceAndPath(Pentamana.MOD_NAMESPACE, "mana_reduction"),
                    203345));
    public static final CustomStatusEffectIdentifier INSTANT_MANA = Registries.register(
            Identifier.fromNamespaceAndPath(Pentamana.MOD_NAMESPACE, "instant_mana"),
            new CustomStatusEffectIdentifier(
                    Identifier.fromNamespaceAndPath(Pentamana.MOD_NAMESPACE, "instant_mana"),
                    6629287));
    public static final CustomStatusEffectIdentifier INSTANT_DEPLETE = Registries.register(
            Identifier.fromNamespaceAndPath(Pentamana.MOD_NAMESPACE, "instant_deplete"),
            new CustomStatusEffectIdentifier(
                    Identifier.fromNamespaceAndPath(Pentamana.MOD_NAMESPACE, "instant_deplete"),
                    11022655));
    public static final CustomStatusEffectIdentifier MANA_POWER = Registries.register(
            Identifier.fromNamespaceAndPath(Pentamana.MOD_NAMESPACE, "mana_power"),
            new CustomStatusEffectIdentifier(
                    Identifier.fromNamespaceAndPath(Pentamana.MOD_NAMESPACE, "mana_power"),
                    5201300));
    public static final CustomStatusEffectIdentifier MANA_SICKNESS = Registries.register(
            Identifier.fromNamespaceAndPath(Pentamana.MOD_NAMESPACE, "mana_sickness"),
            new CustomStatusEffectIdentifier(
                    Identifier.fromNamespaceAndPath(Pentamana.MOD_NAMESPACE, "mana_sickness"),
                    9577997));
    public static final CustomStatusEffectIdentifier MANA_REGENERATION = Registries.register(
            Identifier.fromNamespaceAndPath(Pentamana.MOD_NAMESPACE, "mana_regeneration"),
            new CustomStatusEffectIdentifier(
                    Identifier.fromNamespaceAndPath(Pentamana.MOD_NAMESPACE, "mana_regeneration"),
                    7401408));
    public static final CustomStatusEffectIdentifier MANA_INHIBITION = Registries.register(
            Identifier.fromNamespaceAndPath(Pentamana.MOD_NAMESPACE, "mana_inhibition"),
            new CustomStatusEffectIdentifier(
                    Identifier.fromNamespaceAndPath(Pentamana.MOD_NAMESPACE, "mana_inhibition"),
                    15844503));
}

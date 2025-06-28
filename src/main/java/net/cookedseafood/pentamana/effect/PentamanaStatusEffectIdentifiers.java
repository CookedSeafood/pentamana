package net.cookedseafood.pentamana.effect;

import net.cookedseafood.genericregistry.registry.Registries;
import net.cookedseafood.generalcustomdata.effect.CustomStatusEffectIdentifier;
import net.cookedseafood.pentamana.Pentamana;
import net.minecraft.util.Identifier;

public abstract class PentamanaStatusEffectIdentifiers {
    public static final CustomStatusEffectIdentifier MANA_BOOST =
        Registries.register(
            Identifier.of(Pentamana.MOD_NAMESPACE, "mana_boost"),
            new CustomStatusEffectIdentifier(
                Identifier.of(Pentamana.MOD_NAMESPACE, "mana_boost"),
                "Mana Boost",
                13195725
            )
        );
    public static final CustomStatusEffectIdentifier MANA_REDUCTION =
        Registries.register(
            Identifier.of(Pentamana.MOD_NAMESPACE, "mana_reduction"),
            new CustomStatusEffectIdentifier(
                Identifier.of(Pentamana.MOD_NAMESPACE, "mana_reduction"),
                "Mana Reduction",
                203345
            )
        );
    public static final CustomStatusEffectIdentifier INSTANT_MANA =
        Registries.register(
            Identifier.of(Pentamana.MOD_NAMESPACE, "instant_mana"),
            new CustomStatusEffectIdentifier(
                Identifier.of(Pentamana.MOD_NAMESPACE, "instant_mana"),
                "Instant Mana",
                6629287
            )
        );
    public static final CustomStatusEffectIdentifier INSTANT_DEPLETE =
        Registries.register(
            Identifier.of(Pentamana.MOD_NAMESPACE, "instant_deplete"),
            new CustomStatusEffectIdentifier(
                Identifier.of(Pentamana.MOD_NAMESPACE, "instant_deplete"),
                "Instant Deplete",
                11022655
            )
        );
    public static final CustomStatusEffectIdentifier MANA_POWER =
        Registries.register(
            Identifier.of(Pentamana.MOD_NAMESPACE, "mana_power"),
            new CustomStatusEffectIdentifier(
                Identifier.of(Pentamana.MOD_NAMESPACE, "mana_power"),
                "Mana Power",
                5201300
            )
        );
    public static final CustomStatusEffectIdentifier MANA_SICKNESS =
        Registries.register(
            Identifier.of(Pentamana.MOD_NAMESPACE, "mana_sickness"),
            new CustomStatusEffectIdentifier(
                Identifier.of(Pentamana.MOD_NAMESPACE, "mana_sickness"),
                "Mana Sickness",
                9577997
            )
        );
    public static final CustomStatusEffectIdentifier MANA_REGENERATION =
        Registries.register(
            Identifier.of(Pentamana.MOD_NAMESPACE, "mana_regeneration"),
            new CustomStatusEffectIdentifier(
                Identifier.of(Pentamana.MOD_NAMESPACE, "mana_regeneration"),
                "Mana Regeneration",
                7401408)
        );
    public static final CustomStatusEffectIdentifier MANA_INHIBITION =
        Registries.register(
            Identifier.of(Pentamana.MOD_NAMESPACE, "mana_inhibition"),
            new CustomStatusEffectIdentifier(
                Identifier.of(Pentamana.MOD_NAMESPACE, "mana_inhibition"),
                "Mana Inhibition",
                15844503
            )
        );
}

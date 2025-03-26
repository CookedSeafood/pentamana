package net.cookedseafood.pentamana.mana;

import net.cookedseafood.pentamana.Pentamana;
import net.minecraft.util.Identifier;

public class ManaStatusEffects {
    public static final ManaStatusEffect MANA_BOOST = ManaStatusEffect.of(Identifier.of(Pentamana.MOD_ID, "mana_boost"));
    public static final ManaStatusEffect MANA_REDUCTION = ManaStatusEffect.of(Identifier.of(Pentamana.MOD_ID, "mana_reduction"));
    public static final ManaStatusEffect INSTANT_MANA = ManaStatusEffect.of(Identifier.of(Pentamana.MOD_ID, "instant_mana"));
    public static final ManaStatusEffect INSTANT_DEPLETE = ManaStatusEffect.of(Identifier.of(Pentamana.MOD_ID, "instant_deplete"));
    public static final ManaStatusEffect MANA_POWER = ManaStatusEffect.of(Identifier.of(Pentamana.MOD_ID, "mana_power"));
    public static final ManaStatusEffect MANA_SICKNESS = ManaStatusEffect.of(Identifier.of(Pentamana.MOD_ID, "mana_sickness"));
    public static final ManaStatusEffect MANA_REGENERATION = ManaStatusEffect.of(Identifier.of(Pentamana.MOD_ID, "mana_regeneration"));
    public static final ManaStatusEffect MANA_INHIBITION = ManaStatusEffect.of(Identifier.of(Pentamana.MOD_ID, "mana_inhibition"));
}

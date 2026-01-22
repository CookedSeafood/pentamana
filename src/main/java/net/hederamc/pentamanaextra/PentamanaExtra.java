package net.hederamc.pentamanaextra;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.fabricmc.api.ModInitializer;
import net.hederamc.generalcustomdata.effect.CustomStatusEffectManager;
import net.hederamc.pentamana.api.event.ManaEvents;
import net.hederamc.pentamanaextra.attribute.PentamanaAttributeIdentifiers;
import net.hederamc.pentamanaextra.config.PentamanaExtraConfig;
import net.hederamc.pentamanaextra.effect.PentamanaStatusEffectIdentifiers;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PentamanaExtra implements ModInitializer {
    public static final String MOD_ID = "pentamana";
    public static final String MOD_NAMESPACE = "pentamana";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final PentamanaExtraConfig CONFIG = PentamanaExtraConfig.HANDLER.instance();
    public static final PentamanaExtraConfig DEFAULTS = PentamanaExtraConfig.HANDLER.defaults();

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        ManaEvents.CALCULATE_CAPACITY.register((livingEntity, capacity) -> {
            CustomStatusEffectManager statusEffectManager = livingEntity.getCustomStatusEffectManager();
            float f = 0.0f;

            f += livingEntity.getCustomModifiedValue(PentamanaAttributeIdentifiers.MANA_CAPACITY, capacity.doubleValue());

            for (Entry<Holder<Enchantment>> entry : livingEntity.getEnchantments(Enchantments.CAPACITY)) {
                f += CONFIG.enchantmentCapacityBase * (entry.getIntValue() + 1);
            }

            f += (statusEffectManager.contains(PentamanaStatusEffectIdentifiers.MANA_BOOST)
                    ? CONFIG.statusEffectManaBoostBase * (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_BOOST) + 1)
                    : 0.0f);
            f -= (statusEffectManager.contains(PentamanaStatusEffectIdentifiers.MANA_REDUCTION)
                    ? CONFIG.statusEffectManaReductionBase * (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_REDUCTION) + 1)
                    : 0.0f);
            f += (CONFIG.shouldConvertExperienceLevel && livingEntity instanceof ServerPlayer
                    ? CONFIG.experienceLevelConversionBase * ((ServerPlayer) livingEntity).experienceLevel
                    : 0.0f);
            f = Math.max(f, 0.0f);
            capacity.setValue(f);
            return InteractionResult.PASS;
        });

        ManaEvents.CALCULATE_REGENERATION.register((livingEntity, regeneration) -> {
            CustomStatusEffectManager statusEffectManager = livingEntity.getCustomStatusEffectManager();
            float f = 0.0f;

            f += livingEntity.getCustomModifiedValue(PentamanaAttributeIdentifiers.MANA_REGENERATION, regeneration.doubleValue());

            for (Entry<Holder<Enchantment>> entry : livingEntity.getEnchantments(Enchantments.STREAM)) {
                f += CONFIG.enchantmentStreamBase * (entry.getIntValue() + 1);
            }

            f += (statusEffectManager.contains(PentamanaStatusEffectIdentifiers.INSTANT_MANA)
                    ? CONFIG.statusEffectInstantManaBase * (float) Math.pow(2.0, statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.INSTANT_MANA))
                    : 0.0f);
            f -= (statusEffectManager.contains(PentamanaStatusEffectIdentifiers.INSTANT_DEPLETE)
                    ? CONFIG.statusEffectInstantDepleteBase * (float) Math.pow(2.0, statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.INSTANT_DEPLETE))
                    : 0.0f);
            f += (statusEffectManager.contains(PentamanaStatusEffectIdentifiers.MANA_REGENERATION)
                    ? 1.0f / Math.max(1, CONFIG.statusEffectManaRegenerationBase >> statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_REGENERATION))
                    : 0.0f);
            f -= (statusEffectManager.contains(PentamanaStatusEffectIdentifiers.MANA_INHIBITION)
                    ? 1.0f / Math.max(1, CONFIG.statusEffectManaInhibitionBase >> statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_INHIBITION))
                    : 0.0f);
            regeneration.setValue(f);
            return InteractionResult.PASS;
        });

        ManaEvents.CALCULATE_CONSUMPTION.register((livingEntity, consumption) -> {
            float f = 0.0f;

            f += livingEntity.getCustomModifiedValue(PentamanaAttributeIdentifiers.MANA_CONSUMPTION, consumption.doubleValue());

            for (Entry<Holder<Enchantment>> entry : livingEntity.getEnchantments(Enchantments.MANA_EFFICIENCY)) {
                f *= 1.0f - CONFIG.enchantmentManaEfficiencyBase * (entry.getIntValue() + 1);
            }

            consumption.setValue(f);
            return InteractionResult.PASS;
        });

        ManaEvents.CALCULATE_DAMAGE.register((livingEntity, damage) -> {
            CustomStatusEffectManager statusEffectManager = livingEntity.getCustomStatusEffectManager();
            float f = 0.0f;

            f += livingEntity.getCustomModifiedValue(PentamanaAttributeIdentifiers.CASTING_DAMAGE, damage.doubleValue());

            for (Entry<Holder<Enchantment>> entry : livingEntity.getEnchantments(Enchantments.POTENCY)) {
                f += CONFIG.enchantmentPotencyBase * (entry.getIntValue() + 1);
            }

            f += statusEffectManager.contains(PentamanaStatusEffectIdentifiers.MANA_POWER)
                    ? (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_POWER) + 1) * CONFIG.statusEffectManaPowerBase
                    : 0.0f;
            f -= statusEffectManager.contains(PentamanaStatusEffectIdentifiers.MANA_SICKNESS)
                    ? (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_SICKNESS) + 1) * CONFIG.statusEffectManaSicknessBase
                    : 0.0f;
            f *= livingEntity instanceof Witch
                    ? 0.15f
                    : 1.0f;
            f = Math.max(f, 0.0f);
            damage.setValue(f);
            return InteractionResult.PASS;
        });
    }
}

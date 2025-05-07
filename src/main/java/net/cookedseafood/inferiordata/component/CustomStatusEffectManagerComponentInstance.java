package net.cookedseafood.inferiordata.component;

import net.cookedseafood.inferiordata.InferiorData;
import net.cookedseafood.inferiordata.api.component.CustomStatusEffectManagerComponent;
import net.cookedseafood.inferiordata.effect.CustomStatusEffectManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.BoggedEntity;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.entity.mob.CaveSpiderEntity;
import net.minecraft.entity.mob.CreakingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.IllusionerEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.StrayEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.mob.VindicatorEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.mob.ZoglinEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieHorseEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.passive.ArmadilloEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.MuleEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.SalmonEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.SnifferEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.entity.passive.TadpoleEntity;
import net.minecraft.entity.passive.TraderLlamaEntity;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnableComponent;

public class CustomStatusEffectManagerComponentInstance implements CustomStatusEffectManagerComponent, EntityComponentInitializer, RespawnableComponent<CustomStatusEffectManagerComponentInstance> {
    public static final ComponentKey<CustomStatusEffectManagerComponentInstance> CUSTOM_STATUS_EFFECT_MANAGER =
        ComponentRegistry.getOrCreate(Identifier.of(InferiorData.MOD_NAMESPACE, "custom_status_effect_manager"), CustomStatusEffectManagerComponentInstance.class);
    private CustomStatusEffectManager statusEffectManager;

    public CustomStatusEffectManagerComponentInstance() {
    }

    public CustomStatusEffectManagerComponentInstance(Entity entity) {
        this.statusEffectManager = new CustomStatusEffectManager();
    }

    @Override
    public CustomStatusEffectManager getStatusEffectManager() {
        return this.statusEffectManager;
    }

    @Override
    public void setStatusEffectManager(CustomStatusEffectManager statusEffectManager) {
        this.statusEffectManager = statusEffectManager;
    }

    @Override
    public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        if (!nbtCompound.isEmpty()) {
            this.statusEffectManager = CustomStatusEffectManager.fromNbt(nbtCompound, wrapperLookup);
        }
    }

    @Override
    public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        nbtCompound.copyFrom(this.statusEffectManager.toNbt(wrapperLookup));
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(PlayerEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(ArmorStandEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(BatEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(GhastEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(PhantomEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(AllayEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(IronGolemEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(ShulkerEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(SnowGolemEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(PiglinBruteEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(PiglinEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(BoggedEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(SkeletonEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(StrayEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(WitherSkeletonEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(BlazeEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(BreezeEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(CreakingEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(CreeperEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(EndermanEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(EndermiteEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(GiantEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(GuardianEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(ElderGuardianEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(PillagerEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(EvokerEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(IllusionerEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(VindicatorEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(RavagerEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(WitchEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(SilverfishEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(SpiderEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(CaveSpiderEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(VexEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(WardenEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(ZoglinEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(ZombieEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(DrownedEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(HuskEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(ZombieVillagerEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(ZombifiedPiglinEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(DonkeyEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(LlamaEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(TraderLlamaEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(MuleEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(CamelEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(HorseEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(SkeletonHorseEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(ZombieHorseEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(ArmadilloEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(AxolotlEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(BeeEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(ChickenEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(CowEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(MooshroomEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(FoxEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(FrogEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(GoatEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(HoglinEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(OcelotEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(PandaEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(PigEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(PolarBearEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(RabbitEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(SheepEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(SnifferEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(StriderEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(CatEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(ParrotEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(WolfEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(TurtleEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(DolphinEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(SquidEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(GlowSquidEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(PufferfishEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(CodEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(SalmonEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(TropicalFishEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(TadpoleEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(SlimeEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
        registry.registerFor(MagmaCubeEntity.class, CUSTOM_STATUS_EFFECT_MANAGER, CustomStatusEffectManagerComponentInstance::new);
    }
}

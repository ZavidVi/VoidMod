package com.zavidvi.voidmod.registry;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.entity.supervoid.TentacleEntity;
import com.zavidvi.voidmod.entity.voidsphere.VoidSphereEntity;
import com.zavidvi.voidmod.entity.voidsphere.VoidSphereProjectileEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, VoidMod.MOD_ID);

    public static final float VOID_SPHERE_HITBOX_WIDTH = 9.0F / 16.0F;
    public static final float VOID_SPHERE_HITBOX_HEIGHT = 8.0F / 16.0F;

    public static final DeferredHolder<EntityType<?>, EntityType<TentacleEntity>> TENTACLE =
            ENTITY_TYPES.register("tentacle", () ->
                    EntityType.Builder.<TentacleEntity>of(TentacleEntity::new, MobCategory.MISC)
                            .sized(1.5F, 4.0F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(key("tentacle"))
            );

    public static final DeferredHolder<EntityType<?>, EntityType<VoidSphereEntity>> VOID_SPHERE =
            ENTITY_TYPES.register("void_sphere", () ->
                    EntityType.Builder.<VoidSphereEntity>of(VoidSphereEntity::new, MobCategory.MONSTER)
                            .sized(VOID_SPHERE_HITBOX_WIDTH, VOID_SPHERE_HITBOX_HEIGHT)
                            .clientTrackingRange(8)
                            .build(key("void_sphere"))
            );

    public static final DeferredHolder<EntityType<?>, EntityType<VoidSphereProjectileEntity>> VOID_SPHERE_PROJECTILE =
            ENTITY_TYPES.register("void_sphere_projectile", () ->
                    EntityType.Builder.<VoidSphereProjectileEntity>of(VoidSphereProjectileEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(key("void_sphere_projectile"))
            );

    public static final DeferredHolder<EntityType<?>, EntityType<com.zavidvi.voidmod.entity.vrauj.VraujEntity>> VRAUJ =
            ENTITY_TYPES.register("vrauj", () ->
                    EntityType.Builder.<com.zavidvi.voidmod.entity.vrauj.VraujEntity>of(
                                    com.zavidvi.voidmod.entity.vrauj.VraujEntity::new, MobCategory.MONSTER)
                            .sized(38.0F / 16.0F, 14.0F / 16.0F)
                            .clientTrackingRange(10)
                            .build(key("vrauj"))
            );

    public static final DeferredHolder<EntityType<?>, EntityType<com.zavidvi.voidmod.entity.vrauj.VraujProjectileEntity>> VRAUJ_PROJECTILE =
            ENTITY_TYPES.register("vrauj_projectile", () ->
                    EntityType.Builder.<com.zavidvi.voidmod.entity.vrauj.VraujProjectileEntity>of(
                                    com.zavidvi.voidmod.entity.vrauj.VraujProjectileEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(key("vrauj_projectile"))
            );

    public static final DeferredHolder<EntityType<?>, EntityType<com.zavidvi.voidmod.entity.rime.RimeProjectileEntity>> RIME_PROJECTILE =
            ENTITY_TYPES.register("rime_projectile", () ->
                    EntityType.Builder.<com.zavidvi.voidmod.entity.rime.RimeProjectileEntity>of(
                                    com.zavidvi.voidmod.entity.rime.RimeProjectileEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(key("rime_projectile"))
            );

    public static final DeferredHolder<EntityType<?>, EntityType<com.zavidvi.voidmod.entity.reaper.ReaperLvl1Entity>> REAPER_LVL1 =
            ENTITY_TYPES.register("reaper_lvl1", () ->
                    EntityType.Builder.<com.zavidvi.voidmod.entity.reaper.ReaperLvl1Entity>of(
                                    com.zavidvi.voidmod.entity.reaper.ReaperLvl1Entity::new, MobCategory.MONSTER)
                            .sized(10.0F / 16.0F, 33.0F / 16.0F)
                            .clientTrackingRange(16)
                            .build(key("reaper_lvl1"))
            );

    public static final DeferredHolder<EntityType<?>, EntityType<com.zavidvi.voidmod.entity.reaper.ReaperLvl2Entity>> REAPER_LVL2 =
            ENTITY_TYPES.register("reaper_lvl2", () ->
                    EntityType.Builder.<com.zavidvi.voidmod.entity.reaper.ReaperLvl2Entity>of(
                                    com.zavidvi.voidmod.entity.reaper.ReaperLvl2Entity::new, MobCategory.MONSTER)
                            .sized(10.0F / 16.0F, 33.0F / 16.0F)
                            .clientTrackingRange(16)
                            .build(key("reaper_lvl2"))
            );

    public static final DeferredHolder<EntityType<?>, EntityType<com.zavidvi.voidmod.entity.reaper.ReaperLvl3Entity>> REAPER_LVL3 =
            ENTITY_TYPES.register("reaper_lvl3", () ->
                    EntityType.Builder.<com.zavidvi.voidmod.entity.reaper.ReaperLvl3Entity>of(
                                    com.zavidvi.voidmod.entity.reaper.ReaperLvl3Entity::new, MobCategory.MONSTER)
                            .sized(12.0F / 16.0F, 34.0F / 16.0F)
                            .clientTrackingRange(16)
                            .build(key("reaper_lvl3"))
            );

    public static final DeferredHolder<EntityType<?>, EntityType<com.zavidvi.voidmod.entity.reaper.ReaperFunnelEntity>> REAPER_FUNNEL =
            ENTITY_TYPES.register("reaper_funnel", () ->
                    EntityType.Builder.<com.zavidvi.voidmod.entity.reaper.ReaperFunnelEntity>of(
                                    com.zavidvi.voidmod.entity.reaper.ReaperFunnelEntity::new, MobCategory.MISC)
                            .sized(0.01F, 0.01F)
                            .clientTrackingRange(16)
                            .updateInterval(20)
                            .build(key("reaper_funnel"))
            );

    public static final DeferredHolder<EntityType<?>, EntityType<com.zavidvi.voidmod.entity.reaper.ReaperEnergyFlowEntity>> REAPER_ENERGY_FLOW =
            ENTITY_TYPES.register("reaper_energy_flow", () ->
                    EntityType.Builder.<com.zavidvi.voidmod.entity.reaper.ReaperEnergyFlowEntity>of(
                                    com.zavidvi.voidmod.entity.reaper.ReaperEnergyFlowEntity::new, MobCategory.MISC)
                            .sized(0.01F, 0.01F)
                            .clientTrackingRange(16)
                            .updateInterval(20)
                            .build(key("reaper_energy_flow"))
            );

    public static final DeferredHolder<EntityType<?>, EntityType<com.zavidvi.voidmod.block.BlackBoneEntity>> BLACK_BONE =
            ENTITY_TYPES.register("black_bone", () ->
                    EntityType.Builder.<com.zavidvi.voidmod.block.BlackBoneEntity>of(
                                    com.zavidvi.voidmod.block.BlackBoneEntity::new, MobCategory.MISC)
                            .sized(0.01F, 0.01F)
                            .clientTrackingRange(16)
                            .updateInterval(20)
                            .build(key("black_bone"))
            );

    public static final DeferredHolder<EntityType<?>, EntityType<com.zavidvi.voidmod.entity.supervoid.SupervoidShardEntity>> SUPERVOID_SHARD =
            ENTITY_TYPES.register("supervoid_shard", () ->
                    EntityType.Builder.<com.zavidvi.voidmod.entity.supervoid.SupervoidShardEntity>of(
                                    com.zavidvi.voidmod.entity.supervoid.SupervoidShardEntity::new, MobCategory.MISC)
                            .sized(com.zavidvi.voidmod.entity.supervoid.SupervoidShardEntity.SIZE,
                                    com.zavidvi.voidmod.entity.supervoid.SupervoidShardEntity.SIZE)
                            .clientTrackingRange(8)
                            .updateInterval(10)
                            .build(key("supervoid_shard"))
            );

    public static final DeferredHolder<EntityType<?>, EntityType<com.zavidvi.voidmod.entity.wanderer.WandererEntity>> WANDERER =
            ENTITY_TYPES.register("wanderer_of_time", () ->
                    EntityType.Builder.<com.zavidvi.voidmod.entity.wanderer.WandererEntity>of(com.zavidvi.voidmod.entity.wanderer.WandererEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(10)
                            .build(key("wanderer_of_time"))
            );

    private static net.minecraft.resources.ResourceKey<EntityType<?>> key(String name) {
        return net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.ENTITY_TYPE,
                net.minecraft.resources.Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, name));
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}

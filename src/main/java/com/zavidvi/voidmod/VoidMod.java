package com.zavidvi.voidmod;

import com.zavidvi.voidmod.client.renderer.supervoid.TentacleRenderer;
import com.zavidvi.voidmod.client.renderer.voidsphere.VoidSphereRenderer;
import com.zavidvi.voidmod.client.renderer.voidsphere.VoidSphereProjectileRenderer;
import com.zavidvi.voidmod.entity.voidsphere.VoidSphereEntity;
import com.zavidvi.voidmod.network.SyncProgressionPayload;
import com.zavidvi.voidmod.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(VoidMod.MOD_ID)
public class VoidMod {
    public static final String MOD_ID = "voidmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public VoidMod(IEventBus modEventBus, net.neoforged.fml.ModContainer modContainer) {
        LOGGER.info("Initializing Void Mod!");

        ModEntities.register(modEventBus);
        com.zavidvi.voidmod.registry.ModFluidTypes.register(modEventBus);
        com.zavidvi.voidmod.registry.ModFluids.register(modEventBus);
        com.zavidvi.voidmod.registry.ModBlocks.register(modEventBus);
        com.zavidvi.voidmod.registry.ModBlockEntities.register(modEventBus);
        com.zavidvi.voidmod.registry.ModItems.register(modEventBus);
        com.zavidvi.voidmod.registry.ModSounds.register(modEventBus);
        com.zavidvi.voidmod.registry.ModCreativeTabs.register(modEventBus);
        com.zavidvi.voidmod.registry.ModAttachments.register(modEventBus);
        com.zavidvi.voidmod.registry.ModDataComponents.register(modEventBus);
        com.zavidvi.voidmod.registry.ModStructurePlacements.register(modEventBus);
        com.zavidvi.voidmod.registry.ModStructures.register(modEventBus);
        com.zavidvi.voidmod.registry.ModPlacementModifiers.register(modEventBus);
        com.zavidvi.voidmod.registry.ModEffects.register(modEventBus);
        com.zavidvi.voidmod.registry.ModRecipeSerializers.register(modEventBus);

        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON,
                com.zavidvi.voidmod.config.VoidModConfig.SPEC);

        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::onConfigLoad);
        modEventBus.addListener(this::onConfigReload);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Void Mod Common Setup");
        event.enqueueWork(com.zavidvi.voidmod.stat.ArmorStats::bootstrap);
    }

    private void onConfigLoad(final net.neoforged.fml.event.config.ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == com.zavidvi.voidmod.config.VoidModConfig.SPEC) {
            com.zavidvi.voidmod.config.VoidModConfig.refresh();
        }
    }

    private void onConfigReload(final net.neoforged.fml.event.config.ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == com.zavidvi.voidmod.config.VoidModConfig.SPEC) {
            com.zavidvi.voidmod.config.VoidModConfig.refresh();
        }
    }

    @EventBusSubscriber(modid = VoidMod.MOD_ID)
    public static class ModEvents {
        @SubscribeEvent
        public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
            event.put(ModEntities.VOID_SPHERE.get(), VoidSphereEntity.createAttributes().build());
            event.put(ModEntities.WANDERER.get(), com.zavidvi.voidmod.entity.wanderer.WandererEntity.createAttributes().build());
            event.put(ModEntities.VRAUJ.get(), com.zavidvi.voidmod.entity.vrauj.VraujEntity.createAttributes().build());
            event.put(ModEntities.REAPER_LVL1.get(), com.zavidvi.voidmod.entity.reaper.ReaperLvl1Entity.createAttributes().build());
            event.put(ModEntities.REAPER_LVL2.get(), com.zavidvi.voidmod.entity.reaper.ReaperLvl2Entity.createAttributes().build());
            event.put(ModEntities.REAPER_LVL3.get(), com.zavidvi.voidmod.entity.reaper.ReaperLvl3Entity.createAttributes().build());
        }

        @SubscribeEvent
        public static void addCreative(net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == net.minecraft.world.item.CreativeModeTabs.COMBAT) {
                event.accept(com.zavidvi.voidmod.registry.ModItems.DEBUG_SWORD);
            }
            if (event.getTabKey() == net.minecraft.world.item.CreativeModeTabs.SPAWN_EGGS) {
                event.accept(com.zavidvi.voidmod.registry.ModItems.WANDERER_SPAWN_EGG);
            }
            if (event.getTabKey() == net.minecraft.world.item.CreativeModeTabs.TOOLS_AND_UTILITIES) {
                event.accept(com.zavidvi.voidmod.registry.ModItems.SPACE_DISTORTER);
            }
            if (event.getTabKey() == net.minecraft.world.item.CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                event.accept(com.zavidvi.voidmod.registry.ModBlocks.OTHERWORLDLY_FORGE);
            }
        }

        @SubscribeEvent
        public static void onRegisterSpawnPlacements(net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent event) {
            event.register(
                    ModEntities.VOID_SPHERE.get(),
                    net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    VoidSphereEntity::checkVoidSphereSpawnRules,
                    net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE
            );
        }

        @SubscribeEvent
        public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar("1.0.0");
            registrar.playToClient(
                    SyncProgressionPayload.TYPE,
                    SyncProgressionPayload.STREAM_CODEC,
                    SyncProgressionPayload::handle
            );
            registrar.playToClient(
                    com.zavidvi.voidmod.network.OpenWandererDialoguePayload.TYPE,
                    com.zavidvi.voidmod.network.OpenWandererDialoguePayload.STREAM_CODEC,
                    com.zavidvi.voidmod.network.OpenWandererDialoguePayload::handle
            );
            registrar.playToServer(
                    com.zavidvi.voidmod.network.WandererDialogueActionPayload.TYPE,
                    com.zavidvi.voidmod.network.WandererDialogueActionPayload.STREAM_CODEC,
                    com.zavidvi.voidmod.network.WandererDialogueActionPayload::handle
            );
            registrar.playToClient(
                    com.zavidvi.voidmod.network.WandererDialogueResponsePayload.TYPE,
                    com.zavidvi.voidmod.network.WandererDialogueResponsePayload.STREAM_CODEC,
                    com.zavidvi.voidmod.network.WandererDialogueResponsePayload::handle
            );
            registrar.playToClient(
                    com.zavidvi.voidmod.network.ShowCurseMessagePayload.TYPE,
                    com.zavidvi.voidmod.network.ShowCurseMessagePayload.STREAM_CODEC,
                    com.zavidvi.voidmod.network.ShowCurseMessagePayload::handle
            );
            registrar.playToServer(
                    com.zavidvi.voidmod.network.ChooseDexterityPayload.TYPE,
                    com.zavidvi.voidmod.network.ChooseDexterityPayload.STREAM_CODEC,
                    com.zavidvi.voidmod.network.ChooseDexterityPayload::handle
            );
            registrar.playToClient(
                    com.zavidvi.voidmod.network.ScytheSwingAnimationPayload.TYPE,
                    com.zavidvi.voidmod.network.ScytheSwingAnimationPayload.STREAM_CODEC,
                    com.zavidvi.voidmod.network.ScytheSwingAnimationPayload::handle
            );
            registrar.playToServer(
                    com.zavidvi.voidmod.network.ScytheSwingPayload.TYPE,
                    com.zavidvi.voidmod.network.ScytheSwingPayload.STREAM_CODEC,
                    com.zavidvi.voidmod.network.ScytheSwingPayload::handle
            );
            registrar.playToServer(
                    com.zavidvi.voidmod.network.StatsPanelOpenedPayload.TYPE,
                    com.zavidvi.voidmod.network.StatsPanelOpenedPayload.STREAM_CODEC,
                    com.zavidvi.voidmod.network.StatsPanelOpenedPayload::handle
            );
        }
    }

    @EventBusSubscriber(modid = VoidMod.MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onRegisterItemModelProperties(
                net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent event) {
            event.register(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath(MOD_ID, "cursed"),
                    com.zavidvi.voidmod.client.item.CursedItemProperty.MAP_CODEC
            );
        }

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.TENTACLE.get(), TentacleRenderer::new);
            event.registerEntityRenderer(ModEntities.VOID_SPHERE.get(), VoidSphereRenderer::new);
            event.registerEntityRenderer(ModEntities.VOID_SPHERE_PROJECTILE.get(), VoidSphereProjectileRenderer::new);
            event.registerEntityRenderer(ModEntities.WANDERER.get(), com.zavidvi.voidmod.client.renderer.wanderer.WandererRenderer::new);
            event.registerEntityRenderer(ModEntities.VRAUJ.get(), com.zavidvi.voidmod.client.renderer.vrauj.VraujRenderer::new);
            event.registerEntityRenderer(ModEntities.VRAUJ_PROJECTILE.get(), com.zavidvi.voidmod.client.renderer.vrauj.VraujProjectileRenderer::new);
            event.registerEntityRenderer(ModEntities.SUPERVOID_SHARD.get(), com.zavidvi.voidmod.client.renderer.supervoid.SupervoidShardRenderer::new);
            event.registerEntityRenderer(ModEntities.RIME_PROJECTILE.get(), com.zavidvi.voidmod.client.renderer.rime.RimeProjectileRenderer::new);
            event.registerEntityRenderer(ModEntities.REAPER_LVL1.get(),
                    context -> new com.zavidvi.voidmod.client.renderer.reaper.ReaperRenderer<>(context, "reaper_lvl1", 0.5F));
            event.registerEntityRenderer(ModEntities.REAPER_LVL2.get(),
                    context -> new com.zavidvi.voidmod.client.renderer.reaper.ReaperRenderer<>(context, "reaper_lvl2", 0.5F));
            event.registerEntityRenderer(ModEntities.REAPER_LVL3.get(),
                    context -> new com.zavidvi.voidmod.client.renderer.reaper.ReaperRenderer<>(context, "reaper_lvl3", 0.6F));
            event.registerEntityRenderer(ModEntities.REAPER_FUNNEL.get(), com.zavidvi.voidmod.client.renderer.reaper.ReaperFunnelRenderer::new);
            event.registerEntityRenderer(ModEntities.REAPER_ENERGY_FLOW.get(), com.zavidvi.voidmod.client.renderer.reaper.ReaperEnergyFlowRenderer::new);
            event.registerEntityRenderer(ModEntities.BLACK_BONE.get(), com.zavidvi.voidmod.client.renderer.grave.BlackBoneRenderer::new);
            event.registerBlockEntityRenderer(com.zavidvi.voidmod.registry.ModBlockEntities.OTHERWORLDLY_FORGE.get(), com.zavidvi.voidmod.client.renderer.otherworldlyforge.OtherworldlyForgeRenderer::new);
            event.registerBlockEntityRenderer(com.zavidvi.voidmod.registry.ModBlockEntities.GRAVE.get(), com.zavidvi.voidmod.client.renderer.grave.GraveRenderer::new);
        }

        @SubscribeEvent
        public static void onRegisterFluidModels(
                net.neoforged.neoforge.client.event.RegisterFluidModelsEvent event) {
            com.zavidvi.voidmod.client.LightWaterClientExtensions.registerFluidModels(event);
        }

        @SubscribeEvent
        public static void onRegisterBlockColors(
                net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.BlockTintSources event) {
            event.register(java.util.List.of(com.zavidvi.voidmod.client.PaleCauldronColors.INSTANCE),
                    com.zavidvi.voidmod.registry.ModBlocks.PALE_CAULDRON.get());
        }

        @SubscribeEvent
        public static void onRegisterGuiLayers(net.neoforged.neoforge.client.event.RegisterGuiLayersEvent event) {
            event.registerAboveAll(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath(MOD_ID, "curse_overlay"),
                    (guiGraphics, deltaTracker) -> com.zavidvi.voidmod.client.gui.CurseOverlay.render(
                            guiGraphics, guiGraphics.guiWidth(), guiGraphics.guiHeight()
                    )
            );
        }

        @SubscribeEvent
        public static void onAddPackFinders(net.neoforged.neoforge.event.AddPackFindersEvent event) {
            if (event.getPackType() != net.minecraft.server.packs.PackType.CLIENT_RESOURCES) {
                return;
            }
            event.addPackFinders(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath(MOD_ID, "resourcepacks/vanilla_overrides"),
                    net.minecraft.server.packs.PackType.CLIENT_RESOURCES,
                    net.minecraft.network.chat.Component.literal("VoidMode: vanilla overrides"),
                    net.minecraft.server.packs.repository.PackSource.BUILT_IN,
                    true,
                    net.minecraft.server.packs.repository.Pack.Position.TOP
            );
        }
    }
}


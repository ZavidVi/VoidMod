package com.zavidvi.voidmod.event.worldgen;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.world.progression.WorldProgressionData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.Optional;

@EventBusSubscriber(modid = VoidMod.MOD_ID)
public class SpawnFountainPlacer {
    private static final Identifier TEMPLATE =
            Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "lighted_fountain");

    private static final ResourceKey<StructureProcessorList> PROCESSORS = ResourceKey.create(
            Registries.PROCESSOR_LIST,
            Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "lighted_fountain_water"));

    private static final int GROUND_OFFSET = -1;

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.dimension() != Level.OVERWORLD) return;

        if (!com.zavidvi.voidmod.util.CurseLightState.isServerCursed()) return;

        WorldProgressionData data = WorldProgressionData.get(level);
        if (data.isSpawnFountainPlaced()) return;

        data.setSpawnFountainPlaced(true);
        place(level);
    }

    private static void place(ServerLevel level) {
        Optional<StructureTemplate> template = level.getStructureManager().get(TEMPLATE);
        if (template.isEmpty()) {
            VoidMod.LOGGER.error("Шаблон фонтана света {} не найден — на спавне его не будет", TEMPLATE);
            return;
        }

        StructureTemplate fountain = template.get();
        BlockPos spawn = level.getRespawnData().pos();
        net.minecraft.core.Vec3i size = fountain.getSize();

        int x = spawn.getX() - size.getX() / 2;
        int z = spawn.getZ() - size.getZ() / 2;
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, spawn.getX(), spawn.getZ()) + GROUND_OFFSET;

        BlockPos corner = new BlockPos(x, y, z);
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(Rotation.NONE)
                .setMirror(Mirror.NONE)
                .setIgnoreEntities(false);
        applyProcessors(level, settings);

        fountain.placeInWorld(level, corner, corner, settings, level.getRandom(),
                net.minecraft.world.level.block.Block.UPDATE_ALL);
        VoidMod.LOGGER.info("Фонтан света поставлен на спавне мира: {}", corner);
    }

    private static void applyProcessors(ServerLevel level, StructurePlaceSettings settings) {
        level.registryAccess()
                .lookupOrThrow(Registries.PROCESSOR_LIST)
                .get(PROCESSORS)
                .ifPresentOrElse(
                        holder -> holder.value().list().forEach(settings::addProcessor),
                        () -> VoidMod.LOGGER.error(
                                "Список процессоров {} не найден — фонтан на спавне будет с обычной водой",
                                PROCESSORS.identifier()));
    }
}

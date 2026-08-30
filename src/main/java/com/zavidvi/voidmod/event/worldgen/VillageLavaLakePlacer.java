package com.zavidvi.voidmod.event.worldgen;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.world.progression.WorldProgressionData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = VoidMod.MOD_ID)
public class VillageLavaLakePlacer {
    private static final int CHECK_INTERVAL = 100;

    private static final int LAKE_GAP = 12;

    private static final ResourceKey<ConfiguredFeature<?, ?>> LAKE_LAVA = ResourceKey.create(
            Registries.CONFIGURED_FEATURE, Identifier.withDefaultNamespace("lake_lava"));

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.dimension() != Level.OVERWORLD) return;
        if (level.getGameTime() % CHECK_INTERVAL != 0) return;

        WorldProgressionData data = WorldProgressionData.get(level);

        for (ServerPlayer player : level.players()) {
            StructureStart start = level.structureManager()
                    .getStructureWithPieceAt(player.blockPosition(), StructureTags.VILLAGE);
            if (!start.isValid()) continue;

            BlockPos id = start.getChunkPos().getMiddleBlockPosition(0);
            if (data.isVillageLakePlaced(id)) continue;

            data.markVillageLakePlaced(id);
            place(level, start);
        }
    }

    private static void place(ServerLevel level, StructureStart start) {
        ConfiguredFeature<?, ?> lake = level.registryAccess()
                .lookupOrThrow(Registries.CONFIGURED_FEATURE)
                .getValue(LAKE_LAVA);
        if (lake == null) {
            VoidMod.LOGGER.warn("[village lakes] нет ванильной фичи {} — озера у деревень не будет", LAKE_LAVA.identifier());
            return;
        }

        BoundingBox box = start.getBoundingBox();
        BlockPos centre = box.getCenter();
        RandomSource random = RandomSource.create(start.getChunkPos().pack());
        int firstSide = random.nextInt(4);

        for (int i = 0; i < 4; i++) {
            Direction side = Direction.from2DDataValue((firstSide + i) % 4);
            int reach = (side.getAxis() == Direction.Axis.X ? box.getXSpan() : box.getZSpan()) / 2 + LAKE_GAP;

            int x = centre.getX() + side.getStepX() * reach;
            int z = centre.getZ() + side.getStepZ() * reach;
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);

            if (lake.place(level, level.getChunkSource().getGenerator(), random, new BlockPos(x, y, z))) {
                VoidMod.LOGGER.info("[village lakes] лавовое озеро у деревни {} на {} {} {}",
                        start.getChunkPos(), x, y, z);
                return;
            }
        }
    }
}

package com.zavidvi.voidmod.world.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zavidvi.voidmod.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;

import java.util.Optional;

public class DryJigsawStructure extends Structure {
    public static final MapCodec<DryJigsawStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(s -> s.startPool),
                    Codec.intRange(0, 20).fieldOf("size").forGetter(s -> s.maxDepth),
                    HeightProvider.CODEC.fieldOf("start_height").forGetter(s -> s.startHeight),
                    Codec.BOOL.fieldOf("use_expansion_hack").forGetter(s -> s.useExpansionHack),
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap")
                            .forGetter(s -> s.projectStartToHeightmap),
                    JigsawStructure.MaxDistance.CODEC.fieldOf("max_distance_from_center")
                            .forGetter(s -> s.maxDistanceFromCenter),
                    Codec.intRange(1, 48).optionalFieldOf("dry_footprint", 8).forGetter(s -> s.dryFootprint)
            ).apply(instance, DryJigsawStructure::new));

    private static final int PROBE_STEP = 3;

    private final Holder<StructureTemplatePool> startPool;
    private final int maxDepth;
    private final HeightProvider startHeight;
    private final boolean useExpansionHack;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final JigsawStructure.MaxDistance maxDistanceFromCenter;

    private final int dryFootprint;

    public DryJigsawStructure(Structure.StructureSettings settings,
                              Holder<StructureTemplatePool> startPool,
                              int maxDepth,
                              HeightProvider startHeight,
                              boolean useExpansionHack,
                              Optional<Heightmap.Types> projectStartToHeightmap,
                              JigsawStructure.MaxDistance maxDistanceFromCenter,
                              int dryFootprint) {
        super(settings);
        this.startPool = startPool;
        this.maxDepth = maxDepth;
        this.startHeight = startHeight;
        this.useExpansionHack = useExpansionHack;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.dryFootprint = dryFootprint;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        if (!isDry(context)) return Optional.empty();

        ChunkPos chunkPos = context.chunkPos();
        int height = this.startHeight.sample(context.random(),
                new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
        BlockPos startPos = new BlockPos(chunkPos.getMinBlockX(), height, chunkPos.getMinBlockZ());

        return JigsawPlacement.addPieces(
                context,
                this.startPool,
                Optional.empty(),
                this.maxDepth,
                startPos,
                this.useExpansionHack,
                this.projectStartToHeightmap,
                this.maxDistanceFromCenter,
                PoolAliasLookup.EMPTY,
                JigsawStructure.DEFAULT_DIMENSION_PADDING,
                JigsawStructure.DEFAULT_LIQUID_SETTINGS);
    }

    private boolean isDry(Structure.GenerationContext context) {
        ChunkGenerator generator = context.chunkGenerator();
        LevelHeightAccessor heightAccessor = context.heightAccessor();
        RandomState randomState = context.randomState();

        int minX = context.chunkPos().getMinBlockX();
        int minZ = context.chunkPos().getMinBlockZ();

        int span = 2 * this.dryFootprint;
        int steps = (span + PROBE_STEP - 1) / PROBE_STEP;

        for (int ix = 0; ix <= steps; ix++) {
            int x = minX - this.dryFootprint + Math.min(ix * PROBE_STEP, span);

            for (int iz = 0; iz <= steps; iz++) {
                int z = minZ - this.dryFootprint + Math.min(iz * PROBE_STEP, span);

                int surface = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                        heightAccessor, randomState);
                int floor = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG,
                        heightAccessor, randomState);

                if (surface != floor) return false;
            }
        }

        return true;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.DRY_JIGSAW.get();
    }
}

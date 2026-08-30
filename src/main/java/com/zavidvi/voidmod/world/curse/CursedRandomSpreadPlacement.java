package com.zavidvi.voidmod.world.curse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zavidvi.voidmod.registry.ModStructurePlacements;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import java.util.Optional;

public class CursedRandomSpreadPlacement extends RandomSpreadStructurePlacement {
    public static final MapCodec<CursedRandomSpreadPlacement> CODEC =
            RecordCodecBuilder.mapCodec(instance -> placementCodec(instance).and(
                    instance.group(
                            Codec.intRange(0, 4096).fieldOf("spacing")
                                    .forGetter(RandomSpreadStructurePlacement::spacing),
                            Codec.intRange(0, 4096).fieldOf("separation")
                                    .forGetter(RandomSpreadStructurePlacement::separation),
                            RandomSpreadType.CODEC.optionalFieldOf("spread_type", RandomSpreadType.LINEAR)
                                    .forGetter(RandomSpreadStructurePlacement::spreadType)
                    )).apply(instance, CursedRandomSpreadPlacement::new));

    public CursedRandomSpreadPlacement(Vec3i locateOffset,
                                       FrequencyReductionMethod frequencyReductionMethod,
                                       float frequency,
                                       int salt,
                                       Optional<ExclusionZone> exclusionZone,
                                       int spacing,
                                       int separation,
                                       RandomSpreadType spreadType) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone,
                spacing, separation, spreadType);
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        if (!CurseHelper.isWorldCursedGlobal()) return false;
        return super.isPlacementChunk(state, chunkX, chunkZ);
    }

    @Override
    public StructurePlacementType<?> type() {
        return ModStructurePlacements.CURSED_RANDOM_SPREAD.get();
    }
}

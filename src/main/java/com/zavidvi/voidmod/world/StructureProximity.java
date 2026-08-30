package com.zavidvi.voidmod.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public final class StructureProximity {
    private StructureProximity() {}

    @Nullable
    public static BlockPos centerNear(ServerLevel level, BlockPos pos, int chunkRadius,
                                      Predicate<Structure> filter) {
        ChunkPos origin = ChunkPos.containing(pos);

        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                ChunkPos probe = new ChunkPos(origin.x() + dx, origin.z() + dz);

                for (StructureStart start : level.structureManager().startsForStructure(probe, filter)) {
                    if (start.isValid()) {
                        return start.getBoundingBox().getCenter();
                    }
                }
            }
        }
        return null;
    }
}

package com.zavidvi.voidmod.world.curse;

import com.zavidvi.voidmod.VoidMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import javax.annotation.Nullable;

public final class LightWaterPlacement {
    public static final ResourceKey<Structure> LIGHTED_FOUNTAIN = ResourceKey.create(
            Registries.STRUCTURE,
            Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "lighted_fountain"));

    private LightWaterPlacement() {}

    public static boolean isInsideFountain(Level level, BlockPos pos) {
        return pieceAt(level, pos) != null;
    }

    public static boolean isFountainSource(Level level, BlockPos pos) {
        BoundingBox piece = pieceAt(level, pos);
        if (piece == null) return false;

        BlockPos center = piece.getCenter();
        return pos.getY() == piece.maxY()
                && pos.getX() == center.getX()
                && pos.getZ() == center.getZ();
    }

    @Nullable
    private static BoundingBox pieceAt(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }

        Structure structure = serverLevel.registryAccess()
                .lookupOrThrow(Registries.STRUCTURE)
                .get(LIGHTED_FOUNTAIN).orElseThrow().value();
        if (structure == null) {
            return null;
        }

        StructureStart start = serverLevel.structureManager().getStructureWithPieceAt(pos, structure);
        if (!start.isValid()) return null;

        for (StructurePiece piece : start.getPieces()) {
            if (piece.getBoundingBox().isInside(pos)) {
                return piece.getBoundingBox();
            }
        }
        return null;
    }
}

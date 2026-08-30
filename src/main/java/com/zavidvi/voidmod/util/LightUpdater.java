package com.zavidvi.voidmod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.lighting.LevelLightEngine;

public final class LightUpdater {
    private LightUpdater() {}

    public static int lastTouchedSources = 0;

    public static int relightChunk(Level level, ChunkAccess chunk) {
        lastTouchedSources = 0;
        if (level == null || chunk == null) return 0;

        LevelLightEngine lightEngine = level.getLightEngine();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int chunkX = chunk.getPos().x();
        int chunkZ = chunk.getPos().z();
        int scannedSections = 0;
        int touched = 0;

        LevelChunkSection[] sections = chunk.getSections();
        for (int i = 0; i < sections.length; i++) {
            LevelChunkSection section = sections[i];
            if (section == null || section.hasOnlyAir()) continue;

            if (!section.getStates().maybeHas(CursedLightBlocks::isDimmed)) continue;

            scannedSections++;
            int baseY = chunk.getSectionYFromSectionIndex(i) << 4;

            for (int ly = 0; ly < 16; ly++) {
                for (int lz = 0; lz < 16; lz++) {
                    for (int lx = 0; lx < 16; lx++) {
                        BlockState state = section.getBlockState(lx, ly, lz);
                        if (!CursedLightBlocks.isDimmed(state)) continue;
                        pos.set((chunkX << 4) + lx, baseY + ly, (chunkZ << 4) + lz);
                        lightEngine.checkBlock(pos);
                        touched++;
                    }
                }
            }
        }

        lastTouchedSources = touched;
        return Math.max(scannedSections, 1);
    }
}

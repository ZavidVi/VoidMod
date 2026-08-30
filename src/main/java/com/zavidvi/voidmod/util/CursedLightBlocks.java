package com.zavidvi.voidmod.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class CursedLightBlocks {
    private CursedLightBlocks() {}

    public static final int DIM_DIVISOR = 2;

    public static boolean isDimmed(BlockState state) {
        Block block = state.getBlock();
        return block instanceof TorchBlock
                || block instanceof LanternBlock
                || block instanceof CampfireBlock
                || block instanceof CandleBlock
                || block == Blocks.LAVA
                || block == Blocks.GLOWSTONE
                || block == Blocks.REDSTONE_LAMP
                || block == Blocks.SHROOMLIGHT
                || block == Blocks.SEA_LANTERN
                || block == Blocks.GLOW_LICHEN;
    }
}

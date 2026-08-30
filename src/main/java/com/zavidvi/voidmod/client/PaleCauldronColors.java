package com.zavidvi.voidmod.client;

import com.zavidvi.voidmod.block.PaleCauldronBlock;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Set;

public final class PaleCauldronColors implements BlockTintSource {
    public static final PaleCauldronColors INSTANCE = new PaleCauldronColors();

    private static final int NO_TINT = -1;

    private PaleCauldronColors() {}

    @Override
    public int color(BlockState state) {
        return NO_TINT;
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        if (PaleCauldronBlock.content(state) != PaleCauldronBlock.Content.WATER) return NO_TINT;
        return BiomeColors.getAverageWaterColor(level, pos);
    }

    @Override
    public Set<Property<?>> relevantProperties() {
        return Set.of(PaleCauldronBlock.CONTENT);
    }
}

package com.zavidvi.voidmod.mixin;

import com.zavidvi.voidmod.util.CurseLightState;
import com.zavidvi.voidmod.util.CursedDimmedState;
import com.zavidvi.voidmod.util.CursedLightBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.BlockLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockLightEngine.class)
public abstract class BlockLightEngineMixin {
    @Redirect(
            method = "getEmission(JLnet/minecraft/world/level/block/state/BlockState;)I",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I"
            ),
            remap = false
    )
    private int voidmod$dimCursedEmission(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        int original = state.getLightEmission(blockGetter, pos);

        if (original <= 0 || !CurseLightState.isCursed()) {
            return original;
        }
        if (!(blockGetter instanceof Level level) || level.dimension() != Level.OVERWORLD) {
            return original;
        }
        if (!((CursedDimmedState) (Object) state).voidmod$isCursedDimmed()) {
            return original;
        }

        return original / CursedLightBlocks.DIM_DIVISOR;
    }
}

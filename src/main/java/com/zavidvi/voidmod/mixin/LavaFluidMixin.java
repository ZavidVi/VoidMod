package com.zavidvi.voidmod.mixin;

import com.zavidvi.voidmod.world.curse.CurseHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LavaFluid.class)
public class LavaFluidMixin {
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true, remap = false)
    private void onRandomTick(ServerLevel level, BlockPos pos, FluidState state, RandomSource random, CallbackInfo ci) {
        if (!CurseHelper.isWorldCursed(level)) {
            return;
        }

        ci.cancel();

        if (!level.canSpreadFireAround(pos)) {
            return;
        }

        for (int attempt = 0; attempt < 3; attempt++) {
            BlockPos target = pos.offset(
                    random.nextInt(3) - 1,
                    random.nextInt(3) - 1,
                    random.nextInt(3) - 1);

            if (target.equals(pos) || !level.isLoaded(target)) {
                continue;
            }
            if (!level.getBlockState(target).isAir()) {
                continue;
            }
            if (!voidmod$hasFlammableNeighboursCurse(level, target)) {
                continue;
            }

            level.setBlockAndUpdate(target, BaseFireBlock.getState(level, target));
            return;
        }
    }

    @Unique
    private static boolean voidmod$hasFlammableNeighboursCurse(LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (voidmod$isFlammableCurse(level, pos.relative(direction))) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private static boolean voidmod$isFlammableCurse(LevelReader level, BlockPos pos) {
        return level.isInsideBuildHeight(pos.getY()) && !level.hasChunkAt(pos)
                ? false
                : level.getBlockState(pos).ignitedByLava();
    }
}

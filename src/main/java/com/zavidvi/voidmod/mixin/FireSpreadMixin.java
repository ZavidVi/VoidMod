package com.zavidvi.voidmod.mixin;

import com.zavidvi.voidmod.world.curse.CurseHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public class FireSpreadMixin {
    @Inject(
            method = "tick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void voidmod$freezeFire(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!CurseHelper.isWorldCursed(level)) {
            return;
        }
        level.scheduleTick(pos, (Block) (Object) this, 30 + random.nextInt(10));
        ci.cancel();
    }
}

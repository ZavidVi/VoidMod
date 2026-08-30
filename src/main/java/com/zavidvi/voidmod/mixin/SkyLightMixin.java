package com.zavidvi.voidmod.mixin;

import com.zavidvi.voidmod.config.VoidModConfig;
import com.zavidvi.voidmod.util.CurseLightState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.lighting.SkyLightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightEngine.class)
public abstract class SkyLightMixin {
    @Shadow
    @Final
    protected LightChunkGetter chunkSource;

    @Inject(method = "getLightValue(Lnet/minecraft/core/BlockPos;)I", at = @At("RETURN"), cancellable = true, remap = false)
    private void voidmod$dimSkyLight(BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (!CurseLightState.isCursed()) return;

        int reduction = VoidModConfig.skyLightReduction();
        if (reduction <= 0) return;

        int value = cir.getReturnValue();
        if (value <= 0) return;

        if (!((Object) this instanceof SkyLightEngine)) return;

        BlockGetter blockGetter = this.chunkSource.getLevel();
        if (!(blockGetter instanceof Level level) || level.dimension() != Level.OVERWORLD) return;

        cir.setReturnValue(Math.max(0, value - reduction));
    }
}

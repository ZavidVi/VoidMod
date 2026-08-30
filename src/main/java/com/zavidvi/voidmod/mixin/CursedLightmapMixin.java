package com.zavidvi.voidmod.mixin;

import com.zavidvi.voidmod.util.CurseLightState;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
public class CursedLightmapMixin {
    @Shadow
    private boolean needsUpdate;

    @Inject(method = "extract", at = @At("TAIL"), remap = false)
    private void voidmod$tintCursedLightmap(LightmapRenderState renderState, float partialTicks, CallbackInfo ci) {
        if (!CurseLightState.isCursed()) return;

        net.minecraft.client.multiplayer.ClientLevel level = net.minecraft.client.Minecraft.getInstance().level;
        if (level == null || level.dimension() != net.minecraft.world.level.Level.OVERWORLD) return;

        if (!renderState.needsUpdate || this.needsUpdate) return;

        renderState.blockLightTint = voidmod$curse(renderState.blockLightTint);
        renderState.skyLightColor = voidmod$curse(renderState.skyLightColor);
        renderState.ambientColor = voidmod$curse(renderState.ambientColor);
    }

    @Unique
    private static Vector3fc voidmod$curse(Vector3fc color) {
        float r = color.x();
        float g = color.y();
        float b = color.z();

        float luma = r * 0.299F + g * 0.587F + b * 0.114F;

        return new Vector3f(
                (r + (luma - r) * 0.55F) * 0.74F,
                (g + (luma - g) * 0.55F) * 0.80F,
                (b + (luma - b) * 0.55F) * 0.95F);
    }
}

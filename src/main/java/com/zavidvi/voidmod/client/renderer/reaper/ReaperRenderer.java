package com.zavidvi.voidmod.client.renderer.reaper;

import com.zavidvi.voidmod.client.model.reaper.ReaperModel;
import com.zavidvi.voidmod.entity.reaper.ReaperEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;

public class ReaperRenderer<T extends ReaperEntity> extends GeoEntityRenderer<T, LivingEntityRenderState> {
    public ReaperRenderer(EntityRendererProvider.Context context, String name, float shadowRadius) {
        super(context, new ReaperModel<>(name));
        this.shadowRadius = shadowRadius;
    }

    @Override
    protected float getDeathMaxRotation(GeoRenderState renderState) {
        return 0.0F;
    }

    @Override
    public int getPackedOverlay(T animatable, Void relatedObject, float u, float partialTick) {
        return OverlayTexture.pack(OverlayTexture.u(u), OverlayTexture.v(animatable.hurtTime > 0));
    }
}

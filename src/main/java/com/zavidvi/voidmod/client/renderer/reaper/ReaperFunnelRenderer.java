package com.zavidvi.voidmod.client.renderer.reaper;

import com.zavidvi.voidmod.client.model.reaper.ReaperModel;
import com.zavidvi.voidmod.entity.reaper.ReaperFunnelEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import com.geckolib.renderer.GeoEntityRenderer;

public class ReaperFunnelRenderer extends GeoEntityRenderer<ReaperFunnelEntity, EntityRenderState> {
    public ReaperFunnelRenderer(EntityRendererProvider.Context context) {
        super(context, new ReaperModel<>("attack_warning"));
        this.shadowRadius = 0.0F;
    }

    @Override
    protected net.minecraft.world.phys.AABB getBoundingBoxForCulling(ReaperFunnelEntity entity) {
        return net.minecraft.world.phys.AABB.ofSize(entity.position(), 4.0D, 2.0D, 4.0D);
    }
}

package com.zavidvi.voidmod.client.renderer.reaper;

import com.zavidvi.voidmod.client.model.reaper.ReaperModel;
import com.zavidvi.voidmod.entity.reaper.ReaperEnergyFlowEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import com.geckolib.renderer.GeoEntityRenderer;

public class ReaperEnergyFlowRenderer extends GeoEntityRenderer<ReaperEnergyFlowEntity, EntityRenderState> {
    public ReaperEnergyFlowRenderer(EntityRendererProvider.Context context) {
        super(context, new ReaperModel<>("void_energy_flow"));
        this.shadowRadius = 0.0F;
    }

    @Override
    protected net.minecraft.world.phys.AABB getBoundingBoxForCulling(ReaperEnergyFlowEntity entity) {
        double half = 2.0D;
        return new net.minecraft.world.phys.AABB(
                entity.getX() - half, entity.getY(), entity.getZ() - half,
                entity.getX() + half, entity.getY() + ReaperEnergyFlowEntity.MODEL_HEIGHT, entity.getZ() + half);
    }
}

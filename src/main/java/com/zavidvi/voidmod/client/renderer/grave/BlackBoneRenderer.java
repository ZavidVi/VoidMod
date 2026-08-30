package com.zavidvi.voidmod.client.renderer.grave;

import com.zavidvi.voidmod.client.model.reaper.ReaperModel;
import com.zavidvi.voidmod.block.BlackBoneEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import com.geckolib.renderer.GeoEntityRenderer;

public class BlackBoneRenderer extends GeoEntityRenderer<BlackBoneEntity, EntityRenderState> {
    public BlackBoneRenderer(EntityRendererProvider.Context context) {
        super(context, new ReaperModel<>("black_bone_entity"));
        this.shadowRadius = 0.0F;
    }

    @Override
    protected net.minecraft.world.phys.AABB getBoundingBoxForCulling(BlackBoneEntity entity) {
        return net.minecraft.world.phys.AABB.ofSize(entity.position(), 3.0D, 5.0D, 3.0D);
    }
}

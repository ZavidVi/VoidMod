package com.zavidvi.voidmod.client.renderer.voidsphere;

import com.zavidvi.voidmod.client.model.voidsphere.VoidSphereModel;
import com.zavidvi.voidmod.entity.voidsphere.VoidSphereEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import com.geckolib.renderer.GeoEntityRenderer;

public class VoidSphereRenderer extends GeoEntityRenderer<VoidSphereEntity, LivingEntityRenderState> {
    public VoidSphereRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new VoidSphereModel());
        this.shadowRadius = 0.4f;
    }

    @Override
    protected int getBlockLightLevel(VoidSphereEntity entity, BlockPos pos) {
        return 12;
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityCutout(texture);
    }
}

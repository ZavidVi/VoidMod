package com.zavidvi.voidmod.client.renderer.vrauj;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zavidvi.voidmod.client.model.vrauj.VraujModel;
import com.zavidvi.voidmod.entity.vrauj.VraujEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.RenderPassInfo;

public class VraujRenderer extends GeoEntityRenderer<VraujEntity, LivingEntityRenderState> {
    private static final DataTicket<Boolean> IS_ATTACKING =
            DataTicket.create("voidmod:vrauj_attacking", Boolean.class);

    public VraujRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new VraujModel());
        this.shadowRadius = 0.8F;
    }

    @Override
    public void extractRenderState(VraujEntity entity, LivingEntityRenderState renderState, float partialTick) {
        super.extractRenderState(entity, renderState, partialTick);
        renderState.addGeckolibData(IS_ATTACKING, entity.isAttacking());
    }

    @Override
    protected void applyRotations(RenderPassInfo<LivingEntityRenderState> renderPass, PoseStack poseStack, float nativeScale) {
        super.applyRotations(renderPass, poseStack, nativeScale);

        LivingEntityRenderState renderState = renderPass.renderState();
        if (renderState.getOrDefaultGeckolibData(IS_ATTACKING, false)) return;

        float pitch = renderState.xRot;
        if (pitch != 0.0F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));
        }
    }
}

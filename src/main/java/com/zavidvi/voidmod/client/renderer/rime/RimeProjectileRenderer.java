package com.zavidvi.voidmod.client.renderer.rime;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.entity.rime.RimeProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class RimeProjectileRenderer extends GeoEntityRenderer<RimeProjectileEntity, RimeProjectileRenderer.RimeProjectileRenderState> {
    public static class RimeProjectileRenderState extends EntityRenderState {
        public float xRot;
        public float yRot;
    }

    public RimeProjectileRenderer(EntityRendererProvider.Context context) {
        super(context, new RimeAmmoModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public RimeProjectileRenderState createRenderState(RimeProjectileEntity animatable, Void ignored) {
        return new RimeProjectileRenderState();
    }

    @Override
    public void extractRenderState(RimeProjectileEntity entity, RimeProjectileRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.yRot = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        state.xRot = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
    }

    @Override
    protected void applyRotations(RenderPassInfo<RimeProjectileRenderState> info, PoseStack poseStack, float customFloat) {
        poseStack.mulPose(Axis.YP.rotationDegrees(info.renderState().yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(-info.renderState().xRot));
    }

    private static class RimeAmmoModel extends GeoModel<RimeProjectileEntity> {
        private static final Identifier MODEL =
                Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "rime_ammo");
        private static final Identifier TEXTURE =
                Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "textures/entity/rime_ammo.png");

        @Override
        public Identifier getModelResource(GeoRenderState renderState) {
            return MODEL;
        }

        @Override
        public Identifier getTextureResource(GeoRenderState renderState) {
            return TEXTURE;
        }

        @Override
        public Identifier getAnimationResource(RimeProjectileEntity animatable) {
            return MODEL;
        }
    }
}

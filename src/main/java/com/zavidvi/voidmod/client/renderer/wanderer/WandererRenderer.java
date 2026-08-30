package com.zavidvi.voidmod.client.renderer.wanderer;

import com.zavidvi.voidmod.entity.wanderer.WandererEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.geckolib.renderer.GeoEntityRenderer;

public class WandererRenderer extends GeoEntityRenderer<WandererEntity, LivingEntityRenderState> {
    public WandererRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new WandererModel());
        this.shadowRadius = 0.5f;
    }
}

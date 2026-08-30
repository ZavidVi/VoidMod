package com.zavidvi.voidmod.client.renderer.supervoid;

import com.zavidvi.voidmod.client.model.supervoid.SupervoidShardModel;
import com.zavidvi.voidmod.entity.supervoid.SupervoidShardEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import com.geckolib.renderer.GeoEntityRenderer;

public class SupervoidShardRenderer extends GeoEntityRenderer<SupervoidShardEntity, EntityRenderState> {
    private static final float MODEL_SCALE = SupervoidShardEntity.SIZE / 2.0F;

    public SupervoidShardRenderer(EntityRendererProvider.Context context) {
        super(context, new SupervoidShardModel());
        withScale(MODEL_SCALE);
    }
}

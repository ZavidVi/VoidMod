package com.zavidvi.voidmod.client.renderer.supervoid;

import com.zavidvi.voidmod.client.model.supervoid.TentacleModel;
import com.zavidvi.voidmod.entity.supervoid.TentacleEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import com.geckolib.renderer.GeoEntityRenderer;

public class TentacleRenderer extends GeoEntityRenderer<TentacleEntity, EntityRenderState> {
    public TentacleRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new TentacleModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    protected int getBlockLightLevel(TentacleEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public RenderType getRenderType(EntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityCutout(texture);
    }
}

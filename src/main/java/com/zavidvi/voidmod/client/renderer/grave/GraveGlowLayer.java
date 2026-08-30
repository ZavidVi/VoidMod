package com.zavidvi.voidmod.client.renderer.grave;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.block.GraveBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.resources.Identifier;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;

public class GraveGlowLayer extends AutoGlowingGeoLayer<GraveBlockEntity, Void, BlockEntityRenderState> {
    private static final Identifier GLOW_GREEN =
            Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "textures/block/grave_glow_green.png");
    private static final Identifier GLOW_BLUE =
            Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "textures/block/grave_glow_blue.png");
    private static final Identifier GLOW_VIOLET =
            Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "textures/block/grave_glow_violet.png");

    public GraveGlowLayer(GeoRenderer<GraveBlockEntity, Void, BlockEntityRenderState> renderer) {
        super(renderer);
    }

    @Override
    protected Identifier getTextureResource(BlockEntityRenderState renderState) {
        return switch (renderState.getOrDefaultGeckolibData(GraveRenderer.STAGE, GraveBlockEntity.STAGE_IDLE)) {
            case 1 -> GLOW_GREEN;
            case 2 -> GLOW_BLUE;
            case 3 -> GLOW_VIOLET;
            default -> GLOW_GREEN;
        };
    }

    @Override
    public void submitRenderTask(RenderPassInfo<BlockEntityRenderState> renderPass, SubmitNodeCollector collector) {
        int stage = renderPass.renderState()
                .getOrDefaultGeckolibData(GraveRenderer.STAGE, GraveBlockEntity.STAGE_IDLE);
        if (stage == GraveBlockEntity.STAGE_IDLE) return;

        super.submitRenderTask(renderPass, collector);
    }
}

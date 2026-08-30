package com.zavidvi.voidmod.client.renderer.otherworldlyforge;

import com.zavidvi.voidmod.block.OtherworldlyForgeBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import com.geckolib.renderer.GeoBlockRenderer;

public class OtherworldlyForgeRenderer extends GeoBlockRenderer<OtherworldlyForgeBlockEntity, BlockEntityRenderState> {
    public OtherworldlyForgeRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new OtherworldlyForgeModel());
    }
}

package com.zavidvi.voidmod.client.renderer.grave;

import com.zavidvi.voidmod.block.GraveBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoBlockRenderer;

public class GraveRenderer extends GeoBlockRenderer<GraveBlockEntity, BlockEntityRenderState> {
    public static final DataTicket<Integer> STAGE =
            DataTicket.create("voidmod:grave_stage", Integer.class);

    public static final DataTicket<Boolean> SPENT =
            DataTicket.create("voidmod:grave_spent", Boolean.class);

    public GraveRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new GraveModel());
        withRenderLayer(new GraveGlowLayer(this));
    }

    @Override
    public void addRenderData(GraveBlockEntity grave, Void relatedObject,
                              BlockEntityRenderState renderState, float partialTick) {
        super.addRenderData(grave, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(STAGE, grave.getStage());
        renderState.addGeckolibData(SPENT, grave.isSpent());
    }
}

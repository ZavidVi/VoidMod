package com.zavidvi.voidmod.client.renderer.grave;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.block.GraveBlockEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class GraveModel extends GeoModel<GraveBlockEntity> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "grave");
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "textures/block/grave.png");
    private static final Identifier TEXTURE_SPENT =
            Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "textures/block/grave_empty.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(GraveRenderer.SPENT, false) ? TEXTURE_SPENT : TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(GraveBlockEntity animatable) {
        return MODEL;
    }
}

package com.zavidvi.voidmod.client.renderer.otherworldlyforge;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.block.OtherworldlyForgeBlockEntity;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;

public class OtherworldlyForgeModel extends GeoModel<OtherworldlyForgeBlockEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "extramundane_forge");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "textures/block/extramundane_forge.png");
    }

    @Override
    public Identifier getAnimationResource(OtherworldlyForgeBlockEntity animatable) {
        return Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "extramundane_forge");
    }
}

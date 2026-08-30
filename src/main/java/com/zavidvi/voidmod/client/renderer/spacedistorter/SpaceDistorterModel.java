package com.zavidvi.voidmod.client.renderer.spacedistorter;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.item.SpaceDistorterItem;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;

public class SpaceDistorterModel extends GeoModel<SpaceDistorterItem> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "space_distorter");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "textures/item/space_distorter.png");
    }

    @Override
    public Identifier getAnimationResource(SpaceDistorterItem animatable) {
        return Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "space_distorter");
    }
}

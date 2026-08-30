package com.zavidvi.voidmod.client.renderer.otherworldlyforge;

import com.zavidvi.voidmod.item.OtherworldlyForgeBlockItem;
import com.geckolib.renderer.GeoItemRenderer;

public class OtherworldlyForgeItemRenderer extends GeoItemRenderer<OtherworldlyForgeBlockItem> {
    public OtherworldlyForgeItemRenderer() {
        super(new OtherworldlyForgeItemModel());
    }
}

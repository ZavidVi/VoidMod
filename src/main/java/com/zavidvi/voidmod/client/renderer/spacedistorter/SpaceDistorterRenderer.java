package com.zavidvi.voidmod.client.renderer.spacedistorter;

import com.zavidvi.voidmod.item.SpaceDistorterItem;
import com.geckolib.renderer.GeoItemRenderer;

public class SpaceDistorterRenderer extends GeoItemRenderer<SpaceDistorterItem> {
    public SpaceDistorterRenderer() {
        super(new SpaceDistorterModel());
    }
}

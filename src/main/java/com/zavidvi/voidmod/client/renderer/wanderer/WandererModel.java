package com.zavidvi.voidmod.client.renderer.wanderer;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.entity.wanderer.WandererEntity;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;

public class WandererModel extends GeoModel<WandererEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "wanderer");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "textures/entity/wanderer.png");
    }

    @Override
    public Identifier getAnimationResource(WandererEntity animatable) {
        return Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "wanderer");
    }
}

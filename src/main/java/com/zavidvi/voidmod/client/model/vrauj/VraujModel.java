package com.zavidvi.voidmod.client.model.vrauj;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.entity.vrauj.VraujEntity;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import com.geckolib.model.DefaultedEntityGeoModel;

public class VraujModel extends DefaultedEntityGeoModel<VraujEntity> {
    public VraujModel() {
        super(Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "vrauj"));
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "vrauj");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "textures/entity/vrauj.png");
    }

    @Override
    public Identifier getAnimationResource(VraujEntity animatable) {
        return Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "vrauj");
    }
}

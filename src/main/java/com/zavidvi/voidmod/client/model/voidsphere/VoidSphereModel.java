package com.zavidvi.voidmod.client.model.voidsphere;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.entity.voidsphere.VoidSphereEntity;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import com.geckolib.model.DefaultedEntityGeoModel;

public class VoidSphereModel extends DefaultedEntityGeoModel<VoidSphereEntity> {
    public VoidSphereModel() {
        super(Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "void_sphere"));
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "void_sphere");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "textures/entity/void_sphere.png");
    }

    @Override
    public Identifier getAnimationResource(VoidSphereEntity animatable) {
        return Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "void_sphere");
    }
}

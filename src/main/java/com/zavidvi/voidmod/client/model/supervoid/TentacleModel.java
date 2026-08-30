package com.zavidvi.voidmod.client.model.supervoid;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.entity.supervoid.TentacleEntity;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import com.geckolib.model.DefaultedEntityGeoModel;

public class TentacleModel extends DefaultedEntityGeoModel<TentacleEntity> {
    public TentacleModel() {
        super(Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "tentacle"));
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "tentacla");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "textures/entity/tentacle.png");
    }

    @Override
    public Identifier getAnimationResource(TentacleEntity animatable) {
        return Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "tentacla");
    }
}

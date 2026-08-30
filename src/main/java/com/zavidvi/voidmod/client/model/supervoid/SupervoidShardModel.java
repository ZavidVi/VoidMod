package com.zavidvi.voidmod.client.model.supervoid;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.entity.supervoid.SupervoidShardEntity;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import com.geckolib.model.DefaultedEntityGeoModel;

public class SupervoidShardModel extends DefaultedEntityGeoModel<SupervoidShardEntity> {
    public SupervoidShardModel() {
        super(Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "supervoid_ammo"));
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "supervoid_ammo");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "textures/entity/supervoid_ammo.png");
    }

    @Override
    public Identifier getAnimationResource(SupervoidShardEntity animatable) {
        return Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "supervoid_ammo");
    }
}

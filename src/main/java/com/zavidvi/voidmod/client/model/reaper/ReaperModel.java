package com.zavidvi.voidmod.client.model.reaper;

import com.zavidvi.voidmod.VoidMod;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class ReaperModel<T extends GeoAnimatable> extends GeoModel<T> {
    private final Identifier model;
    private final Identifier texture;
    private final Identifier animations;

    public ReaperModel(String name) {
        this.model = Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, name);
        this.texture = Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "textures/entity/" + name + ".png");
        this.animations = Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, name);
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return this.model;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return this.texture;
    }

    @Override
    public Identifier getAnimationResource(T animatable) {
        return this.animations;
    }
}

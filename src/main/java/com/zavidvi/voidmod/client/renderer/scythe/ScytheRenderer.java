package com.zavidvi.voidmod.client.renderer.scythe;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.item.ScytheItem;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class ScytheRenderer extends GeoItemRenderer<ScytheItem> {
    private static final long NO_OWNER = -1L;

    public ScytheRenderer(String assetName) {
        super(new ScytheModel(assetName));
    }

    @Override
    public long getInstanceId(ScytheItem animatable, RenderData relatedObject) {
        return animatedOwnerId(relatedObject);
    }

    @Override
    public void addRenderData(ScytheItem animatable, RenderData relatedObject,
                              com.geckolib.renderer.base.GeoRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);

        long ownerId = animatedOwnerId(relatedObject);
        renderState.addGeckolibData(ScytheItem.OWNER_ID,
                ownerId == NO_OWNER ? ScytheItem.NO_OWNER : (int) ownerId);
    }

    private static long animatedOwnerId(RenderData relatedObject) {
        if (!relatedObject.renderPerspective().firstPerson()) return NO_OWNER;
        if (relatedObject.itemOwner() == null) return NO_OWNER;

        net.minecraft.world.entity.LivingEntity owner = relatedObject.itemOwner().asLivingEntity();
        return owner == null ? NO_OWNER : owner.getId();
    }

    private static class ScytheModel extends GeoModel<ScytheItem> {
        private static final Identifier ANIMATIONS =
                Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "scythe");

        private final Identifier model;
        private final Identifier texture;

        ScytheModel(String assetName) {
            this.model = Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, assetName);
            this.texture = Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "textures/item/" + assetName + ".png");
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
        public Identifier getAnimationResource(ScytheItem animatable) {
            return ANIMATIONS;
        }
    }
}

package com.zavidvi.voidmod.client.renderer.rime;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.item.RimeItem;
import net.minecraft.resources.Identifier;

public class RimeRenderer extends GeoItemRenderer<RimeItem> {
    private static final long NO_OWNER = -1L;

    public RimeRenderer() {
        super(new RimeModel());
    }

    @Override
    public long getInstanceId(RimeItem animatable, RenderData relatedObject) {
        return animatedOwnerId(relatedObject);
    }

    @Override
    public void addRenderData(RimeItem animatable, RenderData relatedObject,
                              GeoRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);

        long ownerId = animatedOwnerId(relatedObject);
        renderState.addGeckolibData(RimeItem.OWNER_ID,
                ownerId == NO_OWNER ? RimeItem.NO_OWNER : (int) ownerId);
    }

    private static long animatedOwnerId(RenderData relatedObject) {
        if (!relatedObject.renderPerspective().firstPerson()) return NO_OWNER;
        if (relatedObject.itemOwner() == null) return NO_OWNER;

        net.minecraft.world.entity.LivingEntity owner = relatedObject.itemOwner().asLivingEntity();
        return owner == null ? NO_OWNER : owner.getId();
    }

    private static class RimeModel extends GeoModel<RimeItem> {
        private static final Identifier MODEL =
                Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "rime");
        private static final Identifier TEXTURE =
                Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "textures/item/rime.png");

        @Override
        public Identifier getModelResource(GeoRenderState renderState) {
            return MODEL;
        }

        @Override
        public Identifier getTextureResource(GeoRenderState renderState) {
            return TEXTURE;
        }

        @Override
        public Identifier getAnimationResource(RimeItem animatable) {
            return MODEL;
        }
    }
}

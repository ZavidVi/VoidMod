package com.zavidvi.voidmod.client.renderer.voidsphere;

import com.zavidvi.voidmod.entity.voidsphere.VoidSphereProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class VoidSphereProjectileRenderer extends ThrownItemRenderer<VoidSphereProjectileEntity> {
    public VoidSphereProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}

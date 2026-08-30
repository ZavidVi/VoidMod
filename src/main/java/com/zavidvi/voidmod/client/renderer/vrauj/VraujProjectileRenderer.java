package com.zavidvi.voidmod.client.renderer.vrauj;

import com.zavidvi.voidmod.entity.vrauj.VraujProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class VraujProjectileRenderer extends ThrownItemRenderer<VraujProjectileEntity> {
    public VraujProjectileRenderer(EntityRendererProvider.Context context) {
        super(context, 1.0F, true);
    }
}

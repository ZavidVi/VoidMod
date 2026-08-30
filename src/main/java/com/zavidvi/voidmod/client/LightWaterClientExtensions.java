package com.zavidvi.voidmod.client;

import com.zavidvi.voidmod.registry.ModFluidTypes;
import com.zavidvi.voidmod.registry.ModFluids;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;

public final class LightWaterClientExtensions {
    private LightWaterClientExtensions() {}

    public static void registerFluidModels(RegisterFluidModelsEvent event) {
        event.register(
                new FluidModel.Unbaked(
                        new Material(ModFluidTypes.STILL_TEXTURE),
                        new Material(ModFluidTypes.FLOW_TEXTURE),
                        new Material(ModFluidTypes.OVERLAY_TEXTURE),
                        null),
                ModFluids.LIGHT_WATER,
                ModFluids.LIGHT_WATER_FLOWING);
    }
}

package com.zavidvi.voidmod.registry;

import com.zavidvi.voidmod.VoidMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID, VoidMod.MOD_ID);

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> LIGHT_WATER =
            FLUIDS.register("light_water", () -> new BaseFlowingFluid.Source(properties()));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> LIGHT_WATER_FLOWING =
            FLUIDS.register("light_water_flowing", () -> new BaseFlowingFluid.Flowing(properties()));

    private static BaseFlowingFluid.Properties properties() {
        return new BaseFlowingFluid.Properties(
                ModFluidTypes.LIGHT_WATER,
                LIGHT_WATER,
                LIGHT_WATER_FLOWING)
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
                .tickRate(5)
                .block(ModBlocks.LIGHT_WATER)
                .bucket(ModItems.LIGHT_WATER_BUCKET);
    }

    public static boolean isLightWater(Fluid fluid) {
        return fluid == LIGHT_WATER.get() || fluid == LIGHT_WATER_FLOWING.get();
    }

    public static FlowingFluid source() {
        return LIGHT_WATER.get();
    }

    public static void register(IEventBus modEventBus) {
        FLUIDS.register(modEventBus);
    }
}

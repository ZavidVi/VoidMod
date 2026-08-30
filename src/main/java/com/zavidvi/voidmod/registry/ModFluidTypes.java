package com.zavidvi.voidmod.registry;

import com.zavidvi.voidmod.VoidMod;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModFluidTypes {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, VoidMod.MOD_ID);

    public static final Identifier STILL_TEXTURE =
            Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "block/light_water_still");
    public static final Identifier FLOW_TEXTURE =
            Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "block/light_water_flow");
    public static final Identifier OVERLAY_TEXTURE =
            Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "block/light_water_overlay");

    public static final DeferredHolder<FluidType, FluidType> LIGHT_WATER =
            FLUID_TYPES.register("light_water", () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid.voidmod.light_water")
                    .isWaterLike(true)
                    .canSwim(true)
                    .canDrown(true)
                    .canPushEntity(true)
                    .supportsBoating(true)
                    .density(1000)
                    .viscosity(1000)
                    .motionScale(0.014D)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)));

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
    }
}

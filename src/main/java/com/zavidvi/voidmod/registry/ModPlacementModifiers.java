package com.zavidvi.voidmod.registry;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.world.curse.CursedPlacementFilter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModPlacementModifiers {
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, VoidMod.MOD_ID);

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<CursedPlacementFilter>>
            CURSED = PLACEMENT_MODIFIERS.register("cursed",
                    () -> (PlacementModifierType<CursedPlacementFilter>) () -> CursedPlacementFilter.CODEC);

    public static void register(IEventBus modEventBus) {
        PLACEMENT_MODIFIERS.register(modEventBus);
    }
}

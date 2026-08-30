package com.zavidvi.voidmod.registry;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.world.curse.CursedRandomSpreadPlacement;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModStructurePlacements {
    public static final DeferredRegister<StructurePlacementType<?>> PLACEMENTS =
            DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, VoidMod.MOD_ID);

    private static final StructurePlacementType<CursedRandomSpreadPlacement> CURSED_RANDOM_SPREAD_TYPE =
            () -> CursedRandomSpreadPlacement.CODEC;

    public static final DeferredHolder<StructurePlacementType<?>, StructurePlacementType<CursedRandomSpreadPlacement>>
            CURSED_RANDOM_SPREAD = PLACEMENTS.register("cursed_random_spread",
                    () -> CURSED_RANDOM_SPREAD_TYPE);

    public static void register(IEventBus modEventBus) {
        PLACEMENTS.register(modEventBus);
    }
}

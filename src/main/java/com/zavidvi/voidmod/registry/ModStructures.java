package com.zavidvi.voidmod.registry;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.world.structure.DryJigsawStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, VoidMod.MOD_ID);

    private static final StructureType<DryJigsawStructure> DRY_JIGSAW_TYPE = () -> DryJigsawStructure.CODEC;

    public static final DeferredHolder<StructureType<?>, StructureType<DryJigsawStructure>>
            DRY_JIGSAW = STRUCTURE_TYPES.register("dry_jigsaw", () -> DRY_JIGSAW_TYPE);

    public static void register(IEventBus modEventBus) {
        STRUCTURE_TYPES.register(modEventBus);
    }
}

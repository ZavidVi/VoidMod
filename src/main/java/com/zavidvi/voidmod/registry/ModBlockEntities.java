package com.zavidvi.voidmod.registry;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.block.OtherworldlyForgeBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, VoidMod.MOD_ID);

    public static final Supplier<BlockEntityType<OtherworldlyForgeBlockEntity>> OTHERWORLDLY_FORGE =
            BLOCK_ENTITIES.register("otherworldly_forge", () ->
                    new BlockEntityType<>(OtherworldlyForgeBlockEntity::new,
                            ModBlocks.OTHERWORLDLY_FORGE.get()));

    public static final Supplier<BlockEntityType<com.zavidvi.voidmod.block.GraveBlockEntity>> GRAVE =
            BLOCK_ENTITIES.register("grave", () ->
                    new BlockEntityType<>(com.zavidvi.voidmod.block.GraveBlockEntity::new,
                            ModBlocks.GRAVE.get()));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}

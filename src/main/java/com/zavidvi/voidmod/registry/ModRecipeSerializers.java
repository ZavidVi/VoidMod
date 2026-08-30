package com.zavidvi.voidmod.registry;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.recipe.EssenceCraftingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, VoidMod.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EssenceCraftingRecipe>>
            ESSENCE_CRAFTING = RECIPE_SERIALIZERS.register("essence_crafting",
                    () -> new RecipeSerializer<>(EssenceCraftingRecipe.MAP_CODEC,
                            EssenceCraftingRecipe.STREAM_CODEC));

    public static void register(IEventBus modEventBus) {
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}

package com.zavidvi.voidmod.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zavidvi.voidmod.registry.ModItems;
import com.zavidvi.voidmod.registry.ModRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.NormalCraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class EssenceCraftingRecipe extends NormalCraftingRecipe {
    public static final MapCodec<EssenceCraftingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
                    CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.bookInfo),
                    ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                    com.mojang.serialization.Codec.INT.fieldOf("essence").forGetter(EssenceCraftingRecipe::essenceCost)
            ).apply(instance, EssenceCraftingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EssenceCraftingRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Recipe.CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
                    CraftingRecipe.CraftingBookInfo.STREAM_CODEC, recipe -> recipe.bookInfo,
                    ShapedRecipePattern.STREAM_CODEC, recipe -> recipe.pattern,
                    ItemStackTemplate.STREAM_CODEC, recipe -> recipe.result,
                    ByteBufCodecs.VAR_INT, EssenceCraftingRecipe::essenceCost,
                    EssenceCraftingRecipe::new);

    private final ShapedRecipePattern pattern;
    private final ItemStackTemplate result;
    private final int essenceCost;

    public EssenceCraftingRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                                 ShapedRecipePattern pattern, ItemStackTemplate result, int essenceCost) {
        super(commonInfo, bookInfo);
        this.pattern = pattern;
        this.result = result;
        this.essenceCost = essenceCost;
    }

    public int essenceCost() {
        return this.essenceCost;
    }

    public ShapedRecipePattern pattern() {
        return this.pattern;
    }

    public ItemStackTemplate result() {
        return this.result;
    }

    @Override
    public RecipeSerializer<? extends NormalCraftingRecipe> getSerializer() {
        return ModRecipeSerializers.ESSENCE_CRAFTING.get();
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        return PlacementInfo.createFromOptionals(this.pattern.ingredients());
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return this.pattern.matches(input);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return this.result.create();
    }

    @Override
    public List<RecipeDisplay> display() {
        List<SlotDisplay> slots = this.pattern.ingredients().stream()
                .map(ingredient -> ingredient
                        .map(Ingredient::display)
                        .orElse(SlotDisplay.Empty.INSTANCE))
                .toList();

        return List.of(new ShapedCraftingRecipeDisplay(
                this.pattern.width(),
                this.pattern.height(),
                slots,
                new SlotDisplay.ItemStackSlotDisplay(this.result),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return CraftingRecipe.defaultCraftingReminder(input);
    }

    public List<Optional<Ingredient>> ingredients() {
        return this.pattern.ingredients();
    }
}

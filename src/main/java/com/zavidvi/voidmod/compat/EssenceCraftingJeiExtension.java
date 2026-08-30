package com.zavidvi.voidmod.compat;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.recipe.EssenceCraftingRecipe;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;

public class EssenceCraftingJeiExtension implements ICraftingCategoryExtension<EssenceCraftingRecipe> {
    private static final Identifier FIRE_ICON =
            Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "textures/gui/fire_essence.png");

    private static final int ICON_SIZE = 14;
    private static final int TEXTURE_SIZE = 16;
    private static final int GAP = 2;

    @Override
    public int getWidth(RecipeHolder<EssenceCraftingRecipe> recipeHolder) {
        return recipeHolder.value().pattern().width();
    }

    @Override
    public int getHeight(RecipeHolder<EssenceCraftingRecipe> recipeHolder) {
        return recipeHolder.value().pattern().height();
    }

    @Override
    public boolean isHandled(RecipeHolder<EssenceCraftingRecipe> recipeHolder) {
        return true;
    }

    @Override
    public List<SlotDisplay> getIngredients(RecipeHolder<EssenceCraftingRecipe> recipeHolder) {
        return recipeHolder.value().pattern().ingredients().stream()
                .map(opt -> opt.map(Ingredient::display).orElse(SlotDisplay.Empty.INSTANCE))
                .toList();
    }

    @Override
    public void drawInfo(RecipeHolder<EssenceCraftingRecipe> recipeHolder, int recipeWidth, int recipeHeight,
                         GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        int cost = recipeHolder.value().essenceCost();
        if (cost <= 0) return;

        Font font = Minecraft.getInstance().font;
        String costStr = String.valueOf(cost);
        int textWidth = font.width(costStr);
        int totalWidth = ICON_SIZE + GAP + textWidth;

        int centerX = 98;
        int startX = centerX - totalWidth / 2;
        int startY = -1;

        int iconX = startX;
        int iconY = startY;
        int textX = startX + ICON_SIZE + GAP;
        int textY = startY + 3;

        graphics.blit(RenderPipelines.GUI_TEXTURED, FIRE_ICON, iconX, iconY,
                0.0F, 0.0F, ICON_SIZE, ICON_SIZE, TEXTURE_SIZE, TEXTURE_SIZE,
                TEXTURE_SIZE, TEXTURE_SIZE);

        graphics.text(font, costStr, textX, textY, 0xFFFFFFFF, true);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<EssenceCraftingRecipe> recipeHolder,
                           double mouseX, double mouseY) {
        int cost = recipeHolder.value().essenceCost();
        if (cost <= 0) return;

        Font font = Minecraft.getInstance().font;
        String costStr = String.valueOf(cost);
        int textWidth = font.width(costStr);
        int totalWidth = ICON_SIZE + GAP + textWidth;
        int centerX = 98;
        int startX = centerX - totalWidth / 2;
        int startY = -1;

        if (mouseX >= startX - 2 && mouseX <= startX + totalWidth + 2 &&
                mouseY >= startY - 2 && mouseY <= startY + ICON_SIZE + 2) {
            tooltip.add(Component.translatable("voidmod.jei.essence_cost", cost));
        }
    }
}

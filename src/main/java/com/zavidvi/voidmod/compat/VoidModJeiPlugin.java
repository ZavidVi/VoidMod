package com.zavidvi.voidmod.compat;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.client.gui.VoidModGuiAreas;
import com.zavidvi.voidmod.recipe.EssenceCraftingRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.Identifier;

import java.util.List;

@JeiPlugin
public class VoidModJeiPlugin implements IModPlugin {
    private static final Identifier UID =
            Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        registration.getCraftingCategory().addExtension(EssenceCraftingRecipe.class, new EssenceCraftingJeiExtension());
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(InventoryScreen.class, new IGuiContainerHandler<InventoryScreen>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(InventoryScreen screen) {
                return VoidModGuiAreas.forInventory(screen);
            }
        });
    }
}

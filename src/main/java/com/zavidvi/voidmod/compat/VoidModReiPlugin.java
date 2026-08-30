package com.zavidvi.voidmod.compat;

import com.zavidvi.voidmod.client.gui.VoidModGuiAreas;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.Rect2i;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@REIPluginClient
public class VoidModReiPlugin implements REIClientPlugin {
    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(InventoryScreen.class, VoidModReiPlugin::areasOf);
    }

    private static Collection<Rectangle> areasOf(InventoryScreen screen) {
        List<Rect2i> areas = VoidModGuiAreas.forInventory(screen);
        List<Rectangle> zones = new ArrayList<>(areas.size());
        for (Rect2i area : areas) {
            zones.add(new Rectangle(area.getX(), area.getY(), area.getWidth(), area.getHeight()));
        }
        return zones;
    }
}

package com.zavidvi.voidmod.client.gui;

import com.zavidvi.voidmod.client.stat.StatScreenEvents;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.Rect2i;

import java.util.ArrayList;
import java.util.List;

public final class VoidModGuiAreas {
    private VoidModGuiAreas() {}

    public static List<Rect2i> forInventory(InventoryScreen screen) {
        List<Rect2i> areas = new ArrayList<>(3);

        addIfPresent(areas, StatScreenEvents.getStatsButtonArea(screen));
        addIfPresent(areas, StatScreenEvents.getStatPanelArea(screen));

        return areas;
    }

    private static void addIfPresent(List<Rect2i> areas, Rect2i area) {
        if (area != null) {
            areas.add(area);
        }
    }
}

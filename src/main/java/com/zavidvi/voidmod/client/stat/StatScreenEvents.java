package com.zavidvi.voidmod.client.stat;

import com.zavidvi.voidmod.VoidMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.List;

@EventBusSubscriber(modid = VoidMod.MOD_ID, value = Dist.CLIENT)
public class StatScreenEvents {
    private static final int BUTTON_OFFSET_X = 126;

    private static final int PANEL_GAP = 4;

    private static final int EFFECT_COLUMN_WIDTH = 32;

    private static final int EFFECT_LABELS_WIDTH = 120;

    private static StatsButton statsButton;

    public static Rect2i getStatsButtonArea(InventoryScreen screen) {
        return new Rect2i(
                screen.getGuiLeft() + BUTTON_OFFSET_X,
                screen.height / 2 - 22,
                StatsButton.WIDTH,
                StatsButton.HEIGHT);
    }

    public static Rect2i getStatPanelArea(InventoryScreen screen) {
        if (!StatPanelState.isVisible()) return null;

        return new Rect2i(
                screen.getGuiLeft() + screen.getXSize() + PANEL_GAP,
                screen.getGuiTop(),
                StatPanel.WIDTH,
                screen.getYSize());
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen screen)) return;

        Rect2i area = getStatsButtonArea(screen);
        statsButton = new StatsButton(area.getX(), area.getY());
        event.addListener(statsButton);
    }

    @SubscribeEvent
    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        if (!(event.getScreen() instanceof InventoryScreen screen)) return;

        if (statsButton != null) {
            Rect2i buttonArea = getStatsButtonArea(screen);
            statsButton.setPosition(buttonArea.getX(), buttonArea.getY());
        }
    }

    private static void renderStatTooltip(ScreenEvent.Render.Post event, InventoryScreen screen,
                                          Player player, Rect2i area, float progress) {
        List<Component> tooltip = StatPanel.hoveredTooltip(
                player, area.getX(), area.getY(), area.getHeight(),
                event.getMouseX(), event.getMouseY(), progress);
        if (tooltip == null) return;

        Font font = screen.getMinecraft().font;
        List<ClientTooltipComponent> lines = tooltip.stream()
                .map(line -> ClientTooltipComponent.create(line.getVisualOrderText()))
                .toList();

        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        graphics.nextStratum();
        graphics.tooltip(font, lines, event.getMouseX(), event.getMouseY(),
                DefaultTooltipPositioner.INSTANCE, null);
    }

    @SubscribeEvent
    public static void onContainerForeground(ContainerScreenEvent.Render.Foreground event) {
        if (!(event.getContainerScreen() instanceof InventoryScreen screen)) return;

        Rect2i area = getStatPanelArea(screen);
        if (area == null) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        graphics.pose().pushMatrix();
        graphics.pose().translate(-screen.getGuiLeft(), -screen.getGuiTop());
        StatPanel.render(graphics, screen.getMinecraft().font,
                player, area.getX(), area.getY(), area.getHeight(), StatPanelState.progress());
        graphics.pose().popMatrix();
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen screen)) return;

        Rect2i area = getStatPanelArea(screen);
        if (area == null) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        renderStatTooltip(event, screen, player, area, StatPanelState.progress());
    }

    @SubscribeEvent
    public static void onRenderInventoryEffects(ScreenEvent.RenderInventoryMobEffects event) {
        if (!(event.getScreen() instanceof InventoryScreen screen)) return;
        if (getStatPanelArea(screen) == null) return;

        int room = event.getAvailableSpace();
        int shift = Math.min(PANEL_GAP + StatPanel.WIDTH, room - EFFECT_COLUMN_WIDTH);
        if (shift <= 0) return;

        event.addHorizontalOffset(shift);
        event.setCompact(room - shift < EFFECT_LABELS_WIDTH);
    }
}

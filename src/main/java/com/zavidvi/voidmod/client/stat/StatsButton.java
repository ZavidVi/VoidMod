package com.zavidvi.voidmod.client.stat;

import com.zavidvi.voidmod.VoidMod;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class StatsButton extends AbstractButton {
    private static final Identifier ICON =
            Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "textures/gui/stats_icon.png");

    private static final int TEXTURE_SIZE = 32;
    private static final int ICON_SIZE = 16;

    public static final int WIDTH = 20;
    public static final int HEIGHT = 18;

    private static final int OUTLINE = 0xFF000000;
    private static final int FACE = 0xFFC6C6C6;
    private static final int SHADOW = 0xFF555555;

    private static final int OUTLINE_HOVER = 0xFF00073E;
    private static final int FACE_HOVER = 0xFF8892C9;
    private static final int SHADOW_HOVER = 0xFF343E75;

    private static final int HIGHLIGHT = 0xFFFFFFFF;

    public StatsButton(int x, int y) {
        super(x, y, WIDTH, HEIGHT, Component.translatable("voidmod.gui.stats_button"));
    }

    private static boolean openingReported = false;

    @Override
    public void onPress(InputWithModifiers input) {
        StatPanelState.toggle();

        if (StatPanelState.isOpen() && !openingReported) {
            openingReported = true;
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(
                    new com.zavidvi.voidmod.network.StatsPanelOpenedPayload());
        }
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        drawBackground(graphics, getX(), getY(), isHoveredOrFocused());

        graphics.blit(RenderPipelines.GUI_TEXTURED, ICON, getX() + 2, getY() + 1,
                0.0F, 0.0F, ICON_SIZE, ICON_SIZE, TEXTURE_SIZE, TEXTURE_SIZE,
                TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private static void drawBackground(GuiGraphicsExtractor graphics, int x, int y, boolean hovered) {
        int outline = hovered ? OUTLINE_HOVER : OUTLINE;
        int face = hovered ? FACE_HOVER : FACE;
        int shadow = hovered ? SHADOW_HOVER : SHADOW;

        int right = x + WIDTH;
        int bottom = y + HEIGHT;

        graphics.fill(x + 2, y, right - 2, y + 1, outline);
        graphics.fill(x + 2, bottom - 1, right - 2, bottom, outline);
        graphics.fill(x, y + 2, x + 1, bottom - 2, outline);
        graphics.fill(right - 1, y + 2, right, bottom - 2, outline);

        graphics.fill(x + 1, y + 1, x + 2, y + 2, outline);
        graphics.fill(right - 2, y + 1, right - 1, y + 2, outline);
        graphics.fill(x + 1, bottom - 2, x + 2, bottom - 1, outline);
        graphics.fill(right - 2, bottom - 2, right - 1, bottom - 1, outline);

        graphics.fill(x + 2, y + 1, right - 2, y + 2, HIGHLIGHT);
        graphics.fill(x + 1, y + 2, x + 2, bottom - 2, HIGHLIGHT);
        graphics.fill(x + 2, bottom - 2, right - 2, bottom - 1, shadow);
        graphics.fill(right - 2, y + 2, right - 1, bottom - 2, shadow);

        graphics.fill(x + 2, y + 2, right - 2, bottom - 2, face);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}

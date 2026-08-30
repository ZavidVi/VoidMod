package com.zavidvi.voidmod.client.stat;

import com.zavidvi.voidmod.network.ChooseDexterityPayload;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;

public final class ArmorTypePopup {
    private static final int PADDING = 5;
    private static final int BUTTON_HEIGHT = 14;
    private static final int BUTTON_GAP = 2;
    private static final int MAX_WIDTH = 150;
    private static final int SLOT_GAP = 4;

    private static final int PANEL_BORDER = 0xFFFFFFFF;
    private static final int PANEL_BG = 0xFFC6C6C6;
    private static final int PANEL_SHADOW = 0xFF555555;
    private static final int BUTTON_FACE = 0xFF8B8B8B;
    private static final int BUTTON_FACE_HOVER = 0xFF8892C9;
    private static final int BUTTON_EDGE = 0xFF373737;

    private static final int COLOR_TITLE = 0x3F3F3F;
    private static final int COLOR_BUTTON_TEXT = 0xFFFFFF;
    private static final int COLOR_WARNING = StatDisplay.COLOR_NEGATIVE;

    private ArmorTypePopup() {}

    private static int menuSlot = -1;
    private static int left;
    private static int top;
    private static int width;
    private static int height;
    private static int meleeTop;
    private static int rangedTop;
    private static List<FormattedCharSequence> warningLines = List.of();

    public static boolean isOpen() {
        return menuSlot >= 0;
    }

    public static void close() {
        menuSlot = -1;
        warningLines = List.of();
    }

    public static void open(Font font, int slot, int anchorX, int anchorY, int screenWidth, int screenHeight) {
        menuSlot = slot;

        Component title = Component.translatable("voidmod.gui.dexterity_choice.title");
        Component melee = StatDisplay.typeName(com.zavidvi.voidmod.stat.DexterityType.MELEE);
        Component ranged = StatDisplay.typeName(com.zavidvi.voidmod.stat.DexterityType.RANGED);

        int content = Math.max(font.width(title), Math.max(font.width(melee), font.width(ranged)));
        width = Math.min(MAX_WIDTH, content + PADDING * 2);
        warningLines = font.split(Component.translatable("voidmod.gui.dexterity_choice.warning"),
                width - PADDING * 2);

        int y = PADDING + font.lineHeight + BUTTON_GAP;
        meleeTop = y;
        y += BUTTON_HEIGHT + BUTTON_GAP;
        rangedTop = y;
        y += BUTTON_HEIGHT + BUTTON_GAP;
        height = y + warningLines.size() * font.lineHeight + PADDING;

        left = anchorX + 16 + SLOT_GAP;
        if (left + width > screenWidth) {
            left = Math.max(0, anchorX - SLOT_GAP - width);
        }
        top = Math.max(0, Math.min(anchorY, screenHeight - height));
    }

    public static void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        if (!isOpen()) return;

        graphics.fill(left, top, left + width, top + height, PANEL_BORDER);
        graphics.fill(left + 1, top + 1, left + width - 1, top + height - 1, PANEL_BG);
        graphics.fill(left + 2, top + height - 2, left + width - 1, top + height - 1, PANEL_SHADOW);
        graphics.fill(left + width - 2, top + 2, left + width - 1, top + height - 1, PANEL_SHADOW);

        graphics.text(font, Component.translatable("voidmod.gui.dexterity_choice.title"),
                left + PADDING, top + PADDING, ARGB.opaque(COLOR_TITLE), false);

        drawButton(graphics, font, meleeTop,
                StatDisplay.typeName(com.zavidvi.voidmod.stat.DexterityType.MELEE), mouseX, mouseY);
        drawButton(graphics, font, rangedTop,
                StatDisplay.typeName(com.zavidvi.voidmod.stat.DexterityType.RANGED), mouseX, mouseY);

        int y = top + rangedTop + BUTTON_HEIGHT + BUTTON_GAP;
        for (FormattedCharSequence line : warningLines) {
            graphics.text(font, line, left + PADDING, y, ARGB.opaque(COLOR_WARNING), false);
            y += font.lineHeight;
        }
    }

    private static void drawButton(GuiGraphicsExtractor graphics, Font font, int relativeTop,
                                   Component label, int mouseX, int mouseY) {
        int x0 = left + PADDING;
        int x1 = left + width - PADDING;
        int y0 = top + relativeTop;
        int y1 = y0 + BUTTON_HEIGHT;

        boolean hovered = mouseX >= x0 && mouseX < x1 && mouseY >= y0 && mouseY < y1;

        graphics.fill(x0, y0, x1, y1, BUTTON_EDGE);
        graphics.fill(x0 + 1, y0 + 1, x1 - 1, y1 - 1, hovered ? BUTTON_FACE_HOVER : BUTTON_FACE);

        int textX = x0 + (x1 - x0 - font.width(label)) / 2;
        int textY = y0 + (BUTTON_HEIGHT - font.lineHeight) / 2 + 1;
        graphics.text(font, label, textX, textY, ARGB.opaque(COLOR_BUTTON_TEXT), true);
    }

    public static boolean mouseClicked(int mouseX, int mouseY) {
        if (!isOpen()) return false;

        Boolean ranged = buttonAt(mouseX, mouseY);
        int slot = menuSlot;
        close();

        if (ranged != null) {
            ClientPacketDistributor.sendToServer(new ChooseDexterityPayload(slot, ranged));
        }
        return true;
    }

    private static Boolean buttonAt(int mouseX, int mouseY) {
        int x0 = left + PADDING;
        int x1 = left + width - PADDING;
        if (mouseX < x0 || mouseX >= x1) return null;

        if (mouseY >= top + meleeTop && mouseY < top + meleeTop + BUTTON_HEIGHT) return Boolean.FALSE;
        if (mouseY >= top + rangedTop && mouseY < top + rangedTop + BUTTON_HEIGHT) return Boolean.TRUE;
        return null;
    }
}

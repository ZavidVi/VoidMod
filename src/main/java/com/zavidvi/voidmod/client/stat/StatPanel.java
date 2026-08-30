package com.zavidvi.voidmod.client.stat;

import com.zavidvi.voidmod.stat.DexterityType;
import com.zavidvi.voidmod.stat.ManaSystem;
import com.zavidvi.voidmod.stat.PlayerStat;
import com.zavidvi.voidmod.stat.StatData;
import com.zavidvi.voidmod.stat.StatManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public final class StatPanel {
    public static final int WIDTH = 110;

    private static final int PADDING = 6;

    private static final int LINE = 11;
    private static final int MAX_LINE = 15;
    private static final int SEPARATOR_HEIGHT = 1;
    private static final int GAPS = 3;
    private static final int MIN_GAP = 2;

    private static final int INSET_MARGIN = 3;

    private static final int PANEL_BG = 0xFFC6C6C6;
    private static final int PANEL_BORDER = 0xFFFFFFFF;
    private static final int PANEL_SHADOW = 0xFF555555;
    private static final int INSET_BG = 0xFFBDBDBD;
    private static final int INSET_DARK = 0xFF8B8B8B;
    private static final int INSET_LIGHT = 0xFFFFFFFF;
    private static final int SEPARATOR = 0xFF6A6A6A;

    private StatPanel() {}

    private record Row(Component label, String value, int color, boolean gradient) {}

    private record Layout(int line, int gap) {
        int firstStatY() {
            return PADDING + line + gap;
        }
    }

    private static Layout layout(int height, int rows) {
        int free = height - 2 * PADDING - SEPARATOR_HEIGHT;
        int line = Mth.clamp(free / Math.max(1, rows), LINE, MAX_LINE);
        int slack = free - rows * line;
        return new Layout(line, Math.max(MIN_GAP, slack / GAPS));
    }

    private static int rowCount(StatData data, int derivedRows) {
        return 1
                + PlayerStat.values().length
                + (data.getDexterityType() != DexterityType.NONE ? 1 : 0)
                + derivedRows;
    }

    public static void render(GuiGraphicsExtractor graphics, Font font, Player player,
                              int left, int top, int height, float progress) {
        if (progress <= 0.0F) return;

        StatData data = StatManager.compute(player);
        List<Row> derived = derivedRows(player, data);
        Layout layout = layout(height, rowCount(data, derived.size()));

        graphics.pose().pushMatrix();
        graphics.pose().translate(left + WIDTH / 2.0F, top + height / 2.0F);
        graphics.pose().scale(progress, progress);
        graphics.pose().translate(-WIDTH / 2.0F, -height / 2.0F);

        int contentTop = layout.firstStatY() - INSET_MARGIN;
        drawPanel(graphics, 0, 0, WIDTH, height, contentTop);

        int x = PADDING;
        int y = PADDING;

        drawText(graphics, font, Component.translatable("voidmod.gui.attributes"),
                x, y, StatDisplay.COLOR_LABEL, false);
        y = layout.firstStatY();

        for (PlayerStat stat : PlayerStat.values()) {
            boolean negative = data.isNegative(stat);
            drawRow(graphics, font, x, y,
                    StatDisplay.name(stat), StatDisplay.number(data.get(stat)),
                    StatDisplay.color(stat), negative, true);
            y += layout.line();
        }

        DexterityType type = data.getDexterityType();
        if (type != DexterityType.NONE) {
            drawText(graphics, font, StatDisplay.typeName(type), x, y, StatDisplay.COLOR_DEXTERITY, true);
            y += layout.line();
        }

        y += layout.gap();
        graphics.fill(x, y, WIDTH - PADDING, y + SEPARATOR_HEIGHT, SEPARATOR);
        y += SEPARATOR_HEIGHT + layout.gap();

        for (Row row : derived) {
            drawRow(graphics, font, x, y, row.label(), row.value(), row.color(), row.gradient(), false);
            y += layout.line();
        }

        graphics.pose().popMatrix();
    }

    private static List<Row> derivedRows(Player player, StatData data) {
        List<Row> rows = new ArrayList<>(4);
        rows.add(new Row(Component.translatable("voidmod.gui.armor"),
                StatDisplay.number(data.armor()), StatDisplay.COLOR_LABEL, false));
        rows.add(new Row(Component.translatable("voidmod.gui.health"),
                StatDisplay.number(data.maxHealth()), StatDisplay.COLOR_LABEL, false));
        rows.add(new Row(Component.translatable("voidmod.gui.mana"),
                StatDisplay.number(Math.floor(ManaSystem.get(player)))
                        + " / " + StatDisplay.number(data.maxMana()),
                StatDisplay.COLOR_LABEL, false));
        rows.add(new Row(Component.translatable("voidmod.gui.mana_regen"),
                StatDisplay.regen(data.manaRegen()), StatDisplay.COLOR_LABEL, false));
        return rows;
    }

    private static void drawRow(GuiGraphicsExtractor graphics, Font font, int x, int y,
                                Component label, String value, int color, boolean gradient,
                                boolean shadow) {
        if (gradient) {
            drawGradientText(graphics, font, label.getString(), x, y, color, StatDisplay.COLOR_NEGATIVE, shadow);
        } else {
            drawText(graphics, font, label, x, y, color, shadow);
        }

        int valueX = WIDTH - PADDING - font.width(value);
        int valueColor = gradient ? StatDisplay.COLOR_NEGATIVE : color;
        drawText(graphics, font, value, valueX, y, valueColor, shadow);
    }

    private static void drawText(GuiGraphicsExtractor graphics, Font font, Component text,
                                 int x, int y, int color, boolean shadow) {
        graphics.text(font, text, x, y, ARGB.opaque(color), shadow);
    }

    private static void drawText(GuiGraphicsExtractor graphics, Font font, String text,
                                 int x, int y, int color, boolean shadow) {
        graphics.text(font, text, x, y, ARGB.opaque(color), shadow);
    }

    private static void drawGradientText(GuiGraphicsExtractor graphics, Font font, String text,
                                         int x, int y, int from, int to, boolean shadow) {
        int cursor = x;
        int length = text.length();
        for (int i = 0; i < length; i++) {
            String ch = String.valueOf(text.charAt(i));
            float t = length <= 1 ? 1.0F : (float) i / (length - 1);
            drawText(graphics, font, ch, cursor, y, StatDisplay.lerpColor(from, to, t), shadow);
            cursor += font.width(ch);
        }
    }

    private static void drawPanel(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int contentTop) {
        graphics.fill(x, y, x + width, y + height, PANEL_BORDER);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_BG);
        graphics.fill(x + 2, y + height - 2, x + width - 1, y + height - 1, PANEL_SHADOW);
        graphics.fill(x + width - 2, y + 2, x + width - 1, y + height - 1, PANEL_SHADOW);

        int insetLeft = x + PADDING - INSET_MARGIN;
        int insetRight = x + width - PADDING + INSET_MARGIN;
        int insetTop = y + contentTop;
        int insetBottom = y + height - PADDING + INSET_MARGIN;
        if (insetBottom <= insetTop + 2 || insetRight <= insetLeft + 2) return;

        graphics.fill(insetLeft, insetTop, insetRight, insetBottom, INSET_LIGHT);
        graphics.fill(insetLeft, insetTop, insetRight - 1, insetBottom - 1, INSET_DARK);
        graphics.fill(insetLeft + 1, insetTop + 1, insetRight - 1, insetBottom - 1, INSET_BG);
    }

    public static List<Component> hoveredTooltip(Player player, int left, int top, int height,
                                                 int mouseX, int mouseY, float progress) {
        if (progress <= 0.0F) return null;

        float localX = (mouseX - (left + WIDTH / 2.0F)) / progress + WIDTH / 2.0F;
        float localY = (mouseY - (top + height / 2.0F)) / progress + height / 2.0F;
        if (localX < 0 || localX > WIDTH || localY < 0 || localY > height) return null;

        StatData data = StatManager.compute(player);
        Layout layout = layout(height, rowCount(data, derivedRows(player, data).size()));

        int index = Mth.floor((localY - layout.firstStatY()) / (float) layout.line());
        if (index < 0 || index >= PlayerStat.values().length) return null;

        PlayerStat stat = PlayerStat.values()[index];
        boolean negative = data.isNegative(stat);

        List<Component> lines = new ArrayList<>(2);
        lines.add(StatDisplay.name(stat)
                .copy().withStyle(s -> s.withColor(StatDisplay.color(stat))));
        lines.add(StatDisplay.description(stat, negative, net.minecraft.client.Minecraft.getInstance().hasShiftDown())
                .copy().withStyle(s -> s.withColor(
                        negative ? StatDisplay.COLOR_NEGATIVE : StatDisplay.COLOR_NEUTRAL)));
        return lines;
    }
}

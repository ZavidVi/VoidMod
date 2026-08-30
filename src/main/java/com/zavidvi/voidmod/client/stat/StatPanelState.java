package com.zavidvi.voidmod.client.stat;

import net.minecraft.util.Mth;

public final class StatPanelState {
    private static final long ANIMATION_MS = 200L;

    private static boolean open = false;

    private static long toggledAt = 0L;

    private StatPanelState() {}

    public static boolean isOpen() {
        return open;
    }

    public static void toggle() {
        open = !open;
        toggledAt = System.currentTimeMillis();
    }

    public static float progress() {
        long elapsed = System.currentTimeMillis() - toggledAt;
        float t = Mth.clamp(elapsed / (float) ANIMATION_MS, 0.0F, 1.0F);
        return open ? t : 1.0F - t;
    }

    public static boolean isVisible() {
        return progress() > 0.0F;
    }
}

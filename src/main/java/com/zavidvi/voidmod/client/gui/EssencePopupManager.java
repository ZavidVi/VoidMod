package com.zavidvi.voidmod.client.gui;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EssencePopupManager {
    private static final long LIFETIME_MS = 1800 + 4000;
    private static final long FADE_IN_MS = 150;
    private static final long FADE_OUT_MS = 800;
    private static final float RISE_PIXELS = 20.0f;

    private static final List<Popup> POPUPS = new CopyOnWriteArrayList<>();

    private EssencePopupManager() {
    }

    public static void addPopup(int amount) {
        POPUPS.add(new Popup(amount, System.currentTimeMillis()));
    }

    public static List<Popup> getActivePopups() {
        long now = System.currentTimeMillis();
        POPUPS.removeIf(popup -> now - popup.spawnTime() > LIFETIME_MS);
        return POPUPS;
    }

    public record Popup(int amount, long spawnTime) {
        public float getAlpha(long now) {
            long elapsed = now - spawnTime();
            if (elapsed < FADE_IN_MS) {
                return elapsed / (float) FADE_IN_MS;
            }
            if (elapsed > LIFETIME_MS - FADE_OUT_MS) {
                return Math.max(0.0f, (LIFETIME_MS - elapsed) / (float) FADE_OUT_MS);
            }
            return 1.0f;
        }

        public float getRiseOffset(long now) {
            long elapsed = Math.min(Math.max(now - spawnTime(), 0), LIFETIME_MS);
            return (elapsed / (float) LIFETIME_MS) * RISE_PIXELS;
        }
    }
}

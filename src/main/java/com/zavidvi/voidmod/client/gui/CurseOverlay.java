package com.zavidvi.voidmod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

public final class CurseOverlay {
    private static final String TEXT = "«Яркое пламя мира тускнеет…»";

    private static final long FADE_IN_MS = 1500L;
    private static final long FADE_OUT_MS = 1500L;

    private static final long LIGHT_SWITCH_DELAY_MS = 2500L;

    private static final long MIN_HOLD_AFTER_SWITCH_MS = 2500L;

    private static final long MAX_HOLD_AFTER_SWITCH_MS = 15000L;

    private static final long TYPING_MS = 2000L;

    private static final float TEXT_SCALE = 1.6F;

    private static long startTime = -1L;

    private static long switchTime = -1L;

    private static long fadeOutStart = -1L;

    private static Runnable pendingWhenDark = null;

    private CurseOverlay() {}

    public static void show() {
        startTime = System.currentTimeMillis();
        switchTime = -1L;
        fadeOutStart = -1L;
        playTheme();
    }

    private static void playTheme() {
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        minecraft.getSoundManager().play(
                net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                        com.zavidvi.voidmod.registry.ModSounds.WORLD_FLAME_FADES.get(), 1.0F, 1.0F));
    }

    public static boolean isActive() {
        return startTime >= 0L;
    }

    public static void runWhenFullyDark(Runnable action) {
        if (!isActive()) {
            action.run();
            return;
        }
        if (switchTime >= 0L) {
            action.run();
            return;
        }
        pendingWhenDark = action;
    }

    public static void reset() {
        pendingWhenDark = null;
        startTime = -1L;
        switchTime = -1L;
        fadeOutStart = -1L;
    }

    public static void cancelPending() {
        pendingWhenDark = null;
    }

    private static void flushPending() {
        Runnable action = pendingWhenDark;
        pendingWhenDark = null;
        if (action != null) {
            action.run();
        }
    }

    private static boolean canFadeOut(long heldMs) {
        if (heldMs < MIN_HOLD_AFTER_SWITCH_MS) {
            return false;
        }
        if (heldMs >= MAX_HOLD_AFTER_SWITCH_MS) {
            return true;
        }
        if (com.zavidvi.voidmod.event.curse.ClientCurseLightEvents.isRelightPending()) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        return mc.levelRenderer.hasRenderedAllSections();
    }

    public static void render(GuiGraphicsExtractor guiGraphics, int screenWidth, int screenHeight) {
        if (startTime < 0L) {
            return;
        }

        long now = System.currentTimeMillis();
        long elapsed = now - startTime;

        float progress;
        if (elapsed < FADE_IN_MS) {
            progress = elapsed / (float) FADE_IN_MS;
        } else if (fadeOutStart < 0L) {
            progress = 1.0F;
            if (switchTime < 0L && elapsed >= FADE_IN_MS + LIGHT_SWITCH_DELAY_MS) {
                flushPending();
                switchTime = now;
            }
            if (switchTime >= 0L && canFadeOut(now - switchTime)) {
                fadeOutStart = now;
            }
        } else {
            long fading = now - fadeOutStart;
            if (fading >= FADE_OUT_MS) {
                flushPending();
                reset();
                return;
            }
            progress = 1.0F - fading / (float) FADE_OUT_MS;
        }
        progress = Mth.clamp(progress, 0.0F, 1.0F);

        int backgroundAlpha = (int) (progress * 255.0F);
        if (backgroundAlpha <= 0) {
            return;
        }
        guiGraphics.fill(0, 0, screenWidth, screenHeight, backgroundAlpha << 24);

        long typingElapsed = elapsed - FADE_IN_MS;
        if (typingElapsed < 0L) {
            return;
        }

        int visibleChars = (int) Math.min(
                TEXT.length(),
                TEXT.length() * Math.min(typingElapsed, TYPING_MS) / TYPING_MS);
        if (visibleChars <= 0) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        int textAlpha = (int) (progress * 255.0F);
        if (textAlpha < 8) {
            return;
        }

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(screenWidth / 2.0F, screenHeight / 2.0F);
        guiGraphics.pose().scale(TEXT_SCALE, TEXT_SCALE);

        int totalWidth = font.width(TEXT);
        int x = -totalWidth / 2;
        int length = TEXT.length();

        for (int i = 0; i < visibleChars; i++) {
            String ch = String.valueOf(TEXT.charAt(i));
            float ratio = (float) i / Math.max(1, length - 1);
            int channel = (int) (255 * (1.0F - ratio));
            int color = (textAlpha << 24) | (channel << 16) | (channel << 8);

            guiGraphics.text(font, ch, x, -font.lineHeight / 2, color, false);
            x += font.width(ch);
        }
        guiGraphics.pose().popMatrix();
    }
}

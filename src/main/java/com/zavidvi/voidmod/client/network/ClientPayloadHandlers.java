package com.zavidvi.voidmod.client.network;

import com.zavidvi.voidmod.client.gui.CurseOverlay;
import com.zavidvi.voidmod.client.gui.EssencePopupManager;
import com.zavidvi.voidmod.client.gui.WandererDialogueScreen;
import com.zavidvi.voidmod.event.curse.ClientCurseLightEvents;
import com.zavidvi.voidmod.network.SyncProgressionPayload;
import com.zavidvi.voidmod.util.CurseLightState;
import com.zavidvi.voidmod.world.progression.ClientProgressionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class ClientPayloadHandlers {
    private ClientPayloadHandlers() {}

    public static void openWandererDialogue() {
        Minecraft.getInstance().setScreen(new WandererDialogueScreen());
    }

    public static void showWandererResponse(String key, int arg) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof WandererDialogueScreen dialogueScreen) {
            dialogueScreen.updateResponse(key, arg);
        }
    }

    public static void showEssencePopup(int amount) {
        EssencePopupManager.addPopup(amount);
    }

    public static void playScytheSwing(int hit, int swingTicks) {
        net.minecraft.client.player.LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        com.zavidvi.voidmod.client.item.ScytheSwings.start(player.getId(), hit, swingTicks);
    }

    public static void showCurseMessage() {
        CurseOverlay.show();
    }

    public static void syncProgression(SyncProgressionPayload payload) {
        boolean wasCursed = ClientProgressionData.worldCursed;
        boolean cursed = payload.worldCursed();

        ClientProgressionData.portalAttempted = payload.portalAttempted();
        ClientProgressionData.wandererTalked = payload.wandererTalked();
        ClientProgressionData.reaperDefeated = payload.reaperDefeated();

        if (wasCursed == cursed) {
            ClientProgressionData.worldCursed = cursed;
            CurseLightState.setClientCursed(cursed);
            return;
        }

        Runnable applyCurseVisuals = () -> {
            ClientProgressionData.worldCursed = cursed;
            CurseLightState.setClientCursed(cursed);

            ClientCurseLightEvents.onCurseChanged(cursed);

            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                mc.levelRenderer.allChanged();
            }
        };

        if (cursed) {
            CurseOverlay.runWhenFullyDark(applyCurseVisuals);
        } else {
            CurseOverlay.cancelPending();
            applyCurseVisuals.run();
        }
    }
}

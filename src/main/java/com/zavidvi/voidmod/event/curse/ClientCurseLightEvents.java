package com.zavidvi.voidmod.event.curse;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.util.CurseLightState;
import com.zavidvi.voidmod.util.LightUpdater;
import com.zavidvi.voidmod.world.progression.ClientProgressionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@EventBusSubscriber(modid = VoidMod.MOD_ID, value = Dist.CLIENT)
public class ClientCurseLightEvents {
    private static final int SECTION_BUDGET_PER_TICK = 48;

    private static final int MAX_QUEUE_SIZE = 8192;

    private static final LinkedHashSet<Long> QUEUE = new LinkedHashSet<>();
    private static final LinkedHashSet<Long> DONE = new LinkedHashSet<>();

    private static int lightVersion = 0;

    public static boolean isRelightPending() {
        return !QUEUE.isEmpty();
    }

    public static void onCurseChanged(boolean cursed) {
        lightVersion = cursed ? 1 : 0;
        QUEUE.clear();
        DONE.clear();

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) return;
        if (level.dimension() != net.minecraft.world.level.Level.OVERWORLD) return;

        int radius = mc.options.renderDistance().get() + 1;
        ChunkPos center = mc.player.chunkPosition();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (level.getChunkSource().getChunk(center.x() + dx, center.z() + dz, false) != null) {
                    enqueue(ChunkPos.pack(center.x() + dx, center.z() + dz));
                }
            }
        }

        VoidMod.LOGGER.info("[light] client relight queued: {} chunks (version {})", QUEUE.size(), lightVersion);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ClientLevel clientLevel)) return;
        if (clientLevel.dimension() != net.minecraft.world.level.Level.OVERWORLD) return;
        long packed = event.getChunk().getPos().pack();
        if (DONE.contains(packed)) return;
        enqueue(packed);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (QUEUE.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || level.dimension() != net.minecraft.world.level.Level.OVERWORLD) {
            QUEUE.clear();
            return;
        }

        int budget = SECTION_BUDGET_PER_TICK;
        List<Long> batch = new ArrayList<>(Math.min(SECTION_BUDGET_PER_TICK, QUEUE.size()));

        var it = QUEUE.iterator();
        while (it.hasNext() && batch.size() < SECTION_BUDGET_PER_TICK) {
            batch.add(it.next());
            it.remove();
        }

        for (long packed : batch) {
            if (budget <= 0) {
                enqueue(packed);
                continue;
            }
            LevelChunk chunk = level.getChunkSource().getChunk(ChunkPos.getX(packed), ChunkPos.getZ(packed), false);
            if (chunk == null) continue;

            budget -= LightUpdater.relightChunk(level, chunk);
            DONE.add(packed);
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientProgressionData.worldCursed = false;
        ClientProgressionData.portalAttempted = false;
        ClientProgressionData.wandererTalked = false;
        CurseLightState.setClientCursed(false);
        com.zavidvi.voidmod.client.gui.CurseOverlay.reset();
        QUEUE.clear();
        DONE.clear();
        lightVersion = 0;
    }

    private static void enqueue(long packed) {
        if (QUEUE.size() >= MAX_QUEUE_SIZE) return;
        QUEUE.add(packed);
    }
}

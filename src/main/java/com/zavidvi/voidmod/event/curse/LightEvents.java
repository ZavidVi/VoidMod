package com.zavidvi.voidmod.event.curse;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.registry.ModAttachments;
import com.zavidvi.voidmod.util.CurseLightState;
import com.zavidvi.voidmod.util.LightUpdater;
import com.zavidvi.voidmod.world.progression.WorldProgressionData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;

@EventBusSubscriber(modid = VoidMod.MOD_ID)
public class LightEvents {
    private static final int SECTION_BUDGET_PER_TICK = 48;

    private static final int MAX_QUEUE_SIZE = 8192;

    private static final int CURSE_ACTIVATION_DELAY_TICKS = 80;

    private static final Map<ResourceKey<Level>, LinkedHashSet<Long>> QUEUES = new HashMap<>();

    private static int pendingActivationTicks = -1;

    private static int totalProcessed = 0;
    private static int totalSources = 0;

    private static int currentLightVersion() {
        return CurseLightState.isServerCursed() ? 1 : 0;
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        WorldProgressionData progression = WorldProgressionData.get(overworld);
        CurseLightState.setServerCursed(progression.isWorldCursed());
        CurseLightState.setServerReaperDefeated(progression.isReaperDefeated());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        CurseLightState.setServerCursed(false);
        CurseLightState.setServerReaperDefeated(false);
        pendingActivationTicks = -1;
        QUEUES.clear();
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (serverLevel.dimension() != Level.OVERWORLD) return;

        ChunkAccess chunk = event.getChunk();
        if (chunk.getData(ModAttachments.CURSE_LIGHT_VERSION) == currentLightVersion()) return;

        enqueue(serverLevel, chunk.getPos());
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (serverLevel.dimension() != Level.OVERWORLD) return;

        if (pendingActivationTicks >= 0 && --pendingActivationTicks < 0) {
            activateCurseLight();
        }

        List<Long> batch = poll(serverLevel, SECTION_BUDGET_PER_TICK);
        if (batch.isEmpty()) return;

        int version = currentLightVersion();
        int budget = SECTION_BUDGET_PER_TICK;
        int processed = 0;
        int sources = 0;

        for (int i = 0; i < batch.size(); i++) {
            long packed = batch.get(i);

            if (budget <= 0) {
                enqueue(serverLevel, ChunkPos.unpack(packed));
                continue;
            }

            LevelChunk chunk = serverLevel.getChunkSource()
                    .getChunk(ChunkPos.getX(packed), ChunkPos.getZ(packed), false);
            if (chunk == null) continue;
            if (chunk.getData(ModAttachments.CURSE_LIGHT_VERSION) == version) continue;

            budget -= LightUpdater.relightChunk(serverLevel, chunk);
            processed++;
            sources += LightUpdater.lastTouchedSources;

            chunk.setData(ModAttachments.CURSE_LIGHT_VERSION, version);
            chunk.markUnsaved();
        }

        if (processed > 0) {
            totalProcessed += processed;
            totalSources += sources;
            int left = queuedCount();
            if (left == 0) {
                VoidMod.LOGGER.info("[light] server relight done: {} chunks, {} sources", totalProcessed, totalSources);
                totalProcessed = 0;
                totalSources = 0;
            }
        }
    }

    public static void onCurseChanged(boolean cursed) {
        if (cursed) {
            pendingActivationTicks = CURSE_ACTIVATION_DELAY_TICKS;
            VoidMod.LOGGER.info("Curse light activation scheduled in {} ticks", CURSE_ACTIVATION_DELAY_TICKS);
            return;
        }

        pendingActivationTicks = -1;
        applyLightVersion(false);
    }

    private static void activateCurseLight() {
        applyLightVersion(true);
    }

    private static void applyLightVersion(boolean cursed) {
        CurseLightState.setServerCursed(cursed);

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        com.zavidvi.voidmod.stat.StatManager.refreshAll(server);

        if (cursed) {
            com.zavidvi.voidmod.event.wanderer.WandererSpawner.onWorldCursed(server.overworld());

            com.zavidvi.voidmod.event.worldgen.CursedRetroGeneration.onCurseActivated(server);
        }

        int radius = server.getPlayerList().getViewDistance() + 1;

        ServerLevel overworld = server.overworld();
        for (ServerPlayer player : overworld.players()) {
            ChunkPos center = player.chunkPosition();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    net.minecraft.world.level.chunk.LevelChunk chunk =
                            overworld.getChunkSource().getChunk(center.x() + dx, center.z() + dz, false);
                    if (chunk != null) {
                        enqueue(overworld, chunk.getPos());
                    }
                }
            }
        }

        VoidMod.LOGGER.info("Curse light version -> {} ; chunks queued for relight: {}",
                currentLightVersion(), queuedCount());
    }

    private static void enqueue(ServerLevel level, ChunkPos pos) {
        synchronized (QUEUES) {
            LinkedHashSet<Long> queue = QUEUES.computeIfAbsent(level.dimension(), k -> new LinkedHashSet<>());
            if (queue.size() >= MAX_QUEUE_SIZE) return;
            queue.add(pos.pack());
        }
    }

    private static List<Long> poll(ServerLevel level, int limit) {
        synchronized (QUEUES) {
            LinkedHashSet<Long> queue = QUEUES.get(level.dimension());
            if (queue == null || queue.isEmpty()) return List.of();

            List<Long> batch = new ArrayList<>(Math.min(limit, queue.size()));
            var it = queue.iterator();
            while (it.hasNext() && batch.size() < limit) {
                batch.add(it.next());
                it.remove();
            }
            if (queue.isEmpty()) QUEUES.remove(level.dimension());
            return batch;
        }
    }

    public static int forceRequeueAll() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return 0;

        int radius = server.getPlayerList().getViewDistance() + 1;
        for (ServerLevel level : server.getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                ChunkPos center = player.chunkPosition();
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        net.minecraft.world.level.chunk.LevelChunk chunk =
                                level.getChunkSource().getChunk(center.x() + dx, center.z() + dz, false);
                        if (chunk != null) {
                            chunk.setData(ModAttachments.CURSE_LIGHT_VERSION, -1);
                            enqueue(level, chunk.getPos());
                        }
                    }
                }
            }
        }
        return queuedCount();
    }

    public static int queuedCount() {
        synchronized (QUEUES) {
            int total = 0;
            for (LinkedHashSet<Long> queue : QUEUES.values()) {
                total += queue.size();
            }
            return total;
        }
    }
}

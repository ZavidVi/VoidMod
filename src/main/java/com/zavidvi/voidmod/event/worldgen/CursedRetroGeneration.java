package com.zavidvi.voidmod.event.worldgen;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.registry.ModAttachments;
import com.zavidvi.voidmod.registry.ModBlocks;
import com.zavidvi.voidmod.util.CurseLightState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = VoidMod.MOD_ID)
public class CursedRetroGeneration {
    private static final int CONTENT_VERSION = 1;

    private static final int CHUNK_BUDGET_PER_TICK = 2;

    private static final int MAX_QUEUE_SIZE = 8192;

    private static final List<ResourceKey<PlacedFeature>> ORE_FEATURES = List.of(
            placedFeature("pale_ore_upper"),
            placedFeature("pale_ore_middle"),
            placedFeature("pale_ore_small"));

    private static final List<ResourceKey<StructureSet>> STRUCTURE_SETS = List.of(
            structureSet("grave"),
            structureSet("lighted_fountain"),
            structureSet("supervoid"));

    private static final Map<ResourceKey<Level>, LinkedHashSet<Long>> QUEUES = new HashMap<>();

    private static int totalProcessed = 0;

    private CursedRetroGeneration() {}

    private static ResourceKey<PlacedFeature> placedFeature(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE,
                Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, path));
    }

    private static ResourceKey<StructureSet> structureSet(String path) {
        return ResourceKey.create(Registries.STRUCTURE_SET,
                Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, path));
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        QUEUES.clear();
        totalProcessed = 0;
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.dimension() != Level.OVERWORLD) return;

        LevelChunk chunk = event.getChunk();

        if (event.isNewChunk()) {
            if (CurseLightState.isServerCursed()) {
                chunk.setData(ModAttachments.CURSED_CONTENT_VERSION, CONTENT_VERSION);
                chunk.markUnsaved();
            }
            return;
        }

        if (!CurseLightState.isServerCursed()) return;
        if (chunk.getData(ModAttachments.CURSED_CONTENT_VERSION) >= CONTENT_VERSION) return;

        enqueue(level, chunk.getPos());
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.dimension() != Level.OVERWORLD) return;
        if (!CurseLightState.isServerCursed()) return;

        List<Long> batch = poll(level, CHUNK_BUDGET_PER_TICK);
        if (batch.isEmpty()) return;

        for (long packed : batch) {
            int x = ChunkPos.getX(packed);
            int z = ChunkPos.getZ(packed);

            LevelChunk chunk = level.getChunkSource().getChunk(x, z, false);
            if (chunk == null) continue;
            if (chunk.getData(ModAttachments.CURSED_CONTENT_VERSION) >= CONTENT_VERSION) continue;

            if (!neighboursLoaded(level, x, z)) {
                enqueue(level, new ChunkPos(x, z));
                continue;
            }

            retrogen(level, chunk);

            chunk.setData(ModAttachments.CURSED_CONTENT_VERSION, CONTENT_VERSION);
            chunk.markUnsaved();
            totalProcessed++;
        }

        if (queuedCount() == 0 && totalProcessed > 0) {
            VoidMod.LOGGER.info("[retrogen] проклятый контент досыпан в {} чанков", totalProcessed);
            totalProcessed = 0;
        }
    }

    public static void onCurseActivated(MinecraftServer server) {
        if (server == null) return;

        ServerLevel overworld = server.overworld();
        int radius = server.getPlayerList().getViewDistance() + 1;

        for (ServerPlayer player : overworld.players()) {
            ChunkPos center = player.chunkPosition();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    LevelChunk chunk = overworld.getChunkSource()
                            .getChunk(center.x() + dx, center.z() + dz, false);
                    if (chunk != null
                            && chunk.getData(ModAttachments.CURSED_CONTENT_VERSION) < CONTENT_VERSION) {
                        enqueue(overworld, chunk.getPos());
                    }
                }
            }
        }

        VoidMod.LOGGER.info("[retrogen] чанков в очереди на досыпку: {}", queuedCount());
    }

    private static void retrogen(ServerLevel level, LevelChunk chunk) {
        generateOres(level, chunk);
        generateStructures(level, chunk);
    }

    private static void generateOres(ServerLevel level, LevelChunk chunk) {
        if (alreadyHasOre(chunk)) return;

        RegistryAccess registries = level.registryAccess();
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        ChunkPos pos = chunk.getPos();
        BlockPos origin = new BlockPos(pos.getMinBlockX(), level.getMinY(), pos.getMinBlockZ());

        WorldgenRandom random = new WorldgenRandom(new XoroshiroRandomSource(level.getSeed()));
        long decorationSeed = random.setDecorationSeed(level.getSeed(), origin.getX(), origin.getZ());

        int index = 0;
        for (ResourceKey<PlacedFeature> key : ORE_FEATURES) {
            Holder.Reference<PlacedFeature> feature =
                    registries.lookupOrThrow(Registries.PLACED_FEATURE).get(key).orElse(null);
            if (feature == null) continue;

            random.setFeatureSeed(decorationSeed, index++,
                    GenerationStep.Decoration.UNDERGROUND_ORES.ordinal());
            feature.value().placeWithBiomeCheck(level, generator, random, origin);
        }
    }

    private static boolean alreadyHasOre(LevelChunk chunk) {
        for (LevelChunkSection section : chunk.getSections()) {
            if (section.hasOnlyAir()) continue;
            if (section.maybeHas(state -> state.is(ModBlocks.PALE_ORE.get())
                    || state.is(ModBlocks.DEEPSLATE_PALE_ORE.get()))) {
                return true;
            }
        }
        return false;
    }

    private static void generateStructures(ServerLevel level, LevelChunk chunk) {
        ServerChunkCache chunkSource = level.getChunkSource();
        ChunkGeneratorStructureState state = chunkSource.getGeneratorState();
        ChunkGenerator generator = chunkSource.getGenerator();
        StructureManager structureManager = level.structureManager();
        StructureTemplateManager templates = level.getServer().getStructureManager();
        RegistryAccess registries = level.registryAccess();

        ChunkPos pos = chunk.getPos();
        SectionPos sectionPos = SectionPos.bottomOf(chunk);
        long seed = state.getLevelSeed();

        for (Holder<StructureSet> holder : state.possibleStructureSets()) {
            if (STRUCTURE_SETS.stream().noneMatch(holder::is)) continue;

            StructureSet set = holder.value();
            if (!set.placement().isStructureChunk(state, pos.x(), pos.z())) continue;

            for (StructureSet.StructureSelectionEntry entry : set.structures()) {
                Structure structure = entry.structure().value();

                StructureStart existing = structureManager.getStartForStructure(sectionPos, structure, chunk);
                if (existing != null && existing.isValid()) continue;

                StructureStart start = structure.generate(
                        entry.structure(), level.dimension(), registries, generator,
                        generator.getBiomeSource(), state.randomState(), templates, seed,
                        pos, 0, chunk, structure.biomes()::contains);
                if (!start.isValid()) continue;

                BoundingBox box = start.getBoundingBox();
                if (!areaLoaded(level, box)) continue;

                structureManager.setStartForStructure(sectionPos, structure, start, chunk);
                addReferences(level, structureManager, structure, box, pos);

                WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
                random.setLargeFeatureSeed(seed, pos.x(), pos.z());
                start.placeInChunk(level, structureManager, generator, random, box, pos);

                VoidMod.LOGGER.info("[retrogen] {} досыпана в чанк {}", holder.getKey(), pos);
            }
        }
    }

    private static void addReferences(ServerLevel level, StructureManager structureManager,
                                      Structure structure, BoundingBox box, ChunkPos origin) {
        long reference = origin.pack();

        for (int x = SectionPos.blockToSectionCoord(box.minX()); x <= SectionPos.blockToSectionCoord(box.maxX()); x++) {
            for (int z = SectionPos.blockToSectionCoord(box.minZ()); z <= SectionPos.blockToSectionCoord(box.maxZ()); z++) {
                LevelChunk touched = level.getChunkSource().getChunk(x, z, false);
                if (touched == null) continue;

                structureManager.addReferenceForStructure(
                        SectionPos.bottomOf(touched), structure, reference, touched);
                touched.markUnsaved();
            }
        }
    }

    private static boolean areaLoaded(ServerLevel level, BoundingBox box) {
        for (int x = SectionPos.blockToSectionCoord(box.minX()); x <= SectionPos.blockToSectionCoord(box.maxX()); x++) {
            for (int z = SectionPos.blockToSectionCoord(box.minZ()); z <= SectionPos.blockToSectionCoord(box.maxZ()); z++) {
                if (level.getChunkSource().getChunk(x, z, false) == null) return false;
            }
        }
        return true;
    }

    private static boolean neighboursLoaded(ServerLevel level, int x, int z) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (level.getChunkSource().getChunk(x + dx, z + dz, false) == null) return false;
            }
        }
        return true;
    }

    private static void enqueue(ServerLevel level, ChunkPos pos) {
        synchronized (QUEUES) {
            LinkedHashSet<Long> queue = QUEUES.computeIfAbsent(level.dimension(), key -> new LinkedHashSet<>());
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

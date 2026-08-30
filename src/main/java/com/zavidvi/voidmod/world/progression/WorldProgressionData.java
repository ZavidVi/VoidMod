package com.zavidvi.voidmod.world.progression;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zavidvi.voidmod.VoidMod;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.List;
import java.util.Optional;

public class WorldProgressionData extends SavedData {
    private boolean portalAttempted = false;
    private boolean worldCursed = false;
    private boolean wandererTalked = false;
    
    private long nextWandererSpawnTime = -1L;

    private boolean spawnFountainPlaced = false;

    private boolean reaperDefeated = false;
    private long lastDistorterGiveTime = -1L;
    private net.minecraft.core.BlockPos forgePosition = null;
    private final java.util.List<net.minecraft.core.BlockPos> forgePositions = new java.util.ArrayList<>();

    private final java.util.Set<net.minecraft.core.BlockPos> supervoidPopulated = new java.util.HashSet<>();

    private final java.util.Map<net.minecraft.core.BlockPos, Integer> supervoidSwarmSize = new java.util.HashMap<>();
    private final java.util.Map<net.minecraft.core.BlockPos, Integer> supervoidKills = new java.util.HashMap<>();

    private long nextVraujSpawnTime = 0L;

    private final java.util.Set<net.minecraft.core.BlockPos> villageLakes = new java.util.HashSet<>();

    public static final Codec<WorldProgressionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("portalAttempted", false).forGetter(data -> data.portalAttempted),
            Codec.BOOL.optionalFieldOf("worldCursed", false).forGetter(data -> data.worldCursed),
            Codec.BOOL.optionalFieldOf("wandererTalked").forGetter(data -> Optional.of(data.wandererTalked)),
            Codec.BOOL.optionalFieldOf("strangerTalked").forGetter(data -> Optional.<Boolean>empty()),
            Codec.INT.optionalFieldOf("fireEssenceCount", 0).forGetter(data -> 0),
            Codec.LONG.optionalFieldOf("nextWandererSpawnTime").forGetter(data -> Optional.of(data.nextWandererSpawnTime)),
            Codec.LONG.optionalFieldOf("nextStrangerSpawnTime").forGetter(data -> Optional.<Long>empty()),
            Codec.LONG.optionalFieldOf("lastDistorterGiveTime", -1L).forGetter(data -> data.lastDistorterGiveTime),
            BlockPos.CODEC.optionalFieldOf("forgePosition").forGetter(data -> Optional.ofNullable(data.forgePosition)),
            BlockPos.CODEC.listOf().optionalFieldOf("forgePositions", List.of()).forGetter(data -> data.forgePositions),
            SupervoidEntry.CODEC.listOf().optionalFieldOf("supervoid", List.of()).forGetter(WorldProgressionData::supervoidEntries),
            Codec.LONG.optionalFieldOf("nextVraujSpawnTime", 0L).forGetter(data -> data.nextVraujSpawnTime),
            Codec.BOOL.optionalFieldOf("spawnFountainPlaced", false).forGetter(data -> data.spawnFountainPlaced),
            Codec.BOOL.optionalFieldOf("reaperDefeated", false).forGetter(data -> data.reaperDefeated),
            BlockPos.CODEC.listOf().optionalFieldOf("villageLakes", List.of())
                    .forGetter(data -> List.copyOf(data.villageLakes))
    ).apply(instance, WorldProgressionData::new));

    private static final SavedDataType<WorldProgressionData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "voidmod_progression"),
            WorldProgressionData::new,
            CODEC);

    private record SupervoidEntry(BlockPos center, int swarmSize, int kills) {
        static final Codec<SupervoidEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("center").forGetter(SupervoidEntry::center),
                Codec.INT.optionalFieldOf("swarmSize", 0).forGetter(SupervoidEntry::swarmSize),
                Codec.INT.optionalFieldOf("kills", 0).forGetter(SupervoidEntry::kills)
        ).apply(instance, SupervoidEntry::new));
    }

    public WorldProgressionData() {}

    private WorldProgressionData(boolean portalAttempted,
                                 boolean worldCursed,
                                 Optional<Boolean> wandererTalked,
                                 Optional<Boolean> legacyStrangerTalked,
                                 int legacyFireEssenceCount,
                                 Optional<Long> nextWandererSpawnTime,
                                 Optional<Long> legacyNextStrangerSpawnTime,
                                 long lastDistorterGiveTime,
                                 Optional<BlockPos> forgePosition,
                                 List<BlockPos> forgePositions,
                                 List<SupervoidEntry> supervoid,
                                 long nextVraujSpawnTime,
                                 boolean spawnFountainPlaced,
                                 boolean reaperDefeated,
                                 List<BlockPos> villageLakes) {
        this.portalAttempted = portalAttempted;
        this.worldCursed = worldCursed;
        this.wandererTalked = wandererTalked.or(() -> legacyStrangerTalked).orElse(false);
        this.nextWandererSpawnTime = nextWandererSpawnTime.or(() -> legacyNextStrangerSpawnTime).orElse(-1L);
        this.lastDistorterGiveTime = lastDistorterGiveTime;
        this.forgePosition = forgePosition.orElse(null);
        this.forgePositions.addAll(forgePositions);
        this.nextVraujSpawnTime = nextVraujSpawnTime;
        this.spawnFountainPlaced = spawnFountainPlaced;
        this.reaperDefeated = reaperDefeated;
        this.villageLakes.addAll(villageLakes);

        for (SupervoidEntry entry : supervoid) {
            this.supervoidPopulated.add(entry.center());
            this.supervoidSwarmSize.put(entry.center(), entry.swarmSize());
            this.supervoidKills.put(entry.center(), entry.kills());
        }

        if (this.forgePosition != null && !this.forgePositions.contains(this.forgePosition)) {
            this.forgePositions.add(this.forgePosition);
        }
    }

    private List<SupervoidEntry> supervoidEntries() {
        return this.supervoidPopulated.stream()
                .map(center -> new SupervoidEntry(
                        center,
                        this.supervoidSwarmSize.getOrDefault(center, 0),
                        this.supervoidKills.getOrDefault(center, 0)))
                .toList();
    }

    public static WorldProgressionData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean isPortalAttempted() {
        return portalAttempted;
    }

    public void setPortalAttempted(boolean attempted) {
        this.portalAttempted = attempted;
        setDirty();
    }

    public boolean isWorldCursed() {
        return worldCursed;
    }

    public void setWorldCursed(boolean cursed) {
        boolean changed = this.worldCursed != cursed;
        this.worldCursed = cursed;
        setDirty();
        if (changed) {
            com.zavidvi.voidmod.event.curse.LightEvents.onCurseChanged(cursed);
        }
    }

    public boolean isSpawnFountainPlaced() {
        return this.spawnFountainPlaced;
    }

    public void setSpawnFountainPlaced(boolean placed) {
        this.spawnFountainPlaced = placed;
        setDirty();
    }

    public boolean isReaperDefeated() {
        return reaperDefeated;
    }

    public void setReaperDefeated(boolean defeated) {
        this.reaperDefeated = defeated;
        setDirty();
        com.zavidvi.voidmod.util.CurseLightState.setServerReaperDefeated(defeated);
    }

    public boolean isWandererTalked() {
        return wandererTalked;
    }

    public void setWandererTalked(boolean talked) {
        this.wandererTalked = talked;
        setDirty();
    }

    public long getNextWandererSpawnTime() {
        return nextWandererSpawnTime;
    }

    public void setNextWandererSpawnTime(long time) {
        this.nextWandererSpawnTime = time;
        setDirty();
    }

    public long getLastDistorterGiveTime() {
        return lastDistorterGiveTime;
    }

    public void setLastDistorterGiveTime(long time) {
        this.lastDistorterGiveTime = time;
        setDirty();
    }

    public net.minecraft.core.BlockPos getForgePosition() {
        return forgePosition;
    }

    public void setForgePosition(net.minecraft.core.BlockPos pos) {
        this.forgePosition = pos;
        setDirty();
    }

    public boolean isVillageLakePlaced(net.minecraft.core.BlockPos villageStart) {
        return this.villageLakes.contains(villageStart);
    }

    public void markVillageLakePlaced(net.minecraft.core.BlockPos villageStart) {
        if (this.villageLakes.add(villageStart.immutable())) {
            setDirty();
        }
    }

    public long getNextVraujSpawnTime() {
        return nextVraujSpawnTime;
    }

    public void setNextVraujSpawnTime(long time) {
        this.nextVraujSpawnTime = time;
        setDirty();
    }

    public boolean isSupervoidPopulated(net.minecraft.core.BlockPos center) {
        return supervoidPopulated.contains(center);
    }

    public void markSupervoidPopulated(net.minecraft.core.BlockPos center, int swarmSize) {
        if (center == null) return;

        net.minecraft.core.BlockPos key = center.immutable();
        supervoidPopulated.add(key);
        supervoidSwarmSize.put(key, swarmSize);
        supervoidKills.putIfAbsent(key, 0);
        setDirty();
    }

    public int getSupervoidSwarmSize(net.minecraft.core.BlockPos center) {
        return center == null ? 0 : supervoidSwarmSize.getOrDefault(center, 0);
    }

    public int getSupervoidKills(net.minecraft.core.BlockPos center) {
        return center == null ? 0 : supervoidKills.getOrDefault(center, 0);
    }

    public int addSupervoidKill(net.minecraft.core.BlockPos center) {
        if (center == null) return 0;

        int killed = supervoidKills.merge(center.immutable(), 1, Integer::sum);
        setDirty();
        return killed;
    }

    public java.util.List<net.minecraft.core.BlockPos> getForgePositions() {
        return java.util.Collections.unmodifiableList(forgePositions);
    }

    public void addForgePosition(net.minecraft.core.BlockPos pos) {
        if (pos == null || forgePositions.contains(pos)) return;
        forgePositions.add(pos.immutable());
        setDirty();
    }

    public void removeForgePosition(net.minecraft.core.BlockPos pos) {
        if (pos == null || !forgePositions.remove(pos)) return;
        setDirty();
    }

    public static void broadcastCurseMessage(ServerLevel serverLevel) {
        net.neoforged.neoforge.network.PacketDistributor.sendToAllPlayers(
                new com.zavidvi.voidmod.network.ShowCurseMessagePayload());
    }
}

package com.zavidvi.voidmod.world.supervoid;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.entity.supervoid.SupervoidShardEntity;
import com.zavidvi.voidmod.entity.vrauj.VraujEntity;
import com.zavidvi.voidmod.registry.ModEntities;
import com.zavidvi.voidmod.world.StructureProximity;
import com.zavidvi.voidmod.world.progression.WorldProgressionData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class SupervoidEncounters {
    public static final ResourceKey<Structure> SUPERVOID = ResourceKey.create(
            Registries.STRUCTURE,
            Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "supervoid"));

    public static final double SWARM_RADIUS = 64.0;

    public static final int AGGRO_CHUNK_RADIUS = 1;

    private static final int CLEAR_BONUS_ESSENCE = 5;

    private static final int SWARM_MIN = 3;
    private static final int SWARM_MAX = 5;

    private static final double SWARM_SPAWN_RADIUS = 8.0;

    private static final int SHARD_CLEARANCE = (int) SupervoidShardEntity.SIZE;

    private static final int SHARD_EXIT_STEPS = 32;

    private SupervoidEncounters() {}

    public static List<VraujEntity> swarmAround(ServerLevel level, BlockPos center) {
        return level.getEntitiesOfClass(VraujEntity.class,
                new AABB(center).inflate(SWARM_RADIUS),
                v -> v.isAlive() && v.isSupervoidGuard() && center.equals(v.getOrbitCenter()));
    }

    public static boolean isCleared(ServerLevel level, BlockPos center) {
        WorldProgressionData data = WorldProgressionData.get(level);
        int total = data.getSupervoidSwarmSize(center);
        return total > 0 && data.getSupervoidKills(center) >= total;
    }

    public static BlockPos structureCenterAt(ServerLevel level, BlockPos pos) {
        Structure structure = level.registryAccess()
                .lookupOrThrow(Registries.STRUCTURE)
                .get(SUPERVOID).orElseThrow().value();
        if (structure == null) return null;

        StructureStart start = level.structureManager().getStructureWithPieceAt(pos, structure);
        if (!start.isValid()) return null;
        return start.getBoundingBox().getCenter();
    }

    public static BlockPos structureCenterNear(ServerLevel level, BlockPos pos) {
        Structure structure = level.registryAccess()
                .lookupOrThrow(Registries.STRUCTURE)
                .get(SUPERVOID).orElseThrow().value();
        if (structure == null) return null;

        return StructureProximity.centerNear(level, pos, AGGRO_CHUNK_RADIUS, s -> s == structure);
    }

    public static void ensurePopulated(ServerLevel level, BlockPos center) {
        WorldProgressionData data = WorldProgressionData.get(level);
        if (data.isSupervoidPopulated(center)) return;

        int size = SWARM_MIN + level.getRandom().nextInt(SWARM_MAX - SWARM_MIN + 1);
        for (int i = 0; i < size; i++) {
            VraujEntity vrauj = ModEntities.VRAUJ.get().create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
            if (vrauj == null) continue;

            double angle = (Math.PI * 2.0 / size) * i;
            vrauj.snapTo(
                    center.getX() + 0.5 + Math.cos(angle) * SWARM_SPAWN_RADIUS,
                    center.getY() + 3.0,
                    center.getZ() + 0.5 + Math.sin(angle) * SWARM_SPAWN_RADIUS,
                    (float) Math.toDegrees(angle), 0.0F);

            vrauj.setOrbitCenter(center.immutable());
            vrauj.setSupervoidGuard(true);
            vrauj.setSwarmSize(size);
            vrauj.setPersistenceRequired();
            level.addFreshEntity(vrauj);
        }

        data.markSupervoidPopulated(center, size);
    }

    public static boolean aggro(ServerLevel level, BlockPos center, LivingEntity target) {
        List<VraujEntity> swarm = swarmAround(level, center);
        if (swarm.isEmpty()) return false;

        boolean wasIdle = true;
        for (VraujEntity vrauj : swarm) {
            if (vrauj.getTarget() != null) {
                wasIdle = false;
            }
        }

        boolean raised = false;
        for (VraujEntity vrauj : swarm) {
            vrauj.setTarget(target);
            if (vrauj.getTarget() != null) {
                raised = true;
            }
        }
        if (!raised) return false;

        if (target instanceof ServerPlayer aggroPlayer) {
            com.zavidvi.voidmod.advancement.ModAdvancements.grant(aggroPlayer,
                    com.zavidvi.voidmod.advancement.ModAdvancements.LEGACY_OF_THE_VOID);
        }

        if (wasIdle && target instanceof ServerPlayer player) {
            WorldProgressionData data = WorldProgressionData.get(level);
            int total = data.getSupervoidSwarmSize(center);
            if (total <= 0) {
                total = Math.max(swarm.size(), swarm.get(0).getSwarmSize());
            }
            announce(player, data.getSupervoidKills(center), total);
        }
        return wasIdle;
    }

    public static void onVraujKilled(ServerLevel level, VraujEntity killed) {
        BlockPos center = killed.getOrbitCenter();
        if (center == null || !killed.isSupervoidGuard()) return;

        WorldProgressionData data = WorldProgressionData.get(level);

        int total = data.getSupervoidSwarmSize(center);
        if (total <= 0) {
            total = Math.max(1, killed.getSwarmSize());
            data.markSupervoidPopulated(center, total);
        }

        int rawKills = data.addSupervoidKill(center);

        LivingEntity killer = killed.getKillCredit();
        if (killer instanceof ServerPlayer player) {
            announce(player, Math.min(rawKills, total), total);
            launchShard(level, center, player);
        }

        if (rawKills == total) {
            dropClearBonus(level, center);
        }
    }

    private static void launchShard(ServerLevel level, BlockPos center, ServerPlayer target) {
        Vec3 aim = new Vec3(target.getX(), target.getY(0.5), target.getZ());
        Vec3 origin = shardOrigin(level, center, aim);

        SupervoidShardEntity shard = new SupervoidShardEntity(level, origin.x(), origin.y, origin.z());
        shard.aimAt(aim);
        level.addFreshEntity(shard);
    }

    private static Vec3 shardOrigin(ServerLevel level, BlockPos center, Vec3 aim) {
        Vec3 from = Vec3.atCenterOf(center);

        Vec3 step = aim.subtract(from);
        step = step.lengthSqr() < 1.0E-4 ? new Vec3(0.0, -1.0, 0.0) : step.normalize();

        BoundingBox box = structureBoxAt(level, center);
        if (box == null) return from;

        BoundingBox keepOut = box.inflatedBy(SHARD_CLEARANCE);
        Vec3 cursor = from;

        for (int i = 0; i < SHARD_EXIT_STEPS; i++) {
            cursor = cursor.add(step);
            BlockPos pos = BlockPos.containing(cursor);

            if (keepOut.isInside(pos)) continue;
            if (!level.getBlockState(pos).isAir()) continue;
            return cursor;
        }
        return from;
    }

    private static BoundingBox structureBoxAt(ServerLevel level, BlockPos center) {
        Structure structure = level.registryAccess()
                .lookupOrThrow(Registries.STRUCTURE)
                .get(SUPERVOID).orElseThrow().value();
        if (structure == null) return null;

        StructureStart start = level.structureManager().getStructureAt(center, structure);
        return start.isValid() ? start.getBoundingBox() : null;
    }

    private static void dropClearBonus(ServerLevel level, BlockPos center) {
        net.minecraft.world.entity.item.ItemEntity drop = new net.minecraft.world.entity.item.ItemEntity(
                level, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
                new net.minecraft.world.item.ItemStack(
                        com.zavidvi.voidmod.registry.ModItems.FIRE_ESSENCE.get(), CLEAR_BONUS_ESSENCE));
        level.addFreshEntity(drop);
    }

    private static void announce(ServerPlayer player, int killed, int total) {
        player.sendSystemMessage(Component.literal(killed + "/" + total));
    }
}

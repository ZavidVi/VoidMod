package com.zavidvi.voidmod.event.wanderer;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.entity.wanderer.WandererEntity;
import com.zavidvi.voidmod.registry.ModEntities;
import com.zavidvi.voidmod.world.progression.WorldProgressionData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

@EventBusSubscriber(modid = VoidMod.MOD_ID)
public class WandererSpawner {
    private static final long FORGE_RESPAWN_DELAY_TICKS = 24000L;

    private static final int FORGE_SPAWN_RADIUS = 6;

    private static int tickCounter = 0;

    public static void onWorldCursed(ServerLevel serverLevel) {
        WorldProgressionData data = WorldProgressionData.get(serverLevel);
        if (data.getNextWandererSpawnTime() != -1L || data.isWandererTalked()) return;

        data.setNextWandererSpawnTime(serverLevel.getGameTime());
        VoidMod.LOGGER.info("Wanderer spawn scheduled right after the curse screen");
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel) || serverLevel.dimension() != net.minecraft.world.level.Level.OVERWORLD) {
            return;
        }

        tickCounter++;
        if (tickCounter % 100 != 0) return;

        WorldProgressionData data = WorldProgressionData.get(serverLevel);

        BlockPos forgePos = data.getForgePosition();
        long nextSpawnTime = data.getNextWandererSpawnTime();

        boolean forgeCanSummon = forgePos != null && data.isPortalAttempted();
        if (nextSpawnTime == -1L && !forgeCanSummon) return;

        boolean exists = !serverLevel.getEntities(ModEntities.WANDERER.get(), e -> true).isEmpty();
        if (exists) return;

        if (nextSpawnTime == -1L) {
            if (serverLevel.isLoaded(forgePos)) {
                data.setNextWandererSpawnTime(serverLevel.getGameTime() + FORGE_RESPAWN_DELAY_TICKS);
                VoidMod.LOGGER.info("Wanderer return to forge {} scheduled in {} ticks", forgePos, FORGE_RESPAWN_DELAY_TICKS);
            }
            return;
        }

        if (serverLevel.getGameTime() >= nextSpawnTime) {
            spawnWanderer(serverLevel, data, forgePos);
        }
    }

    private static void spawnWanderer(ServerLevel serverLevel, WorldProgressionData data, BlockPos forgePos) {
        if (forgePos != null) {
            spawnAtForge(serverLevel, data, forgePos);
            return;
        }
        spawnNearPlayer(serverLevel, data);
    }

    private static void spawnAtForge(ServerLevel serverLevel, WorldProgressionData data, BlockPos forgePos) {
        if (!serverLevel.isLoaded(forgePos)) return;

        BlockPos spawnPos = findSpawnNearForge(serverLevel, forgePos);
        if (spawnPos == null) {
            spawnPos = forgePos.above();
        }

        spawn(serverLevel, data, spawnPos);
        VoidMod.LOGGER.info("Wanderer spawned at " + spawnPos + " near her forge " + forgePos);
    }

    private static BlockPos findSpawnNearForge(ServerLevel serverLevel, BlockPos forgePos) {
        for (int i = 0; i < 32; i++) {
            int dx = serverLevel.getRandom().nextInt(FORGE_SPAWN_RADIUS * 2 + 1) - FORGE_SPAWN_RADIUS;
            int dz = serverLevel.getRandom().nextInt(FORGE_SPAWN_RADIUS * 2 + 1) - FORGE_SPAWN_RADIUS;
            if (dx == 0 && dz == 0) continue;

            for (int dy = 2; dy >= -2; dy--) {
                BlockPos candidate = forgePos.offset(dx, dy, dz);
                if (!serverLevel.isLoaded(candidate)) continue;
                if (canStandAt(serverLevel, candidate)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static boolean canStandAt(ServerLevel serverLevel, BlockPos pos) {
        BlockPos below = pos.below();
        return serverLevel.isEmptyBlock(pos)
                && serverLevel.isEmptyBlock(pos.above())
                && serverLevel.getBlockState(below).isFaceSturdy(serverLevel, below, Direction.UP);
    }

    private static void spawnNearPlayer(ServerLevel serverLevel, WorldProgressionData data) {
        List<ServerPlayer> players = serverLevel.players();
        if (players.isEmpty()) return;

        Player targetPlayer = players.get(serverLevel.getRandom().nextInt(players.size()));

        for (int i = 0; i < 10; i++) {
            int dx = serverLevel.getRandom().nextInt(17) - 8;
            int dz = serverLevel.getRandom().nextInt(17) - 8;

            int x = targetPlayer.getBlockX() + dx;
            int z = targetPlayer.getBlockZ() + dz;

            int y = serverLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

            if (y > 50 && Math.abs(y - targetPlayer.getBlockY()) < 10) {
                BlockPos spawnPos = new BlockPos(x, y, z);
                spawn(serverLevel, data, spawnPos);
                VoidMod.LOGGER.info("Wanderer spawned at " + spawnPos + " near " + targetPlayer.getName().getString());
                return;
            }
        }
    }

    private static void spawn(ServerLevel serverLevel, WorldProgressionData data, BlockPos spawnPos) {
        WandererEntity wanderer = new WandererEntity(ModEntities.WANDERER.get(), serverLevel);
        wanderer.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        serverLevel.addFreshEntity(wanderer);

        data.setNextWandererSpawnTime(-1L);
    }
}

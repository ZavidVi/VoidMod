package com.zavidvi.voidmod.entity.reaper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

public final class ReaperArena {
    private ReaperArena() {}

    public static final int CHUNK_RADIUS = 1;

    private static final double WALL_INSET = 0.5D;

    private static final double BOUNCE_SPEED = 0.15D;

    private static final int PARTICLE_INTERVAL = 5;

    private static final int WALL_PARTICLES = 6;

    public static boolean isInside(BlockPos gravePos, Entity entity) {
        ChunkPos chunk = ChunkPos.containing(gravePos);
        return entity.getX() >= minX(chunk) && entity.getX() <= maxX(chunk)
                && entity.getZ() >= minZ(chunk) && entity.getZ() <= maxZ(chunk);
    }

    public static void pushInside(ServerLevel level, ServerPlayer player, BlockPos gravePos) {
        ChunkPos chunk = ChunkPos.containing(gravePos);

        double wantedX = Mth.clamp(player.getX(), minX(chunk) + WALL_INSET, maxX(chunk) - WALL_INSET);
        double wantedZ = Mth.clamp(player.getZ(), minZ(chunk) + WALL_INSET, maxZ(chunk) - WALL_INSET);

        double inwardX = wantedX - player.getX();
        double inwardZ = wantedZ - player.getZ();
        if (inwardX == 0.0D && inwardZ == 0.0D) return;

        showWall(level, player);

        if (player.isPassenger()) {
            player.stopRiding();
        }
        player.teleportTo(wantedX, player.getY(), wantedZ);

        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(
                inwardX == 0.0D ? motion.x : Math.signum(inwardX) * BOUNCE_SPEED,
                motion.y,
                inwardZ == 0.0D ? motion.z : Math.signum(inwardZ) * BOUNCE_SPEED);
        player.hurtMarked = true;
    }

    private static void showWall(ServerLevel level, ServerPlayer player) {
        if (player.tickCount % PARTICLE_INTERVAL != 0) return;

        level.sendParticles(ParticleTypes.PORTAL,
                player.getX(), player.getY() + player.getBbHeight() / 2.0D, player.getZ(),
                WALL_PARTICLES, 0.2D, 0.7D, 0.2D, 0.3D);
    }

    public static double minX(ChunkPos chunk) {
        return (chunk.x() - CHUNK_RADIUS) * 16.0D;
    }

    public static double maxX(ChunkPos chunk) {
        return (chunk.x() + CHUNK_RADIUS + 1) * 16.0D;
    }

    public static double minZ(ChunkPos chunk) {
        return (chunk.z() - CHUNK_RADIUS) * 16.0D;
    }

    public static double maxZ(ChunkPos chunk) {
        return (chunk.z() + CHUNK_RADIUS + 1) * 16.0D;
    }
}

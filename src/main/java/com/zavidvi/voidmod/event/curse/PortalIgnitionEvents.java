package com.zavidvi.voidmod.event.curse;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.entity.supervoid.TentacleEntity;
import com.zavidvi.voidmod.network.SyncProgressionPayload;
import com.zavidvi.voidmod.registry.ModEntities;
import com.zavidvi.voidmod.world.progression.WorldProgressionData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = VoidMod.MOD_ID)
public class PortalIgnitionEvents {
    private static final float TENTACLE_YAW_OFFSET = -90.0F;

    @SubscribeEvent
    public static void onPlayerDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) player.level();
            
            java.util.List<com.zavidvi.voidmod.entity.voidsphere.VoidSphereEntity> nearbySpheres = serverLevel.getEntitiesOfClass(
                    com.zavidvi.voidmod.entity.voidsphere.VoidSphereEntity.class,
                    player.getBoundingBox().inflate(64.0),
                    sphere -> sphere.isPortalSphere()
            );
            
            if (!nearbySpheres.isEmpty()) {
                WorldProgressionData progression = WorldProgressionData.get(serverLevel);
                if (!progression.isWorldCursed()) {
                    progression.setWorldCursed(true);
                    WorldProgressionData.broadcastCurseMessage(serverLevel);
                    
                    PacketDistributor.sendToAllPlayers(SyncProgressionPayload.of(progression));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPortalSpawn(BlockEvent.PortalSpawnEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            WorldProgressionData data = WorldProgressionData.get(serverLevel);

            if (!data.isPortalAttempted()) {
                data.setPortalAttempted(true);

                PacketDistributor.sendToAllPlayers(SyncProgressionPayload.of(data));

                BlockPos portalPos = event.getPos();
                PortalFrameInfo frameInfo = findPortalFrame(serverLevel, portalPos);

                awardVoidMode(serverLevel, portalPos);

                TentacleEntity tentacle = new TentacleEntity(ModEntities.TENTACLE.get(), serverLevel);
                tentacle.setFrameBlocks(frameInfo.innerBlocks());

                double spawnX = frameInfo.centerX;
                double spawnY = portalPos.getY() - 0.8;
                double spawnZ = frameInfo.centerZ;

                Player nearestPlayer = serverLevel.getNearestPlayer(spawnX, spawnY, spawnZ, 16.0, false);
                float yaw = 0;
                if (nearestPlayer != null) {
                    boolean portalSpansX = frameInfo.axis == Direction.Axis.X;

                    double playerOffset = portalSpansX
                            ? spawnZ - nearestPlayer.getZ()
                            : spawnX - nearestPlayer.getX();

                    if (playerOffset == 0.0) {
                        net.minecraft.world.phys.Vec3 look = nearestPlayer.getLookAngle();
                        playerOffset = portalSpansX ? look.z : look.x;
                    }

                    float directionYaw;
                    if (portalSpansX) {
                        boolean playerToTheNorth = playerOffset >= 0.0;
                        directionYaw = playerToTheNorth ? 0.0F : 180.0F;
                        spawnX += playerToTheNorth ? -0.5 : 0.5;
                    } else {
                        boolean playerToTheWest = playerOffset >= 0.0;
                        directionYaw = playerToTheWest ? -90.0F : 90.0F;
                        spawnZ += playerToTheWest ? 0.5 : -0.5;
                    }

                    yaw = net.minecraft.util.Mth.wrapDegrees(directionYaw + TENTACLE_YAW_OFFSET);
                }

                tentacle.snapTo(spawnX, spawnY, spawnZ, yaw, 0.0F);

                serverLevel.addFreshEntity(tentacle);

                serverLevel.playSound(null, portalPos, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 1.0F, 0.8F);
            } else {
                event.setCanceled(true);
            }
        }
    }

    private static void awardVoidMode(ServerLevel level, BlockPos portalPos) {
        for (net.minecraft.server.level.ServerPlayer player :
                level.getEntitiesOfClass(net.minecraft.server.level.ServerPlayer.class,
                        new net.minecraft.world.phys.AABB(portalPos).inflate(VOID_MODE_RADIUS))) {
            com.zavidvi.voidmod.advancement.ModAdvancements.grant(player,
                    com.zavidvi.voidmod.advancement.ModAdvancements.VOID_MODE);
        }
    }

    private static final double VOID_MODE_RADIUS = 16.0D;

    public record PortalFrameInfo(List<BlockPos> innerBlocks, Direction.Axis axis, double centerX, double bottomY, double centerZ) {}

    private static PortalFrameInfo findPortalFrame(ServerLevel level, BlockPos clickedPos) {
        List<BlockPos> innerBlocks = new ArrayList<>();
        Direction.Axis axis = Direction.Axis.X;

        BlockPos startAir = clickedPos.above();
        if (!level.isEmptyBlock(startAir) && !level.getBlockState(startAir).is(Blocks.FIRE)) {
            startAir = clickedPos;
        }

        if (level.getBlockState(clickedPos.east()).is(Blocks.OBSIDIAN) || level.getBlockState(clickedPos.west()).is(Blocks.OBSIDIAN)) {
            axis = Direction.Axis.X;
        } else if (level.getBlockState(clickedPos.north()).is(Blocks.OBSIDIAN) || level.getBlockState(clickedPos.south()).is(Blocks.OBSIDIAN)) {
            axis = Direction.Axis.Z;
        }

        int minX = startAir.getX(), maxX = startAir.getX();
        int minZ = startAir.getZ(), maxZ = startAir.getZ();
        int minY = startAir.getY(), maxY = startAir.getY();

        if (axis == Direction.Axis.X) {
            while (level.isEmptyBlock(new BlockPos(minX - 1, minY, startAir.getZ())) && minX > startAir.getX() - 10) minX--;
            while (level.isEmptyBlock(new BlockPos(maxX + 1, minY, startAir.getZ())) && maxX < startAir.getX() + 10) maxX++;
            while (level.isEmptyBlock(new BlockPos(startAir.getX(), minY - 1, startAir.getZ())) && minY > startAir.getY() - 10) minY--;
            while (level.isEmptyBlock(new BlockPos(startAir.getX(), maxY + 1, startAir.getZ())) && maxY < startAir.getY() + 10) maxY++;

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos p = new BlockPos(x, y, startAir.getZ());
                    if (level.isEmptyBlock(p) || level.getBlockState(p).is(Blocks.FIRE)) {
                        innerBlocks.add(p.immutable());
                    }
                }
            }
        } else {
            while (level.isEmptyBlock(new BlockPos(startAir.getX(), minY, minZ - 1)) && minZ > startAir.getZ() - 10) minZ--;
            while (level.isEmptyBlock(new BlockPos(startAir.getX(), minY, maxZ + 1)) && maxZ < startAir.getZ() + 10) maxZ++;
            while (level.isEmptyBlock(new BlockPos(startAir.getX(), minY - 1, startAir.getZ())) && minY > startAir.getY() - 10) minY--;
            while (level.isEmptyBlock(new BlockPos(startAir.getX(), maxY + 1, startAir.getZ())) && maxY < startAir.getY() + 10) maxY++;

            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos p = new BlockPos(startAir.getX(), y, z);
                    if (level.isEmptyBlock(p) || level.getBlockState(p).is(Blocks.FIRE)) {
                        innerBlocks.add(p.immutable());
                    }
                }
            }
        }

        double centerX = (minX + maxX) / 2.0 + 0.5;
        double centerZ = (minZ + maxZ) / 2.0 + 0.5;
        double bottomY = minY - 1;

        return new PortalFrameInfo(innerBlocks, axis, centerX, bottomY, centerZ);
    }
}

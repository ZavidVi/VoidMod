package com.zavidvi.voidmod.client.renderer.reaper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.block.GraveBlockEntity;
import com.zavidvi.voidmod.entity.reaper.ReaperArena;
import com.zavidvi.voidmod.entity.reaper.ReaperEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = VoidMod.MOD_ID, value = Dist.CLIENT)
public final class ReaperArenaBorderRenderer {
    private static final Identifier SOLID_TEXTURE =
            Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "textures/environment/reaper_arena_border.png");
    private static final Identifier TRANSITION_TEXTURE =
            Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "textures/environment/reaper_arena_border_top.png");

    private static final RenderType SOLID_RENDER_TYPE = RenderTypes.entityTranslucentEmissive(SOLID_TEXTURE);
    private static final RenderType TRANSITION_RENDER_TYPE = RenderTypes.entityTranslucentEmissive(TRANSITION_TEXTURE);

    private static final float WALL_OFFSET = 0.01F;

    private ReaperArenaBorderRenderer() {}

    @SubscribeEvent
    public static void onCustomGeometry(SubmitCustomGeometryEvent event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;

        Vec3 camPos = event.getLevelRenderState().cameraRenderState.pos;
        if (camPos == null) return;

        Set<BlockPos> activeGraves = new HashSet<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof ReaperEntity reaper && reaper.isAlive()) {
                BlockPos gravePos = reaper.getGravePos();
                if (gravePos != null) {
                    activeGraves.add(gravePos);
                }
            }
        }

        if (activeGraves.isEmpty()) {
            for (BlockEntity be : level.getGloballyRenderedBlockEntities()) {
                if (be instanceof GraveBlockEntity grave && grave.getStage() != GraveBlockEntity.STAGE_IDLE) {
                    activeGraves.add(grave.getBlockPos());
                }
            }
        }

        if (activeGraves.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        for (BlockPos gravePos : activeGraves) {
            renderArena(event, poseStack, level, gravePos, camPos);
        }
    }

    private static void renderArena(SubmitCustomGeometryEvent event, PoseStack poseStack, ClientLevel level, BlockPos gravePos, Vec3 camPos) {
        ChunkPos chunk = ChunkPos.containing(gravePos);
        float minX = (float) ReaperArena.minX(chunk) + WALL_OFFSET;
        float maxX = (float) ReaperArena.maxX(chunk) - WALL_OFFSET;
        float minZ = (float) ReaperArena.minZ(chunk) + WALL_OFFSET;
        float maxZ = (float) ReaperArena.maxZ(chunk) - WALL_OFFSET;

        int iMinX = (int) Math.floor(minX);
        int iMaxX = (int) Math.ceil(maxX);
        int iMinZ = (int) Math.floor(minZ);
        int iMaxZ = (int) Math.ceil(maxZ);

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        event.getSubmitNodeCollector().submitCustomGeometry(poseStack, SOLID_RENDER_TYPE, (pose, buffer) -> {
            renderPerimeter(buffer, pose, level, gravePos, iMinX, iMaxX, iMinZ, iMaxZ, minX, maxX, minZ, maxZ, 0, 2, 0.0F, 1.0F, 0.0F, 2.0F);
        });

        event.getSubmitNodeCollector().submitCustomGeometry(poseStack, TRANSITION_RENDER_TYPE, (pose, buffer) -> {
            renderPerimeter(buffer, pose, level, gravePos, iMinX, iMaxX, iMinZ, iMaxZ, minX, maxX, minZ, maxZ, 2, 3, 0.0F, 1.0F, 0.0F, 1.0F);
        });

        poseStack.popPose();
    }

    private static void renderPerimeter(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            ClientLevel level,
            BlockPos gravePos,
            int iMinX, int iMaxX, int iMinZ, int iMaxZ,
            float minX, float maxX, float minZ, float maxZ,
            int yOffsetLow, int yOffsetHigh,
            float u1, float u2,
            float vTop, float vBottom) {

        for (int x = iMinX; x < iMaxX; x++) {
            int groundY = getGroundY(level, x, iMinZ, gravePos.getY());
            addQuad(buffer, pose, x, minZ, x + 1, minZ, groundY + yOffsetLow, groundY + yOffsetHigh, u1, u2, vTop, vBottom, 0.0F, 0.0F, 1.0F);
            addQuad(buffer, pose, x + 1, minZ, x, minZ, groundY + yOffsetLow, groundY + yOffsetHigh, u1, u2, vTop, vBottom, 0.0F, 0.0F, -1.0F);
        }

        for (int x = iMinX; x < iMaxX; x++) {
            int groundY = getGroundY(level, x, iMaxZ - 1, gravePos.getY());
            addQuad(buffer, pose, x + 1, maxZ, x, maxZ, groundY + yOffsetLow, groundY + yOffsetHigh, u1, u2, vTop, vBottom, 0.0F, 0.0F, -1.0F);
            addQuad(buffer, pose, x, maxZ, x + 1, maxZ, groundY + yOffsetLow, groundY + yOffsetHigh, u1, u2, vTop, vBottom, 0.0F, 0.0F, 1.0F);
        }

        for (int z = iMinZ; z < iMaxZ; z++) {
            int groundY = getGroundY(level, iMinX, z, gravePos.getY());
            addQuad(buffer, pose, minX, z + 1, minX, z, groundY + yOffsetLow, groundY + yOffsetHigh, u1, u2, vTop, vBottom, 1.0F, 0.0F, 0.0F);
            addQuad(buffer, pose, minX, z, minX, z + 1, groundY + yOffsetLow, groundY + yOffsetHigh, u1, u2, vTop, vBottom, -1.0F, 0.0F, 0.0F);
        }

        for (int z = iMinZ; z < iMaxZ; z++) {
            int groundY = getGroundY(level, iMaxX - 1, z, gravePos.getY());
            addQuad(buffer, pose, maxX, z, maxX, z + 1, groundY + yOffsetLow, groundY + yOffsetHigh, u1, u2, vTop, vBottom, -1.0F, 0.0F, 0.0F);
            addQuad(buffer, pose, maxX, z + 1, maxX, z, groundY + yOffsetLow, groundY + yOffsetHigh, u1, u2, vTop, vBottom, 1.0F, 0.0F, 0.0F);
        }
    }

    private static void addQuad(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            float x1, float z1,
            float x2, float z2,
            float yLow, float yHigh,
            float u1, float u2,
            float vTop, float vBottom,
            float nx, float ny, float nz) {

        buffer.addVertex(pose, x1, yHigh, z1).setColor(255, 255, 255, 255).setUv(u1, vTop).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, nx, ny, nz);
        buffer.addVertex(pose, x1, yLow, z1).setColor(255, 255, 255, 255).setUv(u1, vBottom).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, nx, ny, nz);
        buffer.addVertex(pose, x2, yLow, z2).setColor(255, 255, 255, 255).setUv(u2, vBottom).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, nx, ny, nz);
        buffer.addVertex(pose, x2, yHigh, z2).setColor(255, 255, 255, 255).setUv(u2, vTop).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, nx, ny, nz);
    }

    private static int getGroundY(ClientLevel level, int x, int z, int defaultY) {
        if (!level.hasChunk(x >> 4, z >> 4)) {
            return defaultY;
        }
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (y <= level.getMinY()) {
            return defaultY;
        }
        return y;
    }
}

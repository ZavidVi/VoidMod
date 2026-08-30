package com.zavidvi.voidmod.entity.reaper.ai;

import com.zavidvi.voidmod.entity.reaper.ReaperEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ReaperReturnToGraveGoal extends Goal {
    private static final double ARRIVED_RADIUS = 4.0D;

    private static final double SPEED = 1.0D;

    private static final int REPATH_INTERVAL = 20;

    private static final double HOVER_TOLERANCE = 0.6D;

    private static final int GROUND_SEARCH_DEPTH = 12;

    private final ReaperEntity reaper;

    private int repathIn;

    public ReaperReturnToGraveGoal(ReaperEntity reaper) {
        this.reaper = reaper;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.reaper.isSpawning()) return false;
        if (this.reaper.getTarget() != null) return false;

        return this.reaper.isOutsideGraveLeash() || isHoveringAboveGround();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.reaper.getTarget() != null) return false;

        Vec3 anchor = this.reaper.graveAnchor();
        if (anchor == null) return false;

        return this.reaper.distanceToSqr(anchor) > ARRIVED_RADIUS * ARRIVED_RADIUS
                || isHoveringAboveGround();
    }

    @Override
    public void start() {
        this.repathIn = 0;
    }

    @Override
    public void stop() {
        this.reaper.getNavigation().stop();
    }

    @Override
    public void tick() {
        Vec3 anchor = this.reaper.graveAnchor();
        if (anchor == null) return;

        this.reaper.getLookControl().setLookAt(anchor.x, anchor.y + 1.0D, anchor.z);

        if (this.reaper.distanceToSqr(anchor) <= ARRIVED_RADIUS * ARRIVED_RADIUS) {
            descend();
            return;
        }

        if (--this.repathIn > 0) return;
        this.repathIn = REPATH_INTERVAL;

        this.reaper.getNavigation().moveTo(anchor.x, anchor.y, anchor.z, SPEED);
        if (this.reaper.getNavigation().isDone()) {
            this.reaper.getMoveControl().setWantedPosition(anchor.x, anchor.y, anchor.z, SPEED);
        }
    }

    private void descend() {
        this.reaper.getNavigation().stop();

        Double floor = floorBelow();
        if (floor == null) return;

        this.reaper.getMoveControl().setWantedPosition(
                this.reaper.getX(), floor, this.reaper.getZ(), SPEED);
    }

    private boolean isHoveringAboveGround() {
        Double floor = floorBelow();
        return floor != null && this.reaper.getY() - floor > HOVER_TOLERANCE;
    }

    private Double floorBelow() {
        Level level = this.reaper.level();
        BlockPos pos = this.reaper.blockPosition();

        for (int i = 0; i < GROUND_SEARCH_DEPTH; i++) {
            BlockPos below = pos.below();
            if (!level.getBlockState(below).getCollisionShape(level, below).isEmpty()) {
                return (double) pos.getY();
            }
            pos = below;
        }
        return null;
    }
}

package com.zavidvi.voidmod.entity.voidsphere.ai;

import com.zavidvi.voidmod.entity.voidsphere.VoidSphereEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

public class VoidSphereHoverGoal extends Goal {
    private static final double HOVER_HEIGHT = 2.0;

    private static final int GROUND_SCAN_DEPTH = 32;

    private static final double VERTICAL_SPEED = 0.06;

    private static final double Y_TOLERANCE = 0.15;

    private static final int GROUND_REFRESH_INTERVAL = 40;

    private final VoidSphereEntity sphere;

    private double hoverY = Double.NaN;
    private int ticksSinceScan = 0;

    public VoidSphereHoverGoal(VoidSphereEntity sphere) {
        this.sphere = sphere;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.sphere.getTarget() == null && !this.sphere.isPerformingSpecialAttack();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        this.sphere.setOrbiting(false);
        this.hoverY = resolveHoverY();
        this.ticksSinceScan = 0;
    }

    @Override
    public void tick() {
        if (++this.ticksSinceScan >= GROUND_REFRESH_INTERVAL || Double.isNaN(this.hoverY)) {
            this.hoverY = resolveHoverY();
            this.ticksSinceScan = 0;
        }

        double diff = this.hoverY - this.sphere.getY();
        double vertical = Math.abs(diff) < Y_TOLERANCE
                ? 0.0
                : Mth.clamp(diff * 0.2, -VERTICAL_SPEED, VERTICAL_SPEED);

        this.sphere.setDeltaMovement(0.0, vertical, 0.0);
    }

    private double resolveHoverY() {
        Level level = this.sphere.level();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(
                Mth.floor(this.sphere.getX()),
                Mth.floor(this.sphere.getY()),
                Mth.floor(this.sphere.getZ()));

        int bottom = Math.max(level.getMinY(), cursor.getY() - GROUND_SCAN_DEPTH);
        for (int y = cursor.getY(); y >= bottom; y--) {
            cursor.setY(y);
            if (!level.getBlockState(cursor).getCollisionShape(level, cursor).isEmpty()) {
                return y + 1 + HOVER_HEIGHT;
            }
        }
        return this.sphere.getY();
    }
}

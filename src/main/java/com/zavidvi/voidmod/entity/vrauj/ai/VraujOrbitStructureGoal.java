package com.zavidvi.voidmod.entity.vrauj.ai;

import com.zavidvi.voidmod.entity.vrauj.VraujEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class VraujOrbitStructureGoal extends Goal {
    private static final double ORBIT_RADIUS = 8.0;

    private static final double ORBIT_HEIGHT = 3.0;

    private static final double SPEED = 0.12;

    private static final float ANGULAR_SPEED = 0.02F;

    private final VraujEntity vrauj;
    private float angle;

    public VraujOrbitStructureGoal(VraujEntity vrauj) {
        this.vrauj = vrauj;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.vrauj.getTarget() == null
                && !this.vrauj.isAttacking()
                && this.vrauj.getOrbitCenter() != null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        this.angle = (this.vrauj.getId() % 16) * ((float) Math.PI / 8.0F);
    }

    @Override
    public void tick() {
        BlockPos center = this.vrauj.getOrbitCenter();
        if (center == null) return;

        this.angle += ANGULAR_SPEED;

        Vec3 point = new Vec3(
                center.getX() + 0.5 + Math.cos(this.angle) * ORBIT_RADIUS,
                center.getY() + ORBIT_HEIGHT,
                center.getZ() + 0.5 + Math.sin(this.angle) * ORBIT_RADIUS);

        Vec3 toPoint = point.subtract(this.vrauj.position());
        if (toPoint.lengthSqr() < 1.0E-4D) return;

        Vec3 step = toPoint.normalize().scale(SPEED);
        this.vrauj.setDeltaMovement(this.vrauj.getDeltaMovement().scale(0.8).add(step.scale(0.2)));
        this.vrauj.getLookControl().setLookAt(point.x, point.y, point.z);
    }
}

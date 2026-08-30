package com.zavidvi.voidmod.entity.vrauj.ai;

import com.zavidvi.voidmod.entity.vrauj.VraujEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class VraujHoverOverTargetGoal extends Goal {
    public static final double HOVER_HEIGHT = 15.0;

    public static final double ORBIT_RADIUS = 6.0;

    private static final float ANGULAR_SPEED = 0.02F;

    private static final double APPROACH_SPEED = 0.7;

    private static final double HOVER_SPEED = 0.22;

    private static final double SLOWDOWN_DISTANCE = 12.0;

    private final VraujEntity vrauj;

    private float angle;

    public VraujHoverOverTargetGoal(VraujEntity vrauj) {
        this.vrauj = vrauj;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.vrauj.getTarget();
        return target != null && target.isAlive() && !this.vrauj.isAttacking();
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
        LivingEntity target = this.vrauj.getTarget();
        this.angle = target == null ? 0.0F : (float) Mth.atan2(
                this.vrauj.getZ() - target.getZ(), this.vrauj.getX() - target.getX());
    }

    @Override
    public void tick() {
        LivingEntity target = this.vrauj.getTarget();
        if (target == null) return;

        this.vrauj.getLookControl().setLookAt(target, 30.0F, 30.0F);

        this.angle += ANGULAR_SPEED;

        Vec3 spot = target.position().add(
                Math.cos(this.angle) * ORBIT_RADIUS,
                HOVER_HEIGHT,
                Math.sin(this.angle) * ORBIT_RADIUS);
        Vec3 toSpot = spot.subtract(this.vrauj.position());
        double distance = toSpot.length();

        if (distance < 1.0E-4) return;

        double speed = distance >= SLOWDOWN_DISTANCE
                ? APPROACH_SPEED
                : Mth.lerp(distance / SLOWDOWN_DISTANCE, HOVER_SPEED, APPROACH_SPEED);

        Vec3 step = toSpot.scale(speed / distance);
        this.vrauj.setDeltaMovement(this.vrauj.getDeltaMovement().scale(0.75).add(step.scale(0.25)));
    }
}

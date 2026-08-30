package com.zavidvi.voidmod.entity.voidsphere.ai;

import com.zavidvi.voidmod.entity.voidsphere.VoidSphereEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class VoidSphereOrbitGoal extends Goal {
    private static final double HOVER_HEIGHT = 2.0;

    private final VoidSphereEntity sphere;
    private float orbitAngle = 0.0f;

    public VoidSphereOrbitGoal(VoidSphereEntity sphere) {
        this.sphere = sphere;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.sphere.getTarget();
        return target != null && target.isAlive() && !this.sphere.isPerformingSpecialAttack();
    }

    @Override
    public void start() {
        this.orbitAngle = this.sphere.getRandom().nextFloat() * (float) Math.PI * 2.0f;
    }

    @Override
    public void tick() {
        LivingEntity target = this.sphere.getTarget();
        if (target == null) return;

        this.sphere.getLookControl().setLookAt(target, 30.0F, 30.0F);

        double distSq = this.sphere.distanceToSqr(target);
        double orbitRadius = 3.0;

        if (distSq > 22.0) {
            this.sphere.setOrbiting(false);
            Vec3 chaseVec = target.position().add(0, HOVER_HEIGHT, 0).subtract(this.sphere.position()).normalize().scale(0.35);
            this.sphere.setDeltaMovement(this.sphere.getDeltaMovement().scale(0.70).add(chaseVec.scale(0.30)));
        } else {
            this.sphere.setOrbiting(true);
            this.orbitAngle += 0.06f;

            double targetX = target.getX() + Math.cos(this.orbitAngle) * orbitRadius;
            double targetY = target.getY() + HOVER_HEIGHT;
            double targetZ = target.getZ() + Math.sin(this.orbitAngle) * orbitRadius;

            Vec3 targetPos = new Vec3(targetX, targetY, targetZ);
            Vec3 toTargetPos = targetPos.subtract(this.sphere.position());
            Vec3 orbitMoveVec = toTargetPos.normalize().scale(0.18);
            this.sphere.setDeltaMovement(this.sphere.getDeltaMovement().scale(0.70).add(orbitMoveVec.scale(0.30)));
        }
    }
}

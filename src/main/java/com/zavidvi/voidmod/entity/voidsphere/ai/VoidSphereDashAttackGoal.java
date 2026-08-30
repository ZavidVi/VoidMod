package com.zavidvi.voidmod.entity.voidsphere.ai;

import com.zavidvi.voidmod.entity.voidsphere.VoidSphereEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class VoidSphereDashAttackGoal extends Goal {
    private static final double DASH_TRAVEL = 8.0;

    private final VoidSphereEntity sphere;
    private int cooldown = VoidSphereEntity.ATTACK_COOLDOWN_INITIAL;
    private int attackTicks = 0;
    private boolean dashed = false;
    private boolean hasHitTarget = false;
    private Vec3 targetDashPos = null;
    private Vec3 dashOrigin = null;

    private Vec3 previousPos = null;

    private boolean dashSpent = false;

    public VoidSphereDashAttackGoal(VoidSphereEntity sphere) {
        this.sphere = sphere;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.sphere.getTarget();
        if (target == null || !target.isAlive() || this.sphere.isPerformingSpecialAttack()) {
            return false;
        }
        if (this.sphere.getNextAttackType() != 1) {
            return false;
        }
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.attackTicks < 25 && this.sphere.getTarget() != null && this.sphere.getTarget().isAlive();
    }

    @Override
    public void start() {
        this.attackTicks = 0;
        this.dashed = false;
        this.hasHitTarget = false;
        this.dashOrigin = null;
        this.previousPos = null;
        this.dashSpent = false;
        this.sphere.startDashAttack();
        this.sphere.setPhasesThroughBlocks(false);
    }

    @Override
    public void stop() {
        this.sphere.stopSpecialAttack();
        this.sphere.setPhasesThroughBlocks(true);
        this.cooldown = this.sphere.rollAttackCooldown();
    }

    @Override
    public void tick() {
        LivingEntity target = this.sphere.getTarget();
        if (target != null && !this.dashed) {
            this.sphere.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        this.attackTicks++;

        if (!this.dashed) {
            this.sphere.setDeltaMovement(this.sphere.getDeltaMovement().scale(0.5));
            
            if (this.attackTicks == 10 && target != null) {
                this.targetDashPos = target.position().add(0, target.getBbHeight() / 2.0, 0);
            }
        }

        if (this.attackTicks == 10 && !this.dashed) {
            this.dashed = true;
            Vec3 origin = this.sphere.position();
            Vec3 dashTarget = this.targetDashPos != null ? this.targetDashPos : (target != null ? target.position().add(0, target.getBbHeight() / 2.0, 0) : origin);
            Vec3 dashDir = dashTarget.subtract(origin).normalize();
            this.dashOrigin = origin;
            this.sphere.setDeltaMovement(dashDir.scale(1.5));
        }

        if (this.dashed && !this.hasHitTarget && !this.dashSpent && target != null) {
            if (dashSweepHits(target)) {
                this.hasHitTarget = true;

                if (this.sphere.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    this.sphere.doHurtTarget(serverLevel, target);
                }

                Vec3 knockback = target.position().subtract(this.sphere.position()).normalize().scale(0.5).add(0, 0.2, 0);
                target.setDeltaMovement(target.getDeltaMovement().add(knockback));
                target.hurtMarked = true;
            }
        }

        if (this.dashed && this.dashOrigin != null
                && this.sphere.position().distanceTo(this.dashOrigin) > DASH_TRAVEL) {
            this.sphere.setDeltaMovement(this.sphere.getDeltaMovement().scale(0.55));
            this.dashSpent = true;
        }

        this.previousPos = this.sphere.position();
    }

    private boolean dashSweepHits(LivingEntity target) {
        AABB targetBox = target.getBoundingBox().inflate(this.sphere.getBbWidth() / 2.0);

        if (targetBox.intersects(this.sphere.getBoundingBox())) {
            return true;
        }

        Vec3 to = this.sphere.position();
        Vec3 from = this.previousPos != null ? this.previousPos : to;
        return targetBox.clip(from, to).isPresent();
    }
}

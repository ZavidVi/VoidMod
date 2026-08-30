package com.zavidvi.voidmod.entity.voidsphere.ai;

import com.zavidvi.voidmod.entity.voidsphere.VoidSphereEntity;
import com.zavidvi.voidmod.entity.voidsphere.VoidSphereProjectileEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class VoidSphereRangedAttackGoal extends Goal {
    private static final int FIRE_TICK = 20;

    private static final double RETREAT_DISTANCE = 2.0;

    private final VoidSphereEntity sphere;
    private int cooldown = VoidSphereEntity.ATTACK_COOLDOWN_INITIAL;
    private int attackTicks = 0;
    private boolean fired = false;
    private Vec3 targetSpot = null;
    private Vec3 retreatOrigin = null;

    public VoidSphereRangedAttackGoal(VoidSphereEntity sphere) {
        this.sphere = sphere;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.sphere.getTarget();
        if (target == null || !target.isAlive() || this.sphere.isPerformingSpecialAttack()) {
            return false;
        }
        if (this.sphere.getNextAttackType() != 2) {
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
        if (this.fired) {
            return false;
        }
        return this.attackTicks < 35 && this.sphere.getTarget() != null && this.sphere.getTarget().isAlive();
    }

    @Override
    public void start() {
        this.attackTicks = 0;
        this.fired = false;
        this.retreatOrigin = this.sphere.position();
        this.sphere.startRangedAttack();

        LivingEntity target = this.sphere.getTarget();
        if (target != null) {
            this.targetSpot = null;

            Vec3 away = this.sphere.position().subtract(target.position());
            Vec3 awayDir = new Vec3(away.x, 0.0D, away.z);
            awayDir = awayDir.lengthSqr() < 1.0E-4D
                    ? new Vec3(1.0D, 0.0D, 0.0D)
                    : awayDir.normalize();
            this.sphere.setDeltaMovement(awayDir.scale(0.5));
        }
    }

    @Override
    public void stop() {
        this.sphere.stopSpecialAttack();
        this.cooldown = this.sphere.rollAttackCooldown();
    }

    @Override
    public void tick() {
        LivingEntity target = this.sphere.getTarget();
        if (target != null) {
            this.sphere.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        this.attackTicks++;

        if (this.retreatOrigin != null
                && this.sphere.position().distanceTo(this.retreatOrigin) > RETREAT_DISTANCE) {
            this.sphere.setDeltaMovement(this.sphere.getDeltaMovement().scale(0.3));
        }

        if (this.attackTicks == FIRE_TICK && !this.fired && target != null) {
            this.fired = true;
            this.targetSpot = target.position();
            if (!this.sphere.level().isClientSide()) {
                VoidSphereProjectileEntity projectile = new VoidSphereProjectileEntity(this.sphere.level(), this.sphere);
                projectile.setPos(this.sphere.getX(), this.sphere.getY() + 0.5, this.sphere.getZ());

                double dx = this.targetSpot.x - this.sphere.getX();
                double dy = (this.targetSpot.y + 0.5D) - (this.sphere.getY() + 0.5D);
                double dz = this.targetSpot.z - this.sphere.getZ();
                double horizDist = Math.sqrt(dx * dx + dz * dz);

                projectile.shoot(dx, dy + horizDist * 0.3D, dz, 1.1F, 1.0F);
                this.sphere.level().addFreshEntity(projectile);
            }
        }
    }
}

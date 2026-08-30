package com.zavidvi.voidmod.entity.reaper.ai;

import com.zavidvi.voidmod.entity.reaper.ReaperEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class ReaperRushAttackGoal extends Goal {
    private static final double TRIGGER_RADIUS = 5.0D;

    private static final int DAMAGE_TICK = 6;

    private static final int MAX_RUSH_TICKS = 60;

    private static final double APPROACH_SPEED = 1.0D;

    private final ReaperEntity reaper;
    private final float damage;
    private final int rushInterval;
    private final double rushSpeed;

    private final int attackTicks;

    private final double swingSpeedFactor;

    private int rushCooldown;
    private int swingCooldown;
    private int rushTicks = -1;
    private int swingTicks = -1;
    private boolean damageDealt;

    public ReaperRushAttackGoal(ReaperEntity reaper, float damage, int rushInterval, double rushSpeed,
                                int attackTicks) {
        this(reaper, damage, rushInterval, rushSpeed, attackTicks, 0.0D);
    }

    public ReaperRushAttackGoal(ReaperEntity reaper, float damage, int rushInterval, double rushSpeed,
                                int attackTicks, double swingSpeedFactor) {
        this.reaper = reaper;
        this.damage = damage;
        this.rushInterval = rushInterval;
        this.rushSpeed = rushSpeed;
        this.attackTicks = attackTicks;
        this.swingSpeedFactor = swingSpeedFactor;
        this.rushCooldown = rushInterval;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.reaper.isSpawning()) return false;
        int state = this.reaper.getAttackState();
        if (state == ReaperEntity.ATTACK_CRIT
                || state == ReaperEntity.ATTACK_SPECIAL
                || state == ReaperEntity.ATTACK_SPECIAL_END) {
            return false;
        }

        LivingEntity target = this.reaper.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void stop() {
        endSwing();
        this.rushTicks = -1;
        this.reaper.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = this.reaper.getTarget();
        if (target == null) return;

        this.reaper.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (this.swingCooldown > 0) this.swingCooldown--;
        if (this.rushCooldown > 0) this.rushCooldown--;

        if (this.swingTicks >= 0) {
            moveDuringSwing(target);
            tickSwing(target);
            return;
        }

        ReaperMeleeAttackGoal.approach(this.reaper, target, approachSpeed());

        if (this.rushTicks >= 0) {
            tickRush(target);
        } else if (this.rushCooldown == 0 && this.reaper.distanceTo(target) <= TRIGGER_RADIUS) {
            this.rushTicks = 0;
        }

        if (this.swingCooldown == 0 && inReach(target)) {
            beginSwing(target);
        }
    }

    private void tickRush(LivingEntity target) {
        if (inReach(target) || ++this.rushTicks >= MAX_RUSH_TICKS) {
            this.rushTicks = -1;
            this.rushCooldown = this.rushInterval;
        }
    }

    private void tickSwing(LivingEntity target) {
        if (this.swingTicks == DAMAGE_TICK && !this.damageDealt) {
            this.damageDealt = true;
            if (inReach(target) && this.reaper.level() instanceof ServerLevel serverLevel) {
                target.hurtServer(serverLevel,
                        this.reaper.damageSources().mobAttack(this.reaper), this.damage);
            }
        }

        if (++this.swingTicks >= this.attackTicks) {
            endSwing();
            this.swingCooldown = this.attackTicks;
        }
    }

    private double approachSpeed() {
        return this.rushTicks >= 0 ? this.rushSpeed : APPROACH_SPEED;
    }

    private void moveDuringSwing(LivingEntity target) {
        if (this.swingSpeedFactor <= 0.0D) {
            holdStill();
            return;
        }

        ReaperMeleeAttackGoal.approach(this.reaper, target, approachSpeed() * this.swingSpeedFactor);
    }

    private void holdStill() {
        this.reaper.getNavigation().stop();

        net.minecraft.world.phys.Vec3 motion = this.reaper.getDeltaMovement();
        this.reaper.setDeltaMovement(0.0D, this.reaper.isNoGravity() ? 0.0D : motion.y, 0.0D);
    }

    private void beginSwing(LivingEntity target) {
        this.swingTicks = 0;
        this.damageDealt = false;
        moveDuringSwing(target);
        this.reaper.setAttackState(ReaperEntity.ATTACK_PRIMARY);
        this.reaper.playPhaseSound(this.reaper.attackSound(), ReaperEntity.BOSS_VOLUME);
    }

    private void endSwing() {
        this.swingTicks = -1;
        this.damageDealt = false;
        if (this.reaper.getAttackState() == ReaperEntity.ATTACK_PRIMARY) {
            this.reaper.setAttackState(ReaperEntity.ATTACK_NONE);
        }
    }

    private boolean inReach(LivingEntity target) {
        return this.reaper.getBoundingBox().inflate(ReaperMeleeAttackGoal.REACH)
                .intersects(target.getBoundingBox());
    }
}

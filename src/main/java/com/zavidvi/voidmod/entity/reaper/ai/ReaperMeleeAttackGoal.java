package com.zavidvi.voidmod.entity.reaper.ai;

import com.zavidvi.voidmod.entity.reaper.ReaperEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class ReaperMeleeAttackGoal extends Goal {
    public static final double REACH = 1.5D;

    static final double DIRECT_APPROACH_RANGE = 4.0D;

    private static final int ATTACK_TICKS = 13;

    private static final int DAMAGE_TICK = 6;

    private final ReaperEntity reaper;
    private final float damage;
    private final double speedModifier;

    private int swingTicks = -1;

    private int cooldown = 0;

    private boolean damageDealt = false;

    public ReaperMeleeAttackGoal(ReaperEntity reaper, float damage, double speedModifier) {
        this.reaper = reaper;
        this.damage = damage;
        this.speedModifier = speedModifier;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.reaper.isSpawning()) return false;

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
        this.reaper.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = this.reaper.getTarget();
        if (target == null) return;

        this.reaper.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (this.cooldown > 0) this.cooldown--;

        if (this.swingTicks >= 0) {
            tickSwing(target);
            return;
        }

        approach(this.reaper, target, this.speedModifier);

        if (this.cooldown == 0 && inReach(target)) {
            beginSwing();
        }
    }

    static void approach(ReaperEntity reaper, LivingEntity target, double speed) {
        if (reaper.distanceTo(target) <= DIRECT_APPROACH_RANGE) {
            reaper.getNavigation().stop();
            reaper.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), speed);
            return;
        }

        if (!reaper.getNavigation().moveTo(target, speed)) {
            reaper.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), speed);
        }
    }

    private void tickSwing(LivingEntity target) {
        approach(this.reaper, target, this.speedModifier);

        if (this.swingTicks == DAMAGE_TICK && !this.damageDealt) {
            this.damageDealt = true;
            if (inReach(target) && this.reaper.level() instanceof ServerLevel serverLevel) {
                target.hurtServer(serverLevel,
                        this.reaper.damageSources().mobAttack(this.reaper), this.damage);
            }
        }

        if (++this.swingTicks >= ATTACK_TICKS) {
            endSwing();
            this.cooldown = ATTACK_TICKS;
        }
    }

    private void beginSwing() {
        this.swingTicks = 0;
        this.damageDealt = false;
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
        return this.reaper.getBoundingBox().inflate(REACH).intersects(target.getBoundingBox());
    }
}

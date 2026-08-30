package com.zavidvi.voidmod.entity.reaper.ai;

import com.zavidvi.voidmod.entity.reaper.ReaperEntity;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ReaperTeleportCritGoal extends Goal {
    private static final double TELEPORT_DISTANCE = 8.0D;

    private static final double HIT_WIDTH = 3.0D;
    private static final double HIT_HEIGHT = 1.0D;
    private static final double HIT_DEPTH = 1.0D;

    private static final int TRAIL_PARTICLES = 30;

    private final ReaperEntity reaper;
    private final float damage;
    private final int interval;

    private final int flightTicks;

    private final int aimTick;

    private final int swingTicks;

    private long nextUseTime;
    private int ticks = -1;

    private Vec3 flightFrom;
    private Vec3 flightTo;
    private Vec3 aimPoint;
    private boolean damageDealt;

    public ReaperTeleportCritGoal(ReaperEntity reaper, float damage, int interval,
                                  int flightTicks, int aimTick, int swingTicks) {
        this.reaper = reaper;
        this.damage = damage;
        this.interval = interval;
        this.flightTicks = flightTicks;
        this.aimTick = aimTick;
        this.swingTicks = swingTicks;
        this.nextUseTime = reaper.level().getGameTime() + interval;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.reaper.isSpawning()) return false;
        int state = this.reaper.getAttackState();
        if (state == ReaperEntity.ATTACK_SPECIAL || state == ReaperEntity.ATTACK_SPECIAL_END) {
            return false;
        }

        if (this.reaper.level().getGameTime() < this.nextUseTime) return false;

        LivingEntity target = this.reaper.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticks >= 0 && this.reaper.getTarget() != null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        LivingEntity target = this.reaper.getTarget();
        if (target == null) return;

        Vec3 origin = this.reaper.position();

        Vec3 back = Vec3.directionFromRotation(0.0F, target.getYHeadRot()).scale(-TELEPORT_DISTANCE);
        Vec3 behind = target.position().add(back);

        this.flightFrom = behind;
        this.flightTo = target.position();
        this.aimPoint = null;
        this.damageDealt = false;
        this.ticks = 0;

        this.reaper.getNavigation().stop();
        this.reaper.setPos(behind.x, behind.y, behind.z);
        this.reaper.setAttackState(ReaperEntity.ATTACK_CRIT);
        this.reaper.playPhaseSound(this.reaper.dashSound(), ReaperEntity.BOSS_VOLUME);

        leaveTrail(origin);
    }

    @Override
    public void stop() {
        this.ticks = -1;
        this.flightFrom = null;
        this.flightTo = null;
        this.aimPoint = null;
        this.damageDealt = false;
        this.nextUseTime = this.reaper.level().getGameTime() + this.interval;
        if (this.reaper.getAttackState() == ReaperEntity.ATTACK_CRIT) {
            this.reaper.setAttackState(ReaperEntity.ATTACK_NONE);
        }
        this.reaper.setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public void tick() {
        if (this.ticks < 0) return;

        LivingEntity target = this.reaper.getTarget();
        if (target == null) return;

        if (this.aimPoint == null && this.ticks >= this.aimTick) {
            this.aimPoint = target.position();
        }

        this.flightTo = this.aimPoint != null ? this.aimPoint : target.position();

        if (this.ticks < this.flightTicks) {
            tickFlight();
        } else if (!this.damageDealt) {
            this.damageDealt = true;
            dealDamage();
        }

        if (++this.ticks >= this.flightTicks + this.swingTicks) {
            this.ticks = -1;
        }
    }

    private void tickFlight() {
        double progress = (this.ticks + 1.0D) / this.flightTicks;
        Vec3 at = this.flightFrom.lerp(this.flightTo, progress);
        this.reaper.setPos(at.x, at.y, at.z);
        this.reaper.setDeltaMovement(Vec3.ZERO);

        Vec3 dir = this.flightTo.subtract(this.flightFrom);
        if (dir.horizontalDistanceSqr() > 1.0E-4) {
            float yaw = (float) (Mth.atan2(dir.z, dir.x) * (180.0D / Math.PI)) - 90.0F;
            this.reaper.setYRot(yaw);
            this.reaper.yBodyRot = yaw;
            this.reaper.yHeadRot = yaw;
        }
    }

    private void dealDamage() {
        if (!(this.reaper.level() instanceof ServerLevel serverLevel)) return;
        if (this.aimPoint == null) return;

        AABB hit = AABB.ofSize(this.aimPoint.add(0.0D, HIT_HEIGHT / 2.0D, 0.0D),
                HIT_WIDTH, HIT_HEIGHT, HIT_DEPTH);

        for (LivingEntity victim : serverLevel.getEntitiesOfClass(LivingEntity.class, hit,
                e -> e != this.reaper && e.isAlive() && !(e instanceof ReaperEntity))) {
            victim.hurtServer(serverLevel,
                    this.reaper.damageSources().mobAttack(this.reaper), this.damage);
        }
    }

    private void leaveTrail(Vec3 origin) {
        if (!(this.reaper.level() instanceof ServerLevel serverLevel)) return;

        serverLevel.sendParticles(DustParticleOptions.REDSTONE,
                origin.x, origin.y + this.reaper.getBbHeight() / 2.0D, origin.z,
                TRAIL_PARTICLES, 0.3D, 0.8D, 0.3D, 0.01D);
    }
}

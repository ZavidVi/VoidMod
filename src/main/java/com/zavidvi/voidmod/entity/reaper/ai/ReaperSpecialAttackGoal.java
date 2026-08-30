package com.zavidvi.voidmod.entity.reaper.ai;

import com.zavidvi.voidmod.entity.reaper.ReaperEntity;
import com.zavidvi.voidmod.entity.reaper.ReaperFunnelEntity;
import com.zavidvi.voidmod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ReaperSpecialAttackGoal extends Goal {
    private static final double HOVER_HEIGHT = 5.0D;

    private static final int HOVER_TICKS = 200;

    private static final int START_TICKS = 18;

    private static final int END_TICKS = 5;

    private static final int MAX_DESCENT_TICKS = 60;

    private static final int FUNNEL_INTERVAL = 3;

    private static final int FUNNEL_BURST = 8;

    private static final double FUNNEL_RADIUS = 10.0D;

    private static final int UNDER_TARGET_INTERVAL = 20;

    private static final int GROUND_SEARCH_DEPTH = 6;

    private static final double LIFT_SPEED = 0.25D;

    private final ReaperEntity reaper;
    private final int interval;

    private long nextUseTime;
    private int ticks = -1;

    private double groundY;

    private LivingEntity target;
    private Vec3 lastTargetPos;

    public ReaperSpecialAttackGoal(ReaperEntity reaper, int interval) {
        this.reaper = reaper;
        this.interval = interval;
        this.nextUseTime = reaper.level().getGameTime() + interval;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.reaper.isSpawning()) return false;

        if (this.reaper.level().getGameTime() < this.nextUseTime) return false;

        LivingEntity target = this.reaper.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticks >= 0 && this.reaper.isAlive();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.groundY = this.reaper.getY();
        this.target = this.reaper.getTarget();
        this.lastTargetPos = this.target == null ? null : this.target.position();
        this.reaper.getNavigation().stop();
        this.reaper.setAttackState(ReaperEntity.ATTACK_SPECIAL);
        this.reaper.playPhaseSound(this.reaper.specialSound(), ReaperEntity.BOSS_VOLUME);
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void stop() {
        this.ticks = -1;
        this.target = null;
        this.lastTargetPos = null;
        this.nextUseTime = this.reaper.level().getGameTime() + this.interval;
        int state = this.reaper.getAttackState();
        if (state == ReaperEntity.ATTACK_SPECIAL || state == ReaperEntity.ATTACK_SPECIAL_END) {
            this.reaper.setAttackState(ReaperEntity.ATTACK_NONE);
        }
        this.reaper.setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public void tick() {
        if (this.ticks < 0) return;

        refreshTarget();

        this.reaper.getNavigation().stop();
        this.reaper.setDeltaMovement(Vec3.ZERO);
        if (this.target != null) {
            this.reaper.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        }

        int hoverEnd = START_TICKS + HOVER_TICKS;

        if (this.ticks < START_TICKS) {
            liftTowards(this.groundY + HOVER_HEIGHT);
            this.ticks++;
            return;
        }

        if (this.ticks < hoverEnd) {
            liftTowards(this.groundY + HOVER_HEIGHT);
            int hoverTick = this.ticks - START_TICKS;
            if (this.lastTargetPos != null) {
                if (hoverTick % FUNNEL_INTERVAL == 0) {
                    for (int i = 0; i < FUNNEL_BURST; i++) {
                        spawnFunnel(this.lastTargetPos);
                    }
                }
                if (hoverTick % UNDER_TARGET_INTERVAL == 0) {
                    spawnFunnelAt(this.lastTargetPos.x, this.lastTargetPos.y, this.lastTargetPos.z);
                }
            }
            this.ticks++;
            return;
        }

        if (this.reaper.getAttackState() == ReaperEntity.ATTACK_SPECIAL) {
            this.reaper.setAttackState(ReaperEntity.ATTACK_SPECIAL_END);
        }
        liftTowards(this.groundY);
        this.ticks++;

        boolean landed = Math.abs(this.reaper.getY() - this.groundY) < 0.05D;
        if ((landed && this.ticks >= hoverEnd + END_TICKS) || this.ticks >= hoverEnd + MAX_DESCENT_TICKS) {
            this.ticks = -1;
        }
    }

    private void liftTowards(double wantedY) {
        double dy = wantedY - this.reaper.getY();
        double step = Math.max(-LIFT_SPEED, Math.min(LIFT_SPEED, dy));
        this.reaper.setPos(this.reaper.getX(), this.reaper.getY() + step, this.reaper.getZ());
    }

    private void refreshTarget() {
        LivingEntity current = this.reaper.getTarget();
        if (current != null && current.isAlive()) {
            this.target = current;
        } else if (this.target != null && !this.target.isAlive()) {
            this.target = null;
        }

        if (this.target != null) {
            this.lastTargetPos = this.target.position();
        }
    }

    private void spawnFunnel(Vec3 around) {
        if (!(this.reaper.level() instanceof ServerLevel serverLevel)) return;

        double angle = serverLevel.getRandom().nextDouble() * Math.PI * 2.0D;
        double distance = serverLevel.getRandom().nextDouble() * FUNNEL_RADIUS;
        spawnFunnelAt(around.x + Math.cos(angle) * distance, around.y,
                around.z + Math.sin(angle) * distance);
    }

    private void spawnFunnelAt(double x, double fromY, double z) {
        if (!(this.reaper.level() instanceof ServerLevel serverLevel)) return;

        double centerX = Math.floor(x) + 0.5D;
        double centerZ = Math.floor(z) + 0.5D;

        Double floor = findFloor(serverLevel, centerX, fromY, centerZ);
        if (floor == null) return;

        ReaperFunnelEntity funnel = ModEntities.REAPER_FUNNEL.get().create(serverLevel,
                net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
        if (funnel == null) return;

        funnel.setPos(centerX, floor, centerZ);
        funnel.setOwner(this.reaper);
        serverLevel.addFreshEntity(funnel);
    }

    private static Double findFloor(Level level, double x, double fromY, double z) {
        BlockPos pos = BlockPos.containing(x, fromY, z);
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

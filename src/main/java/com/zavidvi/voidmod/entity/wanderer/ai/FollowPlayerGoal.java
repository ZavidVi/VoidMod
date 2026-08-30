package com.zavidvi.voidmod.entity.wanderer.ai;

import com.zavidvi.voidmod.entity.wanderer.WandererEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class FollowPlayerGoal extends Goal {
    private static final double SEARCH_RADIUS = 32.0D;

    private static final double STOP_DISTANCE = 3.0D;

    private static final int REPATH_INTERVAL = 10;

    private final WandererEntity wanderer;
    private final double speedModifier;

    private Player target;
    private int repathIn;

    public FollowPlayerGoal(WandererEntity wanderer, double speedModifier) {
        this.wanderer = wanderer;
        this.speedModifier = speedModifier;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.wanderer.isBoundToForge()) return false;

        Player nearest = this.wanderer.level().getNearestPlayer(this.wanderer, SEARCH_RADIUS);
        if (nearest == null || nearest.isSpectator()) return false;

        this.target = nearest;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null
                && this.target.isAlive()
                && !this.wanderer.isBoundToForge()
                && this.wanderer.distanceToSqr(this.target) <= SEARCH_RADIUS * SEARCH_RADIUS;
    }

    @Override
    public void start() {
        this.repathIn = 0;
    }

    @Override
    public void stop() {
        this.target = null;
        this.wanderer.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.target == null) return;

        this.wanderer.getLookControl().setLookAt(this.target, 30.0F, 30.0F);

        if (this.wanderer.distanceToSqr(this.target) <= STOP_DISTANCE * STOP_DISTANCE) {
            this.wanderer.getNavigation().stop();
            return;
        }

        if (--this.repathIn > 0) return;
        this.repathIn = REPATH_INTERVAL;

        this.wanderer.getNavigation().moveTo(this.target, this.speedModifier);
    }
}

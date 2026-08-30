package com.zavidvi.voidmod.entity.wanderer.ai;

import com.zavidvi.voidmod.entity.wanderer.WandererEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class WanderNearForgeGoal extends WaterAvoidingRandomStrollGoal {
    private static final int PICK_ATTEMPTS = 8;

    private static final int RETURN_RADIUS = 10;
    private static final int RETURN_Y_RANGE = 7;

    private final WandererEntity wanderer;

    public WanderNearForgeGoal(WandererEntity wanderer, double speedModifier) {
        super(wanderer, speedModifier);
        this.wanderer = wanderer;
    }

    @Override
    protected Vec3 getPosition() {
        BlockPos anchor = this.wanderer.getForgeAnchor();
        if (anchor == null) {
            return super.getPosition();
        }

        Vec3 anchorVec = Vec3.atBottomCenterOf(anchor);
        double leashSqr = WandererEntity.FORGE_LEASH_RADIUS * WandererEntity.FORGE_LEASH_RADIUS;

        if (this.wanderer.distanceToSqr(anchorVec) > leashSqr) {
            return DefaultRandomPos.getPosTowards(
                    this.wanderer, RETURN_RADIUS, RETURN_Y_RANGE, anchorVec, Math.PI / 2.0);
        }

        for (int attempt = 0; attempt < PICK_ATTEMPTS; attempt++) {
            Vec3 candidate = super.getPosition();
            if (candidate != null && candidate.distanceToSqr(anchorVec) <= leashSqr) {
                return candidate;
            }
        }
        return null;
    }
}

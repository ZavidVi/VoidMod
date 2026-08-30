package com.zavidvi.voidmod.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class BleedingEffect extends MobEffect {
    private static final int DAMAGE_INTERVAL = 40;

    private static final float DAMAGE = 1.0F;

    public static final int DURATION_TICKS = 120;

    private static final int COLOUR = 0x8B0000;

    public BleedingEffect() {
        super(MobEffectCategory.HARMFUL, COLOUR);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity mob, int amplification) {
        mob.hurtServer(level, mob.damageSources().magic(), DAMAGE);
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
        return tickCount % DAMAGE_INTERVAL == 0;
    }
}

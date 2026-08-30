package com.zavidvi.voidmod.entity.reaper;

import com.zavidvi.voidmod.entity.reaper.ai.ReaperReturnToGraveGoal;
import com.zavidvi.voidmod.entity.reaper.ai.ReaperRushAttackGoal;
import com.zavidvi.voidmod.entity.reaper.ai.ReaperTeleportCritGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;

public class ReaperLvl2Entity extends ReaperEntity {
    private static final int SPAWN_TICKS = 25;

    public static final float ATTACK_DAMAGE = 10.0F;
    public static final float CRIT_DAMAGE = 12.0F;

    private static final int RUSH_INTERVAL = 200;
    private static final int CRIT_INTERVAL = 400;

    private static final double RUSH_SPEED = 1.5D;

    private static final int ATTACK_TICKS = 13;

    private static final int CRIT_FLIGHT_TICKS = 20;
    private static final int CRIT_AIM_TICK = 10;

    private static final int CRIT_SWING_TICKS = 5;

    private static final RawAnimation ANIM_WALK_TOP = RawAnimation.begin().thenLoop("walkTop");
    private static final RawAnimation ANIM_WALK_BOT = RawAnimation.begin().thenLoop("walkBot");
    private static final RawAnimation ANIM_ATTACK_1 = RawAnimation.begin().thenPlayAndHold("attack1");
    private static final RawAnimation ANIM_ATTACK_2 = RawAnimation.begin().thenPlayAndHold("attack2");
    private static final RawAnimation ANIM_HURT = RawAnimation.begin().thenPlayAndHold("hurt");

    public ReaperLvl2Entity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.29D)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    public int phase() {
        return 2;
    }

    @Override
    public int spawnAnimationTicks() {
        return SPAWN_TICKS;
    }

    @Override
    protected int[] essenceDropRange() {
        return new int[]{10, 15};
    }

    @Override
    protected net.minecraft.sounds.SoundEvent spawnSound() {
        return com.zavidvi.voidmod.registry.ModSounds.REAPER2_SPAWN.get();
    }

    @Override
    public net.minecraft.sounds.SoundEvent attackSound() {
        return com.zavidvi.voidmod.registry.ModSounds.REAPER2_ATTACK1.get();
    }

    @Override
    public net.minecraft.sounds.SoundEvent dashSound() {
        return com.zavidvi.voidmod.registry.ModSounds.REAPER2_ATTACK2.get();
    }

    @Override
    protected net.minecraft.sounds.SoundEvent hurtSound() {
        return com.zavidvi.voidmod.registry.ModSounds.REAPER2_HURT.get();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ReaperTeleportCritGoal(this, CRIT_DAMAGE, CRIT_INTERVAL,
                CRIT_FLIGHT_TICKS, CRIT_AIM_TICK, CRIT_SWING_TICKS));
        this.goalSelector.addGoal(2, new ReaperRushAttackGoal(this, ATTACK_DAMAGE, RUSH_INTERVAL, RUSH_SPEED,
                ATTACK_TICKS));
        this.goalSelector.addGoal(3, new ReaperReturnToGraveGoal(this));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false,
                (target, level) -> isWithinGraveLeash(target)));
    }

    @Override
    protected RawAnimation fullBodyOverride() {
        RawAnimation base = super.fullBodyOverride();
        if (base != null) return base;

        return switch (getAttackState()) {
            case ATTACK_PRIMARY -> ANIM_ATTACK_1;
            case ATTACK_CRIT -> ANIM_ATTACK_2;
            default -> null;
        };
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<ReaperLvl2Entity>("bot", ANIM_TRANSITION_TICKS, state -> {
            RawAnimation override = fullBodyOverride();
            if (override != null) return state.setAndContinue(override);

            return state.setAndContinue(state.isMoving() ? ANIM_WALK_BOT : ANIM_IDLE);
        }));

        controllers.add(new AnimationController<ReaperLvl2Entity>("top", ANIM_TRANSITION_TICKS, state -> {
            RawAnimation override = fullBodyOverride();
            if (override != null) return state.setAndContinue(override);

            if (this.hurtTime > 0) return state.setAndContinue(ANIM_HURT);

            return state.setAndContinue(state.isMoving() ? ANIM_WALK_TOP : ANIM_IDLE);
        }));
    }
}

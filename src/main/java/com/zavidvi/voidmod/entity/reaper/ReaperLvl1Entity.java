package com.zavidvi.voidmod.entity.reaper;

import com.zavidvi.voidmod.entity.reaper.ai.ReaperMeleeAttackGoal;
import com.zavidvi.voidmod.entity.reaper.ai.ReaperReturnToGraveGoal;
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

public class ReaperLvl1Entity extends ReaperEntity {
    private static final int SPAWN_TICKS = 100;

    private static final int CORPSE_TICKS = 80;

    public static final float ATTACK_DAMAGE = 8.0F;

    private static final RawAnimation ANIM_WALK_TOP = RawAnimation.begin().thenLoop("walkTop");
    private static final RawAnimation ANIM_WALK_BOT = RawAnimation.begin().thenLoop("walkBot");
    private static final RawAnimation ANIM_ATTACK = RawAnimation.begin().thenPlayAndHold("attack");
    private static final RawAnimation ANIM_HURT = RawAnimation.begin().thenPlayAndHold("hurt");

    public ReaperLvl1Entity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 120.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    public int phase() {
        return 1;
    }

    @Override
    public int spawnAnimationTicks() {
        return SPAWN_TICKS;
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime >= CORPSE_TICKS && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected int[] essenceDropRange() {
        return new int[]{5, 8};
    }

    @Override
    protected net.minecraft.sounds.SoundEvent spawnSound() {
        return com.zavidvi.voidmod.registry.ModSounds.REAPER1_SPAWN.get();
    }

    @Override
    public net.minecraft.sounds.SoundEvent attackSound() {
        return com.zavidvi.voidmod.registry.ModSounds.REAPER1_ATTACK.get();
    }

    @Override
    protected net.minecraft.sounds.SoundEvent hurtSound() {
        return com.zavidvi.voidmod.registry.ModSounds.REAPER1_HURT.get();
    }

    @Override
    protected net.minecraft.sounds.SoundEvent deathSound() {
        return com.zavidvi.voidmod.registry.ModSounds.REAPER1_DEATH.get();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ReaperMeleeAttackGoal(this, ATTACK_DAMAGE, 1.0D));
        this.goalSelector.addGoal(2, new ReaperReturnToGraveGoal(this));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false,
                (target, level) -> isWithinGraveLeash(target)));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<ReaperLvl1Entity>("bot", ANIM_TRANSITION_TICKS, state -> {
            RawAnimation override = fullBodyOverride();
            if (override != null) return state.setAndContinue(override);

            return state.setAndContinue(state.isMoving() ? ANIM_WALK_BOT : ANIM_IDLE);
        }));

        controllers.add(new AnimationController<ReaperLvl1Entity>("top", ANIM_TRANSITION_TICKS, state -> {
            RawAnimation override = fullBodyOverride();
            if (override != null) return state.setAndContinue(override);

            if (isAttacking()) return state.setAndContinue(ANIM_ATTACK);
            if (this.hurtTime > 0) return state.setAndContinue(ANIM_HURT);

            return state.setAndContinue(state.isMoving() ? ANIM_WALK_TOP : ANIM_IDLE);
        }));
    }
}

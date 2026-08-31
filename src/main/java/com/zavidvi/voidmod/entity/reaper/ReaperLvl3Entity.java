package com.zavidvi.voidmod.entity.reaper;

import com.zavidvi.voidmod.entity.reaper.ai.ReaperReturnToGraveGoal;
import com.zavidvi.voidmod.entity.reaper.ai.ReaperRushAttackGoal;
import com.zavidvi.voidmod.entity.reaper.ai.ReaperSpecialAttackGoal;
import com.zavidvi.voidmod.entity.reaper.ai.ReaperTeleportCritGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;

public class ReaperLvl3Entity extends ReaperEntity {
    private static final int SPAWN_TICKS = 65;

    private static final int DEATH_ANIMATION_TICKS = 80;

    private static final int SPAWN_SOUND_TICK = 40;

    private static final int VICTORY_PARTICLES = 60;

    public static final float ATTACK_DAMAGE = 13.0F;
    public static final float CRIT_DAMAGE = 16.0F;

    private static final int RUSH_INTERVAL = 100;
    private static final int CRIT_INTERVAL = 200;
    private static final int SPECIAL_INTERVAL = 400;

    private static final double RUSH_SPEED = 2.0D;

    private static final int CRIT_AIM_TICK = 4;

    private static final int CRIT_FLIGHT_TICKS = 10;

    private static final int CRIT_SWING_TICKS = 10;

    private static final int ATTACK_TICKS = 12;

    private static final double SWING_SPEED_FACTOR = 1.0D / 3.0D;

    private static final float FLYING_ACCELERATION = 0.198F;

    private static final RawAnimation ANIM_FLY = RawAnimation.begin().thenLoop("fly");
    private static final RawAnimation ANIM_ATTACK_1_TOP = RawAnimation.begin().thenPlayAndHold("attack1Top");
    private static final RawAnimation ANIM_ATTACK_1_BOT = RawAnimation.begin().thenPlayAndHold("attack1Bot");
    private static final RawAnimation ANIM_ATTACK_2_BOT = RawAnimation.begin().thenPlayAndHold("attack2Bot");
    private static final RawAnimation ANIM_SPECIAL = RawAnimation.begin()
            .thenPlay("special_attack_start")
            .thenLoop("special_attack_loop");
    private static final RawAnimation ANIM_SPECIAL_END = RawAnimation.begin().thenPlayAndHold("special_attack_end");

    public ReaperLvl3Entity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.navigation = new FlyingPathNavigation(this, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FLYING_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    public int phase() {
        return 3;
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime >= DEATH_ANIMATION_TICKS && !this.level().isClientSide() && !this.isRemoved()) {
            if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                celebrate(serverLevel);
                uncurseWorld(serverLevel);
            }
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    private void celebrate(net.minecraft.server.level.ServerLevel level) {
        level.playSound(null, getX(), getY(), getZ(),
                com.zavidvi.voidmod.registry.ModSounds.REAPER3_EXPLOSION.get(),
                net.minecraft.sounds.SoundSource.HOSTILE, 4.0F, 1.0F);

        level.sendParticles(net.minecraft.core.particles.ParticleTypes.FIREWORK,
                getX(), getY() + getBbHeight() / 2.0D, getZ(),
                VICTORY_PARTICLES, 0.8D, 1.2D, 0.8D, 0.35D);
    }

    private static void uncurseWorld(net.minecraft.server.level.ServerLevel level) {
        com.zavidvi.voidmod.world.progression.WorldProgressionData progression =
                com.zavidvi.voidmod.world.progression.WorldProgressionData.get(level);
        if (!progression.isWorldCursed()) return;

        progression.setWorldCursed(false);
        net.neoforged.neoforge.network.PacketDistributor.sendToAllPlayers(
                com.zavidvi.voidmod.network.SyncProgressionPayload.of(progression));
    }

    @Override
    public int spawnAnimationTicks() {
        return SPAWN_TICKS;
    }

    @Override
    protected int[] essenceDropRange() {
        return new int[]{20, 30};
    }

    @Override
    protected net.minecraft.sounds.SoundEvent spawnSound() {
        return com.zavidvi.voidmod.registry.ModSounds.REAPER3_SPAWN.get();
    }

    @Override
    protected int spawnSoundTick() {
        return SPAWN_SOUND_TICK;
    }

    @Override
    public net.minecraft.sounds.SoundEvent attackSound() {
        return com.zavidvi.voidmod.registry.ModSounds.REAPER3_ATTACK1.get();
    }

    @Override
    public net.minecraft.sounds.SoundEvent dashSound() {
        return com.zavidvi.voidmod.registry.ModSounds.REAPER3_ATTACK2.get();
    }

    @Override
    public net.minecraft.sounds.SoundEvent specialSound() {
        return com.zavidvi.voidmod.registry.ModSounds.REAPER3_SPECIAL.get();
    }

    @Override
    protected net.minecraft.sounds.SoundEvent hurtSound() {
        return com.zavidvi.voidmod.registry.ModSounds.REAPER3_HURT.get();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ReaperSpecialAttackGoal(this, SPECIAL_INTERVAL));
        this.goalSelector.addGoal(2, new ReaperTeleportCritGoal(this, CRIT_DAMAGE, CRIT_INTERVAL,
                CRIT_FLIGHT_TICKS, CRIT_AIM_TICK, CRIT_SWING_TICKS));
        this.goalSelector.addGoal(3, new ReaperRushAttackGoal(this, ATTACK_DAMAGE, RUSH_INTERVAL, RUSH_SPEED,
                ATTACK_TICKS, SWING_SPEED_FACTOR));
        this.goalSelector.addGoal(4, new ReaperReturnToGraveGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false,
                (target, level) -> isWithinGraveLeash(target)));
    }

    @Override
    protected float getFlyingSpeed() {
        return getSpeed() * FLYING_ACCELERATION;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier,
                                   net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    @Override
    protected RawAnimation fullBodyOverride() {
        RawAnimation base = super.fullBodyOverride();
        if (base != null) return base;

        return switch (getAttackState()) {
            case ATTACK_SPECIAL -> ANIM_SPECIAL;
            case ATTACK_SPECIAL_END -> ANIM_SPECIAL_END;
            case ATTACK_CRIT -> ANIM_ATTACK_2_BOT;
            default -> null;
        };
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<ReaperLvl3Entity>("bot", ANIM_TRANSITION_TICKS, state -> {
            RawAnimation override = fullBodyOverride();
            if (override != null) return state.setAndContinue(override);

            return switch (getAttackState()) {
                case ATTACK_PRIMARY -> state.setAndContinue(ANIM_ATTACK_1_BOT);
                default -> state.setAndContinue(state.isMoving() ? ANIM_FLY : ANIM_IDLE);
            };
        }));

        controllers.add(new AnimationController<ReaperLvl3Entity>("top", ANIM_TRANSITION_TICKS, state -> {
            RawAnimation override = fullBodyOverride();
            if (override != null) return state.setAndContinue(override);

            return switch (getAttackState()) {
                case ATTACK_PRIMARY -> state.setAndContinue(ANIM_ATTACK_1_TOP);
                default -> state.setAndContinue(state.isMoving() ? ANIM_FLY : ANIM_IDLE);
            };
        }));
    }
}

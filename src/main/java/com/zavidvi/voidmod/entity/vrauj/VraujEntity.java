package com.zavidvi.voidmod.entity.vrauj;

import com.zavidvi.voidmod.entity.vrauj.ai.VraujAttackGoal;
import com.zavidvi.voidmod.entity.vrauj.ai.VraujHoverOverTargetGoal;
import com.zavidvi.voidmod.entity.vrauj.ai.VraujOrbitStructureGoal;
import com.zavidvi.voidmod.util.HoldableAnimationController;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

import java.util.Optional;

public class VraujEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Integer> ANIMATION_STATE =
            SynchedEntityData.defineId(VraujEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> ANIMATION_HELD =
            SynchedEntityData.defineId(VraujEntity.class, EntityDataSerializers.BOOLEAN);

    public static final int ANIM_STATE_FLY = 0;
    public static final int ANIM_STATE_ATTACK = 1;
    public static final int ANIM_STATE_RASSTREL = 2;
    public static final int ANIM_STATE_RASKID = 3;

    private static final RawAnimation ANIM_FLY = RawAnimation.begin().thenLoop("fly");
    private static final RawAnimation ANIM_ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation ANIM_RASSTREL = RawAnimation.begin().thenPlay("attack_1_rasstrel");
    private static final RawAnimation ANIM_RASKID = RawAnimation.begin().thenPlay("attack_2_raskid");

    public static final int RASKID_SPEEDUP = 2;

    private static final float YAW_TURN_STEP = 15.0F;
    private static final float PITCH_TURN_STEP = 8.0F;

    public static final int ANIM_TRANSITION_TICKS = 5;

    public static final int PLANT_TRANSITION_TICKS = 0;

    private static final float BODY_THICKNESS = 14.0F / 16.0F;
    private static final float BODY_LENGTH = 38.0F / 16.0F;

    private static final net.minecraft.world.entity.EntityDimensions FLYING_DIMENSIONS =
            net.minecraft.world.entity.EntityDimensions.scalable(BODY_LENGTH, BODY_THICKNESS);

    private static final net.minecraft.world.entity.EntityDimensions PLANTED_DIMENSIONS =
            net.minecraft.world.entity.EntityDimensions.scalable(BODY_THICKNESS, BODY_LENGTH);

    private static final int DEAGGRO_CHUNK_DISTANCE = 6;

    private static final int DEAGGRO_CHECK_INTERVAL = 20;

    private static final double YAW_MOTION_THRESHOLD = 0.05;
    private static final double PITCH_MOTION_THRESHOLD = 0.08;

    public static final float BODY_DAMAGE = 4.0F;

    public static final float FAN_PROJECTILE_DAMAGE = 7.0F;

    public static final float SINGLE_PROJECTILE_DAMAGE = 9.0F;

    private BlockPos orbitCenter = null;

    private boolean supervoidGuard = false;

    private int lastAttackState = ANIM_STATE_FLY;

    private int swarmSize = 0;

    public VraujEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.navigation = new FlyingPathNavigation(this, level);
        this.lookControl = new PitchFreeLookControl(this);
        this.xpReward = 20;
    }

    private static class PitchFreeLookControl extends LookControl {
        PitchFreeLookControl(VraujEntity vrauj) {
            super(vrauj);
        }

        @Override
        protected boolean resetXRotOnTick() {
            return false;
        }

        @Override
        protected Optional<Float> getXRotD() {
            return Optional.empty();
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 80.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FLYING_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, BODY_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION_STATE, ANIM_STATE_FLY);
        builder.define(ANIMATION_HELD, false);
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return com.zavidvi.voidmod.registry.ModSounds.VRAUJ_FLIGHT.get();
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) {
        return com.zavidvi.voidmod.registry.ModSounds.VRAUJ_HURT.get();
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new VraujAttackGoal(this));
        this.goalSelector.addGoal(2, new VraujHoverOverTargetGoal(this));
        this.goalSelector.addGoal(3, new VraujOrbitStructureGoal(this));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) return;

        hurtTouchedPlayers();

        if (this.tickCount % DEAGGRO_CHECK_INTERVAL == 0) {
            dropDistantTarget();
        }

        float yaw = this.getYRot();
        yaw += Mth.clamp(Mth.wrapDegrees(wantedYaw() - yaw), -YAW_TURN_STEP, YAW_TURN_STEP);
        this.setYRot(yaw);
        this.yBodyRot = yaw;

        if (isAttacking()) {
            this.setXRot(0.0F);
            return;
        }

        float pitch = this.getXRot();
        this.setXRot(pitch + Mth.clamp(wantedPitch() - pitch, -PITCH_TURN_STEP, PITCH_TURN_STEP));
    }

    private float wantedYaw() {
        Vec3 velocity = this.getDeltaMovement();
        if (isPlanted() || velocity.horizontalDistance() < YAW_MOTION_THRESHOLD) return this.yHeadRot;

        return (float) (Mth.atan2(velocity.z, velocity.x) * (180.0D / Math.PI)) - 90.0F;
    }

    private float wantedPitch() {
        Vec3 velocity = this.getDeltaMovement();
        if (velocity.lengthSqr() < PITCH_MOTION_THRESHOLD * PITCH_MOTION_THRESHOLD) return 0.0F;

        return (float) -(Mth.atan2(velocity.y, velocity.horizontalDistance()) * (180.0D / Math.PI));
    }

    public boolean isPlanted() {
        return isPlantedState(getAnimationState());
    }

    private static boolean isPlantedState(int state) {
        return state == ANIM_STATE_RASSTREL || state == ANIM_STATE_RASKID;
    }

    public int getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    public void setAnimationState(int state) {
        this.entityData.set(ANIMATION_STATE, state);
    }

    public boolean isAnimationHeld() {
        return this.entityData.get(ANIMATION_HELD);
    }

    public void setAnimationHeld(boolean held) {
        this.entityData.set(ANIMATION_HELD, held);
    }

    public boolean isAttacking() {
        return getAnimationState() != ANIM_STATE_FLY;
    }

    public int getLastAttackState() {
        return this.lastAttackState;
    }

    public void setLastAttackState(int state) {
        this.lastAttackState = state;
    }

    public BlockPos getOrbitCenter() {
        return this.orbitCenter;
    }

    public void setOrbitCenter(BlockPos center) {
        this.orbitCenter = center;
    }

    public boolean isSupervoidGuard() {
        return this.supervoidGuard;
    }

    public void setSupervoidGuard(boolean guard) {
        this.supervoidGuard = guard;
    }

    public int getSwarmSize() {
        return this.swarmSize;
    }

    public void setSwarmSize(int swarmSize) {
        this.swarmSize = swarmSize;
    }

    @Override
    public void setTarget(LivingEntity target) {
        super.setTarget(target);
    }

    private void hurtTouchedPlayers() {
        if (!(this.level() instanceof ServerLevel serverLevel) || !this.isAlive()) return;

        AABB reach = this.getBoundingBox();
        for (Player player : serverLevel.getEntitiesOfClass(Player.class, reach, this::canAttackTouched)) {
            player.hurtServer(serverLevel, this.damageSources().mobAttack(this), BODY_DAMAGE);
        }
    }

    private boolean canAttackTouched(Player player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator();
    }

    private void dropDistantTarget() {
        if (!this.supervoidGuard || this.orbitCenter == null) return;

        LivingEntity target = this.getTarget();
        if (target == null) return;

        int distance = net.minecraft.world.level.ChunkPos.containing(target.blockPosition())
                .getChessboardDistance(net.minecraft.world.level.ChunkPos.containing(this.orbitCenter));
        if (distance <= DEAGGRO_CHUNK_DISTANCE) return;

        this.setTarget(null);
        this.setLastHurtByMob(null);
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isInWall() {
        return false;
    }

    @Override
    public boolean isPersistenceRequired() {
        return this.supervoidGuard || super.isPersistenceRequired();
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("SupervoidGuard", this.supervoidGuard);
        output.putInt("SwarmSize", this.swarmSize);
        if (this.orbitCenter != null) {
            output.putLong("OrbitCenter", this.orbitCenter.asLong());
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.supervoidGuard = input.getBooleanOr("SupervoidGuard", false);
        this.swarmSize = input.getIntOr("SwarmSize", 0);
        this.orbitCenter = input.getLong("OrbitCenter").map(BlockPos::of).orElse(null);
    }

    private static final float ESSENCE_DROP_CHANCE = 0.62F;
    private static final int ESSENCE_MIN = 2;
    private static final int ESSENCE_MAX = 4;

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            dropEssence(serverLevel);
            com.zavidvi.voidmod.world.supervoid.SupervoidEncounters.onVraujKilled(serverLevel, this);
        }
    }

    private void dropEssence(ServerLevel level) {
        if (this.random.nextFloat() >= ESSENCE_DROP_CHANCE) return;

        int amount = ESSENCE_MIN + this.random.nextInt(ESSENCE_MAX - ESSENCE_MIN + 1);
        this.spawnAtLocation(level, new net.minecraft.world.item.ItemStack(
                com.zavidvi.voidmod.registry.ModItems.FIRE_ESSENCE.get(), amount));
    }

    @Override
    protected net.minecraft.world.entity.EntityDimensions getDefaultDimensions(
            net.minecraft.world.entity.Pose pose) {
        return isAttacking() ? PLANTED_DIMENSIONS : FLYING_DIMENSIONS;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (ANIMATION_STATE.equals(accessor)) {
            this.refreshDimensions();
        }
    }

    @Override
    public boolean fudgePositionAfterSizeChange(net.minecraft.world.entity.EntityDimensions previousDimensions) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new HoldableAnimationController<VraujEntity>("controller", ANIM_TRANSITION_TICKS,
                VraujEntity::isAnimationHeld,
                state -> {
                    int animation = getAnimationState();

                    state.controller().setTransitionTicks(isPlantedState(animation)
                            ? PLANT_TRANSITION_TICKS
                            : ANIM_TRANSITION_TICKS);

                    state.controller().setAnimationSpeed(
                            animation == ANIM_STATE_RASKID ? RASKID_SPEEDUP : 1.0D);

                    return switch (animation) {
                        case ANIM_STATE_ATTACK -> state.setAndContinue(ANIM_ATTACK);
                        case ANIM_STATE_RASSTREL -> state.setAndContinue(ANIM_RASSTREL);
                        case ANIM_STATE_RASKID -> state.setAndContinue(ANIM_RASKID);
                        default -> state.setAndContinue(ANIM_FLY);
                    };
                }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

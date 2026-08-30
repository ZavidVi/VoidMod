package com.zavidvi.voidmod.entity.voidsphere;

import com.zavidvi.voidmod.entity.voidsphere.ai.VoidSphereDashAttackGoal;
import com.zavidvi.voidmod.entity.voidsphere.ai.VoidSphereOrbitGoal;
import com.zavidvi.voidmod.entity.voidsphere.ai.VoidSphereRangedAttackGoal;
import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.network.SyncProgressionPayload;
import com.zavidvi.voidmod.registry.ModEntities;
import com.zavidvi.voidmod.world.progression.WorldProgressionData;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

public class VoidSphereEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Boolean> IS_ORBITING =
            SynchedEntityData.defineId(VoidSphereEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ATTACK_STATE =
            SynchedEntityData.defineId(VoidSphereEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_PORTAL_SPHERE =
            SynchedEntityData.defineId(VoidSphereEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation ANIM_SPAWN = RawAnimation.begin().thenPlay("spawn").thenLoop("idle");
    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ANIM_FLY = RawAnimation.begin().thenLoop("fly");
    private static final RawAnimation ANIM_FLY_AROUND = RawAnimation.begin().thenLoop("fly_around");
    private static final RawAnimation ANIM_ATTACK_DASH = RawAnimation.begin().thenPlay("attack").thenLoop("idle");
    private static final RawAnimation ANIM_ATTACK_NAVES = RawAnimation.begin().thenPlay("attack_naves").thenLoop("idle");

    public VoidSphereEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.navigation = new FlyingPathNavigation(this, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.FLYING_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 18.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_ORBITING, false);
        builder.define(ATTACK_STATE, 0);
        builder.define(IS_PORTAL_SPHERE, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new VoidSphereDashAttackGoal(this));
        this.goalSelector.addGoal(2, new VoidSphereRangedAttackGoal(this));
        this.goalSelector.addGoal(3, new VoidSphereOrbitGoal(this));
        this.goalSelector.addGoal(4, new com.zavidvi.voidmod.entity.voidsphere.ai.VoidSphereHoverGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 3;
    }

    @Override
    public void checkDespawn() {
        if (this.isPortalSphere() || this.isPersistenceRequired()) {
            return;
        }
        Player nearestPlayer = this.level().getNearestPlayer(this, -1.0);
        if (nearestPlayer != null) {
            double distSq = this.distanceToSqr(nearestPlayer);
            if (distSq > 128.0 * 128.0) {
                this.discard();
            }
        } else {
            super.checkDespawn();
        }
    }

    public static boolean checkVoidSphereSpawnRules(EntityType<VoidSphereEntity> type, net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.entity.EntitySpawnReason spawnType, net.minecraft.core.BlockPos pos, net.minecraft.util.RandomSource random) {
        if (level.getLevel().getServer() == null || !WorldProgressionData.get(level.getLevel()).isPortalAttempted()) {
            return false;
        }
        if (!level.getLevel().isDarkOutside()) {
            return false;
        }
        Player nearestPlayer = level.getLevel().getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 128.0, false);
        if (nearestPlayer != null) {
            double distSq = nearestPlayer.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
            if (distSq < 24.0 * 24.0 || distSq > 48.0 * 48.0) {
                return false;
            }
        }
        int nearbySpheres = level.getEntitiesOfClass(VoidSphereEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(70.0)).size();
        if (nearbySpheres >= 6) {
            return false;
        }
        return net.minecraft.world.entity.monster.Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }

    private static int rollNightGroupSize(net.minecraft.util.RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 50) {
            return 1;
        }
        return roll < 80 ? 2 : 3;
    }

    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
            net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.DifficultyInstance difficulty,
            net.minecraft.world.entity.EntitySpawnReason spawnType,
            @org.jetbrains.annotations.Nullable net.minecraft.world.entity.SpawnGroupData spawnGroupData) {
        net.minecraft.world.entity.SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);

        if (spawnType == net.minecraft.world.entity.EntitySpawnReason.NATURAL) {
            spawnNightCompanions(level);
        }

        return data;
    }

    private void spawnNightCompanions(net.minecraft.world.level.ServerLevelAccessor level) {
        int groupSize = rollNightGroupSize(this.random);
        if (groupSize <= 1) {
            return;
        }

        int nearbySpheres = level.getEntitiesOfClass(VoidSphereEntity.class,
                new net.minecraft.world.phys.AABB(this.blockPosition()).inflate(70.0)).size();
        int allowed = Math.min(groupSize - 1, 6 - nearbySpheres);

        for (int i = 0; i < allowed; i++) {
            VoidSphereEntity companion = ModEntities.VOID_SPHERE.get().create(level.getLevel(), net.minecraft.world.entity.EntitySpawnReason.EVENT);
            if (companion == null) {
                return;
            }
            companion.snapTo(
                    this.getX() + (this.random.nextDouble() - 0.5) * 4.0,
                    this.getY() + this.random.nextDouble() * 2.0,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 4.0,
                    this.random.nextFloat() * 360.0F,
                    0.0F);
            level.addFreshEntity(companion);
        }
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isLocalInstanceAuthoritative()) {
            if (this.isInWater()) {
                this.moveRelative(0.02F, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.8F));
            } else if (this.isInLava()) {
                this.moveRelative(0.02F, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
            } else {
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.91F));
            }
        }
        this.calculateEntityAnimation(false);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getTarget() != null) {
            this.yBodyRot = this.yHeadRot;
            this.setYRot(this.yHeadRot);
        }
    }

    @Override
    public void setTarget(@org.jetbrains.annotations.Nullable net.minecraft.world.entity.LivingEntity target) {
        if (target instanceof VoidSphereEntity) {
            return;
        }
        super.setTarget(target);
    }

    @Override
    public boolean isInWall() {
        return false;
    }

    public void setPhasesThroughBlocks(boolean phases) {
        this.noPhysics = phases;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource damageSource) {
        return com.zavidvi.voidmod.registry.ModSounds.VOID_SPHERE_HURT.get();
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return com.zavidvi.voidmod.registry.ModSounds.VOID_SPHERE_DEATH.get();
    }

    public void setOrbiting(boolean orbiting) {
        this.entityData.set(IS_ORBITING, orbiting);
    }

    public boolean isOrbiting() {
        return this.entityData.get(IS_ORBITING);
    }

    private int nextAttackType = 1;

    public static final int ATTACK_COOLDOWN_MIN = 30;
    public static final int ATTACK_COOLDOWN_SPREAD = 30;

    public static final int ATTACK_COOLDOWN_INITIAL = 20;

    public int getNextAttackType() {
        return this.nextAttackType;
    }

    public int rollAttackCooldown() {
        return ATTACK_COOLDOWN_MIN + this.random.nextInt(ATTACK_COOLDOWN_SPREAD);
    }

    public void startDashAttack() {
        this.entityData.set(ATTACK_STATE, 1);
    }

    public void startRangedAttack() {
        this.entityData.set(ATTACK_STATE, 2);
    }

    public void stopSpecialAttack() {
        this.entityData.set(ATTACK_STATE, 0);
        this.nextAttackType = this.random.nextBoolean() ? 1 : 2;
    }

    public boolean isPerformingSpecialAttack() {
        return this.entityData.get(ATTACK_STATE) != 0;
    }

    public void setPortalSphere(boolean portalSphere) {
        this.entityData.set(IS_PORTAL_SPHERE, portalSphere);
    }

    public boolean isPortalSphere() {
        return this.entityData.get(IS_PORTAL_SPHERE);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        int amount = rollEssenceDrop(WorldProgressionData.get(level));
        if (amount > 0) {
            this.spawnAtLocation(level, new net.minecraft.world.item.ItemStack(
                    com.zavidvi.voidmod.registry.ModItems.FIRE_ESSENCE.get(), amount));
        }
    }

    private int rollEssenceDrop(WorldProgressionData progression) {
        if (this.isPortalSphere()) {
            return 1 + this.random.nextInt(2);
        }
        if (!progression.isWandererTalked()) {
            return 0;
        }
        return this.random.nextFloat() < 0.42F ? 1 + this.random.nextInt(2) : 0;
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            if (this.isPortalSphere()) {
                long remainingPortalSpheres = serverLevel.getEntities(ModEntities.VOID_SPHERE.get(),
                        sphere -> sphere != this && sphere.getHealth() > 0 && sphere.isPortalSphere()).size();
                
                VoidMod.LOGGER.debug("Portal sphere died, remaining: {}", remainingPortalSpheres);

                if (remainingPortalSpheres <= 0) {
                    WorldProgressionData progression = WorldProgressionData.get(serverLevel);

                    VoidMod.LOGGER.info("All portal spheres defeated (worldCursed={})", progression.isWorldCursed());

                    if (!progression.isWorldCursed()) {
                        progression.setWorldCursed(true);
                        WorldProgressionData.broadcastCurseMessage(serverLevel);
                        
                        PacketDistributor.sendToAllPlayers(SyncProgressionPayload.of(progression));
                    }
                }
            }
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setPortalSphere(input.getBooleanOr("isPortalSphere", false));
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("isPortalSphere", this.isPortalSphere());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<VoidSphereEntity>("controller", 5, state -> {
            int attackState = this.entityData.get(ATTACK_STATE);
            if (attackState == 1) {
                return state.setAndContinue(ANIM_ATTACK_DASH);
            } else if (attackState == 2) {
                return state.setAndContinue(ANIM_ATTACK_NAVES);
            }

            if (this.tickCount < 30) {
                return state.setAndContinue(ANIM_SPAWN);
            }

            if (this.isOrbiting()) {
                return state.setAndContinue(ANIM_FLY_AROUND);
            }

            if (this.getDeltaMovement().horizontalDistanceSqr() > 0.005) {
                return state.setAndContinue(ANIM_FLY);
            }

            return state.setAndContinue(ANIM_IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

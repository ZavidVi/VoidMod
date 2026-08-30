package com.zavidvi.voidmod.entity.reaper;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

public abstract class ReaperEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Mth.createInsecureUUID(this.random), this.getDisplayName(),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    private static final EntityDataAccessor<Integer> SPAWN_TICKS =
            SynchedEntityData.defineId(ReaperEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> ATTACK_STATE =
            SynchedEntityData.defineId(ReaperEntity.class, EntityDataSerializers.INT);

    public static final int ATTACK_NONE = 0;
    public static final int ATTACK_PRIMARY = 1;
    public static final int ATTACK_CRIT = 2;
    public static final int ATTACK_SPECIAL = 3;
    public static final int ATTACK_SPECIAL_END = 4;

    protected static final RawAnimation ANIM_SPAWN = RawAnimation.begin().thenPlayAndHold("spawn");
    protected static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation ANIM_DEATH = RawAnimation.begin().thenPlayAndHold("death");

    protected static final int ANIM_TRANSITION_TICKS = 5;

    protected ReaperEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 50;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SPAWN_TICKS, 0);
        builder.define(ATTACK_STATE, ATTACK_NONE);
    }

    public int getAttackState() {
        return this.entityData.get(ATTACK_STATE);
    }

    public void setAttackState(int state) {
        this.entityData.set(ATTACK_STATE, state);
    }

    public boolean isAttacking() {
        return getAttackState() == ATTACK_PRIMARY;
    }

    protected RawAnimation fullBodyOverride() {
        if (isSpawning()) return ANIM_SPAWN;
        if (isDeadOrDying()) return ANIM_DEATH;
        return null;
    }

    public abstract int spawnAnimationTicks();

    public void beginSpawnAnimation() {
        this.entityData.set(SPAWN_TICKS, spawnAnimationTicks());
    }

    public boolean isSpawning() {
        return this.entityData.get(SPAWN_TICKS) > 0;
    }

    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
            net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.DifficultyInstance difficulty,
            net.minecraft.world.entity.EntitySpawnReason spawnReason,
            net.minecraft.world.entity.SpawnGroupData groupData) {
        beginSpawnAnimation();
        return super.finalizeSpawn(level, difficulty, spawnReason, groupData);
    }

    public int spawnTicksElapsed() {
        return spawnAnimationTicks() - this.entityData.get(SPAWN_TICKS);
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (isSpawning() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return true;
        }
        return super.isInvulnerableTo(level, source);
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || isSpawning();
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            int left = this.entityData.get(SPAWN_TICKS);
            if (left > 0) {
                this.entityData.set(SPAWN_TICKS, left - 1);
                if (spawnAnimationTicks() - (left - 1) == spawnSoundTick()) {
                    playPhaseSound(spawnSound(), BOSS_VOLUME);
                }
                this.setDeltaMovement(Vec3.ZERO);
            }
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
            tickRegression();
            dropTargetOutOfChaseRange();
        }
    }

    private void dropTargetOutOfChaseRange() {
        net.minecraft.world.entity.LivingEntity target = getTarget();
        if (target == null) return;

        if (!target.isAlive() || !isWithinChaseRange(target)) {
            setTarget(null);
        }
    }

    protected abstract int[] essenceDropRange();

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (this.level() instanceof ServerLevel serverLevel) {
            int[] range = essenceDropRange();
            int amount = range[0] + this.random.nextInt(range[1] - range[0] + 1);
            this.spawnAtLocation(serverLevel,
                    new net.minecraft.world.item.ItemStack(
                            com.zavidvi.voidmod.registry.ModItems.FIRE_ESSENCE.get(), amount));

            if (phase() == 3) {
                onFinalPhaseDefeated(serverLevel);
            }
        }
    }

    private static void onFinalPhaseDefeated(ServerLevel level) {
        com.zavidvi.voidmod.world.progression.WorldProgressionData data =
                com.zavidvi.voidmod.world.progression.WorldProgressionData.get(level);
        if (data.isReaperDefeated()) return;

        data.setReaperDefeated(true);
        net.neoforged.neoforge.network.PacketDistributor.sendToAllPlayers(
                com.zavidvi.voidmod.network.SyncProgressionPayload.of(data));
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) {
        return hurtSound();
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return deathSound();
    }

    public static final float BOSS_VOLUME = 2.0F;

    protected net.minecraft.sounds.SoundEvent spawnSound() {
        return null;
    }

    protected int spawnSoundTick() {
        return 1;
    }

    public net.minecraft.sounds.SoundEvent attackSound() {
        return null;
    }

    public net.minecraft.sounds.SoundEvent dashSound() {
        return null;
    }

    public net.minecraft.sounds.SoundEvent specialSound() {
        return null;
    }

    protected net.minecraft.sounds.SoundEvent hurtSound() {
        return null;
    }

    protected net.minecraft.sounds.SoundEvent deathSound() {
        return null;
    }

    public void playPhaseSound(net.minecraft.sounds.SoundEvent sound, float volume) {
        if (sound == null) return;
        playSound(sound, volume, 1.0F);
    }

    @Override
    public void knockback(double power, double xd, double zd) {
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void setTarget(net.minecraft.world.entity.LivingEntity target) {
        if (target instanceof ReaperEntity) return;
        super.setTarget(target);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("SpawnTicks", this.entityData.get(SPAWN_TICKS));
        if (this.gravePos != null) {
            output.putLong("GravePos", this.gravePos.asLong());
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(SPAWN_TICKS, input.getIntOr("SpawnTicks", 0));
        this.gravePos = input.getLong("GravePos").map(net.minecraft.core.BlockPos::of).orElse(null);
        if (hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    private net.minecraft.core.BlockPos gravePos;

    public abstract int phase();

    private static final int REGRESSION_TICKS = 1200;

    private int untouchedTicks = 0;

    public static final double GRAVE_LEASH_RADIUS = 20.0D;

    public static final double GRAVE_CHASE_RADIUS = 48.0D;

    public void setGravePos(net.minecraft.core.BlockPos gravePos) {
        this.gravePos = gravePos == null ? null : gravePos.immutable();
    }

    public net.minecraft.core.BlockPos getGravePos() {
        return this.gravePos;
    }

    public Vec3 graveAnchor() {
        return this.gravePos == null ? null : Vec3.atBottomCenterOf(this.gravePos);
    }

    public boolean isWithinGraveLeash(net.minecraft.world.entity.Entity entity) {
        Vec3 anchor = graveAnchor();
        return anchor == null || entity.distanceToSqr(anchor) <= GRAVE_LEASH_RADIUS * GRAVE_LEASH_RADIUS;
    }

    public boolean isWithinChaseRange(net.minecraft.world.entity.Entity entity) {
        Vec3 anchor = graveAnchor();
        return anchor == null || entity.distanceToSqr(anchor) <= GRAVE_CHASE_RADIUS * GRAVE_CHASE_RADIUS;
    }

    public boolean isOutsideGraveLeash() {
        return !isWithinGraveLeash(this);
    }

    public void despawn(ServerLevel level) {
        if (this.gravePos != null
                && level.getBlockEntity(this.gravePos)
                        instanceof com.zavidvi.voidmod.block.GraveBlockEntity grave) {
            grave.onReaperDespawned();
        }
        discard();
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason == RemovalReason.KILLED && this.gravePos != null
                && this.level() instanceof ServerLevel serverLevel
                && serverLevel.getBlockEntity(this.gravePos)
                        instanceof com.zavidvi.voidmod.block.GraveBlockEntity grave) {
            grave.onReaperRemoved(serverLevel, phase(), this.position(), getTarget());
        }
        super.remove(reason);
    }

    private void tickRegression() {
        if (phase() <= 1 || this.gravePos == null || isSpawning()) return;

        if (getTarget() != null || this.hurtTime > 0) {
            this.untouchedTicks = 0;
            return;
        }

        if (++this.untouchedTicks < REGRESSION_TICKS) return;
        this.untouchedTicks = 0;

        if (this.level() instanceof ServerLevel serverLevel
                && serverLevel.getBlockEntity(this.gravePos)
                        instanceof com.zavidvi.voidmod.block.GraveBlockEntity grave) {
            grave.regressToFirstPhase(serverLevel, this);
        }
    }

    protected static boolean isValidTarget(Player player) {
        return player != null && player.isAlive() && !player.isCreative() && !player.isSpectator();
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

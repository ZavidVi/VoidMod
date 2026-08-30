package com.zavidvi.voidmod.entity.reaper;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

public class ReaperFunnelEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final double RENDER_DISTANCE = 96.0D;

    public static final int SPAWN_TICKS = 10;

    public static final int ERUPT_TICK = 50;

    public static final int LIFETIME = ERUPT_TICK + 10;

    private static final int COLUMN_HEIGHT = 20;

    public static final float COLUMN_DAMAGE = 8.0F;

    private static final RawAnimation ANIM_SPAWN = RawAnimation.begin().thenPlay("spawn");
    private static final RawAnimation ANIM_LOOP = RawAnimation.begin().thenLoop("loop");
    private static final RawAnimation ANIM_END = RawAnimation.begin().thenPlay("end");

    private LivingEntity owner;

    public ReaperFunnelEntity(EntityType<? extends ReaperFunnelEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    public int getAge() {
        return this.tickCount;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) return;

        int age = getAge();
        if (age == ERUPT_TICK) {
            erupt();
        }
        if (age >= LIFETIME) {
            this.discard();
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < RENDER_DISTANCE * RENDER_DISTANCE;
    }

    private void erupt() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        AABB column = new AABB(
                this.getX() - 0.5D, this.getY(), this.getZ() - 0.5D,
                this.getX() + 0.5D, this.getY() + COLUMN_HEIGHT, this.getZ() + 0.5D);

        DamageSource source = this.owner != null
                ? this.damageSources().mobAttack(this.owner)
                : this.damageSources().magic();

        for (LivingEntity victim : serverLevel.getEntitiesOfClass(LivingEntity.class, column,
                e -> e.isAlive() && !(e instanceof ReaperEntity))) {
            victim.hurtServer(serverLevel, source, COLUMN_DAMAGE);
        }

        serverLevel.playSound(null, getX(), getY(), getZ(),
                com.zavidvi.voidmod.registry.ModSounds.REAPER3_PROJECTILE.get(),
                net.minecraft.sounds.SoundSource.HOSTILE, 1.5F, 1.0F);

        spawnFlow(serverLevel);
    }

    private void spawnFlow(ServerLevel level) {
        ReaperEnergyFlowEntity flow = com.zavidvi.voidmod.registry.ModEntities.REAPER_ENERGY_FLOW.get()
                .create(level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (flow == null) return;

        flow.setPos(this.getX(), this.getY(), this.getZ());
        level.addFreshEntity(flow);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<ReaperFunnelEntity>("controller", 0, state -> {
            int age = getAge();
            if (age < SPAWN_TICKS) return state.setAndContinue(ANIM_SPAWN);
            if (age < ERUPT_TICK) return state.setAndContinue(ANIM_LOOP);
            return state.setAndContinue(ANIM_END);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

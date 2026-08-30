package com.zavidvi.voidmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

public class BlackBoneEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final int LIFETIME = 30;

    private static final double RENDER_DISTANCE = 64.0D;

    private static final RawAnimation ANIM = RawAnimation.begin().thenPlay("animation");

    private BlockPos grave;

    public BlackBoneEntity(EntityType<? extends BlackBoneEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setGrave(BlockPos grave) {
        this.grave = grave;
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
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        if (getAge() >= LIFETIME) {
            if (this.grave != null
                    && serverLevel.getBlockEntity(this.grave) instanceof GraveBlockEntity graveEntity) {
                graveEntity.onBoneConsumed(serverLevel);
            }
            this.discard();
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < RENDER_DISTANCE * RENDER_DISTANCE;
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
        controllers.add(new AnimationController<BlackBoneEntity>("controller", 0,
                state -> state.setAndContinue(ANIM)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public BlockEntity graveEntity() {
        return this.grave == null || this.level() == null ? null : this.level().getBlockEntity(this.grave);
    }
}

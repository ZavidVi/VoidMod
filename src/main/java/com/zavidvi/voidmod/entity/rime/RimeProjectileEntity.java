package com.zavidvi.voidmod.entity.rime;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import com.zavidvi.voidmod.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class RimeProjectileEntity extends ThrowableProjectile implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final float DAMAGE = 4.0F;

    public static final int SLOWNESS_TICKS = 40;

    public RimeProjectileEntity(EntityType<? extends RimeProjectileEntity> type, Level level) {
        super(type, level);
    }

    public RimeProjectileEntity(Level level, LivingEntity shooter) {
        this(ModEntities.RIME_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();

        Vec3 motion = this.getDeltaMovement();
        double horizontalDistance = Math.sqrt(motion.x * motion.x + motion.z * motion.z);

        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            this.setYRot((float) (Mth.atan2(motion.x, motion.z) * (180.0F / (float) Math.PI)));
            this.setXRot((float) (Mth.atan2(motion.y, horizontalDistance) * (180.0F / (float) Math.PI)));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        } else {
            float targetYRot = (float) (Mth.atan2(motion.x, motion.z) * (180.0F / (float) Math.PI));
            float targetXRot = (float) (Mth.atan2(motion.y, horizontalDistance) * (180.0F / (float) Math.PI));

            this.setYRot(lerpRotation(this.yRotO, targetYRot));
            this.setXRot(lerpRotation(this.xRotO, targetXRot));
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    this.getX(), this.getY(), this.getZ(), 2, 0.05, 0.05, 0.05, 0.0);
        }
    }

    protected static float lerpRotation(float prev, float current) {
        while (current - prev < -180.0F) {
            prev -= 360.0F;
        }
        while (current - prev >= 180.0F) {
            prev += 360.0F;
        }
        return Mth.lerp(0.2F, prev, current);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide()) return;

        LivingEntity owner = this.getOwner() instanceof LivingEntity living ? living : null;
        result.getEntity().hurt(this.damageSources().mobProjectile(this, owner), DAMAGE);

        if (result.getEntity() instanceof LivingEntity target) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SLOWNESS_TICKS, 0), owner);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                this.getX(), this.getY(), this.getZ(), 8, 0.1, 0.1, 0.1, 0.05);
        this.discard();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

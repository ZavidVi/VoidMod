package com.zavidvi.voidmod.entity.supervoid;

import com.zavidvi.voidmod.entity.vrauj.VraujEntity;
import com.zavidvi.voidmod.registry.ModBlocks;
import com.zavidvi.voidmod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

public class SupervoidShardEntity extends ThrowableItemProjectile implements GeoEntity {
    public static final int PATCH_SIZE = 4;

    public static final float SIZE = 4.0F;

    private static final double SPEED = 1.2;

    public static final float IMPACT_DAMAGE = 10.0F;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private Vec3 aimPoint;

    public SupervoidShardEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public SupervoidShardEntity(Level level, LivingEntity shooter) {
        super(ModEntities.SUPERVOID_SHARD.get(), shooter, level, new ItemStack(Items.BLACK_CONCRETE));
    }

    public SupervoidShardEntity(Level level, double x, double y, double z) {
        super(ModEntities.SUPERVOID_SHARD.get(), x, y, z, level, new ItemStack(Items.BLACK_CONCRETE));
    }

    @Override
    protected Item getDefaultItem() {
        return Items.BLACK_CONCRETE;
    }

    public void aimAt(Vec3 point) {
        this.aimPoint = point;
        Vec3 direction = point.subtract(this.position());
        this.shoot(direction.x, direction.y, direction.z, (float) SPEED, 0.0F);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide() && this.aimPoint != null) {
            Vec3 toAim = this.aimPoint.subtract(this.position());
            double distance = toAim.length();

            if (distance <= SPEED) {
                paintVoid(BlockPos.containing(this.aimPoint));
                this.discard();
                return;
            }

            this.setDeltaMovement(toAim.scale(SPEED / distance));
        }

        super.tick();
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SQUID_INK,
                    this.getX(), this.getY(), this.getZ(), 3, 0.1, 0.1, 0.1, 0.01);
        }
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.aimPoint != null) {
            output.putDouble("AimX", this.aimPoint.x);
            output.putDouble("AimY", this.aimPoint.y);
            output.putDouble("AimZ", this.aimPoint.z);
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        double aimX = input.getDoubleOr("AimX", Double.NaN);
        if (!Double.isNaN(aimX)) {
            this.aimPoint = new Vec3(
                    aimX, input.getDoubleOr("AimY", 0.0), input.getDoubleOr("AimZ", 0.0));
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return !(target instanceof VraujEntity) && super.canHitEntity(target);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide()) return;
        result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), IMPACT_DAMAGE);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide()) return;

        BlockPos landing = result instanceof BlockHitResult blockHit
                ? blockHit.getBlockPos()
                : this.blockPosition();
        paintVoid(landing);
        this.discard();
    }

    private void paintVoid(BlockPos landing) {
        Level level = this.level();
        BlockState voidBlock = ModBlocks.VOID_BLOCK.get().defaultBlockState();
        int half = PATCH_SIZE / 2;

        for (int dx = -half; dx < PATCH_SIZE - half; dx++) {
            for (int dz = -half; dz < PATCH_SIZE - half; dz++) {
                BlockPos.MutableBlockPos cursor = landing.offset(dx, 2, dz).mutable();

                for (int i = 0; i < 6; i++) {
                    BlockState state = level.getBlockState(cursor);
                    if (!state.isAir() && state.getDestroySpeed(level, cursor) >= 0.0F) {
                        level.setBlockAndUpdate(cursor, voidBlock);
                        break;
                    }
                    cursor.move(0, -1, 0);
                }
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

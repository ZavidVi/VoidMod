package com.zavidvi.voidmod.entity.voidsphere;

import com.zavidvi.voidmod.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class VoidSphereProjectileEntity extends ThrowableItemProjectile {
    private Entity directHit = null;

    public VoidSphereProjectileEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public VoidSphereProjectileEntity(Level level, LivingEntity shooter) {
        super(ModEntities.VOID_SPHERE_PROJECTILE.get(), shooter, level, new ItemStack(Items.MAGMA_CREAM));
    }

    @Override
    protected Item getDefaultItem() {
        return Items.MAGMA_CREAM;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            this.level().addParticle(ParticleTypes.SQUID_INK, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        } else if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 4, 0.1, 0.1, 0.1, 0.02);
            serverLevel.sendParticles(net.minecraft.core.particles.PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0F), this.getX(), this.getY(), this.getZ(), 3, 0.05, 0.05, 0.05, 0.01);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY(), this.getZ(), 2, 0.05, 0.05, 0.05, 0.01);
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return !(entity instanceof VoidSphereEntity) && super.canHitEntity(entity);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide()) return;

        Entity target = result.getEntity();
        this.directHit = target;

        if (this.getOwner() instanceof LivingEntity shooter) {
            target.hurt(this.damageSources().mobAttack(shooter), 6.0F);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            Vec3 hitPos = result.getLocation();
            for (LivingEntity living : this.level().getEntitiesOfClass(LivingEntity.class, AABB.ofSize(hitPos, 5.0, 5.0, 5.0))) {
                if (living != this.getOwner() && living != this.directHit && !(living instanceof VoidSphereEntity)) {
                    if (this.getOwner() instanceof LivingEntity shooter) {
                        living.hurt(this.damageSources().mobAttack(shooter), 6.0F);
                    }
                    Vec3 kb = living.position().subtract(hitPos).normalize().scale(0.4).add(0, 0.2, 0);
                    living.setDeltaMovement(living.getDeltaMovement().add(kb));
                    living.hurtMarked = true;
                }
            }
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, hitPos.x, hitPos.y, hitPos.z, 12, 0.6, 0.6, 0.6, 0.02);
                serverLevel.sendParticles(net.minecraft.core.particles.PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0F), hitPos.x, hitPos.y, hitPos.z, 20, 0.8, 0.4, 0.8, 0.03);
            }
            this.discard();
        }
    }
}

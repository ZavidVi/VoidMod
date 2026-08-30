package com.zavidvi.voidmod.entity.vrauj;

import com.zavidvi.voidmod.registry.ModEntities;
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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class VraujProjectileEntity extends ThrowableItemProjectile {
    private float damage = VraujEntity.FAN_PROJECTILE_DAMAGE;

    public VraujProjectileEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public VraujProjectileEntity(Level level, LivingEntity shooter, float damage) {
        super(ModEntities.VRAUJ_PROJECTILE.get(), shooter, level, new ItemStack(Items.MAGMA_CREAM));
        this.damage = damage;
    }

    @Override
    protected Item getDefaultItem() {
        return Items.MAGMA_CREAM;
    }

    public float getDamage() {
        return this.damage;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return !(entity instanceof VraujEntity) && super.canHitEntity(entity);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX(), this.getY(), this.getZ(), 2, 0.05, 0.05, 0.05, 0.01);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide()) return;
        result.getEntity().hurt(this.damageSources().mobProjectile(this, (LivingEntity) this.getOwner()), this.damage);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide()) return;
        this.discard();
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("Damage", this.damage);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.damage = input.getFloatOr("Damage", VraujEntity.FAN_PROJECTILE_DAMAGE);
    }
}

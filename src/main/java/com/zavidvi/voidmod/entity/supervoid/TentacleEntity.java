package com.zavidvi.voidmod.entity.supervoid;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.entity.voidsphere.VoidSphereEntity;
import com.zavidvi.voidmod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

public class TentacleEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation SPAWN_ANIM = RawAnimation.begin().thenPlay("vletel_s_2_nog");

    public static int DESTROY_PORTAL_TICK = 90;
    public static int SPAWN_SPHERES_TICK = 121;

    private int animationTick = 0;
    private boolean frameDestroyed = false;
    private boolean spheresSpawned = false;

    private final List<BlockPos> frameBlocks = new ArrayList<>();

    public TentacleEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public void setFrameBlocks(List<BlockPos> blocks) {
        this.frameBlocks.clear();
        this.frameBlocks.addAll(blocks);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.animationTick = input.getIntOr("animationTick", 0);
        this.frameDestroyed = input.getBooleanOr("frameDestroyed", false);
        this.spheresSpawned = input.getBooleanOr("spheresSpawned", false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("animationTick", this.animationTick);
        output.putBoolean("frameDestroyed", this.frameDestroyed);
        output.putBoolean("spheresSpawned", this.spheresSpawned);
    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel level,
                              net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith(net.minecraft.world.entity.Entity other) {
        return false;
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        this.animationTick++;

        if (!this.level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            if (this.animationTick % 10 == 0 || this.animationTick == DESTROY_PORTAL_TICK) {
                VoidMod.LOGGER.debug("[Tentacle Cutscene] tick {} ({}s)", this.animationTick, this.animationTick / 20.0f);
            }

            if (this.animationTick >= DESTROY_PORTAL_TICK && !this.frameDestroyed) {
                this.frameDestroyed = true;
                VoidMod.LOGGER.debug("[Tentacle Cutscene] destroying portal blocks at tick {}", this.animationTick);
                destroyPortalFrame(serverLevel);
            }

            if (this.animationTick >= SPAWN_SPHERES_TICK && !this.spheresSpawned) {
                this.spheresSpawned = true;
                VoidMod.LOGGER.debug("[Tentacle Cutscene] spawning void spheres at tick {}", this.animationTick);
                spawnVoidSpheres(serverLevel);
            }

            if (this.animationTick >= 170) {
                VoidMod.LOGGER.debug("[Tentacle Cutscene] finished at tick {}, discarding tentacle", this.animationTick);
                this.discard();
            }
        }
    }

    private void destroyPortalFrame(ServerLevel serverLevel) {
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 2.0F, 0.5F);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.2F, 0.7F);

        if (!frameBlocks.isEmpty()) {
            for (BlockPos pos : frameBlocks) {
                if (serverLevel.getBlockState(pos).is(Blocks.NETHER_PORTAL) || serverLevel.getBlockState(pos).is(Blocks.FIRE)) {
                    serverLevel.removeBlock(pos, false);
                }
            }
        } else {
            BlockPos center = this.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, 0, -2), center.offset(2, 5, 2))) {
                BlockState bs = serverLevel.getBlockState(pos);
                if (bs.is(Blocks.NETHER_PORTAL) || bs.is(Blocks.FIRE) || bs.is(Blocks.SOUL_FIRE)) {
                    serverLevel.removeBlock(pos, false);
                }
            }
        }
    }

    private void spawnVoidSpheres(ServerLevel serverLevel) {
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0F, 0.8F);

        net.minecraft.world.entity.player.Player nearestPlayer = serverLevel.getNearestPlayer(this, 32.0);
        BlockPos centerPos = nearestPlayer != null ? nearestPlayer.blockPosition() : this.blockPosition();

        double[][] offsets = {
                {4.0, 3.0, 0.0},
                {-2.0, 3.0, 3.46},
                {-2.0, 3.0, -3.46}
        };

        for (double[] offset : offsets) {
            double spawnX = centerPos.getX() + 0.5 + offset[0];
            double spawnY = centerPos.getY() + offset[1];
            double spawnZ = centerPos.getZ() + 0.5 + offset[2];
            
            VoidSphereEntity sphere = new VoidSphereEntity(ModEntities.VOID_SPHERE.get(), serverLevel);
            sphere.snapTo(spawnX, spawnY, spawnZ, this.getYRot(), 0.0F);
            sphere.setPortalSphere(true);
            sphere.setPersistenceRequired();
            serverLevel.addFreshEntity(sphere);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<TentacleEntity>("controller", 0, state -> state.setAndContinue(SPAWN_ANIM)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

package com.zavidvi.voidmod.entity.wanderer;

import com.zavidvi.voidmod.world.progression.WorldProgressionData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

public class WandererEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    
    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("Idle");
    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("Walk");
    private static final RawAnimation ANIM_HURT = RawAnimation.begin().thenPlay("Hurt");

    public static final double FORGE_LEASH_RADIUS = 10.0;

    private long lastSeenPlayerTime = 0;
    private int hurtTicks = 0;

    private boolean anchoredToForgeTonight = false;

    private BlockPos forgeAnchor = null;

    private BlockPos homeForge = null;

    public WandererEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 80.0D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1,
                new com.zavidvi.voidmod.entity.wanderer.ai.FollowPlayerGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new com.zavidvi.voidmod.entity.wanderer.ai.WanderNearForgeGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.hurtTicks > 0) {
            this.hurtTicks--;
        }

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            WorldProgressionData data = WorldProgressionData.get(serverLevel);
            
            updateForgeAnchor(serverLevel, data);

            if (!data.isWandererTalked()) {
                Player nearestPlayer = serverLevel.getNearestPlayer(this, -1);
                if (nearestPlayer != null && this.distanceToSqr(nearestPlayer) <= 128 * 128) {
                    lastSeenPlayerTime = serverLevel.getGameTime();
                }
                
                if (serverLevel.getGameTime() - lastSeenPlayerTime > 6000) {
                    data.setNextWandererSpawnTime(serverLevel.getGameTime() + 24000L + serverLevel.getRandom().nextInt(48000));
                    this.discard();
                }
            } else {
                this.setPersistenceRequired();
            }
        }
    }

    private void updateForgeAnchor(ServerLevel serverLevel, WorldProgressionData data) {
        if (this.homeForge != null) {
            if (forgeStandsAt(serverLevel, this.homeForge)) {
                this.forgeAnchor = this.homeForge;
                this.anchoredToForgeTonight = true;
                return;
            }
            this.homeForge = null;
        }

        if (!serverLevel.isDarkOutside()) {
            this.anchoredToForgeTonight = false;
            this.forgeAnchor = null;
            return;
        }

        BlockPos forgePos = data.getForgePosition();
        if (forgePos == null) {
            this.forgeAnchor = null;
            return;
        }

        if (!this.anchoredToForgeTonight) {
            double leashSqr = FORGE_LEASH_RADIUS * FORGE_LEASH_RADIUS;
            if (this.distanceToSqr(net.minecraft.world.phys.Vec3.atBottomCenterOf(forgePos)) > leashSqr) {
                this.teleportTo(forgePos.getX() + 0.5, forgePos.getY() + 1, forgePos.getZ() + 0.5);
            }
            this.anchoredToForgeTonight = true;
        }

        this.forgeAnchor = forgePos;
    }

    public BlockPos getForgeAnchor() {
        return this.forgeAnchor;
    }

    public boolean isBoundToForge() {
        return this.homeForge != null;
    }

    public void placeHomeForge(ServerLevel level) {
        if (this.homeForge != null && forgeStandsAt(level, this.homeForge)) return;

        BlockPos pos = this.blockPosition();
        if (!level.getBlockState(pos).canBeReplaced()) return;

        level.setBlockAndUpdate(pos,
                com.zavidvi.voidmod.registry.ModBlocks.OTHERWORLDLY_FORGE.get().defaultBlockState());

        WorldProgressionData data = WorldProgressionData.get(level);
        data.addForgePosition(pos.immutable());
        if (data.getForgePosition() == null || !forgeStandsAt(level, data.getForgePosition())) {
            data.setForgePosition(pos.immutable());
        }

        this.homeForge = pos.immutable();
        this.forgeAnchor = this.homeForge;
        this.setPersistenceRequired();

        stepAsideFrom(level, pos);
    }

    private static boolean forgeStandsAt(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getBlock()
                == com.zavidvi.voidmod.registry.ModBlocks.OTHERWORLDLY_FORGE.get();
    }

    private void stepAsideFrom(ServerLevel level, BlockPos forgePos) {
        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            BlockPos side = forgePos.relative(direction);
            if (level.getBlockState(side).canBeReplaced()
                    && level.getBlockState(side.above()).canBeReplaced()
                    && !level.getBlockState(side.below()).canBeReplaced()) {
                this.teleportTo(side.getX() + 0.5D, side.getY(), side.getZ() + 0.5D);
                return;
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        this.hurtTicks = 10;
        return super.hurtServer(level, source, amount);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            WorldProgressionData data = WorldProgressionData.get(serverLevel);
            if (!data.isWandererTalked()) {
                data.setNextWandererSpawnTime(serverLevel.getGameTime() + 24000L);
            }
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer, new com.zavidvi.voidmod.network.OpenWandererDialoguePayload());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.lastSeenPlayerTime = input.getLongOr("LastSeenPlayerTime", 0L);
        this.anchoredToForgeTonight = input.getBooleanOr("AnchoredToForgeTonight", false);
        this.homeForge = input.getLong("HomeForge").map(BlockPos::of).orElse(null);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putLong("LastSeenPlayerTime", this.lastSeenPlayerTime);
        output.putBoolean("AnchoredToForgeTonight", this.anchoredToForgeTonight);
        if (this.homeForge != null) {
            output.putLong("HomeForge", this.homeForge.asLong());
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<WandererEntity>("controller", 5, state -> {
            if (this.hurtTicks > 0) {
                return state.setAndContinue(ANIM_HURT);
            }
            if (state.isMoving()) {
                return state.setAndContinue(ANIM_WALK);
            }
            return state.setAndContinue(ANIM_IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

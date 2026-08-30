package com.zavidvi.voidmod.block;

import com.zavidvi.voidmod.entity.reaper.ReaperEntity;
import com.zavidvi.voidmod.registry.ModBlockEntities;
import com.zavidvi.voidmod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

public class GraveBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final int STAGE_IDLE = 0;

    private static final long FADE_DELAY = 24000L;

    private static final int RESKIN_RADIUS = 2;

    private static final net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.structure.Structure> GRAVE_STRUCTURE =
            net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.STRUCTURE,
                    net.minecraft.resources.Identifier.fromNamespaceAndPath(
                            com.zavidvi.voidmod.VoidMod.MOD_ID, "grave"));

    private static final double FRONT_OFFSET = 1.0D;

    private static final double AT_GRAVE_RADIUS = 4.0D;

    private static final int GRAVE_MODEL_HEIGHT = 2;

    private static final java.util.Map<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.Block> GRAVE_TERRAIN =
            java.util.Map.of(
                    net.minecraft.world.level.block.Blocks.GRASS_BLOCK, com.zavidvi.voidmod.registry.ModBlocks.GRAVE_GRASS_BLOCK.get(),
                    net.minecraft.world.level.block.Blocks.PODZOL, com.zavidvi.voidmod.registry.ModBlocks.GRAVE_PODZOL.get(),
                    net.minecraft.world.level.block.Blocks.STONE, com.zavidvi.voidmod.registry.ModBlocks.GRAVE_STONE.get(),
                    net.minecraft.world.level.block.Blocks.SAND, com.zavidvi.voidmod.registry.ModBlocks.GRAVE_SAND.get(),
                    net.minecraft.world.level.block.Blocks.RED_SAND, com.zavidvi.voidmod.registry.ModBlocks.GRAVE_RED_SAND.get(),
                    net.minecraft.world.level.block.Blocks.SNOW_BLOCK, com.zavidvi.voidmod.registry.ModBlocks.GRAVE_SNOW.get(),
                    net.minecraft.world.level.block.Blocks.DIRT, com.zavidvi.voidmod.registry.ModBlocks.GRAVE_DIRT.get());

    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ANIM_PHASE_1 = RawAnimation.begin().thenLoop("phase_1");
    private static final RawAnimation ANIM_PHASE_2 = RawAnimation.begin().thenLoop("phase_2");
    private static final RawAnimation ANIM_PHASE_3 = RawAnimation.begin().thenLoop("phase_3");

    private int stage = STAGE_IDLE;

    private boolean spent = false;

    private long fadeAt = 0L;

    private boolean reskinned = false;

    private boolean partsPlaced = false;

    private boolean boneInFlight = false;

    public GraveBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.GRAVE.get(), pos, blockState);
    }

    public int getStage() {
        return this.stage;
    }

    public boolean isSpent() {
        return this.spent;
    }

    public boolean isBusy() {
        return this.stage != STAGE_IDLE || this.boneInFlight;
    }

    public boolean useBlackBone(ServerLevel level) {
        if (isBusy()) return false;

        BlackBoneEntity bone = ModEntities.BLACK_BONE.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (bone == null) return false;

        Vec3 boneAt = bonePos();
        bone.setPos(boneAt.x, boneAt.y, boneAt.z);
        bone.setGrave(this.worldPosition);
        level.addFreshEntity(bone);

        this.boneInFlight = true;
        this.spent = false;
        this.fadeAt = 0L;
        sync();
        return true;
    }

    public void onBoneConsumed(ServerLevel level) {
        spawnPhase(level, 1, reaperSpawnPos(), null);
    }

    public Vec3 reaperSpawnPos() {
        net.minecraft.core.Direction front = graveFacing();

        return new Vec3(
                this.worldPosition.getX() + 0.5D + front.getStepX() * FRONT_OFFSET,
                this.worldPosition.getY(),
                this.worldPosition.getZ() + 0.5D + front.getStepZ() * FRONT_OFFSET);
    }

    private net.minecraft.core.Direction graveFacing() {
        return getBlockState().hasProperty(GraveBlock.FACING)
                ? getBlockState().getValue(GraveBlock.FACING)
                : net.minecraft.core.Direction.NORTH;
    }

    private Vec3 bonePos() {
        return new Vec3(
                this.worldPosition.getX() + 0.5D,
                this.worldPosition.getY() + GRAVE_MODEL_HEIGHT,
                this.worldPosition.getZ() + 0.5D);
    }

    public void onReaperRemoved(ServerLevel level, int phase, Vec3 at,
                                net.minecraft.world.entity.LivingEntity target) {
        if (phase < 3) {
            spawnPhase(level, phase + 1, at, target);
            return;
        }

        this.stage = STAGE_IDLE;
        this.boneInFlight = false;
        this.fadeAt = level.getGameTime() + FADE_DELAY;
        sync();
    }

    public void onReaperDespawned() {
        this.stage = STAGE_IDLE;
        this.boneInFlight = false;
        this.spent = false;
        this.fadeAt = 0L;
        sync();
    }

    public void regressToFirstPhase(ServerLevel level, ReaperEntity reaper) {
        reaper.discard();

        spawnPhase(level, 1, reaperSpawnPos(), null);
    }

    private void spawnPhase(ServerLevel level, int phase, Vec3 at,
                            net.minecraft.world.entity.LivingEntity inherited) {
        EntityType<? extends ReaperEntity> type = switch (phase) {
            case 2 -> ModEntities.REAPER_LVL2.get();
            case 3 -> ModEntities.REAPER_LVL3.get();
            default -> ModEntities.REAPER_LVL1.get();
        };

        ReaperEntity reaper = type.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (reaper == null) return;

        reaper.setPos(at.x, at.y, at.z);
        reaper.setGravePos(this.worldPosition);
        reaper.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(at)),
                EntitySpawnReason.MOB_SUMMONED, null);
        level.addFreshEntity(reaper);
        handOverTarget(level, reaper, inherited, at);
        faceOnSpawn(reaper, at);

        this.stage = phase;
        this.boneInFlight = false;
        this.fadeAt = 0L;
        sync();
    }

    private void faceOnSpawn(ReaperEntity reaper, Vec3 at) {
        double fromGraveX = at.x - (this.worldPosition.getX() + 0.5D);
        double fromGraveZ = at.z - (this.worldPosition.getZ() + 0.5D);
        boolean atGrave = fromGraveX * fromGraveX + fromGraveZ * fromGraveZ
                <= AT_GRAVE_RADIUS * AT_GRAVE_RADIUS;

        net.minecraft.world.entity.LivingEntity target = reaper.getTarget();

        double dx;
        double dz;
        if (atGrave || target == null) {
            dx = fromGraveX;
            dz = fromGraveZ;
        } else {
            dx = target.getX() - at.x;
            dz = target.getZ() - at.z;
        }
        if (dx * dx + dz * dz < 1.0E-4D) return;

        float yaw = net.minecraft.util.Mth.wrapDegrees(
                (float) (net.minecraft.util.Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F);

        reaper.snapTo(at.x, at.y, at.z, yaw, 0.0F);
        reaper.setYHeadRot(yaw);
        reaper.yHeadRotO = yaw;
        reaper.setYBodyRot(yaw);
        reaper.yBodyRotO = yaw;
    }

    private void handOverTarget(ServerLevel level, ReaperEntity reaper,
                                net.minecraft.world.entity.LivingEntity inherited, Vec3 at) {
        if (inherited != null && inherited.isAlive() && reaper.isWithinChaseRange(inherited)) {
            reaper.setTarget(inherited);
            return;
        }

        net.minecraft.world.entity.player.Player nearest = level.getNearestPlayer(
                at.x, at.y, at.z, ReaperEntity.GRAVE_LEASH_RADIUS, true);
        if (nearest != null && reaper.isWithinGraveLeash(nearest)) {
            reaper.setTarget(nearest);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GraveBlockEntity grave) {
        if (!grave.reskinned && level instanceof ServerLevel serverLevel) {
            grave.reskinned = true;
            reskinTerrain(serverLevel, pos);
            grave.setChanged();
        }

        if (!grave.partsPlaced && level instanceof ServerLevel serverLevel) {
            grave.partsPlaced = true;
            placeParts(serverLevel, pos, state);
            grave.setChanged();
        }

        if (grave.fadeAt == 0L || level.getGameTime() < grave.fadeAt) return;

        grave.fadeAt = 0L;
        grave.spent = true;
        grave.sync();
    }

    private static void placeParts(ServerLevel level, BlockPos centre, BlockState grave) {
        net.minecraft.core.Direction side = grave.hasProperty(GraveBlock.FACING)
                ? grave.getValue(GraveBlock.FACING).getClockWise()
                : net.minecraft.core.Direction.EAST;

        for (int offset = -GraveBlock.PART_SIDE_REACH; offset <= GraveBlock.PART_SIDE_REACH; offset++) {
            for (int up = 0; up < GraveBlock.PART_HEIGHT; up++) {
                if (offset == 0 && up == 0) continue;

                BlockPos pos = centre.offset(side.getStepX() * offset, up, side.getStepZ() * offset);
                if (!level.getBlockState(pos).canBeReplaced()) continue;

                level.setBlock(pos, GravePartBlock.stateFor(grave, offset, up > 0), 3);
            }
        }
    }

    private static void reskinTerrain(ServerLevel level, BlockPos centre) {
        net.minecraft.world.level.levelgen.structure.BoundingBox area = reskinArea(level, centre);

        for (BlockPos pos : BlockPos.betweenClosed(area.minX(), area.minY(), area.minZ(),
                area.maxX(), area.maxY(), area.maxZ())) {
            if (pos.equals(centre)) continue;

            net.minecraft.world.level.block.Block replacement = GRAVE_TERRAIN.get(level.getBlockState(pos).getBlock());
            if (replacement != null) {
                level.setBlock(pos.immutable(), replacement.defaultBlockState(), 3);
            }
        }
    }

    private static net.minecraft.world.level.levelgen.structure.BoundingBox reskinArea(
            ServerLevel level, BlockPos centre) {
        net.minecraft.world.level.levelgen.structure.Structure structure =
                level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                        .getValue(GRAVE_STRUCTURE);

        if (structure != null) {
            net.minecraft.world.level.levelgen.structure.StructureStart start =
                    level.structureManager().getStructureAt(centre, structure);
            if (start.isValid()) {
                return start.getBoundingBox().inflatedBy(RESKIN_RADIUS);
            }
        }

        return net.minecraft.world.level.levelgen.structure.BoundingBox.fromCorners(
                centre.offset(-RESKIN_RADIUS, -RESKIN_RADIUS, -RESKIN_RADIUS),
                centre.offset(RESKIN_RADIUS, RESKIN_RADIUS, RESKIN_RADIUS));
    }

    private void sync() {
        setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Stage", this.stage);
        output.putBoolean("Spent", this.spent);
        output.putLong("FadeAt", this.fadeAt);
        output.putBoolean("Reskinned", this.reskinned);
        output.putBoolean("PartsPlaced", this.partsPlaced);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.stage = input.getIntOr("Stage", STAGE_IDLE);
        this.spent = input.getBooleanOr("Spent", false);
        this.fadeAt = input.getLongOr("FadeAt", 0L);
        this.reskinned = input.getBooleanOr("Reskinned", false);
        this.partsPlaced = input.getBooleanOr("PartsPlaced", false);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<GraveBlockEntity>("controller", 0, state -> switch (this.stage) {
            case 1 -> state.setAndContinue(ANIM_PHASE_1);
            case 2 -> state.setAndContinue(ANIM_PHASE_2);
            case 3 -> state.setAndContinue(ANIM_PHASE_3);
            default -> state.setAndContinue(ANIM_IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

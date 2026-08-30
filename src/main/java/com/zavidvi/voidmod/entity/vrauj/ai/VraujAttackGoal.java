package com.zavidvi.voidmod.entity.vrauj.ai;

import com.zavidvi.voidmod.entity.vrauj.VraujEntity;
import com.zavidvi.voidmod.entity.vrauj.VraujProjectileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class VraujAttackGoal extends Goal {
    private static final int LOOP_ANIM_TICKS = 20;

    private static final int LOOP_TICKS = LOOP_ANIM_TICKS + VraujEntity.ANIM_TRANSITION_TICKS;

    private static final int MAX_DIVE_TICKS = 100;

    private static final int MIN_DIVE_TICKS = 3;

    private static final double MISS_DEPTH = 10.0;

    private static final int FAN_FIRST_VOLLEY_START = 20;
    private static final int FAN_FIRST_VOLLEY_END = 60;
    private static final int FAN_SECOND_VOLLEY_START = 80;
    private static final int FAN_SECOND_VOLLEY_END = 120;

    private static final int FAN_ASCENT_START = 147;

    private static final int RASSTREL_LENGTH = 150;

    private static final int FAN_INTERVAL = 10;

    private static final double FAN_RADIUS = 2.0;

    private static final int FAN_PROJECTILES = 6;

    private static final double FAN_LIFTOFF_HEIGHT = 15.0;

    private static final double TAIL_OFFSET_Y = 27.0 / 16.0;

    private static final int RASKID_BLOCK_GONE_TICK = 131 / VraujEntity.RASKID_SPEEDUP;

    private static final int RASKID_ASCENT_START = 134 / VraujEntity.RASKID_SPEEDUP;

    private static final int RASKID_SHOOT_TICK = 167 / VraujEntity.RASKID_SPEEDUP;
    private static final int RASKID_LENGTH = 180 / VraujEntity.RASKID_SPEEDUP;

    private static final double RASKID_LIFTOFF_HEIGHT = 10.0;

    private static final int RASSTREL_WEIGHT = 2;
    private static final int RASKID_WEIGHT = 1;

    private static final int COOLDOWN_MIN = 60;
    private static final int COOLDOWN_SPREAD = 60;

    private static final int STAGGER_MIN = 20;
    private static final int STAGGER_SPREAD = 40;

    private static final double SWARM_SPACING_RADIUS = 32.0;

    private static final double DIVE_OFFSET = 3.0;

    private static final double POSITION_HORIZONTAL_SLACK = 8.0;
    private static final double POSITION_HEIGHT_SLACK = 6.0;

    private static final double DIVE_SPEED = 1.2;

    private static final double ASCENT_SPEED = 1.5;

    private static final int MAX_ASCENT_TICKS = 60;

    private static final double ALIGN_SPEED = 0.5;

    private static final double ALIGN_TOLERANCE = 0.5;

    private final VraujEntity vrauj;

    private int cooldown;
    private int loopTicks;
    private int diveTicks;
    private int ticks;
    private boolean planted;
    private boolean aborted;
    private boolean rasstrel;
    private boolean ascending;
    private double ascentTargetY;
    private int ascentTicks;
    private double columnX;
    private double columnZ;
    private BlockPos absorbedBlock;
    private BlockState absorbedState;

    public VraujAttackGoal(VraujEntity vrauj) {
        this.vrauj = vrauj;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        this.cooldown = COOLDOWN_MIN + vrauj.getRandom().nextInt(COOLDOWN_SPREAD);
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.vrauj.getTarget();
        if (target == null || !target.isAlive()) return false;
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        if (!inAttackPosition(target)) return false;

        if (isNeighbourAttacking()) {
            this.cooldown = STAGGER_MIN + this.vrauj.getRandom().nextInt(STAGGER_SPREAD);
            return false;
        }
        return true;
    }

    private boolean inAttackPosition(LivingEntity target) {
        double height = this.vrauj.getY() - target.getY();
        if (height < 0.0 || height > VraujHoverOverTargetGoal.HOVER_HEIGHT + POSITION_HEIGHT_SLACK) {
            return false;
        }

        double dx = this.vrauj.getX() - target.getX();
        double dz = this.vrauj.getZ() - target.getZ();
        return dx * dx + dz * dz <= POSITION_HORIZONTAL_SLACK * POSITION_HORIZONTAL_SLACK;
    }

    private List<VraujEntity> neighbours() {
        return this.vrauj.level().getEntitiesOfClass(VraujEntity.class,
                AABB.ofSize(this.vrauj.position(), SWARM_SPACING_RADIUS, SWARM_SPACING_RADIUS, SWARM_SPACING_RADIUS),
                other -> other != this.vrauj && other.isAlive());
    }

    private boolean isNeighbourAttacking() {
        for (VraujEntity other : neighbours()) {
            if (other.isAttacking()) return true;
        }
        return false;
    }

    private boolean pickAttack() {
        int rasstrelCount = 0;
        int raskidCount = 0;
        for (VraujEntity other : neighbours()) {
            int last = other.getLastAttackState();
            if (last == VraujEntity.ANIM_STATE_RASSTREL) {
                rasstrelCount++;
            } else if (last == VraujEntity.ANIM_STATE_RASKID) {
                raskidCount++;
            }
        }

        int rasstrelShare = rasstrelCount * RASKID_WEIGHT;
        int raskidShare = raskidCount * RASSTREL_WEIGHT;
        if (rasstrelShare != raskidShare) return rasstrelShare < raskidShare;

        return this.vrauj.getRandom().nextInt(RASSTREL_WEIGHT + RASKID_WEIGHT) < RASSTREL_WEIGHT;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.aborted) return false;

        LivingEntity target = this.vrauj.getTarget();
        if (target == null || !target.isAlive()) return false;

        if (!this.planted) return true;
        return this.ticks < (this.rasstrel ? RASSTREL_LENGTH : RASKID_LENGTH);
    }

    @Override
    public void start() {
        this.loopTicks = 0;
        this.diveTicks = 0;
        this.ticks = 0;
        this.planted = false;
        this.aborted = false;
        this.absorbedBlock = null;
        this.absorbedState = null;
        this.rasstrel = pickAttack();
        endAscent();
        this.vrauj.setLastAttackState(this.rasstrel
                ? VraujEntity.ANIM_STATE_RASSTREL
                : VraujEntity.ANIM_STATE_RASKID);

        aimColumn(this.vrauj.getTarget());
        this.vrauj.setAnimationState(VraujEntity.ANIM_STATE_ATTACK);
    }

    @Override
    public void stop() {
        this.vrauj.setAnimationState(VraujEntity.ANIM_STATE_FLY);
        endAscent();
        this.cooldown = COOLDOWN_MIN + this.vrauj.getRandom().nextInt(COOLDOWN_SPREAD);
        this.absorbedBlock = null;
        this.absorbedState = null;
        this.planted = false;
    }

    @Override
    public void tick() {
        LivingEntity target = this.vrauj.getTarget();
        if (target == null) return;

        this.vrauj.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (!this.planted) {
            if (this.loopTicks < LOOP_TICKS) {
                this.loopTicks++;
                loop();
                if (this.loopTicks == LOOP_TICKS) beginDive();
                return;
            }
            this.diveTicks++;
            dive(target);
            return;
        }

        if (this.ascending) {
            tickAscent();
            return;
        }

        this.ticks++;
        if (this.rasstrel) {
            tickRasstrel(this.ticks);
        } else {
            tickRaskid(this.ticks, target);
        }
    }

    private void loop() {
        double dx = this.columnX - this.vrauj.getX();
        double dz = this.columnZ - this.vrauj.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance < 1.0E-4) {
            this.vrauj.setDeltaMovement(Vec3.ZERO);
            return;
        }

        double speed = Math.min(ALIGN_SPEED, distance);
        this.vrauj.setDeltaMovement(dx / distance * speed, 0.0, dz / distance * speed);
    }

    private void beginDive() {
        if (!alignedWithColumn()) {
            this.columnX = Mth.floor(this.vrauj.getX()) + 0.5;
            this.columnZ = Mth.floor(this.vrauj.getZ()) + 0.5;
        }

        this.vrauj.setPos(this.columnX, this.vrauj.getY(), this.columnZ);
        this.vrauj.setDeltaMovement(0.0, -DIVE_SPEED, 0.0);
    }

    private boolean alignedWithColumn() {
        double dx = this.columnX - this.vrauj.getX();
        double dz = this.columnZ - this.vrauj.getZ();
        return dx * dx + dz * dz <= ALIGN_TOLERANCE * ALIGN_TOLERANCE;
    }

    private void dive(LivingEntity target) {
        BlockPos floor = this.diveTicks > MIN_DIVE_TICKS ? floorBelow() : null;
        if (floor != null) {
            plant(floor);
            return;
        }

        if (this.vrauj.getY() < target.getY() - MISS_DEPTH || this.diveTicks > MAX_DIVE_TICKS) {
            this.aborted = true;
                this.vrauj.setDeltaMovement(Vec3.ZERO);
            return;
        }

        this.vrauj.setDeltaMovement(0.0, -DIVE_SPEED, 0.0);
    }

    private BlockPos floorBelow() {
        Level level = this.vrauj.level();
        int top = Mth.floor(this.vrauj.getY() - 0.2);
        int bottom = Mth.floor(this.vrauj.getY() - 0.2 - DIVE_SPEED);

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(
                Mth.floor(this.vrauj.getX()), top, Mth.floor(this.vrauj.getZ()));
        for (int y = top; y >= bottom; y--) {
            cursor.setY(y);
            if (isFloor(level, cursor)) return cursor.immutable();
        }
        return null;
    }

    private static boolean isFloor(Level level, BlockPos pos) {
        return !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    private void plant(BlockPos floor) {
        this.planted = true;
        this.ticks = 0;
        this.absorbedBlock = floor;

        this.vrauj.setPos(floor.getX() + 0.5, floor.getY() + 1.0, floor.getZ() + 0.5);
        this.vrauj.setDeltaMovement(Vec3.ZERO);

        this.vrauj.setAnimationState(this.rasstrel
                ? VraujEntity.ANIM_STATE_RASSTREL
                : VraujEntity.ANIM_STATE_RASKID);
    }

    private void tickRasstrel(int t) {
        this.vrauj.setDeltaMovement(Vec3.ZERO);

        if (t == FAN_ASCENT_START) {
            beginAscent(FAN_LIFTOFF_HEIGHT);
            return;
        }

        if (t == FAN_FIRST_VOLLEY_START) {
            this.vrauj.playSound(com.zavidvi.voidmod.registry.ModSounds.VRAUJ_SHOOT.get(), 1.5F, 1.0F);
        }

        boolean firstVolley = t >= FAN_FIRST_VOLLEY_START && t <= FAN_FIRST_VOLLEY_END;
        boolean secondVolley = t >= FAN_SECOND_VOLLEY_START && t <= FAN_SECOND_VOLLEY_END;

        if ((firstVolley || secondVolley) && (t - FAN_FIRST_VOLLEY_START) % FAN_INTERVAL == 0) {
            sprayFan();
        }
    }

    private void beginAscent(double height) {
        this.ascending = true;
        this.ascentTicks = 0;
        this.ascentTargetY = this.vrauj.getY() + height;
        this.vrauj.setAnimationHeld(true);
        this.vrauj.setDeltaMovement(0.0, ASCENT_SPEED, 0.0);
    }

    private void tickAscent() {
        double remaining = this.ascentTargetY - this.vrauj.getY();

        if (remaining <= 0.0 || ++this.ascentTicks > MAX_ASCENT_TICKS) {
            endAscent();
            return;
        }

        this.vrauj.setDeltaMovement(0.0, Math.min(ASCENT_SPEED, remaining), 0.0);
    }

    private void endAscent() {
        this.ascending = false;
        this.ascentTicks = 0;
        this.vrauj.setAnimationHeld(false);
        this.vrauj.setDeltaMovement(Vec3.ZERO);
    }

    private void sprayFan() {
        Level level = this.vrauj.level();
        if (level.isClientSide()) return;

        float base = this.vrauj.getRandom().nextFloat() * Mth.TWO_PI;
        for (int i = 0; i < FAN_PROJECTILES; i++) {
            double angle = base + i * (Mth.TWO_PI / FAN_PROJECTILES);

            VraujProjectileEntity projectile = new VraujProjectileEntity(
                    level, this.vrauj, VraujEntity.FAN_PROJECTILE_DAMAGE);
            projectile.setPos(this.vrauj.getX(), this.vrauj.getY() + TAIL_OFFSET_Y, this.vrauj.getZ());
            projectile.shoot(Math.cos(angle) * FAN_RADIUS, 0.35, Math.sin(angle) * FAN_RADIUS,
                    0.45F, 4.0F);
            level.addFreshEntity(projectile);
        }
    }

    private void tickRaskid(int t, LivingEntity target) {
        this.vrauj.setDeltaMovement(Vec3.ZERO);

        if (t == RASKID_BLOCK_GONE_TICK) {
            absorbBlock();
            return;
        }

        if (t == RASKID_ASCENT_START) {
            beginAscent(RASKID_LIFTOFF_HEIGHT);
            return;
        }

        if (t == RASKID_SHOOT_TICK) {
            shootAt(target);
        }
    }

    private void absorbBlock() {
        Level level = this.vrauj.level();
        if (level.isClientSide() || this.absorbedBlock == null) return;

        var state = level.getBlockState(this.absorbedBlock);
        if (state.isAir() || state.getDestroySpeed(level, this.absorbedBlock) < 0.0F) return;
        if (state.is(com.zavidvi.voidmod.registry.ModTags.VRAUJ_CANNOT_ABSORB)) return;

        this.absorbedState = state;
        level.destroyBlock(this.absorbedBlock, false, this.vrauj);
        level.setBlockAndUpdate(this.absorbedBlock, Blocks.AIR.defaultBlockState());
    }

    private void shootAt(LivingEntity target) {
        Level level = this.vrauj.level();
        if (level.isClientSide()) return;

        VraujProjectileEntity projectile = new VraujProjectileEntity(
                level, this.vrauj, VraujEntity.SINGLE_PROJECTILE_DAMAGE);

        if (this.absorbedState != null) {
            Item item = this.absorbedState.getBlock().asItem();
            if (item != Items.AIR) {
                projectile.setItem(new ItemStack(item));
            }
        }

        projectile.setPos(this.vrauj.getX(), this.vrauj.getY() + 0.5, this.vrauj.getZ());

        double dx = target.getX() - this.vrauj.getX();
        double dy = target.getY(0.5) - (this.vrauj.getY() + 0.5);
        double dz = target.getZ() - this.vrauj.getZ();
        projectile.shoot(dx, dy, dz, 1.2F, 1.0F);
        level.addFreshEntity(projectile);
    }

    private void aimColumn(LivingEntity target) {
        if (target == null) {
            this.columnX = this.vrauj.getX();
            this.columnZ = this.vrauj.getZ();
            return;
        }

        double dx = this.vrauj.getX() - target.getX();
        double dz = this.vrauj.getZ() - target.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        if (horizontal < 1.0E-4) {
            double random = this.vrauj.getRandom().nextFloat() * Mth.TWO_PI;
            dx = Math.cos(random);
            dz = Math.sin(random);
            horizontal = 1.0;
        }

        this.columnX = Mth.floor(target.getX() + dx / horizontal * DIVE_OFFSET) + 0.5;
        this.columnZ = Mth.floor(target.getZ() + dz / horizontal * DIVE_OFFSET) + 0.5;
    }
}

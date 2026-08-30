package com.zavidvi.voidmod.block;

import com.mojang.serialization.MapCodec;
import com.zavidvi.voidmod.world.progression.WorldProgressionData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OtherworldlyForgeBlock extends BaseEntityBlock {
    public OtherworldlyForgeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(OtherworldlyForgeBlock::new);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OtherworldlyForgeBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            WorldProgressionData data = WorldProgressionData.get(serverLevel);
            data.addForgePosition(pos);

            BlockPos bound = data.getForgePosition();
            if (bound == null || !forgeStandsAt(serverLevel, bound)) {
                data.setForgePosition(pos.immutable());
            }
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        WorldProgressionData data = WorldProgressionData.get(level);
        data.removeForgePosition(pos);
        if (pos.equals(data.getForgePosition())) {
            data.setForgePosition(findNewBinding(level, data, pos));
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    private static boolean forgeStandsAt(ServerLevel level, BlockPos pos) {
        return !level.isLoaded(pos) || level.getBlockState(pos).getBlock() instanceof OtherworldlyForgeBlock;
    }

    @Nullable
    private static BlockPos findNewBinding(ServerLevel level, WorldProgressionData data, BlockPos brokenPos) {
        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (BlockPos candidate : List.copyOf(data.getForgePositions())) {
            if (candidate.equals(brokenPos)) continue;
            if (level.isLoaded(candidate) && !(level.getBlockState(candidate).getBlock() instanceof OtherworldlyForgeBlock)) {
                data.removeForgePosition(candidate);
                continue;
            }
            double distance = candidate.distSqr(brokenPos);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = candidate;
            }
        }
        return nearest;
    }
}

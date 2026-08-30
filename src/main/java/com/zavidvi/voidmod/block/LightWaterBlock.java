package com.zavidvi.voidmod.block;

import com.zavidvi.voidmod.registry.ModItems;
import com.zavidvi.voidmod.world.curse.LightWaterPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

import javax.annotation.Nullable;

public class LightWaterBlock extends LiquidBlock {
    public LightWaterBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public ItemStack pickupBlock(@Nullable net.minecraft.world.entity.LivingEntity user, LevelAccessor level, BlockPos pos, BlockState state) {
        if (state.getValue(LEVEL) == 0
                && level instanceof Level realLevel
                && LightWaterPlacement.isFountainSource(realLevel, pos)) {
            return new ItemStack(ModItems.LIGHT_WATER_BUCKET.get());
        }
        return super.pickupBlock(user, level, pos, state);
    }
}

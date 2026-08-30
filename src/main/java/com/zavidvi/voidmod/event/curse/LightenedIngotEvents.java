package com.zavidvi.voidmod.event.curse;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.block.PaleCauldronBlock;
import com.zavidvi.voidmod.registry.ModFluids;
import com.zavidvi.voidmod.registry.ModItems;
import com.zavidvi.voidmod.world.curse.LightWaterConversions;
import com.zavidvi.voidmod.world.curse.LightWaterPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = VoidMod.MOD_ID)
public class LightenedIngotEvents {
    @SubscribeEvent
    public static void onItemTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity item)) return;
        if (!(item.level() instanceof ServerLevel level)) return;

        ItemStack stack = item.getItem();
        if (!stack.is(ModItems.PALE_INGOT.get())) return;

        BlockPos pos = item.blockPosition();

        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof PaleCauldronBlock) {
            convertInCauldron(level, item, stack, pos, state);
            return;
        }

        FluidState fluid = level.getFluidState(pos);
        if (!fluid.isSourceOfType(ModFluids.LIGHT_WATER.get())) return;

        convertInWater(level, item, stack, pos);
    }

    private static void convertInWater(ServerLevel level, ItemEntity item, ItemStack stack, BlockPos pos) {
        if (LightWaterPlacement.isFountainSource(level, pos)) {
            convert(level, item, stack, pos, stack.getCount());
            return;
        }

        LightWaterConversions conversions = LightWaterConversions.get(level);
        int remaining = conversions.remaining(pos);
        if (remaining <= 0) {
            exhaust(level, conversions, pos);
            return;
        }

        int converted = convert(level, item, stack, pos, remaining);
        if (conversions.spend(pos, converted) <= 0) {
            exhaust(level, conversions, pos);
        }
    }

    private static void convertInCauldron(ServerLevel level, ItemEntity item, ItemStack stack,
                                          BlockPos pos, BlockState state) {
        int remaining = PaleCauldronBlock.remainingCharges(state);
        if (remaining <= 0) return;

        int converted = convert(level, item, stack, pos, remaining);
        PaleCauldronBlock.spendCharges(level, pos, state, converted);
    }

    private static int convert(ServerLevel level, ItemEntity item, ItemStack stack,
                               BlockPos pos, int remaining) {
        int converted = Math.min(stack.getCount(), remaining);
        int leftover = stack.getCount() - converted;

        item.setItem(new ItemStack(ModItems.LIGHTENED_INGOT.get(), converted));
        if (leftover > 0) {
            dropLeftover(level, item, leftover);
        }

        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 0.6F, 1.4F);
        return converted;
    }

    private static void dropLeftover(ServerLevel level, ItemEntity source, int count) {
        ItemEntity rest = new ItemEntity(level, source.getX(), source.getY(), source.getZ(),
                new ItemStack(ModItems.PALE_INGOT.get(), count));
        rest.setDeltaMovement(source.getDeltaMovement());
        rest.setDefaultPickUpDelay();
        level.addFreshEntity(rest);
    }

    private static void exhaust(ServerLevel level, LightWaterConversions conversions, BlockPos pos) {
        conversions.forget(pos);
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }
}

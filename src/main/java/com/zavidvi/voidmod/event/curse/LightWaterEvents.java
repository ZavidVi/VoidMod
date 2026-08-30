package com.zavidvi.voidmod.event.curse;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.block.PaleCauldronBlock;
import com.zavidvi.voidmod.registry.ModItems;
import com.zavidvi.voidmod.world.curse.LightWaterPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = VoidMod.MOD_ID)
public class LightWaterEvents {
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(ModItems.LIGHT_WATER_BUCKET.get())) return;

        Level level = event.getLevel();
        Player player = event.getEntity();
        BlockState clicked = level.getBlockState(event.getPos());

        if (clicked.getBlock() instanceof AbstractCauldronBlock) {
            deny(event, player, level, false);
            return;
        }

        if (clicked.getBlock() instanceof PaleCauldronBlock) {
            return;
        }

        if (level.isClientSide()) return;

        BlockPos target = clicked.canBeReplaced()
                ? event.getPos()
                : event.getPos().relative(event.getFace());

        if (!LightWaterPlacement.isInsideFountain(level, target)) {
            deny(event, player, level, true);
        }
    }

    private static void deny(PlayerInteractEvent.RightClickBlock event, Player player,
                             Level level, boolean withMessage) {
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);

        if (!level.isClientSide()) {
            restoreBucket(player);
            if (withMessage) {
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    serverPlayer.sendSystemMessage(
                            Component.translatable("message.voidmod.light_water.wrong_place"), true);
                }
            }
        }
    }

    private static void restoreBucket(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.containerMenu.sendAllDataToRemote();
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(ModItems.LIGHT_WATER_BUCKET.get())) return;

        Level level = event.getLevel();
        if (level.isClientSide()) return;

        BlockHitResult hit = net.minecraft.world.item.Item.getPlayerPOVHitResult(
                level, event.getEntity(), net.minecraft.world.level.ClipContext.Fluid.NONE);
        BlockPos target = hit.getBlockPos().relative(hit.getDirection());

        if (!LightWaterPlacement.isInsideFountain(level, target)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            restoreBucket(event.getEntity());
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(
                        Component.translatable("message.voidmod.light_water.wrong_place"), true);
            }
        }
    }
}

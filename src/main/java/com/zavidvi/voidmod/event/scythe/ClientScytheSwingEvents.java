package com.zavidvi.voidmod.event.scythe;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.item.ScytheItem;
import com.zavidvi.voidmod.network.ScytheSwingPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = VoidMod.MOD_ID, value = Dist.CLIENT)
public class ClientScytheSwingEvents {
    @SubscribeEvent
    public static void onAttackInput(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) return;

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) return;
        if (minecraft.gameMode == null || minecraft.gameMode.isSpectator()) return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof ScytheItem)) return;

        if (minecraft.hitResult != null && minecraft.hitResult.getType() == HitResult.Type.ENTITY) return;

        if (!ScytheItem.isAttackReady(player, 0)) return;

        player.resetAttackStrengthTicker();

        ClientPacketDistributor.sendToServer(new ScytheSwingPayload());
    }

    @SubscribeEvent
    public static void hideVanillaSwingInFirstPerson(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) return;
        if (!minecraft.options.getCameraType().isFirstPerson()) return;
        if (!(player.getMainHandItem().getItem() instanceof ScytheItem)) return;

        player.attackAnim = 0.0F;
        player.oAttackAnim = 0.0F;
    }
}

package com.zavidvi.voidmod.event.progression;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.network.SyncProgressionPayload;
import com.zavidvi.voidmod.world.progression.WorldProgressionData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = VoidMod.MOD_ID)
public class ProgressionSyncEvents {
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        WorldProgressionData data = WorldProgressionData.get(player.level());
        PacketDistributor.sendToPlayer(player, SyncProgressionPayload.of(data));
    }
}

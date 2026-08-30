package com.zavidvi.voidmod.network;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.registry.ModItems;
import com.zavidvi.voidmod.world.progression.WorldProgressionData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record WandererDialogueActionPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WandererDialogueActionPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "wanderer_dialogue_action"));

    public static final StreamCodec<ByteBuf, WandererDialogueActionPayload> STREAM_CODEC = StreamCodec.unit(new WandererDialogueActionPayload());

    @Override
    public CustomPacketPayload.Type<WandererDialogueActionPayload> type() {
        return TYPE;
    }

    public static void handle(WandererDialogueActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                WorldProgressionData data = WorldProgressionData.get((net.minecraft.server.level.ServerLevel) player.level());

                com.zavidvi.voidmod.advancement.ModAdvancements.grant(player,
                        com.zavidvi.voidmod.advancement.ModAdvancements.TALE_OF_CRUEL_WORLD);
                
                long currentTime = player.level().getGameTime();
                
                if (!data.isWandererTalked()) {
                    data.setWandererTalked(true);
                    data.setLastDistorterGiveTime(currentTime);

                    placeForgeUnderWanderer(player);

                    giveItemOrDrop(player, new ItemStack(ModItems.SPACE_DISTORTER.get()));
                    
                    PacketDistributor.sendToAllPlayers(SyncProgressionPayload.of(data));
                    
                    PacketDistributor.sendToPlayer(player, WandererDialogueResponsePayload.given());
                } else {
                    long timeSinceLast = currentTime - data.getLastDistorterGiveTime();
                    if (timeSinceLast < 3600) {
                        long ticksLeft = 3600 - timeSinceLast;
                        long secondsLeft = ticksLeft / 20;
                        PacketDistributor.sendToPlayer(player,
                                WandererDialogueResponsePayload.cooldown((int) secondsLeft));
                    } else {
                        data.setLastDistorterGiveTime(currentTime);
                        giveItemOrDrop(player, new ItemStack(ModItems.SPACE_DISTORTER.get()));
                        PacketDistributor.sendToPlayer(player, WandererDialogueResponsePayload.given());
                    }
                }
            }
        });
    }
    
    private static void placeForgeUnderWanderer(ServerPlayer player) {
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel level)) return;

        com.zavidvi.voidmod.entity.wanderer.WandererEntity wanderer = level.getEntitiesOfClass(
                        com.zavidvi.voidmod.entity.wanderer.WandererEntity.class,
                        player.getBoundingBox().inflate(TALK_RADIUS))
                .stream()
                .min(java.util.Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
        if (wanderer == null) return;

        wanderer.placeHomeForge(level);
    }

    private static final double TALK_RADIUS = 16.0D;

    private static void giveItemOrDrop(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}

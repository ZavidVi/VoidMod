package com.zavidvi.voidmod.network;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.advancement.ModAdvancements;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StatsPanelOpenedPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<StatsPanelOpenedPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "stats_panel_opened"));

    public static final StreamCodec<ByteBuf, StatsPanelOpenedPayload> STREAM_CODEC =
            StreamCodec.unit(new StatsPanelOpenedPayload());

    @Override
    public CustomPacketPayload.Type<StatsPanelOpenedPayload> type() {
        return TYPE;
    }

    public static void handle(StatsPanelOpenedPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ModAdvancements.grant(player, ModAdvancements.SOMETHING_NEW);
            }
        });
    }
}

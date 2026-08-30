package com.zavidvi.voidmod.network;

import com.zavidvi.voidmod.VoidMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ShowCurseMessagePayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ShowCurseMessagePayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "show_curse_message"));

    public static final StreamCodec<ByteBuf, ShowCurseMessagePayload> STREAM_CODEC =
            StreamCodec.unit(new ShowCurseMessagePayload());

    @Override
    public CustomPacketPayload.Type<ShowCurseMessagePayload> type() {
        return TYPE;
    }

    public static void handle(ShowCurseMessagePayload payload, IPayloadContext context) {
        context.enqueueWork(com.zavidvi.voidmod.client.network.ClientPayloadHandlers::showCurseMessage);
    }
}

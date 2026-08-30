package com.zavidvi.voidmod.network;

import com.zavidvi.voidmod.VoidMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenWandererDialoguePayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenWandererDialoguePayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "open_wanderer_dialogue"));

    public static final StreamCodec<ByteBuf, OpenWandererDialoguePayload> STREAM_CODEC = StreamCodec.unit(new OpenWandererDialoguePayload());

    @Override
    public CustomPacketPayload.Type<OpenWandererDialoguePayload> type() {
        return TYPE;
    }

    public static void handle(OpenWandererDialoguePayload payload, IPayloadContext context) {
        context.enqueueWork(com.zavidvi.voidmod.client.network.ClientPayloadHandlers::openWandererDialogue);
    }
}

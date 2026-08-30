package com.zavidvi.voidmod.network;

import com.zavidvi.voidmod.VoidMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record WandererDialogueResponsePayload(String key, int arg) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WandererDialogueResponsePayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "wanderer_dialogue_response"));

    public static final StreamCodec<ByteBuf, WandererDialogueResponsePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, WandererDialogueResponsePayload::key,
            ByteBufCodecs.VAR_INT, WandererDialogueResponsePayload::arg,
            WandererDialogueResponsePayload::new
    );

    public static WandererDialogueResponsePayload given() {
        return new WandererDialogueResponsePayload("voidmod.wanderer.given", 0);
    }

    public static WandererDialogueResponsePayload cooldown(int secondsLeft) {
        return new WandererDialogueResponsePayload("voidmod.wanderer.cooldown", secondsLeft);
    }

    @Override
    public CustomPacketPayload.Type<WandererDialogueResponsePayload> type() {
        return TYPE;
    }

    public static void handle(WandererDialogueResponsePayload payload, IPayloadContext context) {
        context.enqueueWork(() ->
                com.zavidvi.voidmod.client.network.ClientPayloadHandlers.showWandererResponse(
                        payload.key(), payload.arg()));
    }
}

package com.zavidvi.voidmod.network;

import com.zavidvi.voidmod.VoidMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ScytheSwingAnimationPayload(int hit, int swingTicks) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ScytheSwingAnimationPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "scythe_swing_animation"));

    public static final StreamCodec<ByteBuf, ScytheSwingAnimationPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ScytheSwingAnimationPayload::hit,
            ByteBufCodecs.VAR_INT, ScytheSwingAnimationPayload::swingTicks,
            ScytheSwingAnimationPayload::new
    );

    @Override
    public CustomPacketPayload.Type<ScytheSwingAnimationPayload> type() {
        return TYPE;
    }

    public static void handle(ScytheSwingAnimationPayload payload, IPayloadContext context) {
        context.enqueueWork(() ->
                com.zavidvi.voidmod.client.network.ClientPayloadHandlers.playScytheSwing(
                        payload.hit(), payload.swingTicks()));
    }
}

package com.zavidvi.voidmod.network;

import com.zavidvi.voidmod.VoidMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncProgressionPayload(boolean portalAttempted, boolean worldCursed, boolean wandererTalked,
                                     boolean reaperDefeated) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncProgressionPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "sync_progression"));

    public static final StreamCodec<ByteBuf, SyncProgressionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SyncProgressionPayload::portalAttempted,
            ByteBufCodecs.BOOL, SyncProgressionPayload::worldCursed,
            ByteBufCodecs.BOOL, SyncProgressionPayload::wandererTalked,
            ByteBufCodecs.BOOL, SyncProgressionPayload::reaperDefeated,
            SyncProgressionPayload::new
    );

    public static SyncProgressionPayload of(com.zavidvi.voidmod.world.progression.WorldProgressionData data) {
        return new SyncProgressionPayload(
                data.isPortalAttempted(),
                data.isWorldCursed(),
                data.isWandererTalked(),
                data.isReaperDefeated());
    }

    @Override
    public CustomPacketPayload.Type<SyncProgressionPayload> type() {
        return TYPE;
    }

    public static void handle(SyncProgressionPayload payload, IPayloadContext context) {
        context.enqueueWork(() ->
                com.zavidvi.voidmod.client.network.ClientPayloadHandlers.syncProgression(payload));
    }
}

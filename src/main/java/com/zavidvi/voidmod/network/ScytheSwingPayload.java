package com.zavidvi.voidmod.network;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.item.ScytheItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ScytheSwingPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ScytheSwingPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "scythe_swing"));

    public static final StreamCodec<ByteBuf, ScytheSwingPayload> STREAM_CODEC =
            StreamCodec.unit(new ScytheSwingPayload());

    @Override
    public CustomPacketPayload.Type<ScytheSwingPayload> type() {
        return TYPE;
    }

    public static void handle(ScytheSwingPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.level() instanceof ServerLevel level)) return;

            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof ScytheItem scythe)) return;
            if (!ScytheItem.isAttackReady(player, ScytheItem.SERVER_CHARGE_TOLERANCE)) return;

            scythe.swingAttack(level, player);
            player.resetAttackStrengthTicker();
        });
    }
}

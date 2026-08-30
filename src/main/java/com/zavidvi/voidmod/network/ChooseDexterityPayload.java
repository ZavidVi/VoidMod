package com.zavidvi.voidmod.network;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.stat.DexterityType;
import com.zavidvi.voidmod.stat.StatManager;
import com.zavidvi.voidmod.stat.UniversalArmor;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ChooseDexterityPayload(int inventorySlot, boolean ranged) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ChooseDexterityPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "choose_dexterity"));

    public static final StreamCodec<ByteBuf, ChooseDexterityPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ChooseDexterityPayload::inventorySlot,
            ByteBufCodecs.BOOL, ChooseDexterityPayload::ranged,
            ChooseDexterityPayload::new);

    @Override
    public CustomPacketPayload.Type<ChooseDexterityPayload> type() {
        return TYPE;
    }

    public static void handle(ChooseDexterityPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            Inventory inventory = player.getInventory();
            if (payload.inventorySlot() < 0 || payload.inventorySlot() >= inventory.getContainerSize()) return;

            ItemStack stack = inventory.getItem(payload.inventorySlot());
            DexterityType type = payload.ranged() ? DexterityType.RANGED : DexterityType.MELEE;
            if (UniversalArmor.choose(stack, type)) {
                inventory.setChanged();
                player.containerMenu.broadcastChanges();
                StatManager.recalculate(player);
            }
        });
    }
}

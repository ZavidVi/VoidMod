package com.zavidvi.voidmod.registry;

import com.mojang.serialization.Codec;
import com.zavidvi.voidmod.VoidMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, VoidMod.MOD_ID);

    public static final Supplier<AttachmentType<Integer>> CURSE_LIGHT_VERSION =
            ATTACHMENT_TYPES.register("curse_light_version",
                    () -> AttachmentType.builder(() -> 0).serialize(Codec.INT.fieldOf("value")).build());

    public static final Supplier<AttachmentType<Integer>> CURSED_CONTENT_VERSION =
            ATTACHMENT_TYPES.register("cursed_content_version",
                    () -> AttachmentType.builder(() -> 0).serialize(Codec.INT.fieldOf("value")).build());

    public static final Supplier<AttachmentType<com.zavidvi.voidmod.stat.StatData>> PLAYER_STATS =
            ATTACHMENT_TYPES.register("player_stats",
                    () -> AttachmentType.builder(com.zavidvi.voidmod.stat.StatData::new).build());

    public static final Supplier<AttachmentType<Double>> MANA =
            ATTACHMENT_TYPES.register("mana",
                    () -> AttachmentType.builder(() -> 100.0)
                            .serialize(Codec.DOUBLE.fieldOf("value"))
                            .sync(net.minecraft.network.codec.ByteBufCodecs.DOUBLE)
                            .build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}

package com.zavidvi.voidmod.client.item;

import com.mojang.serialization.MapCodec;
import com.zavidvi.voidmod.world.curse.CurseHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record CursedItemProperty() implements ConditionalItemModelProperty {
    public static final MapCodec<CursedItemProperty> MAP_CODEC = MapCodec.unit(new CursedItemProperty());

    @Override
    public boolean get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner,
                       int seed, ItemDisplayContext displayContext) {
        return CurseHelper.isLighterCursed(level);
    }

    @Override
    public MapCodec<CursedItemProperty> type() {
        return MAP_CODEC;
    }
}

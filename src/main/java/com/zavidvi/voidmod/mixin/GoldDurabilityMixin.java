package com.zavidvi.voidmod.mixin;

import com.zavidvi.voidmod.stat.GoldDurability;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class GoldDurabilityMixin {
    @ModifyVariable(
            method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;"
                    + "Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 0, remap = false)
    private int voidmod$slowGoldWear(int value, int amount, ServerLevel level,
                                     LivingEntity entity, Consumer<Item> onBreak) {
        if (!(entity instanceof Player player)) return value;
        return GoldDurability.reduce(value, player, (ItemStack) (Object) this, level.getRandom());
    }
}

package com.zavidvi.voidmod.mixin;

import com.zavidvi.voidmod.util.CursedEnchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentEffectMixin {
    @ModifyVariable(
            method = "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentVisitor;)V",
            at = @At("HEAD"),
            argsOnly = true,
            index = 1,
            remap = false
    )
    private static EnchantmentHelper.EnchantmentVisitor voidmod$filterVisitor(EnchantmentHelper.EnchantmentVisitor visitor) {
        return CursedEnchantments.filter(visitor);
    }

    @ModifyVariable(
            method = "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentInSlotVisitor;)V",
            at = @At("HEAD"),
            argsOnly = true,
            index = 3,
            remap = false
    )
    private static EnchantmentHelper.EnchantmentInSlotVisitor voidmod$filterSlotVisitor(EnchantmentHelper.EnchantmentInSlotVisitor visitor) {
        return CursedEnchantments.filter(visitor);
    }
}

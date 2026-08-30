package com.zavidvi.voidmod.mixin;

import com.zavidvi.voidmod.util.CursedEnchantments;
import com.zavidvi.voidmod.world.curse.CurseHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEnchantments.class)
public class EnchantmentMixin {
    @Inject(method = "getLevel", at = @At("HEAD"), cancellable = true, remap = false)
    private void onGetLevel(Holder<Enchantment> enchantment, CallbackInfoReturnable<Integer> cir) {
        if (CursedEnchantments.isSuppressed(enchantment) && CurseHelper.isWorldCursedGlobal()) {
            cir.setReturnValue(0);
        }
    }
}

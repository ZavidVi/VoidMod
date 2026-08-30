package com.zavidvi.voidmod.mixin;

import com.zavidvi.voidmod.world.curse.CurseHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class CooledLavaBucketNameMixin {
    private static final String COOLED_LAVA_BUCKET_KEY = "item.voidmod.cooled_lava_bucket";

    @Inject(method = "getName(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/network/chat/Component;",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void voidmod$cooledLavaBucketName(ItemStack stack, CallbackInfoReturnable<Component> cir) {
        if (!stack.is(Items.LAVA_BUCKET)) return;
        if (!CurseHelper.isWorldCursedGlobal()) return;
        cir.setReturnValue(Component.translatable(COOLED_LAVA_BUCKET_KEY));
    }
}

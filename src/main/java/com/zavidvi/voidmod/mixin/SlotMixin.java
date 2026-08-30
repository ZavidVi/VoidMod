package com.zavidvi.voidmod.mixin;

import com.zavidvi.voidmod.recipe.EssenceCraftingEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {
    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true, remap = false)
    private void voidmod$checkEssenceRequirement(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player == null) return;
        if ((Object) this instanceof ResultSlot) {
            Slot self = (Slot) (Object) this;
            ItemStack result = self.getItem();
            if (result.isEmpty()) return;

            int cost = EssenceCraftingEvents.getEssenceCost(result);
            if (cost > 0 && EssenceCraftingEvents.countEssence(player) < cost) {
                cir.setReturnValue(false);
            }
        }
    }
}

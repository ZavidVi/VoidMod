package com.zavidvi.voidmod.mixin;

import com.zavidvi.voidmod.config.VoidModConfig;
import net.minecraft.server.commands.TickCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TickCommand.class)
public class TickCommandMixin {
    @ModifyArg(
            method = "register(Lcom/mojang/brigadier/CommandDispatcher;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/brigadier/arguments/FloatArgumentType;floatArg(FF)Lcom/mojang/brigadier/arguments/FloatArgumentType;"
            ),
            index = 1,
            remap = false
    )
    private static float voidmod$raiseMaxTickRate(float vanillaMax) {
        return Math.max(vanillaMax, VoidModConfig.maxTickRate());
    }
}

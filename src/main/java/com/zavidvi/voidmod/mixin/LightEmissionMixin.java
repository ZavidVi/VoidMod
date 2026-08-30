package com.zavidvi.voidmod.mixin;

import com.zavidvi.voidmod.util.CursedDimmedState;
import com.zavidvi.voidmod.util.CursedLightBlocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class LightEmissionMixin implements CursedDimmedState {
    @Unique
    private byte voidmod$dimmedCache;

    @Override
    public boolean voidmod$isCursedDimmed() {
        byte cached = this.voidmod$dimmedCache;
        if (cached == 0) {
            cached = CursedLightBlocks.isDimmed((BlockState) (Object) this) ? (byte) 1 : (byte) 2;
            this.voidmod$dimmedCache = cached;
        }
        return cached == 1;
    }
}

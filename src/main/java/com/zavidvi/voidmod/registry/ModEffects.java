package com.zavidvi.voidmod.registry;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.effect.BleedingEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, VoidMod.MOD_ID);

    public static final DeferredHolder<MobEffect, BleedingEffect> BLEEDING =
            EFFECTS.register("bleeding", BleedingEffect::new);

    public static void register(IEventBus modEventBus) {
        EFFECTS.register(modEventBus);
    }

    private ModEffects() {}
}

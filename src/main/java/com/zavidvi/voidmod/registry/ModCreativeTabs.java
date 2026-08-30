package com.zavidvi.voidmod.registry;

import com.zavidvi.voidmod.VoidMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, VoidMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> VOIDMODE_TAB =
            TABS.register("voidmode", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.voidmod.voidmode"))
                    .icon(() -> new ItemStack(ModItems.SPACE_DISTORTER.get()))
                    .displayItems((params, output) ->
                            ModItems.ITEMS.getEntries().forEach(item -> output.accept(item.get())))
                    .build());

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}

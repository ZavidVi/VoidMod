package com.zavidvi.voidmod.client.stat;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.stat.UniversalArmor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = VoidMod.MOD_ID, value = Dist.CLIENT)
public final class ArmorTypeInventoryEvents {
    private ArmorTypeInventoryEvents() {}

    @SubscribeEvent
    public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (ArmorTypePopup.isOpen()) {
            if (ArmorTypePopup.mouseClicked((int) event.getMouseX(), (int) event.getMouseY())) {
                event.setCanceled(true);
            }
            return;
        }

        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_MIDDLE) return;
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) return;

        Player player = screen.getMinecraft().player;
        if (player == null || player.isCreative()) return;

        Slot slot = screen.getHoveredSlot();
        if (slot == null || !(slot.container instanceof Inventory)) return;

        ItemStack stack = slot.getItem();
        if (!UniversalArmor.isUniversal(stack) || UniversalArmor.hasChoice(stack)) return;

        ArmorTypePopup.open(screen.getMinecraft().font, slot.getContainerSlot(),
                screen.getGuiLeft() + slot.x, screen.getGuiTop() + slot.y,
                screen.width, screen.height);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!ArmorTypePopup.isOpen()) return;
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) return;

        event.getGuiGraphics().nextStratum();
        ArmorTypePopup.render(event.getGuiGraphics(), screen.getMinecraft().font,
                event.getMouseX(), event.getMouseY());
    }

    @SubscribeEvent
    public static void onScreenClosing(ScreenEvent.Closing event) {
        ArmorTypePopup.close();
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        ArmorTypePopup.close();
    }
}

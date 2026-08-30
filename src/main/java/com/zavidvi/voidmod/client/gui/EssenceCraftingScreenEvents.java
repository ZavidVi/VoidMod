package com.zavidvi.voidmod.client.gui;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.recipe.EssenceCraftingEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = VoidMod.MOD_ID, value = Dist.CLIENT)
public final class EssenceCraftingScreenEvents {
    private static final Identifier FIRE_ICON =
            Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "textures/gui/fire_essence.png");
    private static final Identifier FIRE_ICON_GRAY =
            Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "textures/gui/fire_essence_gray.png");

    private static final int ICON_SIZE = 16;
    private static final int TEXTURE_SIZE = 16;
    private static final int GAP = 2;

    private static final int COLOR_ENOUGH = 0xFFFFFFFF;
    private static final int COLOR_LACKING = 0xFFAAAAAA;
    private static final int DISABLED_SLOT_OVERLAY = 0x90202020;

    private EssenceCraftingScreenEvents() {}

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof CraftingScreen screen)) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        CraftingMenu menu = screen.getMenu();
        Slot resultSlot = menu.getSlot(0);
        if (resultSlot == null || !resultSlot.hasItem()) return;

        ItemStack result = resultSlot.getItem();
        int cost = EssenceCraftingEvents.getEssenceCost(result);
        if (cost <= 0) return;

        int playerEssence = EssenceCraftingEvents.countEssence(player);
        boolean hasEnough = playerEssence >= cost;

        Font font = screen.getMinecraft().font;
        String costStr = String.valueOf(cost);
        int textWidth = font.width(costStr);
        int totalWidth = ICON_SIZE + GAP + textWidth;

        int slotX = screen.getGuiLeft() + 124;
        int slotY = screen.getGuiTop() + 35;
        int slotCenterX = slotX + 9;

        int startX = slotCenterX - totalWidth / 2;
        int startY = screen.getGuiTop() + 14;

        int iconX = startX;
        int iconY = startY;
        int textX = startX + ICON_SIZE + GAP;
        int textY = startY + 4;

        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        Identifier texture = hasEnough ? FIRE_ICON : FIRE_ICON_GRAY;
        int textColor = hasEnough ? COLOR_ENOUGH : COLOR_LACKING;

        if (!hasEnough) {
            graphics.fill(slotX, slotY, slotX + 16, slotY + 16, DISABLED_SLOT_OVERLAY);
        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, iconX, iconY,
                0.0F, 0.0F, ICON_SIZE, ICON_SIZE, TEXTURE_SIZE, TEXTURE_SIZE,
                TEXTURE_SIZE, TEXTURE_SIZE);

        graphics.text(font, costStr, textX, textY, textColor, true);

        int mouseX = event.getMouseX();
        int mouseY = event.getMouseY();
        if (mouseX >= startX - 2 && mouseX <= startX + totalWidth + 2 &&
                mouseY >= startY - 2 && mouseY <= startY + ICON_SIZE + 2) {
            List<Component> tooltip = new ArrayList<>(2);
            if (hasEnough) {
                tooltip.add(Component.translatable("voidmod.gui.essence_cost_enough", cost, playerEssence));
            } else {
                tooltip.add(Component.translatable("voidmod.gui.essence_cost_lacking", cost, playerEssence));
            }

            graphics.nextStratum();
            List<ClientTooltipComponent> lines = tooltip.stream()
                    .map(line -> ClientTooltipComponent.create(line.getVisualOrderText()))
                    .toList();
            graphics.tooltip(font, lines, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
        }
    }
}

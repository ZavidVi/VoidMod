package com.zavidvi.voidmod.recipe;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.registry.ModBlocks;
import com.zavidvi.voidmod.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = VoidMod.MOD_ID)
public class EssenceCraftingEvents {
    public static int getEssenceCost(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Item item = stack.getItem();

        if (item == ModItems.GOLD_SCYTHE.get()) return 5;
        if (item == ModItems.IRON_SCYTHE.get()) return 10;
        if (item == ModItems.DIAMOND_SCYTHE.get()) return 15;
        if (item == ModItems.PALE_SCYTHE.get()) return 20;
        if (item == ModItems.NETHERITE_SCYTHE.get()) return 25;

        if (item == ModItems.PALE_HELMET.get()) return 2;
        if (item == ModItems.PALE_CHESTPLATE.get()) return 4;
        if (item == ModItems.PALE_LEGGINGS.get()) return 3;
        if (item == ModItems.PALE_BOOTS.get()) return 2;

        if (item == ModItems.LIGHTED_HOOD.get()) return 3;
        if (item == ModItems.LIGHTED_MANTLE.get()) return 5;
        if (item == ModItems.LIGHTED_LEGGUARDS.get()) return 4;
        if (item == ModItems.LIGHTED_SANDALS.get()) return 3;

        if (item == ModItems.PRISMARINE_HOOD.get()) return 3;
        if (item == ModItems.PRISMARINE_JACKET.get()) return 5;
        if (item == ModItems.PRISMARINE_KNEE_PADS.get()) return 4;
        if (item == ModItems.PRISMARINE_BOOTS.get()) return 3;

        return 0;
    }

    public static int getEssenceCostFromGrid(CraftingInput input) {
        if (input == null || input.isEmpty()) return 0;
        int count = input.ingredientCount();
        if (count == 0) return 0;

        if (count == 5) {
            ItemStack s0 = input.getItem(0, 0);
            ItemStack s1 = input.getItem(1, 0);
            ItemStack s2 = input.getItem(2, 1);
            ItemStack stick1 = input.getItem(0, 1);
            ItemStack stick2 = input.getItem(0, 2);
            if (stick1.is(Items.STICK) && stick2.is(Items.STICK)
                    && !s0.isEmpty() && !s1.isEmpty() && !s2.isEmpty()
                    && s0.is(s1.getItem()) && s0.is(s2.getItem())) {
                Item mat = s0.getItem();
                if (mat == Items.DIAMOND) return 15;
                if (mat == Items.GOLD_INGOT) return 5;
                if (mat == Items.IRON_INGOT) return 10;
                if (mat == ModItems.PALE_INGOT.get()) return 20;
                if (mat == Items.NETHERITE_INGOT) return 25;
            }
        }

        if (isPureMaterial(input, ModItems.PALE_INGOT.get())) {
            if (count == 5) return 2;
            if (count == 8) return 4;
            if (count == 7) return 3;
            if (count == 4) return 2;
        }

        Item lightedWool = ModBlocks.LIGHTED_WOOL.get().asItem();
        if (isPureMaterial(input, lightedWool)) {
            if (count == 2) return 3;
            if (count == 4) return 4;
        }
        if (count == 5 && countItem(input, lightedWool) == 3 && countItem(input, ModItems.LIGHTENED_INGOT.get()) == 2) {
            return 3;
        }
        if (count == 8 && countItem(input, lightedWool) == 4 && countItem(input, ModItems.LIGHTENED_INGOT.get()) == 4) {
            return 5;
        }

        if (isPureMaterial(input, Items.PRISMARINE_SHARD)) {
            if (count == 2) return 4;
        }
        if (count == 4 && countItem(input, Items.PRISMARINE_SHARD) == 2 && countItem(input, Items.PRISMARINE_CRYSTALS) == 2) {
            return 3;
        }
        if (count == 5 && countItem(input, Items.PRISMARINE_SHARD) == 4 && countItem(input, Items.PRISMARINE_CRYSTALS) == 1) {
            return 3;
        }
        if (count == 8 && countItem(input, Items.PRISMARINE_SHARD) == 5 && countItem(input, Items.PRISMARINE_CRYSTALS) == 3) {
            return 5;
        }

        return 0;
    }

    private static boolean isPureMaterial(CraftingInput input, Item expected) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getItem(i);
            if (!s.isEmpty() && !s.is(expected)) return false;
        }
        return true;
    }

    private static int countItem(CraftingInput input, Item expected) {
        int total = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getItem(i);
            if (!s.isEmpty() && s.is(expected)) total++;
        }
        return total;
    }

    public static int countEssence(Player player) {
        if (player == null) return 0;
        int total = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.FIRE_ESSENCE.get())) {
                total += stack.getCount();
            }
        }
        return total;
    }

    public static void consumeEssence(Player player, int amount) {
        if (player == null || amount <= 0) return;
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.FIRE_ESSENCE.get())) {
                int take = Math.min(stack.getCount(), remaining);
                stack.shrink(take);
                remaining -= take;
            }
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        ItemStack crafted = event.getCrafting();
        int cost = getEssenceCost(crafted);
        if (cost > 0) {
            consumeEssence(player, cost);
        }
    }
}

package com.zavidvi.voidmod.stat;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Set;

public final class GoldDurability {
    private static final Set<Item> GOLDEN_ITEMS = Set.of(
            Items.GOLDEN_HELMET,
            Items.GOLDEN_CHESTPLATE,
            Items.GOLDEN_LEGGINGS,
            Items.GOLDEN_BOOTS,
            Items.GOLDEN_SHOVEL,
            Items.GOLDEN_HOE,
            Items.GOLDEN_AXE,
            Items.GOLDEN_PICKAXE,
            Items.GOLDEN_SWORD);

    private GoldDurability() {}

    public static boolean isGolden(ItemStack stack) {
        return GOLDEN_ITEMS.contains(stack.getItem());
    }

    public static int reduce(int amount, Player player, ItemStack stack, RandomSource random) {
        if (amount <= 0 || !isGolden(stack)) return amount;

        int factor = StatManager.get(player).goldDurabilityFactor();
        if (factor <= 1) return amount;

        int reduced = 0;
        for (int i = 0; i < amount; i++) {
            if (random.nextInt(factor) == 0) {
                reduced++;
            }
        }
        return reduced;
    }
}

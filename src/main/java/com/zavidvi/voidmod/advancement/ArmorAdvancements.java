package com.zavidvi.voidmod.advancement;

import com.zavidvi.voidmod.registry.ModItems;
import com.zavidvi.voidmod.stat.PlayerStat;
import com.zavidvi.voidmod.stat.StatData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Set;

public final class ArmorAdvancements {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private static final Set<Item> WARRIOR = Set.of(
            Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS,
            Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS,
            Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS,
            ModItems.PALE_HELMET.get(), ModItems.PALE_CHESTPLATE.get(),
            ModItems.PALE_LEGGINGS.get(), ModItems.PALE_BOOTS.get());

    private static final Set<Item> MARKSMAN = Set.of(
            ModItems.PRISMARINE_HOOD.get(), ModItems.PRISMARINE_JACKET.get(),
            ModItems.PRISMARINE_KNEE_PADS.get(), ModItems.PRISMARINE_BOOTS.get());

    private static final Set<Item> MAGE = Set.of(
            ModItems.LIGHTED_HOOD.get(), ModItems.LIGHTED_MANTLE.get(),
            ModItems.LIGHTED_LEGGUARDS.get(), ModItems.LIGHTED_SANDALS.get());

    private static final Set<Item> NETHERITE = Set.of(
            Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE,
            Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);

    private ArmorAdvancements() {}

    public static void check(ServerPlayer player, StatData stats) {
        checkNegativeStats(player, stats);
        checkArmorSets(player);
    }

    private static void checkNegativeStats(ServerPlayer player, StatData stats) {
        if (stats.get(PlayerStat.INTELLIGENCE) < 0) {
            ModAdvancements.grant(player, ModAdvancements.NUGGET);
        }
        if (stats.get(PlayerStat.VITALITY) < 0) {
            ModAdvancements.grant(player, ModAdvancements.STRONG_IN_SPIRIT);
        }
    }

    private static void checkArmorSets(ServerPlayer player) {
        ItemStack[] worn = new ItemStack[ARMOR_SLOTS.length];
        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            worn[i] = player.getItemBySlot(ARMOR_SLOTS[i]);
            if (worn[i].isEmpty()) return;
        }

        if (allFrom(worn, WARRIOR)) {
            ModAdvancements.grant(player, ModAdvancements.PATH_OF_THE_WARRIOR);
        }
        if (allFrom(worn, MARKSMAN)) {
            ModAdvancements.grant(player, ModAdvancements.PATH_OF_THE_MARKSMAN);
        }
        if (allFrom(worn, MAGE)) {
            ModAdvancements.grant(player, ModAdvancements.PATH_OF_THE_MAGE);
        }
        if (isMixedPath(worn)) {
            ModAdvancements.grant(player, ModAdvancements.WHOSE_PATH_IS_THIS);
        }
    }

    private static boolean isMixedPath(ItemStack[] worn) {
        int mage = 0;
        int marksman = 0;
        int netherite = 0;

        for (ItemStack stack : worn) {
            Item item = stack.getItem();
            if (MAGE.contains(item)) mage++;
            else if (MARKSMAN.contains(item)) marksman++;
            else if (NETHERITE.contains(item)) netherite++;
        }

        return mage == 1 && marksman == 1 && netherite >= 1;
    }

    private static boolean allFrom(ItemStack[] worn, Set<Item> set) {
        for (ItemStack stack : worn) {
            if (!set.contains(stack.getItem())) return false;
        }
        return true;
    }
}

package com.zavidvi.voidmod.stat;

import com.zavidvi.voidmod.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class ArmorStats {
    public record Entry(ArmorClass armorClass, Map<PlayerStat, Double> stats, double flatArmor) {
        public double stat(PlayerStat stat) {
            return this.stats.getOrDefault(stat, 0.0);
        }
    }

    private static final Map<Item, Entry> TABLE = new HashMap<>();

    private ArmorStats() {}

    public static Entry get(Item item) {
        return TABLE.get(item);
    }

    public static Entry get(ItemStack stack) {
        return stack.isEmpty() ? null : TABLE.get(stack.getItem());
    }

    public static boolean isManaged(Item item) {
        return TABLE.containsKey(item);
    }

    public static void clear() {
        TABLE.clear();
    }

    public static void put(Item item, ArmorClass armorClass,
                           double vitality, double dexterity, double intelligence, double flatArmor) {
        Map<PlayerStat, Double> stats = new EnumMap<>(PlayerStat.class);
        stats.put(PlayerStat.VITALITY, vitality);
        stats.put(PlayerStat.DEXTERITY, dexterity);
        stats.put(PlayerStat.INTELLIGENCE, intelligence);
        TABLE.put(item, new Entry(armorClass, stats, flatArmor));
    }

    public static void bootstrap() {
        clear();
        putVanilla();
        putModArmor();
    }

    private static void putModArmor() {
        put(ModItems.PALE_HELMET.get(),     ArmorClass.MELEE, 0.5, 1.0, 0,   0);
        put(ModItems.PALE_CHESTPLATE.get(), ArmorClass.MELEE, 2.0, 1.0, 0,   0);
        put(ModItems.PALE_LEGGINGS.get(),   ArmorClass.MELEE, 1.0, 1.0, 0.5, 0);
        put(ModItems.PALE_BOOTS.get(),      ArmorClass.MELEE, 1.0, 1.0, 0,   0);

        put(ModItems.LIGHTED_HOOD.get(),      ArmorClass.RANGED, 0.5, 0,   1.5, 0);
        put(ModItems.LIGHTED_MANTLE.get(),    ArmorClass.RANGED, 1.0, 0.5, 1.5, 0);
        put(ModItems.LIGHTED_LEGGUARDS.get(), ArmorClass.RANGED, 0.5, 0.5, 1.5, 0);
        put(ModItems.LIGHTED_SANDALS.get(),   ArmorClass.RANGED, 0.5, 0,   1.5, 0);

        put(ModItems.PRISMARINE_HOOD.get(),      ArmorClass.RANGED, 0.5, 1.5, 0.5, 0);
        put(ModItems.PRISMARINE_JACKET.get(),    ArmorClass.RANGED, 1.0, 1.5, 1.0, 0);
        put(ModItems.PRISMARINE_KNEE_PADS.get(), ArmorClass.RANGED, 1.0, 1.5, 0.5, 0);
        put(ModItems.PRISMARINE_BOOTS.get(),     ArmorClass.RANGED, 1.0, 1.5, 0.5, 0);
    }

    private static void putVanilla() {
        put(Items.LEATHER_HELMET,     ArmorClass.NONE, 0, 0, 0, 0.5);
        put(Items.LEATHER_CHESTPLATE, ArmorClass.NONE, 0, 0, 0, 2.0);
        put(Items.LEATHER_LEGGINGS,   ArmorClass.NONE, 0, 0, 0, 1.0);
        put(Items.LEATHER_BOOTS,      ArmorClass.NONE, 0, 0, 0, 0.5);

        put(Items.CHAINMAIL_HELMET,     ArmorClass.UNIVERSAL, -3, 0.5, 0, 0);
        put(Items.CHAINMAIL_CHESTPLATE, ArmorClass.UNIVERSAL, -4, 1.0, 0, 0);
        put(Items.CHAINMAIL_LEGGINGS,   ArmorClass.UNIVERSAL, -4, 0.5, 0, 0);
        put(Items.CHAINMAIL_BOOTS,      ArmorClass.UNIVERSAL, -3, 0.5, 0, 0);

        put(Items.GOLDEN_HELMET,     ArmorClass.MELEE, 0.5, 0, -3, 0);
        put(Items.GOLDEN_CHESTPLATE, ArmorClass.MELEE, 1.0, 0, -4, 0);
        put(Items.GOLDEN_LEGGINGS,   ArmorClass.MELEE, 0.5, 0, -4, 0);
        put(Items.GOLDEN_BOOTS,      ArmorClass.MELEE, 0.5, 0, -3, 0);

        put(Items.IRON_HELMET,     ArmorClass.MELEE, 0.5, 0.5, 0,   0);
        put(Items.IRON_CHESTPLATE, ArmorClass.MELEE, 1.0, 0,   0.5, 0);
        put(Items.IRON_LEGGINGS,   ArmorClass.MELEE, 0.5, 0,   0.5, 0);
        put(Items.IRON_BOOTS,      ArmorClass.MELEE, 0.5, 0.5, 0,   0);

        put(Items.DIAMOND_HELMET,     ArmorClass.MELEE, 1.5, 0, 0.5, 0);
        put(Items.DIAMOND_CHESTPLATE, ArmorClass.MELEE, 2.0, 0, 0,   0);
        put(Items.DIAMOND_LEGGINGS,   ArmorClass.MELEE, 1.5, 1.0, 0, 0);
        put(Items.DIAMOND_BOOTS,      ArmorClass.MELEE, 1.0, 1.0, 0, 0);

        put(Items.NETHERITE_HELMET,     ArmorClass.UNIVERSAL, 1.5, 0,   0.5, 0);
        put(Items.NETHERITE_CHESTPLATE, ArmorClass.UNIVERSAL, 2.0, 1.0, 0.5, 0);
        put(Items.NETHERITE_LEGGINGS,   ArmorClass.UNIVERSAL, 1.5, 1.0, 0,   0);
        put(Items.NETHERITE_BOOTS,      ArmorClass.UNIVERSAL, 1.0, 1.0, 1.0, 0);
    }
}

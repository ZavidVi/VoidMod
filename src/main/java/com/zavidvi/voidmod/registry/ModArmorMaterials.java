package com.zavidvi.voidmod.registry;

import com.google.common.collect.Maps;
import com.zavidvi.voidmod.VoidMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;
import java.util.Map;

public class ModArmorMaterials {
    public static final int DURABILITY_MULTIPLIER = 15;

    private static final int ENCHANTMENT_VALUE = 15;

    public static final TagKey<Item> REPAIRS_PALE_ARMOR = itemTag("repairs_pale_armor");
    public static final TagKey<Item> REPAIRS_LIGHTED_ARMOR = itemTag("repairs_lighted_armor");
    public static final TagKey<Item> REPAIRS_PRISMARINE_ARMOR = itemTag("repairs_prismarine_armor");

    public static final ArmorMaterial PALE = material("pale", REPAIRS_PALE_ARMOR);
    public static final ArmorMaterial LIGHTED = material("lighted", REPAIRS_LIGHTED_ARMOR);
    public static final ArmorMaterial PRISMARINE = material("prismarine", REPAIRS_PRISMARINE_ARMOR);

    private static ArmorMaterial material(String name, TagKey<Item> repairIngredient) {
        return new ArmorMaterial(
                DURABILITY_MULTIPLIER,
                noDefense(),
                ENCHANTMENT_VALUE,
                SoundEvents.ARMOR_EQUIP_IRON,
                0.0F,
                0.0F,
                repairIngredient,
                equipmentAsset(name));
    }

    private static ResourceKey<EquipmentAsset> equipmentAsset(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, name));
    }

    private static TagKey<Item> itemTag(String name) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, name));
    }

    private static Map<ArmorType, Integer> noDefense() {
        Map<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
        for (ArmorType type : ArmorType.values()) {
            defense.put(type, 0);
        }
        return Maps.newEnumMap(defense);
    }

    public static int durability(ArmorType type) {
        return type.getDurability(DURABILITY_MULTIPLIER);
    }
}

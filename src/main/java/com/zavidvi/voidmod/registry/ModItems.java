package com.zavidvi.voidmod.registry;

import com.zavidvi.voidmod.VoidMod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VoidMod.MOD_ID);

    private static final int DEBUG_SWORD_BONUS_DAMAGE = 1000;

    public static final DeferredItem<Item> DEBUG_SWORD = ITEMS.registerSimpleItem("debug_sword",
            props -> props
                    .fireResistant()
                    .sword(ToolMaterial.NETHERITE, 3 + DEBUG_SWORD_BONUS_DAMAGE, -2.4F));

    public static final DeferredItem<com.zavidvi.voidmod.item.SpaceDistorterItem> SPACE_DISTORTER =
            ITEMS.registerItem("space_distorter", com.zavidvi.voidmod.item.SpaceDistorterItem::new);

    public static final DeferredItem<SpawnEggItem> WANDERER_SPAWN_EGG =
            ITEMS.registerItem("wanderer_spawn_egg", SpawnEggItem::new,
                    props -> props.spawnEgg(ModEntities.WANDERER.get()));

    public static final DeferredItem<Item> PALE_INGOT = ITEMS.registerSimpleItem("pale_ingot");

    public static final DeferredItem<Item> LIGHTENED_INGOT = ITEMS.registerSimpleItem("lightened_ingot");

    public static final DeferredItem<Item> BLACK_BONE = ITEMS.registerSimpleItem("black_bone");

    public static final DeferredItem<Item> RAW_PALE = ITEMS.registerSimpleItem("raw_pale");

    public static final DeferredItem<Item> FIRE_ESSENCE = ITEMS.registerSimpleItem("fire_essence");

    private static final float PLAYER_BASE_DAMAGE = 1.0F;
    private static final float VANILLA_BASE_ATTACK_SPEED = 4.0F;

    public static final DeferredItem<com.zavidvi.voidmod.item.ScytheItem> GOLD_SCYTHE =
            registerScythe("gold_scythe", ToolMaterial.GOLD, 4.0F, 1.4F, false);

    public static final DeferredItem<com.zavidvi.voidmod.item.ScytheItem> IRON_SCYTHE =
            registerScythe("iron_scythe", ToolMaterial.IRON, 5.5F, 1.4F, false);

    public static final DeferredItem<com.zavidvi.voidmod.item.ScytheItem> DIAMOND_SCYTHE =
            registerScythe("diamond_scythe", ToolMaterial.DIAMOND, 6.0F, 1.6F, false);

    public static final DeferredItem<com.zavidvi.voidmod.item.ScytheItem> PALE_SCYTHE =
            registerScythe("pale_scythe", ModToolMaterials.PALE, 6.5F, 1.6F, false);

    public static final DeferredItem<com.zavidvi.voidmod.item.ScytheItem> NETHERITE_SCYTHE =
            registerScythe("netherite_scythe", ToolMaterial.NETHERITE, 6.5F, 1.8F, false);

    public static final DeferredItem<com.zavidvi.voidmod.item.ScytheItem> REAPER_SCYTHE =
            registerScythe("reaper_scythe", ModToolMaterials.REAPER, 5.5F, 1.8F, true);

    private static DeferredItem<com.zavidvi.voidmod.item.ScytheItem> registerScythe(
            String name, ToolMaterial material, float damage, float attackSpeed, boolean bleedingOnCrit) {
        float damageBaseline = damage - PLAYER_BASE_DAMAGE - material.attackDamageBonus();
        float speedBaseline = attackSpeed - VANILLA_BASE_ATTACK_SPEED;

        return ITEMS.registerItem(name,
                props -> new com.zavidvi.voidmod.item.ScytheItem(props, name, damage, attackSpeed, bleedingOnCrit),
                props -> props.sword(material, damageBaseline, speedBaseline)
                        .component(net.minecraft.core.component.DataComponents.MINIMUM_ATTACK_CHARGE,
                                com.zavidvi.voidmod.item.ScytheItem.MINIMUM_ATTACK_CHARGE));
    }

    public static final DeferredItem<net.minecraft.world.item.BucketItem> LIGHT_WATER_BUCKET =
            ITEMS.registerItem("light_water_bucket",
                    props -> new net.minecraft.world.item.BucketItem(ModFluids.LIGHT_WATER.get(), props),
                    props -> props.craftRemainder(net.minecraft.world.item.Items.BUCKET).stacksTo(1));

    public static final DeferredItem<com.zavidvi.voidmod.item.RimeItem> RIME =
            ITEMS.registerItem("rime", com.zavidvi.voidmod.item.RimeItem::new,
                    props -> props.stacksTo(1));

    public static final DeferredItem<Item> PALE_HELMET =
            armor("pale_helmet", ModArmorMaterials.PALE, ArmorType.HELMET);
    public static final DeferredItem<Item> PALE_CHESTPLATE =
            armor("pale_chestplate", ModArmorMaterials.PALE, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> PALE_LEGGINGS =
            armor("pale_leggings", ModArmorMaterials.PALE, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> PALE_BOOTS =
            armor("pale_boots", ModArmorMaterials.PALE, ArmorType.BOOTS);

    public static final DeferredItem<Item> LIGHTED_HOOD =
            armor("lighted_hood", ModArmorMaterials.LIGHTED, ArmorType.HELMET);
    public static final DeferredItem<Item> LIGHTED_MANTLE =
            armor("lighted_mantle", ModArmorMaterials.LIGHTED, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> LIGHTED_LEGGUARDS =
            armor("lighted_legguards", ModArmorMaterials.LIGHTED, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> LIGHTED_SANDALS =
            armor("lighted_sandals", ModArmorMaterials.LIGHTED, ArmorType.BOOTS);

    public static final DeferredItem<Item> PRISMARINE_HOOD =
            armor("prismarine_hood", ModArmorMaterials.PRISMARINE, ArmorType.HELMET);
    public static final DeferredItem<Item> PRISMARINE_JACKET =
            armor("prismarine_jacket", ModArmorMaterials.PRISMARINE, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> PRISMARINE_KNEE_PADS =
            armor("prismarine_knee_pads", ModArmorMaterials.PRISMARINE, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> PRISMARINE_BOOTS =
            armor("prismarine_boots", ModArmorMaterials.PRISMARINE, ArmorType.BOOTS);

    private static DeferredItem<Item> armor(String name, ArmorMaterial material, ArmorType type) {
        return ITEMS.registerSimpleItem(name, props -> props.humanoidArmor(material, type));
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

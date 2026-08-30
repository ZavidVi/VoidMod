package com.zavidvi.voidmod.registry;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public class ModToolMaterials {
    private static final TagKey<Item> REPAIRS_PALE =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                    net.minecraft.resources.Identifier.fromNamespaceAndPath(
                            com.zavidvi.voidmod.VoidMod.MOD_ID, "repairs_pale_armor"));

    public static final ToolMaterial PALE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1800, 8.5F, 3.0F, 12, REPAIRS_PALE);

    public static final ToolMaterial REAPER = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2031, 9.0F, 4.0F, 15,
            ItemTags.NETHERITE_TOOL_MATERIALS);

    private ModToolMaterials() {}
}

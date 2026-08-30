package com.zavidvi.voidmod.registry;

import com.zavidvi.voidmod.VoidMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    private ModTags() {}

    public static final TagKey<Block> VRAUJ_CANNOT_ABSORB = blockTag("vrauj_cannot_absorb");

    private static TagKey<Block> blockTag(String name) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, name));
    }
}

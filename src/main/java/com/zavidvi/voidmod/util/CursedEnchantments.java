package com.zavidvi.voidmod.util;

import com.zavidvi.voidmod.world.curse.CurseHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class CursedEnchantments {
    private CursedEnchantments() {}

    public static boolean isSuppressed(Holder<Enchantment> enchantment) {
        if (enchantment == null) {
            return false;
        }
        return enchantment.is(Enchantments.FIRE_ASPECT)
                || enchantment.is(Enchantments.FLAME)
                || enchantment.unwrapKey().map(key -> key.identifier().getPath().equals("lunge")).orElse(false);
    }

    public static EnchantmentHelper.EnchantmentVisitor filter(EnchantmentHelper.EnchantmentVisitor visitor) {
        if (visitor == null || !CurseHelper.isWorldCursedGlobal()) {
            return visitor;
        }
        return (enchantment, level) -> {
            if (!isSuppressed(enchantment)) {
                visitor.accept(enchantment, level);
            }
        };
    }

    public static EnchantmentHelper.EnchantmentInSlotVisitor filter(EnchantmentHelper.EnchantmentInSlotVisitor visitor) {
        if (visitor == null || !CurseHelper.isWorldCursedGlobal()) {
            return visitor;
        }
        return (enchantment, level, item) -> {
            if (!isSuppressed(enchantment)) {
                visitor.accept(enchantment, level, item);
            }
        };
    }
}

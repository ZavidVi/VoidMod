package com.zavidvi.voidmod.stat;

import com.zavidvi.voidmod.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;

public final class UniversalArmor {
    private UniversalArmor() {}

    public static boolean isUniversal(ItemStack stack) {
        ArmorStats.Entry entry = ArmorStats.get(stack);
        return entry != null && entry.armorClass() == ArmorClass.UNIVERSAL;
    }

    public static DexterityType getChoice(ItemStack stack) {
        String id = stack.get(ModDataComponents.CHOSEN_DEXTERITY.get());
        if (id == null) return DexterityType.NONE;

        return switch (ArmorClass.byId(id)) {
            case MELEE -> DexterityType.MELEE;
            case RANGED -> DexterityType.RANGED;
            default -> DexterityType.NONE;
        };
    }

    public static boolean hasChoice(ItemStack stack) {
        return getChoice(stack) != DexterityType.NONE;
    }

    public static boolean choose(ItemStack stack, DexterityType type) {
        if (!isUniversal(stack) || hasChoice(stack)) return false;
        if (type != DexterityType.MELEE && type != DexterityType.RANGED) return false;

        ArmorClass armorClass = type == DexterityType.MELEE ? ArmorClass.MELEE : ArmorClass.RANGED;
        stack.set(ModDataComponents.CHOSEN_DEXTERITY.get(), armorClass.getId());
        return true;
    }

    public static DexterityType effectiveType(ItemStack stack) {
        ArmorStats.Entry entry = ArmorStats.get(stack);
        if (entry == null) return DexterityType.NONE;

        if (entry.armorClass() == ArmorClass.UNIVERSAL) {
            return getChoice(stack);
        }
        return entry.armorClass().getDexterityType();
    }
}

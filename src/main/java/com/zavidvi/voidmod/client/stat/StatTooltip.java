package com.zavidvi.voidmod.client.stat;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.stat.ArmorClass;
import com.zavidvi.voidmod.stat.ArmorStats;
import com.zavidvi.voidmod.stat.DexterityType;
import com.zavidvi.voidmod.stat.PlayerStat;
import com.zavidvi.voidmod.stat.StatData;
import com.zavidvi.voidmod.stat.StatManager;
import com.zavidvi.voidmod.stat.StatRounding;
import com.zavidvi.voidmod.stat.UniversalArmor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

@EventBusSubscriber(modid = VoidMod.MOD_ID, value = Dist.CLIENT)
public class StatTooltip {
    private static final double BOW_FULL_DRAW_DAMAGE = 6.0;
    private static final double CROSSBOW_DAMAGE = 7.0;

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        ArmorStats.Entry armor = ArmorStats.get(stack);
        if (armor != null) {
            hideVanillaAttributes(event.getToolTip());
            armorTooltip(event, stack, armor);
            return;
        }

        DexterityType weaponType = weaponType(stack);
        if (weaponType != null) {
            hideVanillaAttributes(event.getToolTip());
            weaponTooltip(event, stack, weaponType);
        }
    }

    private static void armorTooltip(ItemTooltipEvent event, ItemStack stack, ArmorStats.Entry entry) {
        List<Component> tooltip = event.getToolTip();

        if (!net.minecraft.client.Minecraft.getInstance().hasShiftDown()) {
            tooltip.add(hint());
            return;
        }

        boolean any = false;
        for (PlayerStat stat : PlayerStat.values()) {
            double value = entry.stat(stat);
            if (value == 0.0) continue;
            any = true;
            tooltip.add(Component.literal(StatDisplay.signed(value) + " ")
                    .append(StatDisplay.name(stat))
                    .withStyle(s -> s.withColor(StatDisplay.color(stat))));
        }

        double armorValue = pieceArmor(entry);
        if (armorValue != 0.0) {
            any = true;
            tooltip.add(Component.translatable("voidmod.tooltip.armor_value")
                    .append(": " + StatDisplay.signed(armorValue))
                    .withStyle(s -> s.withColor(StatDisplay.COLOR_NEUTRAL)));
        }

        if (!any) {
            tooltip.add(Component.translatable("voidmod.tooltip.no_stats")
                    .withStyle(s -> s.withColor(StatDisplay.COLOR_HINT)));
        }

        appendArmorType(tooltip, stack, entry);
    }

    private static void appendArmorType(List<Component> tooltip, ItemStack stack, ArmorStats.Entry entry) {
        if (entry.armorClass() == ArmorClass.NONE) return;

        DexterityType type = UniversalArmor.effectiveType(stack);
        if (type == DexterityType.NONE) {
            tooltip.add(Component.translatable("voidmod.tooltip.armor_type_unset")
                    .withStyle(s -> s.withColor(StatDisplay.COLOR_HINT)));
            return;
        }

        tooltip.add(Component.translatable("voidmod.tooltip.armor_type", StatDisplay.typeName(type))
                .withStyle(s -> s.withColor(StatDisplay.COLOR_DEXTERITY)));
    }

    private static double pieceArmor(ArmorStats.Entry entry) {
        double fromVitality = entry.stat(PlayerStat.VITALITY) * StatData.ARMOR_PER_VITALITY;
        double fromDexterity = entry.stat(PlayerStat.DEXTERITY) * StatData.ARMOR_PER_DEXTERITY;
        return StatRounding.toHalf(fromVitality + fromDexterity + entry.flatArmor());
    }

    private static void weaponTooltip(ItemTooltipEvent event, ItemStack stack, DexterityType weaponType) {
        List<Component> tooltip = event.getToolTip();

        if (!net.minecraft.client.Minecraft.getInstance().hasShiftDown()) {
            tooltip.add(hint());
            return;
        }

        Player player = event.getEntity();
        double multiplier = 1.0;
        double playerBaseDamage = 1.0;
        double dexterityPercent = 0.0;

        if (player != null) {
            StatData data = StatManager.compute(player);
            multiplier = data.physicalDamageMultiplier(weaponType)
                    * data.negativeVitalityDamageMultiplier();
            dexterityPercent = data.dexterityDamagePercent(weaponType);
            playerBaseDamage = player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
        }

        String labelKey;
        double total;
        if (weaponType == DexterityType.MELEE) {
            labelKey = "voidmod.tooltip.damage";
            total = (playerBaseDamage + modifierAmount(stack, Attributes.ATTACK_DAMAGE)) * multiplier;
        } else if (stack.getItem() instanceof CrossbowItem) {
            labelKey = "voidmod.tooltip.projectile_damage";
            total = CROSSBOW_DAMAGE * multiplier;
        } else {
            labelKey = "voidmod.tooltip.full_draw_damage";
            total = BOW_FULL_DRAW_DAMAGE * multiplier;
        }

        if (stack.getItem() instanceof com.zavidvi.voidmod.item.ScytheItem) {
            double hit1 = total * com.zavidvi.voidmod.item.ScytheCombo.damageMultiplier(com.zavidvi.voidmod.item.ScytheCombo.HIT_SWEEP);
            double hit2 = total * com.zavidvi.voidmod.item.ScytheCombo.damageMultiplier(com.zavidvi.voidmod.item.ScytheCombo.HIT_CIRCLE);
            double hit3 = total * com.zavidvi.voidmod.item.ScytheCombo.damageMultiplier(com.zavidvi.voidmod.item.ScytheCombo.HIT_CRIT);
            tooltip.add(Component.translatable(labelKey)
                    .append(": " + StatDisplay.number(round1(hit1)) + " | "
                            + StatDisplay.number(round1(hit2)) + " | "
                            + StatDisplay.number(round1(hit3)))
                    .withStyle(s -> s.withColor(StatDisplay.COLOR_NEUTRAL)));
        } else {
            tooltip.add(Component.translatable(labelKey)
                    .append(": " + StatDisplay.number(round1(total)))
                    .withStyle(s -> s.withColor(StatDisplay.COLOR_NEUTRAL)));
        }

        if (weaponType == DexterityType.MELEE) {
            double attackSpeed = 4.0 + modifierAmount(stack, Attributes.ATTACK_SPEED);
            tooltip.add(Component.translatable("voidmod.tooltip.attack_speed")
                    .append(": " + StatDisplay.number(round1(attackSpeed)))
                    .withStyle(s -> s.withColor(StatDisplay.COLOR_NEUTRAL)));
        }

        tooltip.add(Component.translatable("voidmod.tooltip.from_dexterity",
                        StatDisplay.signed(dexterityPercent))
                .withStyle(s -> s.withColor(StatDisplay.COLOR_DEXTERITY)));
    }

    private static void hideVanillaAttributes(List<Component> tooltip) {
        TreeSet<Integer> remove = new TreeSet<>(Comparator.reverseOrder());

        for (int i = 0; i < tooltip.size(); i++) {
            String key = translationKey(tooltip.get(i));
            if (key == null) continue;

            if (key.startsWith("attribute.modifier.")) {
                remove.add(i);
            } else if (key.startsWith("item.modifiers.")) {
                remove.add(i);
                if (i > 0 && tooltip.get(i - 1).getString().isEmpty()) {
                    remove.add(i - 1);
                }
            }
        }

        for (int index : remove) {
            if (index >= 0 && index < tooltip.size()) {
                tooltip.remove(index);
            }
        }
    }

    private static String translationKey(Component component) {
        if (component.getContents() instanceof TranslatableContents contents) {
            return contents.getKey();
        }
        for (Component sibling : component.getSiblings()) {
            String key = translationKey(sibling);
            if (key != null) return key;
        }
        return null;
    }

    private static double modifierAmount(ItemStack stack,
                                         net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute) {
        double[] sum = {0.0};
        stack.forEachModifier(EquipmentSlot.MAINHAND, (holder, modifier) -> {
            if (holder.value() != attribute.value()) return;
            if (modifier.operation() != AttributeModifier.Operation.ADD_VALUE) return;
            sum[0] += modifier.amount();
        });
        return sum[0];
    }

    private static DexterityType weaponType(ItemStack stack) {
        if (stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem) {
            return DexterityType.RANGED;
        }
        return modifierAmount(stack, Attributes.ATTACK_DAMAGE) > 0.0 ? DexterityType.MELEE : null;
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static Component hint() {
        return Component.translatable("voidmod.tooltip.hold_shift")
                .withStyle(s -> s.withColor(StatDisplay.COLOR_HINT));
    }
}

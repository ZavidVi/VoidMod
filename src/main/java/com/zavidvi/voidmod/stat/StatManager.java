package com.zavidvi.voidmod.stat;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.registry.ModAttachments;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class StatManager {
    private static final Identifier HEALTH_MODIFIER =
            Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "stat_health");
    private static final Identifier ARMOR_MODIFIER =
            Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "stat_armor");
    private static final Identifier SPEED_MODIFIER =
            Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, "stat_movement_speed");

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private static final double VANILLA_BASE_HEALTH = 20.0;

    private StatManager() {}

    public static StatData get(Player player) {
        return player.getData(ModAttachments.PLAYER_STATS.get());
    }

    public static StatData compute(Player player) {
        StatData data = new StatData();

        double[] sums = new double[PlayerStat.values().length];
        for (PlayerStat stat : PlayerStat.values()) {
            sums[stat.ordinal()] = stat.getBase();
        }

        double flatArmor = 0.0;
        int melee = 0;
        int ranged = 0;

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            ArmorStats.Entry entry = ArmorStats.get(stack);
            if (entry == null) continue;

            for (PlayerStat stat : PlayerStat.values()) {
                sums[stat.ordinal()] += entry.stat(stat);
            }
            flatArmor += entry.flatArmor();

            switch (UniversalArmor.effectiveType(stack)) {
                case MELEE -> melee++;
                case RANGED -> ranged++;
                default -> {  }
            }
        }

        for (PlayerStat stat : PlayerStat.values()) {
            data.set(stat, StatRounding.toHalf(sums[stat.ordinal()]));
        }
        data.setFlatArmor(flatArmor);
        data.setDexterityType(resolveDexterityType(melee, ranged));
        return data;
    }

    public static void recalculate(Player player) {
        StatData data = compute(player);
        player.setData(ModAttachments.PLAYER_STATS.get(), data);
        apply(player, data);

        if (player instanceof ServerPlayer serverPlayer) {
            com.zavidvi.voidmod.advancement.ArmorAdvancements.check(serverPlayer, data);
        }
    }

    public static DexterityType resolveDexterityType(int melee, int ranged) {
        if (melee >= 3) return DexterityType.MELEE;
        if (ranged >= 3) return DexterityType.RANGED;
        if (melee == 0 && ranged == 0) return DexterityType.NONE;
        if (melee == ranged) return DexterityType.SPLIT;
        return melee > ranged ? DexterityType.MELEE : DexterityType.RANGED;
    }

    public static void refreshAll(MinecraftServer server) {
        if (server == null) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            recalculate(player);
        }
    }

    private static void apply(Player player, StatData data) {
        applyModifier(player.getAttribute(Attributes.MAX_HEALTH), HEALTH_MODIFIER,
                data.maxHealth() - VANILLA_BASE_HEALTH,
                AttributeModifier.Operation.ADD_VALUE);

        applyModifier(player.getAttribute(Attributes.ARMOR), ARMOR_MODIFIER,
                data.armor(), AttributeModifier.Operation.ADD_VALUE);

        applyModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_MODIFIER,
                data.movementSpeedPercent() / 100.0,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void applyModifier(AttributeInstance instance, Identifier id,
                                      double amount, AttributeModifier.Operation operation) {
        if (instance == null) return;
        instance.removeModifier(id);
        if (amount != 0.0) {
            instance.addTransientModifier(new AttributeModifier(id, amount, operation));
        }
    }
}

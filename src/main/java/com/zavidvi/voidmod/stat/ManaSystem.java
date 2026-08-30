package com.zavidvi.voidmod.stat;

import com.zavidvi.voidmod.registry.ModAttachments;
import net.minecraft.world.entity.player.Player;

public final class ManaSystem {
    private static final int TICKS_PER_SECOND = 20;

    private ManaSystem() {}

    public static double get(Player player) {
        return player.getData(ModAttachments.MANA.get());
    }

    public static void set(Player player, double value) {
        player.setData(ModAttachments.MANA.get(), Math.max(0.0, value));
    }

    public static double max(Player player) {
        return StatManager.get(player).maxMana();
    }

    public static boolean spend(Player player, double cost) {
        double current = get(player);
        if (current < cost) return false;
        set(player, current - cost);
        return true;
    }

    public static void tick(Player player) {
        StatData data = StatManager.get(player);
        double max = data.maxMana();
        double current = get(player);

        if (current > max) {
            set(player, max);
            return;
        }
        if (current >= max) return;

        double perTick = data.manaRegen() / TICKS_PER_SECOND;
        if (perTick <= 0.0) return;

        set(player, Math.min(max, current + perTick));
    }
}

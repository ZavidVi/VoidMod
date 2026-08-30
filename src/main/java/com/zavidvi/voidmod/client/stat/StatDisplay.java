package com.zavidvi.voidmod.client.stat;

import com.zavidvi.voidmod.stat.DexterityType;
import com.zavidvi.voidmod.stat.PlayerStat;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class StatDisplay {
    public static final int COLOR_VITALITY = 0xFF8F27;
    public static final int COLOR_DEXTERITY = 0x37D837;
    public static final int COLOR_INTELLIGENCE = 0x13C0F5;

    public static final int COLOR_NEGATIVE = 0xFF2A2A;

    public static final int COLOR_HINT = 0x8B8B8B;
    public static final int COLOR_NEUTRAL = 0xC8C8C8;

    public static final int COLOR_LABEL = 0x3F3F3F;

    private StatDisplay() {}

    public static int color(PlayerStat stat) {
        return switch (stat) {
            case VITALITY -> COLOR_VITALITY;
            case DEXTERITY -> COLOR_DEXTERITY;
            case INTELLIGENCE -> COLOR_INTELLIGENCE;
        };
    }

    public static Component name(PlayerStat stat) {
        return Component.translatable("voidmod.stat." + stat.getId());
    }

    public static Component typeName(DexterityType type) {
        return Component.translatable("voidmod.dexterity_type." + type.name().toLowerCase(java.util.Locale.ROOT));
    }

    public static Component description(PlayerStat stat, boolean negative, boolean shift) {
        StringBuilder key = new StringBuilder("voidmod.stat.").append(stat.getId());
        if (negative) key.append(".negative");
        key.append(".desc");
        if (shift) key.append(".shift");
        return Component.translatable(key.toString());
    }

    public static int lerpColor(int from, int to, float t) {
        t = Mth.clamp(t, 0.0F, 1.0F);
        int r = Mth.lerpInt(t, (from >> 16) & 0xFF, (to >> 16) & 0xFF);
        int g = Mth.lerpInt(t, (from >> 8) & 0xFF, (to >> 8) & 0xFF);
        int b = Mth.lerpInt(t, from & 0xFF, to & 0xFF);
        return (r << 16) | (g << 8) | b;
    }

    public static String number(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    public static String signed(double value) {
        return (value > 0 ? "+" : "") + number(value);
    }

    public static String regen(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }
}

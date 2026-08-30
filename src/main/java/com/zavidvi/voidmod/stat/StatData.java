package com.zavidvi.voidmod.stat;

import java.util.EnumMap;
import java.util.Map;

public class StatData {
    public static final double ARMOR_PER_VITALITY = 3.0;
    public static final double HP_PER_VITALITY = 2.0;

    public static final double ARMOR_PER_DEXTERITY = -0.1;
    public static final double DAMAGE_PERCENT_PER_DEXTERITY = 1.0;
    public static final double MOVE_CD_PERCENT_PER_DEXTERITY = -0.83;

    public static final double MANA_PER_INTELLIGENCE = 10.0;
    public static final double MANA_REGEN_PER_INTELLIGENCE = 0.2;
    public static final double HP_PER_INTELLIGENCE = -1.0;
    public static final double MAGIC_DAMAGE_PERCENT_PER_INTELLIGENCE = 2.5;

    public static final double HP_PER_NEGATIVE_INTELLIGENCE = 1.0;
    public static final double ARMOR_PER_NEGATIVE_DEXTERITY = 0.5;
    public static final double MOVE_SPEED_PERCENT_PER_NEGATIVE_DEXTERITY = -0.5;
    public static final double DAMAGE_PERCENT_PER_NEGATIVE_VITALITY = 5.0;

    public static final int NEGATIVE_INTELLIGENCE_GOLD_DURABILITY_FACTOR = 5;

    public static final double NEGATIVE_VITALITY_MAX_HEALTH = 1.0;

    public static final double MIN_MAX_HEALTH = 1.0;

    private final Map<PlayerStat, Double> values = new EnumMap<>(PlayerStat.class);
    private DexterityType dexterityType = DexterityType.NONE;

    private double flatArmor = 0.0;

    public StatData() {
        for (PlayerStat stat : PlayerStat.values()) {
            this.values.put(stat, stat.getBase());
        }
    }

    public double get(PlayerStat stat) {
        return this.values.getOrDefault(stat, stat.getBase());
    }

    public void set(PlayerStat stat, double value) {
        this.values.put(stat, value);
    }

    public DexterityType getDexterityType() {
        return this.dexterityType;
    }

    public void setDexterityType(DexterityType type) {
        this.dexterityType = type;
    }

    public double getFlatArmor() {
        return this.flatArmor;
    }

    public void setFlatArmor(double flatArmor) {
        this.flatArmor = flatArmor;
    }

    public boolean isNegative(PlayerStat stat) {
        return get(stat) < 0.0;
    }

    public double negativePoints(PlayerStat stat) {
        double value = get(stat);
        return value < 0.0 ? -value : 0.0;
    }

    public double armor() {
        if (isNegative(PlayerStat.VITALITY)) {
            return 0.0;
        }

        double fromVitality = (get(PlayerStat.VITALITY) - PlayerStat.VITALITY.getBase()) * ARMOR_PER_VITALITY;
        double fromDexterity = get(PlayerStat.DEXTERITY) * ARMOR_PER_DEXTERITY;
        double fromNegativeDexterity = negativePoints(PlayerStat.DEXTERITY) * ARMOR_PER_NEGATIVE_DEXTERITY;

        double total = fromVitality + fromDexterity + fromNegativeDexterity + this.flatArmor;
        return Math.max(0.0, StatRounding.toHalf(total));
    }

    public double maxHealth() {
        if (isNegative(PlayerStat.VITALITY)) {
            return NEGATIVE_VITALITY_MAX_HEALTH;
        }

        double fromVitality = get(PlayerStat.VITALITY) * HP_PER_VITALITY;
        double intelligenceOverBase =
                Math.max(0.0, get(PlayerStat.INTELLIGENCE) - PlayerStat.INTELLIGENCE.getBase());
        double penalty = intelligenceOverBase * HP_PER_INTELLIGENCE;
        double bonus = negativePoints(PlayerStat.INTELLIGENCE) * HP_PER_NEGATIVE_INTELLIGENCE;

        return Math.max(MIN_MAX_HEALTH, StatRounding.toHalf(fromVitality + penalty + bonus));
    }

    public double maxMana() {
        if (isNegative(PlayerStat.INTELLIGENCE)) {
            return 0.0;
        }
        return Math.max(0.0, get(PlayerStat.INTELLIGENCE) * MANA_PER_INTELLIGENCE);
    }

    public double manaRegen() {
        if (isNegative(PlayerStat.INTELLIGENCE)) {
            return 0.0;
        }
        return Math.max(0.0, get(PlayerStat.INTELLIGENCE) * MANA_REGEN_PER_INTELLIGENCE);
    }

    public double dexterityDamagePercent(DexterityType weaponType) {
        if (weaponType != DexterityType.MELEE && weaponType != DexterityType.RANGED) {
            return 0.0;
        }

        double bonus = get(PlayerStat.DEXTERITY) * DAMAGE_PERCENT_PER_DEXTERITY;

        if (this.dexterityType == DexterityType.SPLIT) {
            return StatRounding.toHalf(bonus * 0.5);
        }
        if (this.dexterityType == weaponType) {
            return StatRounding.toHalf(bonus);
        }
        return 0.0;
    }

    public double physicalDamageMultiplier(DexterityType weaponType) {
        return 1.0 + dexterityDamagePercent(weaponType) / 100.0;
    }

    public double magicDamagePercent() {
        double overBase = get(PlayerStat.INTELLIGENCE) - PlayerStat.INTELLIGENCE.getBase();
        return StatRounding.toHalf(overBase * MAGIC_DAMAGE_PERCENT_PER_INTELLIGENCE);
    }

    public double magicDamageMultiplier() {
        return 1.0 + magicDamagePercent() / 100.0;
    }

    public double negativeVitalityDamagePercent() {
        return StatRounding.toHalf(
                negativePoints(PlayerStat.VITALITY) * DAMAGE_PERCENT_PER_NEGATIVE_VITALITY);
    }

    public double negativeVitalityDamageMultiplier() {
        return 1.0 + negativeVitalityDamagePercent() / 100.0;
    }

    public double moveCooldownPercent() {
        return StatRounding.toHundredths(
                get(PlayerStat.DEXTERITY) * MOVE_CD_PERCENT_PER_DEXTERITY);
    }

    public double moveCooldownMultiplier() {
        return Math.max(0.0, 1.0 + moveCooldownPercent() / 100.0);
    }

    public double movementSpeedPercent() {
        return negativePoints(PlayerStat.DEXTERITY) * MOVE_SPEED_PERCENT_PER_NEGATIVE_DEXTERITY;
    }

    public double movementSpeedMultiplier() {
        return Math.max(0.0, 1.0 + movementSpeedPercent() / 100.0);
    }

    public int goldDurabilityFactor() {
        return isNegative(PlayerStat.INTELLIGENCE) ? NEGATIVE_INTELLIGENCE_GOLD_DURABILITY_FACTOR : 1;
    }
}

package com.zavidvi.voidmod.stat;

public enum PlayerStat {
    VITALITY("vitality", 10.0),
    DEXTERITY("dexterity", 0.0),
    INTELLIGENCE("intelligence", 10.0);

    private final String id;
    private final double base;

    PlayerStat(String id, double base) {
        this.id = id;
        this.base = base;
    }

    public String getId() {
        return this.id;
    }

    public double getBase() {
        return this.base;
    }

    public static PlayerStat byId(String id) {
        for (PlayerStat stat : values()) {
            if (stat.id.equalsIgnoreCase(id)) return stat;
        }
        return null;
    }
}

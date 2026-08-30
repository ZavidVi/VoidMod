package com.zavidvi.voidmod.stat;

public enum ArmorClass {
    MELEE("melee"),
    RANGED("ranged"),
    UNIVERSAL("universal"),
    NONE("none");

    private final String id;

    ArmorClass(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public DexterityType getDexterityType() {
        return switch (this) {
            case MELEE -> DexterityType.MELEE;
            case RANGED -> DexterityType.RANGED;
            case UNIVERSAL, NONE -> DexterityType.NONE;
        };
    }

    public static ArmorClass byId(String id) {
        for (ArmorClass value : values()) {
            if (value.id.equalsIgnoreCase(id)) return value;
        }
        return NONE;
    }
}

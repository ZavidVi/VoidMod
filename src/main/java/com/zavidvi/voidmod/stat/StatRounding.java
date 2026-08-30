package com.zavidvi.voidmod.stat;

public final class StatRounding {
    private StatRounding() {}

    public static double toHalf(double value) {
        double doubled = value * 2.0;
        double floor = Math.floor(doubled);
        return (doubled - floor >= 0.5 ? floor + 1.0 : floor) / 2.0;
    }

    public static double toHundredths(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

package com.zavidvi.voidmod.util;

public final class CurseLightState {
    private static volatile boolean serverCursed = false;
    private static volatile boolean clientCursed = false;

    private static volatile boolean serverReaperDefeated = false;

    private CurseLightState() {}

    public static boolean isCursed() {
        return serverCursed || clientCursed;
    }

    public static boolean isServerCursed() {
        return serverCursed;
    }

    public static boolean isClientCursed() {
        return clientCursed;
    }

    public static void setServerCursed(boolean cursed) {
        serverCursed = cursed;
    }

    public static void setClientCursed(boolean cursed) {
        clientCursed = cursed;
    }

    public static boolean isServerReaperDefeated() {
        return serverReaperDefeated;
    }

    public static void setServerReaperDefeated(boolean defeated) {
        serverReaperDefeated = defeated;
    }
}

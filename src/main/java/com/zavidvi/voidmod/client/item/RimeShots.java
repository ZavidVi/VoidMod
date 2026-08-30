package com.zavidvi.voidmod.client.item;

import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import net.minecraft.client.Minecraft;

public final class RimeShots {
    private static final Int2LongMap SHOTS = new Int2LongOpenHashMap();

    private RimeShots() {}

    public static void start(int ownerId, int ticks) {
        SHOTS.put(ownerId, currentTick() + ticks);
    }

    public static boolean isActive(int ownerId) {
        if (!SHOTS.containsKey(ownerId)) return false;

        if (currentTick() >= SHOTS.get(ownerId)) {
            SHOTS.remove(ownerId);
            return false;
        }
        return true;
    }

    private static long currentTick() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level == null ? 0L : minecraft.level.getGameTime();
    }
}

package com.zavidvi.voidmod.client.item;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;

public final class ScytheSwings {
    private ScytheSwings() {}

    private record Swing(int hit, long endsAtTick) {}

    private static final Int2ObjectMap<Swing> SWINGS = new Int2ObjectOpenHashMap<>();

    public static void start(int ownerId, int hit, int ticks) {
        SWINGS.put(ownerId, new Swing(hit, currentTick() + ticks));
    }

    public static int activeHit(int ownerId) {
        Swing swing = SWINGS.get(ownerId);
        if (swing == null) return -1;

        if (currentTick() >= swing.endsAtTick()) {
            SWINGS.remove(ownerId);
            return -1;
        }

        return swing.hit();
    }

    private static long currentTick() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level == null ? 0L : minecraft.level.getGameTime();
    }
}

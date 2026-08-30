package com.zavidvi.voidmod.item;

import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ScytheCombo {
    private ScytheCombo() {}

    public static final int HIT_SWEEP = 0;
    public static final int HIT_CIRCLE = 1;
    public static final int HIT_CRIT = 2;

    private static final long COMBO_WINDOW_TICKS = 40L;

    private record State(int hit, long tick) {}

    private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();

    public static int registerHit(Player player) {
        if (player.level().isClientSide()) return currentHit(player);

        long now = player.level().getGameTime();
        State previous = STATES.get(player.getUUID());

        int hit = HIT_SWEEP;
        if (previous != null && now - previous.tick() <= COMBO_WINDOW_TICKS && previous.hit() < HIT_CRIT) {
            hit = previous.hit() + 1;
        }

        STATES.put(player.getUUID(), new State(hit, now));
        return hit;
    }

    public static int currentHit(Player player) {
        State state = STATES.get(player.getUUID());
        return state == null ? HIT_SWEEP : state.hit();
    }

    public static float damageMultiplier(int hit) {
        return switch (hit) {
            case HIT_CIRCLE -> 1.2F;
            case HIT_CRIT -> 1.5F;
            default -> 1.0F;
        };
    }

    public static void forget(Player player) {
        STATES.remove(player.getUUID());
    }
}

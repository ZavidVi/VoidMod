package com.zavidvi.voidmod.world.curse;

import com.zavidvi.voidmod.util.CurseLightState;
import com.zavidvi.voidmod.world.progression.ClientProgressionData;
import com.zavidvi.voidmod.world.progression.WorldProgressionData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class CurseHelper {
    public static boolean isWorldCursed(Level level) {
        if (level == null) return isWorldCursedGlobal();
        
        if (level.isClientSide()) {
            return isClientCursed();
        } else if (level instanceof ServerLevel serverLevel) {
            return WorldProgressionData.get(serverLevel).isWorldCursed();
        }
        return false;
    }

    public static boolean isWorldCursedGlobal() {
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            return CurseLightState.isServerCursed();
        }
        return FMLEnvironment.getDist() == Dist.CLIENT && isClientCursed();
    }

    public static boolean isLighterCursed(Level level) {
        if (!isWorldCursed(level)) return false;

        if (level instanceof ServerLevel serverLevel) {
            return !WorldProgressionData.get(serverLevel).isReaperDefeated();
        }
        if (level != null && level.isClientSide()) return !ClientProgressionData.reaperDefeated;
        return !isReaperDefeatedGlobal();
    }

    private static boolean isReaperDefeatedGlobal() {
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            return CurseLightState.isServerReaperDefeated();
        }
        return FMLEnvironment.getDist() == Dist.CLIENT && ClientProgressionData.reaperDefeated;
    }

    private static boolean isClientCursed() {
        return ClientProgressionData.worldCursed;
    }
}

package com.zavidvi.voidmod.world.curse;

import com.mojang.serialization.MapCodec;
import com.zavidvi.voidmod.registry.ModPlacementModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class CursedPlacementFilter extends PlacementFilter {
    private static final CursedPlacementFilter INSTANCE = new CursedPlacementFilter();

    public static final MapCodec<CursedPlacementFilter> CODEC = MapCodec.unit(() -> INSTANCE);

    private CursedPlacementFilter() {}

    public static CursedPlacementFilter cursed() {
        return INSTANCE;
    }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos origin) {
        return CurseHelper.isWorldCursedGlobal();
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModPlacementModifiers.CURSED.get();
    }
}

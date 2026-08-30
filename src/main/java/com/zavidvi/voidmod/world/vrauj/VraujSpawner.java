package com.zavidvi.voidmod.world.vrauj;

import com.zavidvi.voidmod.entity.vrauj.VraujEntity;
import com.zavidvi.voidmod.registry.ModEntities;
import com.zavidvi.voidmod.world.StructureProximity;
import com.zavidvi.voidmod.world.curse.LightWaterPlacement;
import com.zavidvi.voidmod.world.progression.WorldProgressionData;
import com.zavidvi.voidmod.world.supervoid.SupervoidEncounters;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.List;
import java.util.Set;

public final class VraujSpawner {
    private static final long COOLDOWN_TICKS = 24000L;

    private static final int CHECK_INTERVAL = 200;

    private static final int SEARCH_CHUNK_RADIUS = 1;

    private static final double OCCUPANCY_RADIUS = 48.0;

    private static final int SPAWN_MIN = 1;
    private static final int SPAWN_MAX = 2;

    private static final ResourceKey<Structure>[] STRUCTURES = new ResourceKey[] {
            SupervoidEncounters.SUPERVOID,
            LightWaterPlacement.LIGHTED_FOUNTAIN
    };

    private VraujSpawner() {}

    public static void tick(ServerLevel level, ServerPlayer player) {
        if (player.tickCount % CHECK_INTERVAL != 0) return;

        BlockPos anchor = structureNear(level, player.blockPosition());
        if (anchor == null) return;

        WorldProgressionData data = WorldProgressionData.get(level);
        long now = level.getGameTime();
        if (now < data.getNextVraujSpawnTime()) return;

        if (!SupervoidEncounters.isCleared(level, anchor)
                && !SupervoidEncounters.swarmAround(level, anchor).isEmpty()) {
            return;
        }

        List<VraujEntity> existing = level.getEntitiesOfClass(VraujEntity.class,
                new AABB(anchor).inflate(OCCUPANCY_RADIUS), VraujEntity::isAlive);
        if (!existing.isEmpty()) return;

        int count = SPAWN_MIN + level.getRandom().nextInt(SPAWN_MAX - SPAWN_MIN + 1);
        for (int i = 0; i < count; i++) {
            VraujEntity vrauj = ModEntities.VRAUJ.get().create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
            if (vrauj == null) continue;

            vrauj.snapTo(
                    anchor.getX() + 0.5 + level.getRandom().nextInt(9) - 4,
                    anchor.getY() + 6.0,
                    anchor.getZ() + 0.5 + level.getRandom().nextInt(9) - 4,
                    level.getRandom().nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(vrauj);
        }

        data.setNextVraujSpawnTime(now + COOLDOWN_TICKS);
    }

    private static BlockPos structureNear(ServerLevel level, BlockPos pos) {
        var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);

        Set<Structure> wanted = new ReferenceOpenHashSet<>();
        for (ResourceKey<Structure> key : STRUCTURES) {
            registry.get(key).map(net.minecraft.core.Holder::value).ifPresent(wanted::add);
        }
        if (wanted.isEmpty()) return null;

        return StructureProximity.centerNear(level, pos, SEARCH_CHUNK_RADIUS, wanted::contains);
    }
}

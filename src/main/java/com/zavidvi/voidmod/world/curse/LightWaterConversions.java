package com.zavidvi.voidmod.world.curse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zavidvi.voidmod.VoidMod;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LightWaterConversions extends SavedData {
    public static final int MAX_CONVERSIONS = 20;

    private static final String NAME = "voidmod_light_water_conversions";

    private final Map<Long, Integer> used = new HashMap<>();

    private record Entry(BlockPos pos, int count) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(Entry::pos),
                Codec.INT.fieldOf("count").forGetter(Entry::count)
        ).apply(instance, Entry::new));
    }

    public static final Codec<LightWaterConversions> CODEC = Entry.CODEC.listOf()
            .optionalFieldOf("conversions", List.of())
            .xmap(LightWaterConversions::new, LightWaterConversions::entries)
            .codec();

    private static final SavedDataType<LightWaterConversions> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, NAME),
            LightWaterConversions::new,
            CODEC);

    public LightWaterConversions() {}

    private LightWaterConversions(List<Entry> entries) {
        for (Entry entry : entries) {
            this.used.put(entry.pos().asLong(), entry.count());
        }
    }

    private List<Entry> entries() {
        return this.used.entrySet().stream()
                .map(e -> new Entry(BlockPos.of(e.getKey()), e.getValue()))
                .toList();
    }

    public static LightWaterConversions get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public int remaining(BlockPos pos) {
        return MAX_CONVERSIONS - used.getOrDefault(pos.asLong(), 0);
    }

    public int spend(BlockPos pos, int amount) {
        if (amount <= 0) return remaining(pos);

        long key = pos.asLong();
        used.merge(key, amount, Integer::sum);
        setDirty();
        return MAX_CONVERSIONS - used.get(key);
    }

    public void forget(BlockPos pos) {
        if (used.remove(pos.asLong()) != null) {
            setDirty();
        }
    }
}

package com.zavidvi.voidmod.client;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.entity.reaper.ReaperLvl2Entity;
import com.zavidvi.voidmod.entity.reaper.ReaperLvl3Entity;
import com.zavidvi.voidmod.registry.ModBlocks;
import com.zavidvi.voidmod.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SelectMusicEvent;

@EventBusSubscriber(modid = VoidMod.MOD_ID, value = Dist.CLIENT)
public final class VoidModMusic {
    private static final double REAPER_RADIUS = 48.0D;

    private static final int FOUNTAIN_RADIUS = 8;

    private static final int FOUNTAIN_HEIGHT = 4;

    private static final int FOUNTAIN_STEP = 2;

    private static final int RECHECK_TICKS = 20;

    private static final int LINGER_TICKS = 400;

    private static final long NOT_LINGERING = Long.MIN_VALUE;

    private static Music cached;

    private static long cachedAt;
    private static boolean cacheFilled;

    private static long lingerUntil = NOT_LINGERING;

    private VoidModMusic() {}

    @SubscribeEvent
    public static void onSelectMusic(SelectMusicEvent event) {
        Music music = current();
        if (music != null) {
            event.setMusic(music);
            lingerUntil = NOT_LINGERING;
            return;
        }

        if (!isOurs(event.getPlayingMusic())) {
            lingerUntil = NOT_LINGERING;
            return;
        }

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            lingerUntil = NOT_LINGERING;
            event.setMusic(null);
            return;
        }

        long now = level.getGameTime();
        if (lingerUntil == NOT_LINGERING) {
            lingerUntil = now + LINGER_TICKS;
            return;
        }
        if (now < lingerUntil) {
            return;
        }

        lingerUntil = NOT_LINGERING;
        event.setMusic(null);
    }

    private static boolean isOurs(SoundInstance playing) {
        if (playing == null) return false;

        Identifier id = playing.getIdentifier();
        return id.equals(ModSounds.MUSIC_REAPER_PHASE2.get().location())
                || id.equals(ModSounds.MUSIC_REAPER_PHASE3.get().location())
                || id.equals(ModSounds.MUSIC_LIGHT_FOUNTAIN.get().location());
    }

    private static Music current() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        Player player = minecraft.player;
        if (level == null || player == null) {
            cached = null;
            cacheFilled = false;
            return null;
        }

        long now = level.getGameTime();
        long since = now - cachedAt;
        if (cacheFilled && since >= 0 && since < RECHECK_TICKS) {
            return cached;
        }
        cachedAt = now;
        cacheFilled = true;
        cached = choose(level, player);
        return cached;
    }

    private static Music choose(ClientLevel level, Player player) {
        AABB around = player.getBoundingBox().inflate(REAPER_RADIUS);

        if (!level.getEntitiesOfClass(ReaperLvl3Entity.class, around, ReaperLvl3Entity::isAlive).isEmpty()) {
            return music(ModSounds.MUSIC_REAPER_PHASE3.getDelegate());
        }
        if (!level.getEntitiesOfClass(ReaperLvl2Entity.class, around, ReaperLvl2Entity::isAlive).isEmpty()) {
            return music(ModSounds.MUSIC_REAPER_PHASE2.getDelegate());
        }
        if (nearLightWater(level, player)) {
            return music(ModSounds.MUSIC_LIGHT_FOUNTAIN.getDelegate());
        }
        return null;
    }

    private static Music music(net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> sound) {
        return new Music(sound, 0, 0, true);
    }

    private static boolean nearLightWater(ClientLevel level, Player player) {
        BlockPos centre = player.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int dx = -FOUNTAIN_RADIUS; dx <= FOUNTAIN_RADIUS; dx += FOUNTAIN_STEP) {
            for (int dz = -FOUNTAIN_RADIUS; dz <= FOUNTAIN_RADIUS; dz += FOUNTAIN_STEP) {
                for (int dy = -FOUNTAIN_HEIGHT; dy <= FOUNTAIN_HEIGHT; dy += FOUNTAIN_STEP) {
                    cursor.set(centre.getX() + dx, centre.getY() + dy, centre.getZ() + dz);
                    if (level.getBlockState(cursor).is(ModBlocks.LIGHT_WATER.get())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

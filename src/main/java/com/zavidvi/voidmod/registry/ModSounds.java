package com.zavidvi.voidmod.registry;

import com.zavidvi.voidmod.VoidMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, VoidMod.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_SPHERE_HURT =
            registerSound("entity.void_sphere.hurt");

    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_SPHERE_DEATH =
            registerSound("entity.void_sphere.death");

    public static final DeferredHolder<SoundEvent, SoundEvent> VRAUJ_FLIGHT =
            registerSound("entity.vrauj.flight");

    public static final DeferredHolder<SoundEvent, SoundEvent> VRAUJ_SHOOT =
            registerSound("entity.vrauj.shoot");

    public static final DeferredHolder<SoundEvent, SoundEvent> VRAUJ_HURT =
            registerSound("entity.vrauj.hurt");

    public static final DeferredHolder<SoundEvent, SoundEvent> REAPER1_SPAWN =
            registerSound("entity.reaper1.spawn");

    public static final DeferredHolder<SoundEvent, SoundEvent> REAPER1_ATTACK =
            registerSound("entity.reaper1.attack");

    public static final DeferredHolder<SoundEvent, SoundEvent> REAPER1_DEATH =
            registerSound("entity.reaper1.death");

    public static final DeferredHolder<SoundEvent, SoundEvent> REAPER1_HURT =
            registerSound("entity.reaper1.hurt");

    public static final DeferredHolder<SoundEvent, SoundEvent> REAPER2_SPAWN =
            registerSound("entity.reaper2.spawn");

    public static final DeferredHolder<SoundEvent, SoundEvent> REAPER2_ATTACK1 =
            registerSound("entity.reaper2.attack1");

    public static final DeferredHolder<SoundEvent, SoundEvent> REAPER2_ATTACK2 =
            registerSound("entity.reaper2.attack2");

    public static final DeferredHolder<SoundEvent, SoundEvent> REAPER2_HURT =
            registerSound("entity.reaper2.hurt");

    public static final DeferredHolder<SoundEvent, SoundEvent> REAPER3_SPAWN =
            registerSound("entity.reaper3.spawn");

    public static final DeferredHolder<SoundEvent, SoundEvent> REAPER3_ATTACK1 =
            registerSound("entity.reaper3.attack1");

    public static final DeferredHolder<SoundEvent, SoundEvent> REAPER3_ATTACK2 =
            registerSound("entity.reaper3.attack2");

    public static final DeferredHolder<SoundEvent, SoundEvent> REAPER3_SPECIAL =
            registerSound("entity.reaper3.special");

    public static final DeferredHolder<SoundEvent, SoundEvent> REAPER3_HURT =
            registerSound("entity.reaper3.hurt");

    public static final DeferredHolder<SoundEvent, SoundEvent> REAPER3_PROJECTILE =
            registerSound("entity.reaper3.projectile");

    public static final DeferredHolder<SoundEvent, SoundEvent> REAPER3_EXPLOSION =
            registerSound("entity.reaper3.explosion");

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_REAPER_PHASE2 =
            registerSound("music.reaper_phase2");

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_REAPER_PHASE3 =
            registerSound("music.reaper_phase3");

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_LIGHT_FOUNTAIN =
            registerSound("music.light_fountain");

    public static final DeferredHolder<SoundEvent, SoundEvent> WORLD_FLAME_FADES =
            registerSound("event.world_flame_fades");

    private static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(
                Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}

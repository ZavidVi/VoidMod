package com.zavidvi.voidmod.event.stat;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.stat.ArmorStats;
import com.zavidvi.voidmod.stat.DexterityType;
import com.zavidvi.voidmod.stat.ManaSystem;
import com.zavidvi.voidmod.stat.StatData;
import com.zavidvi.voidmod.stat.StatManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = VoidMod.MOD_ID)
public class StatEvents {
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            StatManager.recalculate(player);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        StatManager.recalculate(event.getEntity());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        StatManager.recalculate(player);
        ManaSystem.set(player, ManaSystem.max(player));
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        StatManager.recalculate(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        ManaSystem.tick(event.getEntity());
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof Player attacker)) return;
        if (attacker.level().isClientSide()) return;

        Entity direct = source.getDirectEntity();
        DexterityType weaponType = (direct != null && direct != attacker)
                ? DexterityType.RANGED
                : DexterityType.MELEE;

        StatData data = StatManager.get(attacker);
        float multiplier = (float) (data.physicalDamageMultiplier(weaponType)
                * data.negativeVitalityDamageMultiplier());

        if (multiplier != 1.0f) {
            event.setAmount(event.getAmount() * multiplier);
        }
    }

    @SubscribeEvent
    public static void onItemAttributes(ItemAttributeModifierEvent event) {
        if (!ArmorStats.isManaged(event.getItemStack().getItem())) return;
        event.removeAllModifiersFor(Attributes.ARMOR);
        event.removeAllModifiersFor(Attributes.ARMOR_TOUGHNESS);
    }
}

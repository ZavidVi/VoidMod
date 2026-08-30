package com.zavidvi.voidmod.event.scythe;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.item.ScytheCombo;
import com.zavidvi.voidmod.item.ScytheItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = VoidMod.MOD_ID)
public class ScytheCombatEvents {
    private static boolean applyingSweepDamage = false;

    public static void withSweepDamage(Runnable damage) {
        applyingSweepDamage = true;
        try {
            damage.run();
        } finally {
            applyingSweepDamage = false;
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (applyingSweepDamage) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        if (event.getSource().getDirectEntity() != player) return;

        ItemStack weapon = player.getMainHandItem();
        if (!(weapon.getItem() instanceof ScytheItem)) return;

        int hit = ScytheCombo.currentHit(player);
        float multiplier = ScytheCombo.damageMultiplier(hit);
        if (multiplier != 1.0F) {
            event.setAmount(event.getAmount() * multiplier);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ScytheCombo.forget(event.getEntity());
    }
}

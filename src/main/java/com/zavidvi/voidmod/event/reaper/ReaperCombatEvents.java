package com.zavidvi.voidmod.event.reaper;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.entity.reaper.ReaperEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = VoidMod.MOD_ID)
public final class ReaperCombatEvents {
    private ReaperCombatEvents() {}

    private static final int BLOCKS_UNTIL_DISABLE = 3;

    private static final float DISABLE_SECONDS = 5.0F;

    private static final Map<UUID, Integer> BLOCKED_HITS = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        if (!event.getBlocked() || event.getBlockedDamage() <= 0.0F) return;
        if (!(event.getDamageSource().getEntity() instanceof ReaperEntity)) return;

        int blocked = BLOCKED_HITS.merge(player.getUUID(), 1, Integer::sum);
        if (blocked < BLOCKS_UNTIL_DISABLE) return;

        BLOCKED_HITS.remove(player.getUUID());
        disableShield(serverLevel, player);
    }

    private static void disableShield(ServerLevel level, Player player) {
        ItemStack shield = player.getItemBlockingWith();
        if (shield == null) return;

        BlocksAttacks blocksAttacks = shield.get(DataComponents.BLOCKS_ATTACKS);
        if (blocksAttacks == null) return;

        blocksAttacks.disable(level, player, DISABLE_SECONDS, shield);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        BLOCKED_HITS.remove(player.getUUID());

        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        despawnReapers(serverLevel, player);
    }

    private static void despawnReapers(ServerLevel level, Player player) {
        AABB area = player.getBoundingBox().inflate(ReaperEntity.GRAVE_CHASE_RADIUS);

        for (ReaperEntity reaper : level.getEntitiesOfClass(ReaperEntity.class, area,
                candidate -> candidate.getTarget() == player || candidate.isWithinChaseRange(player))) {
            reaper.despawn(level);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        BLOCKED_HITS.remove(event.getEntity().getUUID());
    }
}

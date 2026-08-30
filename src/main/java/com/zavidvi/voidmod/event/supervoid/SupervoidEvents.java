package com.zavidvi.voidmod.event.supervoid;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.entity.vrauj.VraujEntity;
import com.zavidvi.voidmod.world.supervoid.SupervoidEncounters;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = VoidMod.MOD_ID)
public class SupervoidEvents {
    private static final float PROTECTION_DAMAGE = 6.0F;

    private static final double PROTECTION_KNOCKBACK = 30.0;

    private static final double PROTECTION_LIFT = 0.8;

    private static final int AGGRO_CHECK_INTERVAL = 20;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel level)) return;
        if (player.tickCount % AGGRO_CHECK_INTERVAL != 0) return;

        if (player instanceof ServerPlayer serverPlayer) {
            com.zavidvi.voidmod.world.vrauj.VraujSpawner.tick(level, serverPlayer);
        }

        BlockPos center = SupervoidEncounters.structureCenterNear(level, player.blockPosition());
        if (center == null) return;

        SupervoidEncounters.ensurePopulated(level, center);
        SupervoidEncounters.aggro(level, center, player);
    }

    @SubscribeEvent
    public static void onVraujHurt(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof VraujEntity vrauj)) return;
        if (!(vrauj.level() instanceof ServerLevel level)) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        BlockPos center = vrauj.getOrbitCenter();
        if (center != null) {
            SupervoidEncounters.aggro(level, center, player);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(net.neoforged.neoforge.event.level.block.BreakBlockEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        Player player = event.getPlayer();
        if (player == null || player.isCreative()) return;

        BlockPos center = SupervoidEncounters.structureCenterAt(level, event.getPos());
        if (center == null) return;

        SupervoidEncounters.ensurePopulated(level, center);
        if (SupervoidEncounters.isCleared(level, center)) return;

        event.setCanceled(true);
        punish(player, center);
    }

    private static void punish(Player player, BlockPos center) {
        player.hurt(player.damageSources().magic(), PROTECTION_DAMAGE);

        Vec3 away = player.position().subtract(Vec3.atCenterOf(center));
        Vec3 direction = away.horizontalDistanceSqr() < 1.0E-4
                ? new Vec3(1.0, 0.0, 0.0)
                : new Vec3(away.x, 0.0, away.z).normalize();

        double impulse = PROTECTION_KNOCKBACK / 10.0;
        player.push(direction.x * impulse, PROTECTION_LIFT, direction.z * impulse);
        player.hurtMarked = true;

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(
                    new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(serverPlayer));
        }
    }
}

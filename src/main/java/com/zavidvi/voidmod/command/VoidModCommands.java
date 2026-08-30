package com.zavidvi.voidmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.network.SyncProgressionPayload;
import com.zavidvi.voidmod.world.progression.WorldProgressionData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = VoidMod.MOD_ID)
public class VoidModCommands {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("voidmod")
                        .requires(Commands.hasPermission(Commands.LEVEL_ALL))
                        .then(Commands.literal("reset")
                                .executes(context -> {
                                    ServerLevel level = context.getSource().getLevel();
                                    WorldProgressionData data = WorldProgressionData.get(level);
                                    data.setPortalAttempted(false);
                                    data.setWorldCursed(false);
                                    data.setWandererTalked(false);
                                    data.setReaperDefeated(false);

                                    PacketDistributor.sendToAllPlayers(SyncProgressionPayload.of(data));

                                    context.getSource().sendSuccess(() -> Component.literal("§a[VoidMod] Прогресс мира сброшен! (portalAttempted = false)"), true);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("status")
                                .executes(context -> {
                                    ServerLevel level = context.getSource().getLevel();
                                    WorldProgressionData data = WorldProgressionData.get(level);
                                    String msg = String.format("§b[VoidMod Status] portalAttempted: %b | worldCursed: %b | wandererTalked: %b | fireEssences: %d | destroyTick: %d (%.2fs) | lightQueue: %d",
                                            data.isPortalAttempted(), data.isWorldCursed(), data.isWandererTalked(),
                                            com.zavidvi.voidmod.entity.supervoid.TentacleEntity.DESTROY_PORTAL_TICK, com.zavidvi.voidmod.entity.supervoid.TentacleEntity.DESTROY_PORTAL_TICK / 20.0f,
                                            com.zavidvi.voidmod.event.curse.LightEvents.queuedCount());
                                    context.getSource().sendSuccess(() -> Component.literal(msg), false);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("setdestroytick")
                                .then(Commands.argument("tick", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 200))
                                        .executes(context -> {
                                            int tick = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "tick");
                                            com.zavidvi.voidmod.entity.supervoid.TentacleEntity.DESTROY_PORTAL_TICK = tick;
                                            float seconds = tick / 20.0f;
                                            context.getSource().sendSuccess(() -> Component.literal(String.format("§a[VoidMod] DESTROY_PORTAL_TICK установлен в %d тиков (%.2f сек)!", tick, seconds)), true);
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("relight")
                                .executes(context -> {
                                    int queued = com.zavidvi.voidmod.event.curse.LightEvents.forceRequeueAll();
                                    context.getSource().sendSuccess(() -> Component.literal("§e[VoidMod] Чанков в очереди на пересчёт света: " + queued), false);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("count_spheres")
                                .executes(context -> {
                                    ServerLevel level = context.getSource().getLevel();
                                    long count = level.getEntities(com.zavidvi.voidmod.registry.ModEntities.VOID_SPHERE.get(),
                                            s -> s.isPortalSphere() && s.getHealth() > 0).size();
                                    context.getSource().sendSuccess(() -> Component.literal("§d[VoidMod] Живых портальных сфер в мире: " + count), false);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("hitboxes")
                                .executes(VoidModCommands::showHitboxes)
                        )
                        .then(Commands.literal("wanderer")
                                .executes(VoidModCommands::locateWanderer)
                        )
                        .then(Commands.literal("spawn_reaper")
                                .then(Commands.argument("phase", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 3))
                                        .executes(VoidModCommands::spawnReaper)
                                )
                        )
                        .then(Commands.literal("spawn_wanderer")
                                .executes(context -> {
                                    if (context.getSource().getEntity() instanceof net.minecraft.world.entity.player.Player player) {
                                        ServerLevel level = context.getSource().getLevel();
                                        com.zavidvi.voidmod.entity.wanderer.WandererEntity wanderer = new com.zavidvi.voidmod.entity.wanderer.WandererEntity(com.zavidvi.voidmod.registry.ModEntities.WANDERER.get(), level);
                                        wanderer.setPos(player.getX(), player.getY(), player.getZ());
                                        level.addFreshEntity(wanderer);
                                        context.getSource().sendSuccess(() -> Component.literal("§a[VoidMod] Странница успешно заспавнена!"), false);
                                    } else {
                                        context.getSource().sendFailure(Component.literal("Команду может выполнить только игрок."));
                                    }
                                    return 1;
                                })
                        )
        );
    }

    private static int spawnReaper(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        int phase = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "phase");

        net.minecraft.world.entity.EntityType<? extends com.zavidvi.voidmod.entity.reaper.ReaperEntity> type =
                switch (phase) {
                    case 2 -> com.zavidvi.voidmod.registry.ModEntities.REAPER_LVL2.get();
                    case 3 -> com.zavidvi.voidmod.registry.ModEntities.REAPER_LVL3.get();
                    default -> com.zavidvi.voidmod.registry.ModEntities.REAPER_LVL1.get();
                };

        ServerLevel level = source.getLevel();
        net.minecraft.world.phys.Vec3 at = source.getPosition();

        com.zavidvi.voidmod.entity.reaper.ReaperEntity reaper = type.spawn(level,
                net.minecraft.core.BlockPos.containing(at),
                net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (reaper == null) {
            source.sendFailure(Component.literal("§c[VoidMod] Не удалось заспавнить вестника."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal(String.format(
                "§d[VoidMod] Вестник смерти фазы %d заспавнен (появление %.1f с).",
                phase, reaper.spawnAnimationTicks() / 20.0F)), false);
        return 1;
    }

    private static final double HITBOX_SCAN_RADIUS = 32.0;

    private static int showHitboxes(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        net.minecraft.world.phys.AABB area = net.minecraft.world.phys.AABB
                .ofSize(source.getPosition(), HITBOX_SCAN_RADIUS * 2, HITBOX_SCAN_RADIUS * 2, HITBOX_SCAN_RADIUS * 2);

        java.util.List<net.minecraft.world.entity.Mob> found = new java.util.ArrayList<>();
        found.addAll(level.getEntitiesOfClass(com.zavidvi.voidmod.entity.vrauj.VraujEntity.class, area,
                net.minecraft.world.entity.Entity::isAlive));
        found.addAll(level.getEntitiesOfClass(com.zavidvi.voidmod.entity.voidsphere.VoidSphereEntity.class, area,
                net.minecraft.world.entity.Entity::isAlive));

        if (found.isEmpty()) {
            source.sendSuccess(() -> Component.literal(String.format(
                    "§e[VoidMod] В радиусе %.0f м нет враужей и пустотных сфер.", HITBOX_SCAN_RADIUS)), false);
            return 0;
        }

        for (net.minecraft.world.entity.Mob mob : found) {
            net.minecraft.world.phys.AABB box = mob.getBoundingBox();
            String header = String.format("§d[VoidMod] %s (%d %d %d): бокс %.2f×%.2f×%.2f",
                    mob.getType().getDescription().getString(),
                    Math.round(mob.getX()), Math.round(mob.getY()), Math.round(mob.getZ()),
                    box.getXsize(), box.getYsize(), box.getZsize());
            source.sendSuccess(() -> Component.literal(header), false);

            if (mob instanceof com.zavidvi.voidmod.entity.vrauj.VraujEntity vrauj) {
                boolean vertical = box.getYsize() > box.getXsize();
                String line = String.format("  §7поза %s → бокс %s%s",
                        animationStateName(vrauj.getAnimationState()),
                        vertical ? "§aвертикальный" : "§aгоризонтальный",
                        vertical == vrauj.isAttacking() ? "" : " §c(не совпал с позой!)");
                source.sendSuccess(() -> Component.literal(line), false);
            }
        }
        return found.size();
    }

    private static String animationStateName(int state) {
        return switch (state) {
            case com.zavidvi.voidmod.entity.vrauj.VraujEntity.ANIM_STATE_ATTACK -> "заход";
            case com.zavidvi.voidmod.entity.vrauj.VraujEntity.ANIM_STATE_RASSTREL -> "расстрел";
            case com.zavidvi.voidmod.entity.vrauj.VraujEntity.ANIM_STATE_RASKID -> "раскид";
            default -> "полёт";
        };
    }

    private static int locateWanderer(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        java.util.List<com.zavidvi.voidmod.entity.wanderer.WandererEntity> found = new java.util.ArrayList<>();
        for (ServerLevel level : source.getServer().getAllLevels()) {
            found.addAll(level.getEntities(com.zavidvi.voidmod.registry.ModEntities.WANDERER.get(),
                    net.minecraft.world.entity.Entity::isAlive));
        }

        if (found.isEmpty()) {
            source.sendSuccess(() -> Component.literal(
                    "§e[VoidMod] Странницы нет в мире (искали по загруженным чанкам)."), false);
            return 0;
        }

        net.minecraft.world.phys.Vec3 from = source.getPosition();
        for (com.zavidvi.voidmod.entity.wanderer.WandererEntity wanderer : found) {
            String distance = wanderer.level() == source.getLevel()
                    ? String.format(", до неё %.1f м", wanderer.position().distanceTo(from))
                    : "";
            String msg = String.format("§d[VoidMod] Странница: %d %d %d (%s)%s",
                    Math.round(wanderer.getX()), Math.round(wanderer.getY()), Math.round(wanderer.getZ()),
                    wanderer.level().dimension().identifier(), distance);
            source.sendSuccess(() -> Component.literal(msg), false);
        }
        return found.size();
    }
}

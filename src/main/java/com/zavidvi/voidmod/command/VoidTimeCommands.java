package com.zavidvi.voidmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.zavidvi.voidmod.VoidMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = VoidMod.MOD_ID)
public final class VoidTimeCommands {
    private static final long DAY = 1000L;
    private static final long NOON = 6000L;
    private static final long NIGHT = 13000L;
    private static final long MIDNIGHT = 18000L;

    private static final int PERMISSION = 2;

    private VoidTimeCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("voidfreeze")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("day").executes(context -> freeze(context, DAY, "день")))
                .then(Commands.literal("noon").executes(context -> freeze(context, NOON, "полдень")))
                .then(Commands.literal("night").executes(context -> freeze(context, NIGHT, "ночь")))
                .then(Commands.literal("midnight").executes(context -> freeze(context, MIDNIGHT, "полночь")))
                .then(Commands.literal("unfreeze").executes(VoidTimeCommands::unfreeze)));

        dispatcher.register(Commands.literal("voidtime")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("unfreeze").executes(VoidTimeCommands::unfreeze)));
    }

    private static int freeze(CommandContext<CommandSourceStack> context, long time, String name) {
        MinecraftServer server = context.getSource().getServer();

        forEachClock(server, (clocks, clock) -> {
            clocks.setTotalTicks(clock, time);
            clocks.setPaused(clock, true);
        });

        context.getSource().sendSuccess(() -> Component.literal(
                "§b[VoidMod] Время суток: " + name + " (" + time + "). Солнце остановлено — "
                        + "/voidtime unfreeze, чтобы отпустить."), true);
        return 1;
    }

    private static void forEachClock(MinecraftServer server,
                                     java.util.function.BiConsumer<ServerClockManager, Holder<WorldClock>> action) {
        ServerClockManager clocks = server.clockManager();
        for (ServerLevel level : server.getAllLevels()) {
            level.dimensionTypeRegistration().value().defaultClock()
                    .ifPresent(clock -> action.accept(clocks, clock));
        }
    }

    private static int unfreeze(CommandContext<CommandSourceStack> context) {
        MinecraftServer server = context.getSource().getServer();
        forEachClock(server, (clocks, clock) -> clocks.setPaused(clock, false));

        context.getSource().sendSuccess(
                () -> Component.literal("§a[VoidMod] Солнце снова идёт своим ходом."), true);
        return 1;
    }
}

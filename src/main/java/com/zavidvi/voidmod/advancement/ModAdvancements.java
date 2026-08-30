package com.zavidvi.voidmod.advancement;

import com.zavidvi.voidmod.VoidMod;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class ModAdvancements {
    private static final String CODE_CRITERION = "code";

    public static final String VOID_MODE = "void_mode";

    public static final String TALE_OF_CRUEL_WORLD = "tale_of_cruel_world";

    public static final String LEGACY_OF_THE_VOID = "legacy_of_the_void";

    public static final String RESTORATION = "restoration";

    public static final String SOMETHING_NEW = "something_new";

    public static final String NUGGET = "nugget";

    public static final String STRONG_IN_SPIRIT = "strong_in_spirit";

    public static final String PATH_OF_THE_WARRIOR = "path_of_the_warrior";

    public static final String PATH_OF_THE_MARKSMAN = "path_of_the_marksman";

    public static final String PATH_OF_THE_MAGE = "path_of_the_mage";

    public static final String WHOSE_PATH_IS_THIS = "whose_path_is_this";

    private ModAdvancements() {}

    public static void grant(Player player, String path) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        grant(serverPlayer, path);
    }

    public static void grant(ServerPlayer player, String path) {
        MinecraftServer server = player.level().getServer();
        if (server == null) return;

        AdvancementHolder holder = server.getAdvancements()
                .get(Identifier.fromNamespaceAndPath(VoidMod.MOD_ID, path));
        if (holder == null) {
            VoidMod.LOGGER.warn("[advancements] нет достижения voidmod:{} — выдавать нечего", path);
            return;
        }

        player.getAdvancements().award(holder, CODE_CRITERION);
    }
}

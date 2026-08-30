package com.zavidvi.voidmod.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class VoidModConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.IntValue SKY_DARKEN;
    private static final ModConfigSpec.IntValue MAX_TICK_RATE;

    private static volatile int skyLightReduction = 3;

    private static volatile int maxTickRate = 100000000;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Настройки проклятия мира").push("curse");

        SKY_DARKEN = builder
                .comment("На сколько уменьшается уровень небесного света в проклятом мире.",
                        "Это то самое значение, которое в F3 показано как «N sky»:",
                        "под открытым небом 15 - 3 = 12, под блоком 14 - 3 = 11 и так далее.",
                        "От него же зависит спавн мобов под навесами.",
                        "Действует только в Верхнем мире: в Аду и Энде небо не трогается.")
                .defineInRange("skyLightReduction", 3, 0, 15);

        builder.pop();

        builder.comment("Прочее").push("misc");

        MAX_TICK_RATE = builder
                .comment("Верхняя граница аргумента ванильной команды /tick rate.",
                        "Ванильный предел — 10000; значения выше нужны для отладки кат-сцен.",
                        "Читается один раз при регистрации команд, /reload его не подхватит.")
                .defineInRange("maxTickRate", 100000, 1, 1000000);

        builder.pop();

        SPEC = builder.build();
    }

    private VoidModConfig() {}

    public static int skyLightReduction() {
        return skyLightReduction;
    }

    public static int maxTickRate() {
        return maxTickRate;
    }

    public static void refresh() {
        skyLightReduction = SKY_DARKEN.get();
        maxTickRate = MAX_TICK_RATE.get();
    }
}

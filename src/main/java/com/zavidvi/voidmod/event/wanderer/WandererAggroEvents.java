package com.zavidvi.voidmod.event.wanderer;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.entity.wanderer.WandererEntity;
import com.zavidvi.voidmod.entity.voidsphere.VoidSphereEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = VoidMod.MOD_ID)
public class WandererAggroEvents {
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof Mob mob) || !(mob instanceof Enemy)) {
            return;
        }
        if (mob instanceof Creeper || mob instanceof VoidSphereEntity) {
            return;
        }

        mob.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(mob, WandererEntity.class, true));
    }
}

package com.zavidvi.voidmod.item;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoItemRenderer;
import com.zavidvi.voidmod.client.renderer.rime.RimeRenderer;
import com.zavidvi.voidmod.entity.rime.RimeProjectileEntity;
import com.zavidvi.voidmod.stat.ManaSystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class RimeItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = com.geckolib.util.GeckoLibUtil.createInstanceCache(this);

    public static final double MANA_COST = 3.0D;

    public static final int COOLDOWN_TICKS = 12;

    public static final int ATTACK_ANIMATION_TICKS = 10;

    private static final float PROJECTILE_SPEED = 1.5F;

    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("Idle");
    private static final RawAnimation ANIM_ATTACK = RawAnimation.begin().thenPlay("attack");

    private static final String CONTROLLER = "controller";

    public static final DataTicket<Integer> OWNER_ID =
            DataTicket.create("voidmod:rime_owner", Integer.class);

    public static final int NO_OWNER = -1;

    public RimeItem(Properties properties) {
        super(properties);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.PASS;
        }
        if (ManaSystem.get(player) < MANA_COST) {
            return InteractionResult.FAIL;
        }

        if (level instanceof ServerLevel serverLevel) {
            if (!ManaSystem.spend(player, MANA_COST)) {
                return InteractionResult.FAIL;
            }

            RimeProjectileEntity shot = new RimeProjectileEntity(serverLevel, player);
            shot.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, PROJECTILE_SPEED, 1.0F);
            serverLevel.addFreshEntity(shot);
        } else {
            com.zavidvi.voidmod.client.item.RimeShots.start(player.getId(), ATTACK_ANIMATION_TICKS);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private RimeRenderer renderer;

            @Override
            public GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new RimeRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<RimeItem>(CONTROLLER, 0, state -> {
            int ownerId = state.getDataOrDefault(OWNER_ID, NO_OWNER);
            boolean shooting = ownerId != NO_OWNER
                    && com.zavidvi.voidmod.client.item.RimeShots.isActive(ownerId);

            return state.setAndContinue(shooting ? ANIM_ATTACK : ANIM_IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

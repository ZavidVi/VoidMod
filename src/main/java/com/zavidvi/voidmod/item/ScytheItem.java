package com.zavidvi.voidmod.item;

import com.zavidvi.voidmod.client.renderer.scythe.ScytheRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;

public class ScytheItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final String assetName;

    private final String animationSuffix;

    private final float baseDamage;

    private final boolean bleedingOnCrit;

    private final RawAnimation[] attackAnimations;

    private final int swingTicks;

    public static final double SWEEP_RADIUS = 3.0D;

    private static final double FRONT_ARC_COS = 0.0D;

    private static final String CONTROLLER = "controller";

    public static final com.geckolib.constant.dataticket.DataTicket<Integer> OWNER_ID =
            com.geckolib.constant.dataticket.DataTicket.create("voidmod:scythe_owner", Integer.class);

    public static final int NO_OWNER = -1;

    public ScytheItem(Properties properties, String assetName, float baseDamage, float attackSpeed,
                      boolean bleedingOnCrit) {
        super(properties);
        this.bleedingOnCrit = bleedingOnCrit;
        GeoItem.registerSyncedAnimatable(this);
        this.assetName = assetName;
        this.baseDamage = baseDamage;
        this.animationSuffix = animationSuffixFor(attackSpeed);
        this.swingTicks = swingTicksFor(attackSpeed);
        this.attackAnimations = new RawAnimation[]{
                RawAnimation.begin().thenPlay("attack1_" + this.animationSuffix),
                RawAnimation.begin().thenPlay("attack2_" + this.animationSuffix),
                RawAnimation.begin().thenPlay("attack3_" + this.animationSuffix)
        };
    }

    private static String animationSuffixFor(float attackSpeed) {
        int code = Math.round(attackSpeed * 10.0F);
        return switch (code) {
            case 14 -> "14";
            case 16 -> "16";
            case 18 -> "18";
            default -> throw new IllegalArgumentException(
                    "Для скорости атаки " + attackSpeed + " нет анимаций косы (есть только 1.4 / 1.6 / 1.8)");
        };
    }

    public static final float MINIMUM_ATTACK_CHARGE = 1.0F;

    public static final int SERVER_CHARGE_TOLERANCE = 5;

    public static boolean isAttackReady(Player player, int tolerance) {
        return !player.cannotAttackWithItem(player.getMainHandItem(), tolerance);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, net.minecraft.world.entity.Entity target) {
        if (player.level().isClientSide()) return false;

        announceSwing(player, ScytheCombo.registerHit(player));
        return false;
    }

    public void swingAttack(ServerLevel level, Player player) {
        int hit = ScytheCombo.registerHit(player);
        announceSwing(player, hit);
    }

    private static LivingEntity nearestSweepTarget(ServerLevel level, Player player) {
        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (LivingEntity victim : sweepTargets(level, player, null, false)) {
            double distance = player.distanceToSqr(victim);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = victim;
            }
        }

        return nearest;
    }

    private void applyBleeding(LivingEntity target, Player source) {
        if (!this.bleedingOnCrit) return;

        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                com.zavidvi.voidmod.registry.ModEffects.BLEEDING,
                com.zavidvi.voidmod.effect.BleedingEffect.DURATION_TICKS), source);
    }

    private static int swingTicksFor(float attackSpeed) {
        return switch (Math.round(attackSpeed * 10.0F)) {
            case 14 -> 15;
            case 16 -> 12;
            default -> 10;
        };
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHurtEnemy(stack, target, attacker);

        if (!(attacker instanceof Player player) || !(attacker.level() instanceof ServerLevel level)) return;

        int hit = ScytheCombo.currentHit(player);

        if (hit == ScytheCombo.HIT_CRIT) {
            applyBleeding(target, player);
            return;
        }
    }

    private static void hurtSwept(ServerLevel level, Player player, LivingEntity victim, float damage) {
        com.zavidvi.voidmod.event.scythe.ScytheCombatEvents.withSweepDamage(() ->
                victim.hurtServer(level, player.damageSources().playerAttack(player), damage));
    }

    private void announceSwing(Player player, int hit) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) return;

        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer,
                new com.zavidvi.voidmod.network.ScytheSwingAnimationPayload(hit, this.swingTicks));
    }

    private static List<LivingEntity> sweepTargets(ServerLevel level, Player player,
                                                   LivingEntity primary, boolean fullCircle) {
        AABB area = player.getBoundingBox().inflate(SWEEP_RADIUS);
        Vec3 look = player.getLookAngle();
        Vec3 lookFlat = new Vec3(look.x, 0.0D, look.z).normalize();

        return level.getEntitiesOfClass(LivingEntity.class, area, victim -> {
            if (victim == player || victim == primary || !victim.isAlive()) return false;
            if (!victim.isAttackable() || victim.isAlliedTo(player)) return false;
            if (victim instanceof Player other && !player.canHarmPlayer(other)) return false;
            if (player.distanceTo(victim) > SWEEP_RADIUS) return false;
            if (fullCircle) return true;

            Vec3 toVictim = victim.position().subtract(player.position());
            Vec3 flat = new Vec3(toVictim.x, 0.0D, toVictim.z);
            if (flat.lengthSqr() < 1.0E-4) return true;

            return lookFlat.dot(flat.normalize()) >= FRONT_ARC_COS;
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                net.minecraft.world.item.component.TooltipDisplay display,
                                java.util.function.Consumer<net.minecraft.network.chat.Component> builder,
                                net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);

        if (this.bleedingOnCrit) {
            builder.accept(net.minecraft.network.chat.Component
                    .translatable("item.voidmod.reaper_scythe.bleeding")
                    .withStyle(net.minecraft.ChatFormatting.DARK_RED));
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !oldStack.is(newStack.getItem());
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private ScytheRenderer renderer;

            @Override
            public GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new ScytheRenderer(ScytheItem.this.assetName);
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<ScytheItem>(CONTROLLER, 0, state -> {
            int ownerId = state.getDataOrDefault(OWNER_ID, NO_OWNER);
            int hit = ownerId == NO_OWNER
                    ? -1
                    : com.zavidvi.voidmod.client.item.ScytheSwings.activeHit(ownerId);

            if (hit < 0) return PlayState.STOP;

            return state.setAndContinue(this.attackAnimations[hit]);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

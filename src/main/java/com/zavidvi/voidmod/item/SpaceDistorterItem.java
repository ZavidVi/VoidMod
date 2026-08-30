package com.zavidvi.voidmod.item;

import com.zavidvi.voidmod.client.renderer.spacedistorter.SpaceDistorterRenderer;
import net.minecraft.world.item.Item;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class SpaceDistorterItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("everytime");

    public SpaceDistorterItem(Properties properties) {
        super(properties);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private SpaceDistorterRenderer renderer;

            @Override
            public GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new SpaceDistorterRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        net.minecraft.world.entity.player.Player player = context.getPlayer();
        net.minecraft.world.level.Level level = context.getLevel();
        net.minecraft.core.BlockPos blockpos = context.getClickedPos().relative(context.getClickedFace());
        net.minecraft.world.level.block.state.BlockState blockstate = level.getBlockState(context.getClickedPos());

        if (blockstate.is(net.minecraft.world.level.block.Blocks.OBSIDIAN)) {
            java.util.Optional<net.minecraft.world.level.portal.PortalShape> optional = net.minecraft.world.level.portal.PortalShape.findEmptyPortalShape(level, blockpos, net.minecraft.core.Direction.Axis.X);
            if (optional.isEmpty()) {
                optional = net.minecraft.world.level.portal.PortalShape.findEmptyPortalShape(level, blockpos, net.minecraft.core.Direction.Axis.Z);
            }

            if (optional.isPresent()) {
                optional.get().createPortalBlocks(level);
                level.playSound(player, blockpos, net.minecraft.sounds.SoundEvents.FLINTANDSTEEL_USE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);

                if (player != null) {
                    com.zavidvi.voidmod.advancement.ModAdvancements.grant(player,
                            com.zavidvi.voidmod.advancement.ModAdvancements.RESTORATION);
                }

                return net.minecraft.world.InteractionResult.SUCCESS;
            }
        }
        
        return net.minecraft.world.InteractionResult.PASS;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<SpaceDistorterItem>("controller", 0, state -> state.setAndContinue(ANIM_IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

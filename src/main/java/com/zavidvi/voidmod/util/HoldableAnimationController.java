package com.zavidvi.voidmod.util;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animation.AnimationController;
import com.geckolib.renderer.base.GeoRenderState;

import java.util.function.Predicate;

public class HoldableAnimationController<T extends GeoAnimatable> extends AnimationController<T> {
    private final Predicate<T> hold;

    public HoldableAnimationController(String name, int transitionTicks,
                                       Predicate<T> hold, AnimationStateHandler<T> handler) {
        super(name, transitionTicks, handler);
        this.hold = hold;
    }

    @Override
    protected void progressExistingAnimation(T animatable, GeoRenderState renderState,
                                             double animationTime, double delta) {
        super.progressExistingAnimation(animatable, renderState, animationTime,
                this.hold.test(animatable) ? 0.0 : delta);
    }
}

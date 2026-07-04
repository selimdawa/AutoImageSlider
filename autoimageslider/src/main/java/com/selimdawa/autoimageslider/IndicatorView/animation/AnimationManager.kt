package com.selimdawa.autoimageslider.IndicatorView.animation

import com.selimdawa.autoimageslider.IndicatorView.animation.controller.AnimationController
import com.selimdawa.autoimageslider.IndicatorView.animation.controller.ValueController
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Indicator

class AnimationManager(indicator: Indicator, listener: ValueController.UpdateListener) {
    private val animationController: AnimationController?

    init {
        this.animationController = AnimationController(indicator, listener)
    }

    fun basic() {
        if (animationController != null) {
            animationController.end()
            animationController.basic()
        }
    }

    fun interactive(progress: Float) {
        if (animationController != null) {
            animationController.interactive(progress)
        }
    }

    fun end() {
        if (animationController != null) {
            animationController.end()
        }
    }
}

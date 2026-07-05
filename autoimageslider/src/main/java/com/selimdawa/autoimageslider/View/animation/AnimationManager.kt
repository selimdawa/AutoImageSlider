package com.selimdawa.autoimageslider.View.animation

import com.selimdawa.autoimageslider.View.animation.controller.AnimationController
import com.selimdawa.autoimageslider.View.animation.controller.ValueController
import com.selimdawa.autoimageslider.View.draw.data.Indicator

class AnimationManager(indicator: Indicator, listener: ValueController.UpdateListener) {
    private val animationController = AnimationController(indicator, listener)

    fun basic() {
        animationController.end(); animationController.basic()
    }

    fun interactive(progress: Float) = animationController.interactive(progress)
    fun end() = animationController.end()
}
package io.selimdawa.autoimageslider.view.animation

import io.selimdawa.autoimageslider.view.animation.controller.AnimationController
import io.selimdawa.autoimageslider.view.animation.controller.ValueController
import io.selimdawa.autoimageslider.view.draw.data.Indicator

class AnimationManager(indicator: Indicator, listener: ValueController.UpdateListener) {
    private val animationController = AnimationController(indicator, listener)

    fun basic() {
        animationController.end(); animationController.basic()
    }

    fun interactive(progress: Float) = animationController.interactive(progress)
    fun end() = animationController.end()
}
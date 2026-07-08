package io.selimdawa.autoimageslider.view

import io.selimdawa.autoimageslider.view.animation.AnimationManager
import io.selimdawa.autoimageslider.view.animation.controller.ValueController
import io.selimdawa.autoimageslider.view.animation.data.Value
import io.selimdawa.autoimageslider.view.draw.DrawManager

class IndicatorManager internal constructor(private val listener: Listener?) :
    ValueController.UpdateListener {
    private val drawManager = DrawManager()
    private val animationManager = AnimationManager(drawManager.indicator(), this)

    internal interface Listener {
        fun onIndicatorUpdated()
    }

    fun animate() = animationManager
    fun indicator() = drawManager.indicator()
    fun drawer() = drawManager

    override fun onValueUpdated(value: Value?) {
        drawManager.updateValue(value)
        listener?.onIndicatorUpdated()
    }
}
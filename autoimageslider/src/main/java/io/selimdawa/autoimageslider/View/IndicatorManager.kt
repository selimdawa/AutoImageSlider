package io.selimdawa.autoimageslider.View

import com.selimdawa.autoimageslider.View.animation.AnimationManager
import com.selimdawa.autoimageslider.View.animation.controller.ValueController
import com.selimdawa.autoimageslider.View.animation.data.Value
import com.selimdawa.autoimageslider.View.draw.DrawManager

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
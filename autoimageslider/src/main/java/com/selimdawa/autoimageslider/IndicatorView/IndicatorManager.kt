package com.selimdawa.autoimageslider.IndicatorView

import com.selimdawa.autoimageslider.IndicatorView.animation.AnimationManager
import com.selimdawa.autoimageslider.IndicatorView.animation.controller.ValueController
import com.selimdawa.autoimageslider.IndicatorView.animation.data.Value
import com.selimdawa.autoimageslider.IndicatorView.draw.DrawManager
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Indicator

class IndicatorManager internal constructor(private val listener: Listener?) :
    ValueController.UpdateListener {

    private val drawManager: DrawManager = DrawManager()
    private val animationManager: AnimationManager = AnimationManager(drawManager.indicator(), this)

    internal interface Listener {
        fun onIndicatorUpdated()
    }

    fun animate(): AnimationManager? {
        return animationManager
    }

    fun indicator(): Indicator {
        return drawManager.indicator()
    }

    fun drawer(): DrawManager {
        return drawManager
    }

    override fun onValueUpdated(value: Value?) {
        drawManager.updateValue(value)
        listener?.onIndicatorUpdated()
    }
}

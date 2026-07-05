package com.selimdawa.autoimageslider.IndicatorView.animation.controller

import com.selimdawa.autoimageslider.IndicatorView.animation.data.Value
import com.selimdawa.autoimageslider.IndicatorView.animation.type.ColorAnimation
import com.selimdawa.autoimageslider.IndicatorView.animation.type.DropAnimation
import com.selimdawa.autoimageslider.IndicatorView.animation.type.FillAnimation
import com.selimdawa.autoimageslider.IndicatorView.animation.type.ScaleAnimation
import com.selimdawa.autoimageslider.IndicatorView.animation.type.ScaleDownAnimation
import com.selimdawa.autoimageslider.IndicatorView.animation.type.SlideAnimation
import com.selimdawa.autoimageslider.IndicatorView.animation.type.SwapAnimation
import com.selimdawa.autoimageslider.IndicatorView.animation.type.ThinWormAnimation
import com.selimdawa.autoimageslider.IndicatorView.animation.type.WormAnimation

class ValueController(private val updateListener: UpdateListener?) {

    private var colorAnimation: ColorAnimation? = null
    private var scaleAnimation: ScaleAnimation? = null
    private var wormAnimation: WormAnimation? = null
    private var slideAnimation: SlideAnimation? = null
    private var fillAnimation: FillAnimation? = null
    private var thinWormAnimation: ThinWormAnimation? = null
    private var dropAnimation: DropAnimation? = null
    private var swapAnimation: SwapAnimation? = null
    private var scaleDownAnimation: ScaleDownAnimation? = null

    interface UpdateListener {
        fun onValueUpdated(value: Value?)
    }

    fun color(): ColorAnimation {
        return colorAnimation ?: ColorAnimation(updateListener).also { colorAnimation = it }
    }

    fun scale(): ScaleAnimation {
        val listener = requireListener()
        return scaleAnimation ?: ScaleAnimation(listener).also { scaleAnimation = it }
    }

    fun worm(): WormAnimation {
        val listener = requireListener()
        return wormAnimation ?: WormAnimation(listener).also { wormAnimation = it }
    }

    fun slide(): SlideAnimation {
        val listener = requireListener()
        return slideAnimation ?: SlideAnimation(listener).also { slideAnimation = it }
    }

    fun fill(): FillAnimation {
        val listener = requireListener()
        return fillAnimation ?: FillAnimation(listener).also { fillAnimation = it }
    }

    fun thinWorm(): ThinWormAnimation {
        val listener = requireListener()
        return thinWormAnimation ?: ThinWormAnimation(listener).also { thinWormAnimation = it }
    }

    fun drop(): DropAnimation {
        val listener = requireListener()
        return dropAnimation ?: DropAnimation(listener).also { dropAnimation = it }
    }

    fun swap(): SwapAnimation {
        val listener = requireListener()
        return swapAnimation ?: SwapAnimation(listener).also { swapAnimation = it }
    }

    fun scaleDown(): ScaleDownAnimation {
        val listener = requireListener()
        return scaleDownAnimation ?: ScaleDownAnimation(listener).also { scaleDownAnimation = it }
    }

    private fun requireListener(): UpdateListener {
        return updateListener ?: throw IllegalStateException("UpdateListener cannot be null")
    }
}
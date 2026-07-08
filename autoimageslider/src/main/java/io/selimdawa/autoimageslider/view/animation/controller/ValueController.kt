package io.selimdawa.autoimageslider.view.animation.controller

import io.selimdawa.autoimageslider.view.animation.data.Value
import io.selimdawa.autoimageslider.view.animation.type.ColorAnimation
import io.selimdawa.autoimageslider.view.animation.type.DropAnimation
import io.selimdawa.autoimageslider.view.animation.type.FillAnimation
import io.selimdawa.autoimageslider.view.animation.type.ScaleAnimation
import io.selimdawa.autoimageslider.view.animation.type.ScaleDownAnimation
import io.selimdawa.autoimageslider.view.animation.type.SlideAnimation
import io.selimdawa.autoimageslider.view.animation.type.SwapAnimation
import io.selimdawa.autoimageslider.view.animation.type.ThinWormAnimation
import io.selimdawa.autoimageslider.view.animation.type.WormAnimation

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

    fun color() = colorAnimation ?: ColorAnimation(updateListener).also { colorAnimation = it }
    fun scale() = scaleAnimation ?: ScaleAnimation(requireListener()).also { scaleAnimation = it }
    fun worm() = wormAnimation ?: WormAnimation(requireListener()).also { wormAnimation = it }
    fun slide() = slideAnimation ?: SlideAnimation(requireListener()).also { slideAnimation = it }
    fun fill() = fillAnimation ?: FillAnimation(requireListener()).also { fillAnimation = it }
    fun thinWorm() =
        thinWormAnimation ?: ThinWormAnimation(requireListener()).also { thinWormAnimation = it }

    fun drop() = dropAnimation ?: DropAnimation(requireListener()).also { dropAnimation = it }
    fun swap() = swapAnimation ?: SwapAnimation(requireListener()).also { swapAnimation = it }
    fun scaleDown() =
        scaleDownAnimation ?: ScaleDownAnimation(requireListener()).also { scaleDownAnimation = it }

    private fun requireListener() =
        updateListener ?: throw IllegalStateException("UpdateListener cannot be null")
}
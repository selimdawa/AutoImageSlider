package io.selimdawa.autoimageslider.View.animation.controller

import com.selimdawa.autoimageslider.View.animation.data.Value
import com.selimdawa.autoimageslider.View.animation.type.ColorAnimation
import com.selimdawa.autoimageslider.View.animation.type.DropAnimation
import com.selimdawa.autoimageslider.View.animation.type.FillAnimation
import com.selimdawa.autoimageslider.View.animation.type.ScaleAnimation
import com.selimdawa.autoimageslider.View.animation.type.ScaleDownAnimation
import com.selimdawa.autoimageslider.View.animation.type.SlideAnimation
import com.selimdawa.autoimageslider.View.animation.type.SwapAnimation
import com.selimdawa.autoimageslider.View.animation.type.ThinWormAnimation
import com.selimdawa.autoimageslider.View.animation.type.WormAnimation

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
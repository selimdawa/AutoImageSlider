package com.selimdawa.autoimageslider.IndicatorView.animation.controller

import com.selimdawa.autoimageslider.IndicatorView.animation.type.BaseAnimation
import com.selimdawa.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Indicator
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Orientation
import com.selimdawa.autoimageslider.IndicatorView.utils.CoordinatesUtils

class AnimationController(
    private val indicator: Indicator, private val listener: ValueController.UpdateListener
) {
    private val valueController = ValueController(listener)
    private var runningAnimation: BaseAnimation<*>? = null
    private var progress = 0f
    private var isInteractive = false

    fun interactive(progress: Float) {
        isInteractive = true
        this.progress = progress
        animate()
    }

    fun basic() {
        isInteractive = false
        progress = 0f
        animate()
    }

    fun end() = runningAnimation?.end()

    private fun animate() {
        val (from, to) = getTargetCoordinates()
        val duration = indicator.animationDuration

        runningAnimation = when (indicator.animationType) {
            IndicatorAnimationType.NONE, null -> {
                listener.onValueUpdated(null); null
            }

            IndicatorAnimationType.COLOR -> valueController.color()
                .with(indicator.unselectedColor, indicator.selectedColor).duration(duration)

            IndicatorAnimationType.SCALE -> valueController.scale().with(
                indicator.unselectedColor,
                indicator.selectedColor,
                indicator.radius,
                indicator.scaleFactor
            ).duration(duration)

            IndicatorAnimationType.WORM -> valueController.worm()
                .with(from, to, indicator.radius, to > from)?.duration(duration)

            IndicatorAnimationType.SLIDE -> valueController.slide().with(from, to)
                .duration(duration)

            IndicatorAnimationType.FILL -> valueController.fill().with(
                indicator.unselectedColor,
                indicator.selectedColor,
                indicator.radius,
                indicator.stroke
            ).duration(duration)

            IndicatorAnimationType.THIN_WORM -> valueController.thinWorm()
                .with(from, to, indicator.radius, to > from).duration(duration)

            IndicatorAnimationType.DROP -> {
                val padding =
                    if (indicator.orientation == Orientation.HORIZONTAL) indicator.paddingTop else indicator.paddingLeft
                valueController.drop().with(
                    from,
                    to,
                    indicator.radius * 3 + padding,
                    indicator.radius + padding,
                    indicator.radius
                ).duration(duration)
            }

            IndicatorAnimationType.SWAP -> valueController.swap().with(from, to).duration(duration)
            IndicatorAnimationType.SCALE_DOWN -> valueController.scaleDown().with(
                indicator.unselectedColor,
                indicator.selectedColor,
                indicator.radius,
                indicator.scaleFactor
            ).duration(duration)
        }?.configureAndExecute()
    }

    private fun getTargetCoordinates(): Pair<Int, Int> {
        val fromPosition =
            if (indicator.isInteractiveAnimation) indicator.selectedPosition else indicator.lastSelectedPosition
        val toPosition =
            if (indicator.isInteractiveAnimation) indicator.selectingPosition else indicator.selectedPosition
        return CoordinatesUtils.getCoordinate(
            indicator, fromPosition
        ) to CoordinatesUtils.getCoordinate(indicator, toPosition)
    }

    private fun <T : BaseAnimation<*>> T.configureAndExecute(): T {
        if (isInteractive) this.progress(progress) else this.start()
        return this
    }
}
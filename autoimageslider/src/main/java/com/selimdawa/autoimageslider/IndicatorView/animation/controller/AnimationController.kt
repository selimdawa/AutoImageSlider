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
        when (indicator.animationType) {
            IndicatorAnimationType.NONE, null -> listener.onValueUpdated(null)
            IndicatorAnimationType.COLOR -> colorAnimation()
            IndicatorAnimationType.SCALE -> scaleAnimation()
            IndicatorAnimationType.WORM -> wormAnimation()
            IndicatorAnimationType.FILL -> fillAnimation()
            IndicatorAnimationType.SLIDE -> slideAnimation()
            IndicatorAnimationType.THIN_WORM -> thinWormAnimation()
            IndicatorAnimationType.DROP -> dropAnimation()
            IndicatorAnimationType.SWAP -> swapAnimation()
            IndicatorAnimationType.SCALE_DOWN -> scaleDownAnimation()
        }
    }

    private fun colorAnimation() {
        runningAnimation =
            valueController.color().with(indicator.unselectedColor, indicator.selectedColor)
                .duration(indicator.animationDuration).configureAndExecute()
    }

    private fun scaleAnimation() {
        runningAnimation = valueController.scale().with(
            indicator.unselectedColor,
            indicator.selectedColor,
            indicator.radius,
            indicator.scaleFactor
        ).duration(indicator.animationDuration).configureAndExecute()
    }

    private fun wormAnimation() {
        val (from, to) = getTargetCoordinates()
        runningAnimation = valueController.worm().with(from, to, indicator.radius, to > from)
            ?.duration(indicator.animationDuration)?.configureAndExecute()
    }

    private fun slideAnimation() {
        val (from, to) = getTargetCoordinates()
        runningAnimation =
            valueController.slide().with(from, to).duration(indicator.animationDuration)
                .configureAndExecute()
    }

    private fun fillAnimation() {
        runningAnimation = valueController.fill().with(
            indicator.unselectedColor, indicator.selectedColor, indicator.radius, indicator.stroke
        ).duration(indicator.animationDuration).configureAndExecute()
    }

    private fun thinWormAnimation() {
        val (from, to) = getTargetCoordinates()
        runningAnimation = valueController.thinWorm().with(from, to, indicator.radius, to > from)
            .duration(indicator.animationDuration).configureAndExecute()
    }

    private fun dropAnimation() {
        val (widthFrom, widthTo) = getTargetCoordinates()
        val padding =
            if (indicator.orientation == Orientation.HORIZONTAL) indicator.paddingTop else indicator.paddingLeft
        val radius = indicator.radius
        val heightFrom = radius * 3 + padding
        val heightTo = radius + padding

        runningAnimation =
            valueController.drop().with(widthFrom, widthTo, heightFrom, heightTo, radius)
                .duration(indicator.animationDuration).configureAndExecute()
    }

    private fun swapAnimation() {
        val (from, to) = getTargetCoordinates()
        runningAnimation =
            valueController.swap().with(from, to).duration(indicator.animationDuration)
                .configureAndExecute()
    }

    private fun scaleDownAnimation() {
        runningAnimation = valueController.scaleDown().with(
            indicator.unselectedColor,
            indicator.selectedColor,
            indicator.radius,
            indicator.scaleFactor
        ).duration(indicator.animationDuration).configureAndExecute()
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

    private fun <T : BaseAnimation<*>> T?.configureAndExecute(): T? {
        val animation = this ?: return null
        if (isInteractive) {
            animation.progress(progress)
        } else {
            animation.start()
        }
        return animation
    }
}
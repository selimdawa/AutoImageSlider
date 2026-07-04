package com.selimdawa.autoimageslider.IndicatorView.animation.controller

import com.selimdawa.autoimageslider.IndicatorView.animation.type.BaseAnimation
import com.selimdawa.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Indicator
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Orientation
import com.selimdawa.autoimageslider.IndicatorView.utils.CoordinatesUtils

class AnimationController(
    private val indicator: Indicator, private val listener: ValueController.UpdateListener
) {
    private val valueController: ValueController = ValueController(listener)

    private var runningAnimation: BaseAnimation<*>? = null

    private var progress = 0f
    private var isInteractive = false

    fun interactive(progress: Float) {
        this.isInteractive = true
        this.progress = progress
        animate()
    }

    fun basic() {
        this.isInteractive = false
        this.progress = 0f
        animate()
    }

    fun end() {
        if (runningAnimation != null) {
            runningAnimation!!.end()
        }
    }

    private fun animate() {
        val animationType = indicator.animationType
        when (animationType) {
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
        val selectedColor = indicator.selectedColor
        val unselectedColor = indicator.unselectedColor
        val animationDuration = indicator.animationDuration

        runningAnimation =
            valueController.color().with(unselectedColor, selectedColor).duration(animationDuration)
                .configureAndExecute()
    }

    private fun scaleAnimation() {
        val selectedColor = indicator.selectedColor
        val unselectedColor = indicator.unselectedColor
        val radiusPx = indicator.radius
        val scaleFactor = indicator.scaleFactor
        val animationDuration = indicator.animationDuration

        runningAnimation =
            valueController.scale().with(unselectedColor, selectedColor, radiusPx, scaleFactor)
                .duration(animationDuration).configureAndExecute()
    }

    private fun wormAnimation() {
        val fromPosition =
            if (indicator.isInteractiveAnimation) indicator.selectedPosition else indicator.lastSelectedPosition
        val toPosition =
            if (indicator.isInteractiveAnimation) indicator.selectingPosition else indicator.selectedPosition

        val from = CoordinatesUtils.getCoordinate(indicator, fromPosition)
        val to = CoordinatesUtils.getCoordinate(indicator, toPosition)
        val isRightSide = toPosition > fromPosition

        val radiusPx = indicator.radius
        val animationDuration = indicator.animationDuration

        runningAnimation = valueController.worm().with(from, to, radiusPx, isRightSide)
            ?.duration(animationDuration).configureAndExecute()
    }

    private fun slideAnimation() {
        val fromPosition =
            if (indicator.isInteractiveAnimation) indicator.selectedPosition else indicator.lastSelectedPosition
        val toPosition =
            if (indicator.isInteractiveAnimation) indicator.selectingPosition else indicator.selectedPosition

        val from = CoordinatesUtils.getCoordinate(indicator, fromPosition)
        val to = CoordinatesUtils.getCoordinate(indicator, toPosition)
        val animationDuration = indicator.animationDuration

        runningAnimation =
            valueController.slide().with(from, to).duration(animationDuration).configureAndExecute()
    }

    private fun fillAnimation() {
        val selectedColor = indicator.selectedColor
        val unselectedColor = indicator.unselectedColor
        val radiusPx = indicator.radius
        val strokePx = indicator.stroke
        val animationDuration = indicator.animationDuration

        runningAnimation =
            valueController.fill().with(unselectedColor, selectedColor, radiusPx, strokePx)
                .duration(animationDuration).configureAndExecute()
    }

    private fun thinWormAnimation() {
        val fromPosition =
            if (indicator.isInteractiveAnimation) indicator.selectedPosition else indicator.lastSelectedPosition
        val toPosition =
            if (indicator.isInteractiveAnimation) indicator.selectingPosition else indicator.selectedPosition

        val from = CoordinatesUtils.getCoordinate(indicator, fromPosition)
        val to = CoordinatesUtils.getCoordinate(indicator, toPosition)
        val isRightSide = toPosition > fromPosition

        val radiusPx = indicator.radius
        val animationDuration = indicator.animationDuration

        runningAnimation = valueController.thinWorm().with(from, to, radiusPx, isRightSide)
            .duration(animationDuration).configureAndExecute()
    }

    private fun dropAnimation() {
        val fromPosition =
            if (indicator.isInteractiveAnimation) indicator.selectedPosition else indicator.lastSelectedPosition
        val toPosition =
            if (indicator.isInteractiveAnimation) indicator.selectingPosition else indicator.selectedPosition

        val widthFrom = CoordinatesUtils.getCoordinate(indicator, fromPosition)
        val widthTo = CoordinatesUtils.getCoordinate(indicator, toPosition)

        val paddingTop = indicator.paddingTop
        val paddingLeft = indicator.paddingLeft
        val padding =
            if (indicator.orientation == Orientation.HORIZONTAL) paddingTop else paddingLeft

        val radius = indicator.radius
        val heightFrom = radius * 3 + padding
        val heightTo = radius + padding

        val animationDuration = indicator.animationDuration

        runningAnimation = valueController.drop().duration(animationDuration)
            .with(widthFrom, widthTo, heightFrom, heightTo, radius).configureAndExecute()
    }

    private fun swapAnimation() {
        val fromPosition =
            if (indicator.isInteractiveAnimation) indicator.selectedPosition else indicator.lastSelectedPosition
        val toPosition =
            if (indicator.isInteractiveAnimation) indicator.selectingPosition else indicator.selectedPosition

        val from = CoordinatesUtils.getCoordinate(indicator, fromPosition)
        val to = CoordinatesUtils.getCoordinate(indicator, toPosition)
        val animationDuration = indicator.animationDuration

        runningAnimation =
            valueController.swap().with(from, to).duration(animationDuration).configureAndExecute()
    }

    private fun scaleDownAnimation() {
        val selectedColor = indicator.selectedColor
        val unselectedColor = indicator.unselectedColor
        val radiusPx = indicator.radius
        val scaleFactor = indicator.scaleFactor
        val animationDuration = indicator.animationDuration

        runningAnimation =
            valueController.scaleDown().with(unselectedColor, selectedColor, radiusPx, scaleFactor)
                .duration(animationDuration).configureAndExecute()
    }

    // Updated extension to accept a nullable receiver, ensuring smooth and safe chaining
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
package com.selimdawa.autoimageslider.IndicatorView.animation.type

import android.animation.IntEvaluator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import com.selimdawa.autoimageslider.IndicatorView.animation.controller.ValueController
import com.selimdawa.autoimageslider.IndicatorView.animation.data.type.SwapAnimationValue

class SwapAnimation(listener: ValueController.UpdateListener) :
    BaseAnimation<ValueAnimator?>(listener) {
    private var coordinateStart: Int = COORDINATE_NONE
    private var coordinateEnd: Int = COORDINATE_NONE

    private val value: SwapAnimationValue = SwapAnimationValue()

    override fun createAnimator(): ValueAnimator {
        val animator = ValueAnimator()
        animator.duration = DEFAULT_ANIMATION_TIME.toLong()
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { animation ->
            onAnimateUpdated(animation)
        }

        return animator
    }

    override fun progress(progress: Float): SwapAnimation {
        val currentAnimator = animator
        if (currentAnimator != null) {
            val playTime = (progress * animationDuration).toLong()
            val values = currentAnimator.values

            if (values != null && values.isNotEmpty()) {
                currentAnimator.currentPlayTime = playTime
            }
        }

        return this
    }

    fun with(coordinateStart: Int, coordinateEnd: Int): SwapAnimation {
        val currentAnimator = animator
        if (currentAnimator != null && hasChanges(coordinateStart, coordinateEnd)) {
            this.coordinateStart = coordinateStart
            this.coordinateEnd = coordinateEnd

            val holder =
                createColorPropertyHolder(ANIMATION_COORDINATE, coordinateStart, coordinateEnd)
            val holderReverse = createColorPropertyHolder(
                ANIMATION_COORDINATE_REVERSE,
                coordinateEnd,
                coordinateStart
            )
            currentAnimator.setValues(holder, holderReverse)
        }

        return this
    }

    private fun createColorPropertyHolder(
        propertyName: String?,
        startValue: Int,
        endValue: Int
    ): PropertyValuesHolder {
        val holder = PropertyValuesHolder.ofInt(propertyName, startValue, endValue)
        holder.setEvaluator(IntEvaluator())

        return holder
    }

    private fun onAnimateUpdated(animation: ValueAnimator) {
        val coordinate = animation.getAnimatedValue(ANIMATION_COORDINATE) as Int
        val coordinateReverse = animation.getAnimatedValue(ANIMATION_COORDINATE_REVERSE) as Int

        value.coordinate = coordinate
        value.coordinateReverse = coordinateReverse

        listener?.onValueUpdated(value)
    }

    private fun hasChanges(coordinateStart: Int, coordinateEnd: Int): Boolean {
        if (this.coordinateStart != coordinateStart) {
            return true
        }

        if (this.coordinateEnd != coordinateEnd) {
            return true
        }

        return false
    }

    companion object {
        private const val ANIMATION_COORDINATE = "ANIMATION_COORDINATE"
        private const val ANIMATION_COORDINATE_REVERSE = "ANIMATION_COORDINATE_REVERSE"
        private const val COORDINATE_NONE = -1
    }
}
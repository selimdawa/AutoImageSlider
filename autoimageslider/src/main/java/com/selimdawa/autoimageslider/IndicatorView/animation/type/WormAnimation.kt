package com.selimdawa.autoimageslider.IndicatorView.animation.type

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import com.selimdawa.autoimageslider.IndicatorView.animation.controller.ValueController
import com.selimdawa.autoimageslider.IndicatorView.animation.data.type.WormAnimationValue

open class WormAnimation(listener: ValueController.UpdateListener) :
    BaseAnimation<AnimatorSet?>(listener) {

    var coordinateStart: Int = 0
    var coordinateEnd: Int = 0

    var radius: Int = 0
    var isRightSide: Boolean = false

    var rectLeftEdge: Int = 0
    var rectRightEdge: Int = 0

    private val value: WormAnimationValue = WormAnimationValue()

    override fun createAnimator(): AnimatorSet {
        val animator = AnimatorSet()
        animator.interpolator = AccelerateDecelerateInterpolator()

        return animator
    }

    override fun duration(duration: Long): WormAnimation? {
        super.duration(duration)
        return this
    }

    open fun with(
        coordinateStart: Int,
        coordinateEnd: Int,
        radius: Int,
        isRightSide: Boolean
    ): WormAnimation? {
        if (hasChanges(coordinateStart, coordinateEnd, radius, isRightSide)) {
            val currentAnimator = createAnimator()
            animator = currentAnimator

            this.coordinateStart = coordinateStart
            this.coordinateEnd = coordinateEnd

            this.radius = radius
            this.isRightSide = isRightSide

            rectLeftEdge = coordinateStart - radius
            rectRightEdge = coordinateStart + radius

            value.rectStart = rectLeftEdge
            value.rectEnd = rectRightEdge

            val rect = createRectValues(isRightSide)
            val duration = animationDuration / 2

            val straightAnimator = createWormAnimator(rect.fromX, rect.toX, duration, false, value)
            val reverseAnimator =
                createWormAnimator(rect.reverseFromX, rect.reverseToX, duration, true, value)
            currentAnimator.playSequentially(straightAnimator, reverseAnimator)
        }
        return this
    }

    override fun progress(progress: Float): WormAnimation? {
        val currentAnimator = animator ?: return this

        var progressDuration = (progress * animationDuration).toLong()
        for (anim in currentAnimator.childAnimations) {
            val childAnimator = anim as ValueAnimator
            val duration = childAnimator.duration
            var setDuration = progressDuration

            if (setDuration > duration) {
                setDuration = duration
            }

            childAnimator.currentPlayTime = setDuration
            progressDuration -= setDuration
        }

        return this
    }

    fun createWormAnimator(
        fromValue: Int,
        toValue: Int,
        duration: Long,
        isReverse: Boolean,
        value: WormAnimationValue
    ): ValueAnimator {
        val anim = ValueAnimator.ofInt(fromValue, toValue)
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.duration = duration
        anim.addUpdateListener { animation ->
            onAnimateUpdated(value, animation, isReverse)
        }

        return anim
    }

    private fun onAnimateUpdated(
        value: WormAnimationValue,
        animation: ValueAnimator,
        isReverse: Boolean
    ) {
        val rectEdge = animation.animatedValue as Int

        if (isRightSide) {
            if (!isReverse) {
                value.rectEnd = rectEdge
            } else {
                value.rectStart = rectEdge
            }
        } else {
            if (!isReverse) {
                value.rectStart = rectEdge
            } else {
                value.rectEnd = rectEdge
            }
        }

        listener?.onValueUpdated(value)
    }

    fun hasChanges(
        coordinateStart: Int,
        coordinateEnd: Int,
        radius: Int,
        isRightSide: Boolean
    ): Boolean {
        if (this.coordinateStart != coordinateStart) {
            return true
        }

        if (this.coordinateEnd != coordinateEnd) {
            return true
        }

        if (this.radius != radius) {
            return true
        }

        if (this.isRightSide != isRightSide) {
            return true
        }

        return false
    }

    fun createRectValues(isRightSide: Boolean): RectValues {
        val fromX: Int
        val toX: Int

        val reverseFromX: Int
        val reverseToX: Int

        if (isRightSide) {
            fromX = coordinateStart + radius
            toX = coordinateEnd + radius

            reverseFromX = coordinateStart - radius
            reverseToX = coordinateEnd - radius
        } else {
            fromX = coordinateStart - radius
            toX = coordinateEnd - radius

            reverseFromX = coordinateStart + radius
            reverseToX = coordinateEnd + radius
        }

        return RectValues(fromX, toX, reverseFromX, reverseToX)
    }

    inner class RectValues(
        val fromX: Int,
        val toX: Int,
        val reverseFromX: Int,
        val reverseToX: Int
    )
}
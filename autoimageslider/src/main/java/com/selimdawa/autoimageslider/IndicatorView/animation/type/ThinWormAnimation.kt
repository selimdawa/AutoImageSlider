package com.selimdawa.autoimageslider.IndicatorView.animation.type

import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import com.selimdawa.autoimageslider.IndicatorView.animation.controller.ValueController
import com.selimdawa.autoimageslider.IndicatorView.animation.data.type.ThinWormAnimationValue

class ThinWormAnimation(listener: ValueController.UpdateListener) : WormAnimation(listener) {

    private val value: ThinWormAnimationValue = ThinWormAnimationValue()

    override fun duration(duration: Long): ThinWormAnimation {
        super.duration(duration)
        return this
    }

    override fun with(
        coordinateStart: Int, coordinateEnd: Int, radius: Int, isRightSide: Boolean
    ): WormAnimation {
        if (hasChanges(coordinateStart, coordinateEnd, radius, isRightSide)) {
            animator = createAnimator()

            this.coordinateStart = coordinateStart
            this.coordinateEnd = coordinateEnd

            this.radius = radius
            this.isRightSide = isRightSide

            val height = radius * 2
            rectLeftEdge = coordinateStart - radius
            rectRightEdge = coordinateStart + radius

            value.rectStart = rectLeftEdge
            value.rectEnd = rectRightEdge
            value.height = height

            val rec = createRectValues(isRightSide)
            val sizeDuration = (animationDuration * 0.8).toLong()
            val reverseDelay = (animationDuration * 0.2).toLong()

            val heightDuration = (animationDuration * 0.5).toLong()
            val reverseHeightDelay = (animationDuration * 0.5).toLong()

            val straightAnimator =
                createWormAnimator(rec.fromX, rec.toX, sizeDuration, false, value)
            val reverseAnimator =
                createWormAnimator(rec.reverseFromX, rec.reverseToX, sizeDuration, true, value)
            reverseAnimator.setStartDelay(reverseDelay)

            val straightHeightAnimator = createHeightAnimator(height, radius, heightDuration)
            val reverseHeightAnimator = createHeightAnimator(radius, height, heightDuration)
            reverseHeightAnimator.setStartDelay(reverseHeightDelay)

            animator!!.playTogether(
                straightAnimator, reverseAnimator, straightHeightAnimator, reverseHeightAnimator
            )
        }
        return this
    }

    private fun createHeightAnimator(
        fromHeight: Int, toHeight: Int, duration: Long
    ): ValueAnimator {
        val anim = ValueAnimator.ofInt(fromHeight, toHeight)
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.duration = duration
        anim.addUpdateListener { animation ->
            onAnimateUpdated(animation)
        }

        return anim
    }

    private fun onAnimateUpdated(animation: ValueAnimator) {
        value.height = animation.animatedValue as Int

        listener?.onValueUpdated(value)
    }

    override fun progress(progress: Float): ThinWormAnimation {
        val currentAnimator = animator
        if (currentAnimator != null) {
            val progressDuration = (progress * animationDuration).toLong()
            val childAnimations = currentAnimator.childAnimations
            val size = childAnimations.size

            for (i in 0..<size) {
                val anim = childAnimations[i] as ValueAnimator

                var setDuration = progressDuration - anim.startDelay
                val duration = anim.duration

                if (setDuration > duration) {
                    setDuration = duration
                } else if (setDuration < 0) {
                    setDuration = 0
                }

                if (i == size - 1 && setDuration <= 0) {
                    continue
                }

                val values = anim.values
                if (values != null && values.isNotEmpty()) {
                    anim.currentPlayTime = setDuration
                }
            }
        }

        return this
    }
}
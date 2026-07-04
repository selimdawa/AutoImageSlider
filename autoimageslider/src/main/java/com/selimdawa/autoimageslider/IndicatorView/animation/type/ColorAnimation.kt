package com.selimdawa.autoimageslider.IndicatorView.animation.type

import android.animation.ArgbEvaluator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import com.selimdawa.autoimageslider.IndicatorView.animation.controller.ValueController
import com.selimdawa.autoimageslider.IndicatorView.animation.data.type.ColorAnimationValue

open class ColorAnimation(listener: ValueController.UpdateListener?) :
    BaseAnimation<ValueAnimator?>(listener) {
    private val value: ColorAnimationValue = ColorAnimationValue()

    var colorStart: Int = 0
    var colorEnd: Int = 0

    override fun createAnimator(): ValueAnimator {
        val animator = ValueAnimator()
        animator.setDuration(DEFAULT_ANIMATION_TIME.toLong())
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { animation -> onAnimateUpdated(animation) }

        return animator
    }

    override fun progress(progress: Float): ColorAnimation {
        if (animator != null) {
            val playTime = (progress * animationDuration).toLong()

            if (animator!!.values != null && animator!!.values.size > 0) {
                animator!!.setCurrentPlayTime(playTime)
            }
        }

        return this
    }

    fun with(colorStart: Int, colorEnd: Int): ColorAnimation {
        if (animator != null && hasChanges(colorStart, colorEnd)) {
            this.colorStart = colorStart
            this.colorEnd = colorEnd

            val colorHolder = createColorPropertyHolder(false)
            val reverseColorHolder = createColorPropertyHolder(true)

            animator!!.setValues(colorHolder, reverseColorHolder)
        }

        return this
    }

    fun createColorPropertyHolder(isReverse: Boolean): PropertyValuesHolder {
        val propertyName: String?
        val colorStart: Int
        val colorEnd: Int

        if (isReverse) {
            propertyName = ANIMATION_COLOR_REVERSE
            colorStart = this.colorEnd
            colorEnd = this.colorStart
        } else {
            propertyName = ANIMATION_COLOR
            colorStart = this.colorStart
            colorEnd = this.colorEnd
        }

        val holder = PropertyValuesHolder.ofInt(propertyName, colorStart, colorEnd)
        holder.setEvaluator(ArgbEvaluator())

        return holder
    }

    private fun hasChanges(colorStart: Int, colorEnd: Int): Boolean {
        if (this.colorStart != colorStart) {
            return true
        }

        if (this.colorEnd != colorEnd) {
            return true
        }

        return false
    }

    private fun onAnimateUpdated(animation: ValueAnimator) {
        val color = animation.getAnimatedValue(ANIMATION_COLOR) as Int
        val colorReverse = animation.getAnimatedValue(ANIMATION_COLOR_REVERSE) as Int

        value.color = color
        value.colorReverse = colorReverse

        listener?.onValueUpdated(value)
    }

    companion object {
        const val DEFAULT_UNSELECTED_COLOR: String = "#33ffffff"
        const val DEFAULT_SELECTED_COLOR: String = "#ffffff"

        const val ANIMATION_COLOR_REVERSE: String = "ANIMATION_COLOR_REVERSE"
        const val ANIMATION_COLOR: String = "ANIMATION_COLOR"
    }
}

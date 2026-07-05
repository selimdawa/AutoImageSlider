package com.selimdawa.autoimageslider.IndicatorView.animation.type

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.IntEvaluator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import com.selimdawa.autoimageslider.IndicatorView.animation.controller.ValueController
import com.selimdawa.autoimageslider.IndicatorView.animation.data.ColorAnimationValue
import com.selimdawa.autoimageslider.IndicatorView.animation.data.DropAnimationValue
import com.selimdawa.autoimageslider.IndicatorView.animation.data.FillAnimationValue
import com.selimdawa.autoimageslider.IndicatorView.animation.data.ScaleAnimationValue
import com.selimdawa.autoimageslider.IndicatorView.animation.data.SlideAnimationValue
import com.selimdawa.autoimageslider.IndicatorView.animation.data.SwapAnimationValue
import com.selimdawa.autoimageslider.IndicatorView.animation.data.ThinWormAnimationValue
import com.selimdawa.autoimageslider.IndicatorView.animation.data.WormAnimationValue

abstract class BaseAnimation<T : Animator?>(protected var listener: ValueController.UpdateListener?) {
    protected var animationDuration: Long = DEFAULT_ANIMATION_TIME.toLong()
    protected var animator: T? = createAnimator()

    abstract fun createAnimator(): T
    abstract fun progress(progress: Float): BaseAnimation<*>?

    open fun duration(duration: Long): BaseAnimation<*>? {
        animationDuration = duration
        val currentAnimator = animator
        if (currentAnimator is ValueAnimator) {
            currentAnimator.duration = animationDuration
        }
        return this
    }

    fun start() {
        val currentAnimator = animator
        if (currentAnimator != null && !currentAnimator.isRunning) {
            currentAnimator.start()
        }
    }

    fun end() {
        val currentAnimator = animator
        if (currentAnimator != null && currentAnimator.isStarted) {
            currentAnimator.end()
        }
    }

    companion object {
        const val DEFAULT_ANIMATION_TIME: Int = 350
    }
}

open class ColorAnimation(listener: ValueController.UpdateListener?) : BaseAnimation<ValueAnimator?>(listener) {
    private val value = ColorAnimationValue()
    var colorStart: Int = 0
    var colorEnd: Int = 0

    override fun createAnimator(): ValueAnimator {
        val animator = ValueAnimator()
        animator.duration = DEFAULT_ANIMATION_TIME.toLong()
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { animation -> onAnimateUpdated(animation) }
        return animator
    }

    override fun progress(progress: Float): ColorAnimation {
        val currentAnimator = animator
        if (currentAnimator != null) {
            val playTime = (progress * animationDuration).toLong()
            val valuesArray = currentAnimator.values
            if (valuesArray != null && valuesArray.isNotEmpty()) {
                currentAnimator.currentPlayTime = playTime
            }
        }
        return this
    }

    fun with(colorStart: Int, colorEnd: Int): ColorAnimation {
        val currentAnimator = animator
        if (currentAnimator != null && hasChanges(colorStart, colorEnd)) {
            this.colorStart = colorStart
            this.colorEnd = colorEnd
            currentAnimator.setValues(createColorPropertyHolder(false), createColorPropertyHolder(true))
        }
        return this
    }

    fun createColorPropertyHolder(isReverse: Boolean): PropertyValuesHolder {
        val propertyName = if (isReverse) ANIMATION_COLOR_REVERSE else ANIMATION_COLOR
        val startColor = if (isReverse) colorEnd else colorStart
        val endColor = if (isReverse) colorStart else colorEnd
        val holder = PropertyValuesHolder.ofInt(propertyName, startColor, endColor)
        holder.setEvaluator(ArgbEvaluator())
        return holder
    }

    private fun hasChanges(colorStart: Int, colorEnd: Int): Boolean = this.colorStart != colorStart || this.colorEnd != colorEnd

    private fun onAnimateUpdated(animation: ValueAnimator) {
        value.color = animation.getAnimatedValue(ANIMATION_COLOR) as Int
        value.colorReverse = animation.getAnimatedValue(ANIMATION_COLOR_REVERSE) as Int
        listener?.onValueUpdated(value)
    }

    companion object {
        const val ANIMATION_COLOR_REVERSE: String = "ANIMATION_COLOR_REVERSE"
        const val ANIMATION_COLOR: String = "ANIMATION_COLOR"
    }
}

class FillAnimation(listener: ValueController.UpdateListener) : ColorAnimation(listener) {
    private val value = FillAnimationValue()
    private var radius = 0
    private var stroke = 0

    override fun createAnimator(): ValueAnimator {
        val animator = ValueAnimator()
        animator.duration = DEFAULT_ANIMATION_TIME.toLong()
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { animation -> onAnimateUpdated(animation) }
        return animator
    }

    fun with(colorStart: Int, colorEnd: Int, radius: Int, stroke: Int): FillAnimation {
        val currentAnimator = animator
        if (currentAnimator != null && hasChanges(colorStart, colorEnd, radius, stroke)) {
            this.colorStart = colorStart
            this.colorEnd = colorEnd
            this.radius = radius
            this.stroke = stroke
            currentAnimator.setValues(
                createColorPropertyHolder(false), createColorPropertyHolder(true),
                createRadiusPropertyHolder(false), createRadiusPropertyHolder(true),
                createStrokePropertyHolder(false), createStrokePropertyHolder(true)
            )
        }
        return this
    }

    private fun createRadiusPropertyHolder(isReverse: Boolean): PropertyValuesHolder {
        val startRadiusValue = if (isReverse) radius / 2 else radius
        val endRadiusValue = if (isReverse) radius else radius / 2
        val holder = PropertyValuesHolder.ofInt(if (isReverse) "ANIMATION_RADIUS_REVERSE" else "ANIMATION_RADIUS", startRadiusValue, endRadiusValue)
        holder.setEvaluator(IntEvaluator())
        return holder
    }

    private fun createStrokePropertyHolder(isReverse: Boolean): PropertyValuesHolder {
        val startStrokeValue = if (isReverse) radius else 0
        val endStrokeValue = if (isReverse) 0 else radius
        val holder = PropertyValuesHolder.ofInt(if (isReverse) "ANIMATION_STROKE_REVERSE" else "ANIMATION_STROKE", startStrokeValue, endStrokeValue)
        holder.setEvaluator(IntEvaluator())
        return holder
    }

    private fun onAnimateUpdated(animation: ValueAnimator) {
        value.color = animation.getAnimatedValue(ANIMATION_COLOR) as Int
        value.colorReverse = animation.getAnimatedValue(ANIMATION_COLOR_REVERSE) as Int
        value.radius = animation.getAnimatedValue("ANIMATION_RADIUS") as Int
        value.radiusReverse = animation.getAnimatedValue("ANIMATION_RADIUS_REVERSE") as Int
        value.stroke = animation.getAnimatedValue("ANIMATION_STROKE") as Int
        value.strokeReverse = animation.getAnimatedValue("ANIMATION_STROKE_REVERSE") as Int
        listener?.onValueUpdated(value)
    }

    private fun hasChanges(colorStart: Int, colorEnd: Int, radiusValue: Int, strokeValue: Int): Boolean {
        return this.colorStart != colorStart || this.colorEnd != colorEnd || radius != radiusValue || stroke != strokeValue
    }
}
open class ScaleAnimation(listener: ValueController.UpdateListener) : ColorAnimation(listener) {
    var radius: Int = 0
    var scaleFactor: Float = 0f
    private val value = ScaleAnimationValue()

    override fun createAnimator(): ValueAnimator {
        val animator = ValueAnimator()
        animator.duration = DEFAULT_ANIMATION_TIME.toLong()
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { animation -> onAnimateUpdated(animation) }
        return animator
    }

    fun with(colorStart: Int, colorEnd: Int, radius: Int, scaleFactor: Float): ScaleAnimation {
        val currentAnimator = animator
        if (currentAnimator != null && hasChanges(colorStart, colorEnd, radius, scaleFactor)) {
            this.colorStart = colorStart
            this.colorEnd = colorEnd
            this.radius = radius
            this.scaleFactor = scaleFactor
            currentAnimator.setValues(
                createColorPropertyHolder(false), createColorPropertyHolder(true),
                createScalePropertyHolder(false), createScalePropertyHolder(true)
            )
        }
        return this
    }

    private fun onAnimateUpdated(animation: ValueAnimator) {
        value.color = animation.getAnimatedValue(ANIMATION_COLOR) as Int
        value.colorReverse = animation.getAnimatedValue(ANIMATION_COLOR_REVERSE) as Int
        value.radius = animation.getAnimatedValue(ANIMATION_SCALE) as Int
        value.radiusReverse = animation.getAnimatedValue(ANIMATION_SCALE_REVERSE) as Int
        listener?.onValueUpdated(value)
    }

    protected open fun createScalePropertyHolder(isReverse: Boolean): PropertyValuesHolder {
        val startRadiusValue = if (isReverse) radius else (radius * scaleFactor).toInt()
        val endRadiusValue = if (isReverse) (radius * scaleFactor).toInt() else radius
        val holder = PropertyValuesHolder.ofInt(if (isReverse) ANIMATION_SCALE_REVERSE else ANIMATION_SCALE, startRadiusValue, endRadiusValue)
        holder.setEvaluator(IntEvaluator())
        return holder
    }

    private fun hasChanges(colorStart: Int, colorEnd: Int, radiusValue: Int, scaleFactorValue: Float): Boolean {
        return this.colorStart != colorStart || this.colorEnd != colorEnd || radius != radiusValue || scaleFactor != scaleFactorValue
    }

    companion object {
        const val ANIMATION_SCALE_REVERSE: String = "ANIMATION_SCALE_REVERSE"
        const val ANIMATION_SCALE: String = "ANIMATION_SCALE"
    }
}

class ScaleDownAnimation(listener: ValueController.UpdateListener) : ScaleAnimation(listener) {
    override fun createScalePropertyHolder(isReverse: Boolean): PropertyValuesHolder {
        val startRadiusValue = if (isReverse) (radius * scaleFactor).toInt() else radius
        val endRadiusValue = if (isReverse) radius else (radius * scaleFactor).toInt()
        val holder = PropertyValuesHolder.ofInt(if (isReverse) ANIMATION_SCALE_REVERSE else ANIMATION_SCALE, startRadiusValue, endRadiusValue)
        holder.setEvaluator(IntEvaluator())
        return holder
    }
}

class SlideAnimation(listener: ValueController.UpdateListener) : BaseAnimation<ValueAnimator?>(listener) {
    private val value = SlideAnimationValue()
    private var coordinateStart: Int = -1
    private var coordinateEnd: Int = -1

    override fun createAnimator(): ValueAnimator {
        val animator = ValueAnimator()
        animator.duration = DEFAULT_ANIMATION_TIME.toLong()
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { animation -> onAnimateUpdated(animation) }
        return animator
    }

    override fun progress(progress: Float): SlideAnimation {
        val currentAnimator = animator
        if (currentAnimator != null) {
            val playTime = (progress * animationDuration).toLong()
            val valuesArray = currentAnimator.values
            if (valuesArray != null && valuesArray.isNotEmpty()) {
                currentAnimator.currentPlayTime = playTime
            }
        }
        return this
    }

    fun with(coordinateStart: Int, coordinateEnd: Int): SlideAnimation {
        val currentAnimator = animator
        if (currentAnimator != null && hasChanges(coordinateStart, coordinateEnd)) {
            this.coordinateStart = coordinateStart
            this.coordinateEnd = coordinateEnd
            currentAnimator.setValues(PropertyValuesHolder.ofInt("ANIMATION_COORDINATE", coordinateStart, coordinateEnd).also { it.setEvaluator(IntEvaluator()) })
        }
        return this
    }

    private fun onAnimateUpdated(animation: ValueAnimator) {
        value.coordinate = animation.getAnimatedValue("ANIMATION_COORDINATE") as Int
        listener?.onValueUpdated(value)
    }

    private fun hasChanges(coordinateStart: Int, coordinateEnd: Int): Boolean = this.coordinateStart != coordinateStart || this.coordinateEnd != coordinateEnd
}

class SwapAnimation(listener: ValueController.UpdateListener) : BaseAnimation<ValueAnimator?>(listener) {
    private var coordinateStart: Int = -1
    private var coordinateEnd: Int = -1
    private val value = SwapAnimationValue()

    override fun createAnimator(): ValueAnimator {
        val animator = ValueAnimator()
        animator.duration = DEFAULT_ANIMATION_TIME.toLong()
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { animation -> onAnimateUpdated(animation) }
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
            val holder = PropertyValuesHolder.ofInt("ANIMATION_COORDINATE", coordinateStart, coordinateEnd).also { it.setEvaluator(IntEvaluator()) }
            val holderReverse = PropertyValuesHolder.ofInt("ANIMATION_COORDINATE_REVERSE", coordinateEnd, coordinateStart).also { it.setEvaluator(IntEvaluator()) }
            currentAnimator.setValues(holder, holderReverse)
        }
        return this
    }

    private fun onAnimateUpdated(animation: ValueAnimator) {
        value.coordinate = animation.getAnimatedValue("ANIMATION_COORDINATE") as Int
        value.coordinateReverse = animation.getAnimatedValue("ANIMATION_COORDINATE_REVERSE") as Int
        listener?.onValueUpdated(value)
    }

    private fun hasChanges(coordinateStart: Int, coordinateEnd: Int): Boolean = this.coordinateStart != coordinateStart || this.coordinateEnd != coordinateEnd
}

open class WormAnimation(listener: ValueController.UpdateListener) : BaseAnimation<AnimatorSet?>(listener) {
    var coordinateStart: Int = 0
    var coordinateEnd: Int = 0
    var radius: Int = 0
    var isRightSide: Boolean = false
    var rectLeftEdge: Int = 0
    var rectRightEdge: Int = 0
    private val value = WormAnimationValue()

    override fun createAnimator(): AnimatorSet {
        val animator = AnimatorSet()
        animator.interpolator = AccelerateDecelerateInterpolator()
        return animator
    }

    override fun duration(duration: Long): WormAnimation? {
        super.duration(duration)
        return this
    }

    open fun with(coordinateStart: Int, coordinateEnd: Int, radius: Int, isRightSide: Boolean): WormAnimation? {
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
            val halfDuration = animationDuration / 2
            currentAnimator.playSequentially(
                createWormAnimator(rect.fromX, rect.toX, halfDuration, false, value),
                createWormAnimator(rect.reverseFromX, rect.reverseToX, halfDuration, true, value)
            )
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
            if (setDuration > duration) setDuration = duration
            childAnimator.currentPlayTime = setDuration
            progressDuration -= setDuration
        }
        return this
    }

    fun createWormAnimator(fromValue: Int, toValue: Int, duration: Long, isReverse: Boolean, value: WormAnimationValue): ValueAnimator {
        val anim = ValueAnimator.ofInt(fromValue, toValue).apply {
            interpolator = AccelerateDecelerateInterpolator()
            this.duration = duration
        }
        anim.addUpdateListener { animation -> onAnimateUpdated(value, animation, isReverse) }
        return anim
    }

    private fun onAnimateUpdated(value: WormAnimationValue, animation: ValueAnimator, isReverse: Boolean) {
        val rectEdge = animation.animatedValue as Int

        if (isRightSide) {
            if (!isReverse) value.rectEnd = rectEdge else value.rectStart = rectEdge
        } else {
            if (!isReverse) value.rectStart = rectEdge else value.rectEnd = rectEdge
        }

        listener?.onValueUpdated(value)
    }

    fun hasChanges(
        coordinateStart: Int,
        coordinateEnd: Int,
        radius: Int,
        isRightSide: Boolean
    ): Boolean {
        return this.coordinateStart != coordinateStart ||
                this.coordinateEnd != coordinateEnd ||
                this.radius != radius ||
                this.isRightSide != isRightSide
    }

    fun createRectValues(isRightSide: Boolean): RectValues {
        val fromX = if (isRightSide) coordinateStart + radius else coordinateStart - radius
        val toX = if (isRightSide) coordinateEnd + radius else coordinateEnd - radius
        val reverseFromX = if (isRightSide) coordinateStart - radius else coordinateStart + radius
        val reverseToX = if (isRightSide) coordinateEnd - radius else coordinateEnd + radius
        return RectValues(fromX, toX, reverseFromX, reverseToX)
    }

    inner class RectValues(val fromX: Int, val toX: Int, val reverseFromX: Int, val reverseToX: Int)
}

class ThinWormAnimation(listener: ValueController.UpdateListener) : WormAnimation(listener) {
    private val value = ThinWormAnimationValue()

    override fun duration(duration: Long): ThinWormAnimation {
        super.duration(duration)
        return this
    }

    override fun with(coordinateStart: Int, coordinateEnd: Int, radius: Int, isRightSide: Boolean): WormAnimation {
        if (hasChanges(coordinateStart, coordinateEnd, radius, isRightSide)) {
            val newAnimator = createAnimator()
            animator = newAnimator
            this.coordinateStart = coordinateStart
            this.coordinateEnd = coordinateEnd
            this.radius = radius
            this.isRightSide = isRightSide
            rectLeftEdge = coordinateStart - radius
            rectRightEdge = coordinateStart + radius
            value.rectStart = rectLeftEdge
            value.rectEnd = rectRightEdge
            value.height = radius * 2
            val rec = createRectValues(isRightSide)
            val sizeDuration = (animationDuration * 0.8).toLong()
            val reverseDelay = (animationDuration * 0.2).toLong()
            val heightDuration = (animationDuration * 0.5).toLong()
            newAnimator.playTogether(
                createWormAnimator(rec.fromX, rec.toX, sizeDuration, false, value),
                createWormAnimator(rec.reverseFromX, rec.reverseToX, sizeDuration, true, value).apply { startDelay = reverseDelay },
                createHeightAnimator(radius * 2, radius, heightDuration),
                createHeightAnimator(radius, radius * 2, heightDuration).apply { startDelay = heightDuration }
            )
        }
        return this
    }

    private fun createHeightAnimator(fromHeight: Int, toHeight: Int, duration: Long): ValueAnimator {
        return ValueAnimator.ofInt(fromHeight, toHeight).apply {
            interpolator = AccelerateDecelerateInterpolator()
            this.duration = duration
            addUpdateListener { animation -> onAnimateUpdated(animation) }
        }
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
                if (setDuration > duration) setDuration = duration else if (setDuration < 0) setDuration = 0
                if (i == size - 1 && setDuration <= 0) continue
                val values = anim.values
                if (values != null && values.isNotEmpty()) {
                    anim.currentPlayTime = setDuration
                }
            }
        }
        return this
    }
}

class DropAnimation(listener: ValueController.UpdateListener) : BaseAnimation<AnimatorSet?>(listener) {
    private var widthStart = 0
    private var widthEnd = 0
    private var heightStart = 0
    private var heightEnd = 0
    private var radius = 0
    private enum class AnimationType { Width, Height, Radius }
    private val value = DropAnimationValue()

    override fun createAnimator(): AnimatorSet = AnimatorSet().apply { interpolator = AccelerateDecelerateInterpolator() }

    override fun progress(progress: Float): DropAnimation {
        val currentAnimator = animator
        if (currentAnimator != null) {
            val playTimeLeft = (progress * animationDuration).toLong()
            var isReverse = false
            for (anim in currentAnimator.childAnimations) {
                val valueAnim = anim as ValueAnimator
                val animDuration = valueAnim.duration
                var currPlayTime = playTimeLeft
                if (isReverse) currPlayTime -= animDuration
                if (currPlayTime < 0) continue else if (currPlayTime >= animDuration) currPlayTime = animDuration
                val valuesArray = valueAnim.values
                if (valuesArray != null && valuesArray.isNotEmpty()) {
                    valueAnim.currentPlayTime = currPlayTime
                }
                if (!isReverse && animDuration >= animationDuration) isReverse = true
            }
        }
        return this
    }

    override fun duration(duration: Long): DropAnimation {
        super.duration(duration)
        return this
    }

    fun with(widthStart: Int, widthEnd: Int, heightStart: Int, heightEnd: Int, radius: Int): DropAnimation {
        if (hasChanges(widthStart, widthEnd, heightStart, heightEnd, radius)) {
            val newAnimator = createAnimator()
            animator = newAnimator
            this.widthStart = widthStart
            this.widthEnd = widthEnd
            this.heightStart = heightStart
            this.heightEnd = heightEnd
            this.radius = radius
            val toRadius = (radius / 1.5).toInt()
            val halfDuration = animationDuration / 2
            newAnimator.play(createValueAnimation(heightStart, heightEnd, halfDuration, AnimationType.Height))
                .with(createValueAnimation(radius, toRadius, halfDuration, AnimationType.Radius))
                .with(createValueAnimation(widthStart, widthEnd, animationDuration, AnimationType.Width))
                .before(createValueAnimation(heightEnd, heightStart, halfDuration, AnimationType.Height))
                .before(createValueAnimation(toRadius, radius, halfDuration, AnimationType.Radius))
        }
        return this
    }

    private fun createValueAnimation(fromValue: Int, toValue: Int, duration: Long, type: AnimationType): ValueAnimator {
        return ValueAnimator.ofInt(fromValue, toValue).apply {
            interpolator = AccelerateDecelerateInterpolator()
            this.duration = duration
            addUpdateListener { animation -> onAnimatorUpdate(animation, type) }
        }
    }

    private fun onAnimatorUpdate(animation: ValueAnimator, type: AnimationType) {
        val frameValue = animation.animatedValue as Int
        when (type) {
            AnimationType.Width -> value.width = frameValue
            AnimationType.Height -> value.height = frameValue
            AnimationType.Radius -> value.radius = frameValue
        }
        listener?.onValueUpdated(value)
    }

    private fun hasChanges(widthStart: Int, widthEnd: Int, heightStart: Int, heightEnd: Int, radius: Int): Boolean {
        return this.widthStart != widthStart || this.widthEnd != widthEnd || this.heightStart != heightStart || this.heightEnd != heightEnd || this.radius != radius
    }
}
package io.selimdawa.autoimageslider.view.animation.type

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.IntEvaluator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import io.selimdawa.autoimageslider.view.animation.controller.ValueController
import io.selimdawa.autoimageslider.view.animation.data.ColorAnimationValue
import io.selimdawa.autoimageslider.view.animation.data.DropAnimationValue
import io.selimdawa.autoimageslider.view.animation.data.FillAnimationValue
import io.selimdawa.autoimageslider.view.animation.data.ScaleAnimationValue
import io.selimdawa.autoimageslider.view.animation.data.SlideAnimationValue
import io.selimdawa.autoimageslider.view.animation.data.SwapAnimationValue
import io.selimdawa.autoimageslider.view.animation.data.ThinWormAnimationValue
import io.selimdawa.autoimageslider.view.animation.data.WormAnimationValue

abstract class BaseAnimation<T : Animator?>(protected var listener: ValueController.UpdateListener?) {
    protected var animationDuration = DEFAULT_ANIMATION_TIME.toLong()
    protected var animator = createAnimator()

    abstract fun createAnimator(): T
    abstract fun progress(progress: Float): BaseAnimation<*>?

    open fun duration(duration: Long) = this.apply {
        animationDuration = duration
        (animator as? ValueAnimator)?.duration = animationDuration
    }

    fun start() = animator?.takeIf { !it.isRunning }?.start()
    fun end() = animator?.takeIf { it.isStarted }?.end()

    companion object {
        const val DEFAULT_ANIMATION_TIME = 350
    }
}

open class ColorAnimation(listener: ValueController.UpdateListener?) :
    BaseAnimation<ValueAnimator?>(listener) {
    protected val value = ColorAnimationValue()
    var colorStart = 0
    var colorEnd = 0

    override fun createAnimator() = ValueAnimator().apply {
        duration = DEFAULT_ANIMATION_TIME.toLong()
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener { onAnimateUpdated(it) }
    }

    override fun progress(progress: Float) = this.apply {
        animator?.takeIf { it.values != null && it.values.isNotEmpty() }?.currentPlayTime =
            (progress * animationDuration).toLong()
    }

    fun with(colorStart: Int, colorEnd: Int) = this.apply {
        if (animator != null && (this.colorStart != colorStart || this.colorEnd != colorEnd)) {
            this.colorStart = colorStart; this.colorEnd = colorEnd
            animator?.setValues(createColorPropertyHolder(false), createColorPropertyHolder(true))
        }
    }

    fun createColorPropertyHolder(isReverse: Boolean) = PropertyValuesHolder.ofInt(
        if (isReverse) ANIMATION_COLOR_REVERSE else ANIMATION_COLOR,
        if (isReverse) colorEnd else colorStart,
        if (isReverse) colorStart else colorEnd
    ).apply { setEvaluator(ArgbEvaluator()) }!!

    private fun onAnimateUpdated(animation: ValueAnimator) {
        value.color = animation.getAnimatedValue(ANIMATION_COLOR) as Int
        value.colorReverse = animation.getAnimatedValue(ANIMATION_COLOR_REVERSE) as Int
        listener?.onValueUpdated(value)
    }

    companion object {
        const val ANIMATION_COLOR_REVERSE = "ANIMATION_COLOR_REVERSE"
        const val ANIMATION_COLOR = "ANIMATION_COLOR"
    }
}

class FillAnimation(listener: ValueController.UpdateListener) : ColorAnimation(listener) {
    private val fillValue = FillAnimationValue()
    private var radius = 0
    private var stroke = 0

    override fun createAnimator() = ValueAnimator().apply {
        duration = DEFAULT_ANIMATION_TIME.toLong()
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener { onAnimateUpdated(it) }
    }

    fun with(colorStart: Int, colorEnd: Int, radius: Int, stroke: Int) = this.apply {
        if (animator != null && (this.colorStart != colorStart || this.colorEnd != colorEnd || this.radius != radius || this.stroke != stroke)) {
            this.colorStart = colorStart; this.colorEnd = colorEnd; this.radius =
                radius; this.stroke = stroke
            animator?.setValues(
                createColorPropertyHolder(false),
                createColorPropertyHolder(true),
                createRadiusPropertyHolder(false),
                createRadiusPropertyHolder(true),
                createStrokePropertyHolder(false),
                createStrokePropertyHolder(true)
            )
        }
    }

    private fun createRadiusPropertyHolder(isReverse: Boolean) = PropertyValuesHolder.ofInt(
        if (isReverse) "ANIMATION_RADIUS_REVERSE" else "ANIMATION_RADIUS",
        if (isReverse) radius / 2 else radius,
        if (isReverse) radius else radius / 2
    ).apply { setEvaluator(IntEvaluator()) }

    private fun createStrokePropertyHolder(isReverse: Boolean) = PropertyValuesHolder.ofInt(
        if (isReverse) "ANIMATION_STROKE_REVERSE" else "ANIMATION_STROKE",
        if (isReverse) radius else 0,
        if (isReverse) 0 else radius
    ).apply { setEvaluator(IntEvaluator()) }

    private fun onAnimateUpdated(animation: ValueAnimator) {
        fillValue.color = animation.getAnimatedValue(ANIMATION_COLOR) as Int
        fillValue.colorReverse = animation.getAnimatedValue(ANIMATION_COLOR_REVERSE) as Int
        fillValue.radius = animation.getAnimatedValue("ANIMATION_RADIUS") as Int
        fillValue.radiusReverse = animation.getAnimatedValue("ANIMATION_RADIUS_REVERSE") as Int
        fillValue.stroke = animation.getAnimatedValue("ANIMATION_STROKE") as Int
        fillValue.strokeReverse = animation.getAnimatedValue("ANIMATION_STROKE_REVERSE") as Int
        listener?.onValueUpdated(fillValue)
    }
}

open class ScaleAnimation(listener: ValueController.UpdateListener) : ColorAnimation(listener) {
    var radius = 0
    var scaleFactor = 0f
    protected val scaleValue = ScaleAnimationValue()

    override fun createAnimator() = ValueAnimator().apply {
        duration = DEFAULT_ANIMATION_TIME.toLong()
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener { onAnimateUpdated(it) }
    }

    open fun with(colorStart: Int, colorEnd: Int, radius: Int, scaleFactor: Float) = this.apply {
        if (animator != null && (this.colorStart != colorStart || this.colorEnd != colorEnd || this.radius != radius || this.scaleFactor != scaleFactor)) {
            this.colorStart = colorStart; this.colorEnd = colorEnd; this.radius =
                radius; this.scaleFactor = scaleFactor
            animator?.setValues(
                createColorPropertyHolder(false),
                createColorPropertyHolder(true),
                createScalePropertyHolder(false),
                createScalePropertyHolder(true)
            )
        }
    }

    private fun onAnimateUpdated(animation: ValueAnimator) {
        scaleValue.color = animation.getAnimatedValue(ANIMATION_COLOR) as Int
        scaleValue.colorReverse = animation.getAnimatedValue(ANIMATION_COLOR_REVERSE) as Int
        scaleValue.radius = animation.getAnimatedValue(ANIMATION_SCALE) as Int
        scaleValue.radiusReverse = animation.getAnimatedValue(ANIMATION_SCALE_REVERSE) as Int
        listener?.onValueUpdated(scaleValue)
    }

    protected open fun createScalePropertyHolder(isReverse: Boolean): PropertyValuesHolder? =
        PropertyValuesHolder.ofInt(
            if (isReverse) ANIMATION_SCALE_REVERSE else ANIMATION_SCALE,
            if (isReverse) radius else (radius * scaleFactor).toInt(),
            if (isReverse) (radius * scaleFactor).toInt() else radius
        ).apply { setEvaluator(IntEvaluator()) }

    companion object {
        const val ANIMATION_SCALE_REVERSE = "ANIMATION_SCALE_REVERSE"
        const val ANIMATION_SCALE = "ANIMATION_SCALE"
    }
}

class ScaleDownAnimation(listener: ValueController.UpdateListener) : ScaleAnimation(listener) {

    override fun createScalePropertyHolder(isReverse: Boolean) = PropertyValuesHolder.ofInt(
        if (isReverse) ANIMATION_SCALE_REVERSE else ANIMATION_SCALE,
        if (isReverse) (radius * scaleFactor).toInt() else radius,
        if (isReverse) radius else (radius * scaleFactor).toInt()
    ).apply { setEvaluator(IntEvaluator()) }!!

    override fun with(
        colorStart: Int, colorEnd: Int, radius: Int, scaleFactor: Float
    ): ScaleAnimation {
        if (animator != null && (this.colorStart != colorStart || this.colorEnd != colorEnd || this.radius != radius || this.scaleFactor != scaleFactor)) {
            this.colorStart = colorStart; this.colorEnd = colorEnd; this.radius =
                radius; this.scaleFactor = scaleFactor
            animator?.setValues(
                createColorPropertyHolder(false),
                createColorPropertyHolder(true),
                createScalePropertyHolder(false),
                createScalePropertyHolder(true)
            )
        }
        return this
    }
}

class SlideAnimation(listener: ValueController.UpdateListener) :
    BaseAnimation<ValueAnimator?>(listener) {
    private val slideValue = SlideAnimationValue()
    private var coordinateStart = -1
    private var coordinateEnd = -1

    override fun createAnimator() = ValueAnimator().apply {
        duration = DEFAULT_ANIMATION_TIME.toLong()
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener { onAnimateUpdated(it) }
    }

    override fun progress(progress: Float) = this.apply {
        animator?.takeIf { it.values != null && it.values.isNotEmpty() }?.currentPlayTime =
            (progress * animationDuration).toLong()
    }

    fun with(coordinateStart: Int, coordinateEnd: Int) = this.apply {
        if (animator != null && (this.coordinateStart != coordinateStart || this.coordinateEnd != coordinateEnd)) {
            this.coordinateStart = coordinateStart; this.coordinateEnd = coordinateEnd
            animator?.setValues(
                PropertyValuesHolder.ofInt(
                    "ANIMATION_COORDINATE", coordinateStart, coordinateEnd
                ).apply { setEvaluator(IntEvaluator()) })
        }
    }

    private fun onAnimateUpdated(animation: ValueAnimator) {
        slideValue.coordinate = animation.getAnimatedValue("ANIMATION_COORDINATE") as Int
        listener?.onValueUpdated(slideValue)
    }
}

class SwapAnimation(listener: ValueController.UpdateListener) :
    BaseAnimation<ValueAnimator?>(listener) {
    private var coordinateStart = -1
    private var coordinateEnd = -1
    private val swapValue = SwapAnimationValue()

    override fun createAnimator() = ValueAnimator().apply {
        duration = DEFAULT_ANIMATION_TIME.toLong()
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener { onAnimateUpdated(it) }
    }

    override fun progress(progress: Float) = this.apply {
        animator?.takeIf { it.values != null && it.values.isNotEmpty() }?.currentPlayTime =
            (progress * animationDuration).toLong()
    }

    fun with(coordinateStart: Int, coordinateEnd: Int) = this.apply {
        if (animator != null && (this.coordinateStart != coordinateStart || this.coordinateEnd != coordinateEnd)) {
            this.coordinateStart = coordinateStart; this.coordinateEnd = coordinateEnd
            animator?.setValues(
                PropertyValuesHolder.ofInt("ANIMATION_COORDINATE", coordinateStart, coordinateEnd)
                    .apply { setEvaluator(IntEvaluator()) }, PropertyValuesHolder.ofInt(
                    "ANIMATION_COORDINATE_REVERSE", coordinateEnd, coordinateStart
                ).apply { setEvaluator(IntEvaluator()) })
        }
    }

    private fun onAnimateUpdated(animation: ValueAnimator) {
        swapValue.coordinate = animation.getAnimatedValue("ANIMATION_COORDINATE") as Int
        swapValue.coordinateReverse =
            animation.getAnimatedValue("ANIMATION_COORDINATE_REVERSE") as Int
        listener?.onValueUpdated(swapValue)
    }
}

open class WormAnimation(listener: ValueController.UpdateListener) :
    BaseAnimation<AnimatorSet?>(listener) {
    var coordinateStart = 0
    var coordinateEnd = 0
    var radius = 0
    var isRightSide = false
    var rectLeftEdge = 0
    var rectRightEdge = 0
    protected val wormValue = WormAnimationValue()

    override fun createAnimator() =
        AnimatorSet().apply { interpolator = AccelerateDecelerateInterpolator() }

    open fun with(coordinateStart: Int, coordinateEnd: Int, radius: Int, isRightSide: Boolean) =
        this.apply {
            if (hasChanges(coordinateStart, coordinateEnd, radius, isRightSide)) {
                this@WormAnimation.coordinateStart =
                    coordinateStart; this@WormAnimation.coordinateEnd = coordinateEnd
                this@WormAnimation.radius = radius; this@WormAnimation.isRightSide = isRightSide
                rectLeftEdge = coordinateStart - radius; rectRightEdge = coordinateStart + radius
                wormValue.rectStart = rectLeftEdge; wormValue.rectEnd = rectRightEdge
                val rect = createRectValues(isRightSide)
                val halfDuration = animationDuration / 2
                animator?.playSequentially(
                    createWormAnimator(rect.fromX, rect.toX, halfDuration, false, wormValue),
                    createWormAnimator(
                        rect.reverseFromX, rect.reverseToX, halfDuration, true, wormValue
                    )
                )
            }
        }

    override fun progress(progress: Float) = this.apply {
        var progressDuration = (progress * animationDuration).toLong()
        animator?.childAnimations?.forEach { anim ->
            val child = anim as ValueAnimator
            val d = child.duration
            val setDuration = if (progressDuration > d) d else progressDuration
            child.currentPlayTime = setDuration; progressDuration -= setDuration
        }
    }

    fun createWormAnimator(
        from: Int, to: Int, d: Long, isReverse: Boolean, value: WormAnimationValue
    ) = ValueAnimator.ofInt(from, to).apply {
        interpolator = AccelerateDecelerateInterpolator(); duration = d
        addUpdateListener { onAnimateUpdated(value, it, isReverse) }
    }!!

    private fun onAnimateUpdated(
        value: WormAnimationValue, animation: ValueAnimator, isReverse: Boolean
    ) {
        val edge = animation.animatedValue as Int
        if (isRightSide) {
            if (!isReverse) value.rectEnd = edge else value.rectStart = edge
        } else {
            if (!isReverse) value.rectStart = edge else value.rectEnd = edge
        }
        listener?.onValueUpdated(value)
    }

    fun hasChanges(cStart: Int, cEnd: Int, r: Int, isRight: Boolean) =
        coordinateStart != cStart || coordinateEnd != cEnd || radius != r || isRightSide != isRight

    fun createRectValues(isRightSide: Boolean) = if (isRightSide) {
        RectValues(
            coordinateStart + radius,
            coordinateEnd + radius,
            coordinateStart - radius,
            coordinateEnd - radius
        )
    } else {
        RectValues(
            coordinateStart - radius,
            coordinateEnd - radius,
            coordinateStart + radius,
            coordinateEnd + radius
        )
    }

    class RectValues(val fromX: Int, val toX: Int, val reverseFromX: Int, val reverseToX: Int)
}

class ThinWormAnimation(listener: ValueController.UpdateListener) : WormAnimation(listener) {
    private val thinWormValue = ThinWormAnimationValue()

    override fun duration(duration: Long) = this.apply { super.duration(duration) }

    override fun with(coordinateStart: Int, coordinateEnd: Int, radius: Int, isRightSide: Boolean) =
        this.apply {
            if (hasChanges(coordinateStart, coordinateEnd, radius, isRightSide)) {
                this@ThinWormAnimation.coordinateStart =
                    coordinateStart; this@ThinWormAnimation.coordinateEnd = coordinateEnd
                this@ThinWormAnimation.radius = radius; this@ThinWormAnimation.isRightSide =
                    isRightSide
                rectLeftEdge = coordinateStart - radius; rectRightEdge = coordinateStart + radius
                thinWormValue.rectStart = rectLeftEdge; thinWormValue.rectEnd =
                    rectRightEdge; thinWormValue.height = radius * 2
                val rec = createRectValues(isRightSide)
                val d = animationDuration
                animator?.playTogether(
                    createWormAnimator(
                        rec.fromX, rec.toX, (d * 0.8).toLong(), false, thinWormValue
                    ),
                    createWormAnimator(
                        rec.reverseFromX, rec.reverseToX, (d * 0.8).toLong(), true, thinWormValue
                    ).apply { startDelay = (d * 0.2).toLong() },
                    createHeightAnimator(radius * 2, radius, (d * 0.5).toLong()),
                    createHeightAnimator(
                        radius, radius * 2, (d * 0.5).toLong()
                    ).apply { startDelay = (d * 0.5).toLong() })
            }
        }

    private fun createHeightAnimator(from: Int, to: Int, d: Long) =
        ValueAnimator.ofInt(from, to).apply {
            interpolator = AccelerateDecelerateInterpolator(); duration = d
            addUpdateListener { onAnimateUpdated(it) }
        }

    private fun onAnimateUpdated(animation: ValueAnimator) {
        thinWormValue.height = animation.animatedValue as Int
        listener?.onValueUpdated(thinWormValue)
    }

    override fun progress(progress: Float) = this.apply {
        val progressDuration = (progress * animationDuration).toLong()
        val childAnimators = animator?.childAnimations ?: return@apply
        val size = childAnimators.size
        for (i in 0 until size) {
            val anim = childAnimators[i] as ValueAnimator
            val setDuration = (progressDuration - anim.startDelay).coerceIn(0, anim.duration)
            if (i == size - 1 && setDuration <= 0) continue
            if (anim.values != null && anim.values.isNotEmpty()) anim.currentPlayTime = setDuration
        }
    }
}

class DropAnimation(listener: ValueController.UpdateListener) :
    BaseAnimation<AnimatorSet?>(listener) {
    private var widthStart = 0
    private var widthEnd = 0
    private var heightStart = 0
    private var heightEnd = 0
    private var radius = 0

    private enum class AnimationType { Width, Height, Radius }

    private val dropValue = DropAnimationValue()

    override fun createAnimator() =
        AnimatorSet().apply { interpolator = AccelerateDecelerateInterpolator() }

    override fun progress(progress: Float) = this.apply {
        val playTimeLeft = (progress * animationDuration).toLong()
        var isReverse = false
        animator?.childAnimations?.forEach { anim ->
            val valueAnim = anim as ValueAnimator
            val d = valueAnim.duration
            val currPlayTime = if (isReverse) playTimeLeft - d else playTimeLeft
            if (currPlayTime in 0..d) {
                if (valueAnim.values != null && valueAnim.values.isNotEmpty()) valueAnim.currentPlayTime =
                    currPlayTime
            }
            if (!isReverse && d >= animationDuration) isReverse = true
        }
    }

    override fun duration(duration: Long) = this.apply { super.duration(duration) }

    fun with(widthStart: Int, widthEnd: Int, heightStart: Int, heightEnd: Int, radius: Int) =
        this.apply {
            if (this.widthStart != widthStart || this.widthEnd != widthEnd || this.heightStart != heightStart || this.heightEnd != heightEnd || this.radius != radius) {
                this@DropAnimation.widthStart = widthStart; this@DropAnimation.widthEnd = widthEnd
                this@DropAnimation.heightStart = heightStart; this@DropAnimation.heightEnd =
                    heightEnd; this@DropAnimation.radius = radius
                val toRadius = (radius / 1.5).toInt()
                val halfDuration = animationDuration / 2
                animator?.play(
                    createValueAnimation(
                        heightStart, heightEnd, halfDuration, AnimationType.Height
                    )
                )?.with(
                    createValueAnimation(
                        radius, toRadius, halfDuration, AnimationType.Radius
                    )
                )?.with(
                    createValueAnimation(
                        widthStart, widthEnd, animationDuration, AnimationType.Width
                    )
                )?.before(
                    createValueAnimation(
                        heightEnd, heightStart, halfDuration, AnimationType.Height
                    )
                )?.before(
                    createValueAnimation(
                        toRadius, radius, halfDuration, AnimationType.Radius
                    )
                )
            }
        }

    private fun createValueAnimation(from: Int, to: Int, d: Long, type: AnimationType) =
        ValueAnimator.ofInt(from, to).apply {
            interpolator = AccelerateDecelerateInterpolator(); duration = d
            addUpdateListener { onAnimatorUpdate(it, type) }
        }

    private fun onAnimatorUpdate(animation: ValueAnimator, type: AnimationType) {
        val frameValue = animation.animatedValue as Int
        when (type) {
            AnimationType.Width -> dropValue.width = frameValue
            AnimationType.Height -> dropValue.height = frameValue
            AnimationType.Radius -> dropValue.radius = frameValue
        }
        listener?.onValueUpdated(dropValue)
    }
}
package io.selimdawa.autoimageslider.view.animation

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import io.selimdawa.autoimageslider.view.model.ColorAnimationValue
import io.selimdawa.autoimageslider.view.model.DropAnimationValue
import io.selimdawa.autoimageslider.view.model.FillAnimationValue
import io.selimdawa.autoimageslider.view.model.Indicator
import io.selimdawa.autoimageslider.view.model.IndicatorAnimationType
import io.selimdawa.autoimageslider.view.model.JumpAnimationValue
import io.selimdawa.autoimageslider.view.model.Orientation
import io.selimdawa.autoimageslider.view.model.ScaleAnimationValue
import io.selimdawa.autoimageslider.view.model.SlideAnimationValue
import io.selimdawa.autoimageslider.view.model.SwapAnimationValue
import io.selimdawa.autoimageslider.view.model.ThinWormAnimationValue
import io.selimdawa.autoimageslider.view.model.Value
import io.selimdawa.autoimageslider.view.model.WormAnimationValue
import io.selimdawa.autoimageslider.view.utils.CoordinatesUtils

// --- Interfaces ---

interface UpdateListener {
    fun onValueUpdated(value: Value?)
}

// --- Animation Manager ---

class AnimationManager(private val indicator: Indicator, private val listener: UpdateListener) {
    private var runningAnimation: BaseAnimation<*>? = null
    private var progress = 0f
    private var isInteractive = false
    
    private var lastType: IndicatorAnimationType? = null
    private var lastFrom: Int = -1
    private var lastTo: Int = -1

    fun basic() {
        isInteractive = false; progress = 0f; animate()
    }

    fun interactive(progress: Float) {
        isInteractive = true; this.progress = progress; animate()
    }

    fun end() = runningAnimation?.end()

    private fun animate() {
        val (from, to) = getTargetCoordinates()
        val type = indicator.animationType
        
        // Reuse animation if possible
        if (runningAnimation != null && lastType == type && lastFrom == from && lastTo == to) {
            if (isInteractive) runningAnimation?.progress(progress) else runningAnimation?.start()
            return
        }

        end()
        lastType = type; lastFrom = from; lastTo = to
        val duration = indicator.animationDuration

        runningAnimation = when (type) {
            IndicatorAnimationType.COLOR -> ColorAnimation(listener).with(
                indicator.unselectedColor,
                indicator.selectedColor
            )

            IndicatorAnimationType.SCALE -> ScaleAnimation(listener).with(
                indicator.unselectedColor,
                indicator.selectedColor,
                indicator.radius,
                indicator.scaleFactor
            )

            IndicatorAnimationType.WORM -> WormAnimation(listener).with(
                from,
                to,
                indicator.radius,
                to > from
            )

            IndicatorAnimationType.SLIDE -> SlideAnimation(listener).with(from, to)

            IndicatorAnimationType.FILL -> FillAnimation(listener).with(
                indicator.unselectedColor,
                indicator.selectedColor,
                indicator.radius,
                indicator.stroke
            )

            IndicatorAnimationType.THIN_WORM -> ThinWormAnimation(listener).with(
                from,
                to,
                indicator.radius,
                to > from
            )

            IndicatorAnimationType.DROP -> DropAnimation(listener).with(
                from,
                to,
                indicator.radius * 3 + (if (indicator.orientation == Orientation.HORIZONTAL) indicator.paddingTop else indicator.paddingLeft),
                indicator.radius + (if (indicator.orientation == Orientation.HORIZONTAL) indicator.paddingTop else indicator.paddingLeft),
                indicator.radius
            )

            IndicatorAnimationType.SWAP -> SwapAnimation(listener).with(from, to)

            IndicatorAnimationType.SCALE_DOWN -> ScaleDownAnimation(listener).with(
                indicator.unselectedColor,
                indicator.selectedColor,
                indicator.radius,
                indicator.scaleFactor
            )

            IndicatorAnimationType.JUMP -> JumpAnimation(listener).with(from, to, indicator.radius)

            else -> {
                listener.onValueUpdated(null); null
            }
        }?.duration(duration)?.apply {
            if (isInteractive) progress(progress) else start()
        }
    }

    private fun getTargetCoordinates(): Pair<Int, Int> {
        val fromPos =
            if (indicator.isInteractiveAnimation) indicator.selectedPosition else indicator.lastSelectedPosition
        val toPos =
            if (indicator.isInteractiveAnimation) indicator.selectingPosition else indicator.selectedPosition
        return CoordinatesUtils.getCoordinate(indicator, fromPos) to CoordinatesUtils.getCoordinate(
            indicator, toPos
        )
    }
}

// --- Base Animation ---

abstract class BaseAnimation<T : Animator?>(protected var listener: UpdateListener?) {
    protected var animationDuration: Long = 350L
    protected var animator: T = createAnimator()

    abstract fun createAnimator(): T
    abstract fun progress(progress: Float): BaseAnimation<*>?

    fun duration(duration: Long) = this.apply {
        animationDuration = duration
        (animator as? ValueAnimator)?.duration = animationDuration
    }

    fun start() = animator?.takeIf { !it.isRunning }?.start()
    fun end() = animator?.takeIf { it.isStarted }?.end()
}

// --- Animation Implementations ---

open class ColorAnimation(listener: UpdateListener?) : BaseAnimation<ValueAnimator?>(listener) {
    protected val value = ColorAnimationValue()
    private var colorStart = 0
    private var colorEnd = 0

    override fun createAnimator(): ValueAnimator = ValueAnimator().apply {
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener {
            value.color = it.getAnimatedValue("COLOR") as Int
            value.colorReverse = it.getAnimatedValue("COLOR_REVERSE") as Int
            listener?.onValueUpdated(value)
        }
    }

    override fun progress(progress: Float) = this.apply {
        animator?.takeIf { it.values != null && it.values.isNotEmpty() }?.currentPlayTime =
            (progress * animationDuration).toLong()
    }

    fun with(start: Int, end: Int) = this.apply {
        colorStart = start; colorEnd = end
        animator?.setValues(createHolder(false), createHolder(true))
    }

    protected fun createHolder(rev: Boolean): PropertyValuesHolder = PropertyValuesHolder.ofInt(
        if (rev) "COLOR_REVERSE" else "COLOR",
        if (rev) colorEnd else colorStart,
        if (rev) colorStart else colorEnd
    ).apply { setEvaluator(ArgbEvaluator()) }
}

class FillAnimation(listener: UpdateListener) : ColorAnimation(listener) {
    private val fillValue = FillAnimationValue()
    private var radius = 0
    private var stroke = 0

    fun with(cStart: Int, cEnd: Int, r: Int, s: Int) = this.apply {
        with(cStart, cEnd); radius = r; stroke = s
        animator?.setValues(
            createHolder(false),
            createHolder(true),
            createIntHolder("RADIUS", radius, radius / 2, false),
            createIntHolder("RADIUS", radius, radius / 2, true),
            createIntHolder("STROKE", 0, radius, false),
            createIntHolder("STROKE", 0, radius, true)
        )
    }

    private fun createIntHolder(
        name: String, start: Int, end: Int, rev: Boolean
    ): PropertyValuesHolder = PropertyValuesHolder.ofInt(
        if (rev) "${name}_REVERSE" else name, if (rev) end else start, if (rev) start else end
    )

    override fun createAnimator(): ValueAnimator = super.createAnimator().apply {
        addUpdateListener {
            fillValue.color = it.getAnimatedValue("COLOR") as Int
            fillValue.colorReverse = it.getAnimatedValue("COLOR_REVERSE") as Int
            fillValue.radius = it.getAnimatedValue("RADIUS") as Int
            fillValue.radiusReverse = it.getAnimatedValue("RADIUS_REVERSE") as Int
            fillValue.stroke = it.getAnimatedValue("STROKE") as Int
            fillValue.strokeReverse = it.getAnimatedValue("STROKE_REVERSE") as Int
            listener?.onValueUpdated(fillValue)
        }
    }
}

open class ScaleAnimation(listener: UpdateListener) : ColorAnimation(listener) {
    protected var radius = 0
    protected var scaleFactor = 0f
    protected val scaleValue = ScaleAnimationValue()

    open fun with(cStart: Int, cEnd: Int, r: Int, sf: Float) = this.apply {
        with(cStart, cEnd); radius = r; scaleFactor = sf
        animator?.setValues(
            createHolder(false),
            createHolder(true),
            createScaleHolder(false),
            createScaleHolder(true)
        )
    }

    override fun createAnimator(): ValueAnimator = super.createAnimator().apply {
        addUpdateListener {
            scaleValue.color = it.getAnimatedValue("COLOR") as Int
            scaleValue.colorReverse = it.getAnimatedValue("COLOR_REVERSE") as Int
            scaleValue.radius = it.getAnimatedValue("SCALE") as Int
            scaleValue.radiusReverse = it.getAnimatedValue("SCALE_REVERSE") as Int
            listener?.onValueUpdated(scaleValue)
        }
    }

    protected open fun createScaleHolder(rev: Boolean): PropertyValuesHolder =
        PropertyValuesHolder.ofInt(
            if (rev) "SCALE_REVERSE" else "SCALE",
            if (rev) radius else (radius * scaleFactor).toInt(),
            if (rev) (radius * scaleFactor).toInt() else radius
        )
}

class ScaleDownAnimation(listener: UpdateListener) : ScaleAnimation(listener) {
    override fun createScaleHolder(rev: Boolean): PropertyValuesHolder = PropertyValuesHolder.ofInt(
        if (rev) "SCALE_REVERSE" else "SCALE",
        if (rev) (radius * scaleFactor).toInt() else radius,
        if (rev) radius else (radius * scaleFactor).toInt()
    )
}

class SlideAnimation(listener: UpdateListener) : BaseAnimation<ValueAnimator?>(listener) {
    private val value = SlideAnimationValue()
    override fun createAnimator() = ValueAnimator().apply {
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener {
            value.coordinate = it.animatedValue as Int; listener?.onValueUpdated(
            value
        )
        }
    }

    override fun progress(progress: Float) =
        this.apply { animator?.currentPlayTime = (progress * animationDuration).toLong() }

    fun with(start: Int, end: Int) = this.apply { animator?.setIntValues(start, end) }
}

class SwapAnimation(listener: UpdateListener) : BaseAnimation<ValueAnimator?>(listener) {
    private val value = SwapAnimationValue()
    override fun createAnimator() = ValueAnimator().apply {
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener {
            value.coordinate = it.getAnimatedValue("C") as Int
            value.coordinateReverse = it.getAnimatedValue("CR") as Int
            listener?.onValueUpdated(value)
        }
    }

    override fun progress(progress: Float) =
        this.apply { animator?.currentPlayTime = (progress * animationDuration).toLong() }

    fun with(start: Int, end: Int) = this.apply {
        animator?.setValues(
            PropertyValuesHolder.ofInt("C", start, end),
            PropertyValuesHolder.ofInt("CR", end, start)
        )
    }
}

open class WormAnimation(listener: UpdateListener) : BaseAnimation<AnimatorSet?>(listener) {
    protected val wormValue = WormAnimationValue()
    protected var coordinateStart = 0
    protected var coordinateEnd = 0
    protected var radius = 0
    protected var isRightSide = false

    override fun createAnimator(): AnimatorSet =
        AnimatorSet().apply { interpolator = AccelerateDecelerateInterpolator() }

    open fun with(cStart: Int, cEnd: Int, r: Int, right: Boolean) = this.apply {
        coordinateStart = cStart; coordinateEnd = cEnd; radius = r; isRightSide = right
        wormValue.rectStart = cStart - r
        wormValue.rectEnd = cStart + r
        animator = createAnimator().apply {
            val rect = if (right) RectValues(cStart + r, cEnd + r, cStart - r, cEnd - r)
            else RectValues(cStart - r, cEnd - r, cStart + r, cEnd + r)
            playSequentially(
                createWormAnim(rect.from, rect.to, false),
                createWormAnim(rect.revFrom, rect.revTo, true)
            )
        }
    }

    private fun createWormAnim(from: Int, to: Int, rev: Boolean): ValueAnimator =
        ValueAnimator.ofInt(from, to).apply {
            duration = animationDuration / 2
            addUpdateListener {
                val edge = it.animatedValue as Int
                if (isRightSide) {
                    if (!rev) wormValue.rectEnd = edge else wormValue.rectStart = edge
                } else {
                    if (!rev) wormValue.rectStart = edge else wormValue.rectEnd = edge
                }
                listener?.onValueUpdated(wormValue)
            }
        }

    override fun progress(progress: Float) = this.apply {
        var playTime = (progress * animationDuration).toLong()
        animator?.childAnimations?.forEach {
            val anim = it as ValueAnimator
            val d = anim.duration
            anim.currentPlayTime = playTime.coerceIn(0, d)
            playTime -= d
        }
    }

    class RectValues(val from: Int, val to: Int, val revFrom: Int, val revTo: Int)
}

class ThinWormAnimation(listener: UpdateListener) : WormAnimation(listener) {
    private val thinValue = ThinWormAnimationValue()
    override fun with(cStart: Int, cEnd: Int, r: Int, right: Boolean) = this.apply {
        coordinateStart = cStart; coordinateEnd = cEnd; radius = r; isRightSide = right
        thinValue.rectStart = cStart - r
        thinValue.rectEnd = cStart + r
        thinValue.height = r * 2
        animator = createAnimator().apply {
            val rect = if (right) RectValues(cStart + r, cEnd + r, cStart - r, cEnd - r)
            else RectValues(cStart - r, cEnd - r, cStart + r, cEnd + r)
            val d = animationDuration
            playTogether(
                createThinAnim(rect.from, rect.to, (d * 0.8).toLong(), 0, false), createThinAnim(
                    rect.revFrom, rect.revTo, (d * 0.8).toLong(), (d * 0.2).toLong(), true
                ), createHeightAnim(r * 2, r, d / 2, 0), createHeightAnim(r, r * 2, d / 2, d / 2)
            )
        }
    }

    private fun createThinAnim(f: Int, t: Int, d: Long, delay: Long, rev: Boolean): ValueAnimator =
        ValueAnimator.ofInt(f, t).apply {
            duration = d; startDelay = delay
            addUpdateListener {
                val edge = it.animatedValue as Int
                if (isRightSide) {
                    if (!rev) thinValue.rectEnd = edge else thinValue.rectStart = edge
                } else {
                    if (!rev) thinValue.rectStart = edge else thinValue.rectEnd = edge
                }
                listener?.onValueUpdated(thinValue)
            }
        }

    private fun createHeightAnim(f: Int, t: Int, d: Long, delay: Long): ValueAnimator =
        ValueAnimator.ofInt(f, t).apply {
            duration = d; startDelay = delay
            addUpdateListener {
                thinValue.height = it.animatedValue as Int; listener?.onValueUpdated(thinValue)
            }
        }

    override fun progress(progress: Float) = this.apply {
        val playTime = (progress * animationDuration).toLong()
        animator?.childAnimations?.forEach {
            val anim = it as ValueAnimator
            anim.currentPlayTime = (playTime - anim.startDelay).coerceIn(0, anim.duration)
        }
    }
}

class DropAnimation(listener: UpdateListener) : BaseAnimation<AnimatorSet?>(listener) {
    private val value = DropAnimationValue()
    override fun createAnimator(): AnimatorSet =
        AnimatorSet().apply { interpolator = AccelerateDecelerateInterpolator() }

    fun with(wS: Int, wE: Int, hS: Int, hE: Int, r: Int) = this.apply {
        val half = animationDuration / 2
        animator = createAnimator().apply {
            play(createAnim(hS, hE, half) { value.height = it }).with(
                createAnim(
                    r, (r / 1.5).toInt(), half
                ) { value.radius = it })
                .with(createAnim(wS, wE, animationDuration) { value.width = it })
                .before(createAnim(hE, hS, half) { value.height = it })
                .before(createAnim((r / 1.5).toInt(), r, half) { value.radius = it })
        }
    }

    private fun createAnim(f: Int, t: Int, d: Long, update: (Int) -> Unit): ValueAnimator =
        ValueAnimator.ofInt(f, t).apply {
            duration = d
            addUpdateListener { update(it.animatedValue as Int); listener?.onValueUpdated(value) }
        }

    override fun progress(progress: Float) = this.apply {
        val playTime = (progress * animationDuration).toLong()
        animator?.childAnimations?.forEach {
            val anim = it as ValueAnimator
            anim.currentPlayTime = playTime.coerceIn(0, anim.duration)
        }
    }
}

class JumpAnimation(listener: UpdateListener) : BaseAnimation<ValueAnimator?>(listener) {
    private val value = JumpAnimationValue()
    override fun createAnimator(): ValueAnimator = ValueAnimator().apply {
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener {
            value.coordinate = it.getAnimatedValue("C") as Int
            value.radius = it.getAnimatedValue("R") as Int
            listener?.onValueUpdated(value)
        }
    }

    override fun progress(progress: Float) =
        this.apply { animator?.currentPlayTime = (progress * animationDuration).toLong() }

    fun with(start: Int, end: Int, radius: Int) = this.apply {
        animator?.setValues(
            PropertyValuesHolder.ofInt("C", start, end),
            PropertyValuesHolder.ofInt("R", 0, radius, 0)
        )
    }
}
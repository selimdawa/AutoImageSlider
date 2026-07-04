package com.selimdawa.autoimageslider.IndicatorView.animation.type

import android.animation.Animator
import android.animation.ValueAnimator
import com.selimdawa.autoimageslider.IndicatorView.animation.controller.ValueController

abstract class BaseAnimation<T : Animator?>(protected var listener: ValueController.UpdateListener?) {
    protected var animationDuration: Long = DEFAULT_ANIMATION_TIME.toLong()

    protected var animator: T?

    init {
        animator = createAnimator()
    }

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
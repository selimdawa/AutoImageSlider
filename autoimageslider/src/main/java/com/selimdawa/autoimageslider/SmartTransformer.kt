package com.selimdawa.autoimageslider

import android.view.View
import androidx.core.view.isVisible
import kotlin.math.abs
import kotlin.math.max

class SmartTransformer(private val animation: SliderAnimations) : SliderPager.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        val absPos = abs(position)

        if (animation.cameraDist > 0f) {
            page.cameraDistance = animation.cameraDist
        }

        if (animation.isCubeIn || animation.isCubeOut) {
            if (position < -1f || position > 1f) {
                page.alpha = 0f
            } else {
                page.alpha = 1f
                page.pivotX = if (position <= 0f) page.width.toFloat() else 0f
                page.rotationY =
                    if (position <= 0f) animation.rotationFactor * absPos else -animation.rotationFactor * absPos
            }
            if (animation.hasScaleY) {
                page.scaleY =
                    if (animation.scaleMode == 1) max(0.4f, 1f - absPos) else max(0.4f, absPos)
            }
            return
        }

        when (animation) {
            SliderAnimations.ANTICLOCKSPINTRANSFORMATION, SliderAnimations.CLOCK_SPINTRANSFORMATION -> {
                page.translationX = -position * page.width
                page.visibility =
                    if (absPos < 0.5f || (animation == SliderAnimations.CLOCK_SPINTRANSFORMATION && absPos <= 0.5f)) View.VISIBLE else View.GONE
                if (page.isVisible) {
                    page.scaleX = 1f - absPos
                    page.scaleY = 1f - absPos
                }
                if (position < -1f || position > 1f) page.alpha = 0f
                else {
                    page.alpha = 1f
                    val factor =
                        if (animation == SliderAnimations.ANTICLOCKSPINTRANSFORMATION) 1f - absPos else absPos
                    page.rotation = if (position <= 0f) 360f * factor else -360f * factor
                }
            }

            SliderAnimations.DEPTHTRANSFORMATION -> {
                if (position < -1f) page.alpha = 0f
                else if (position <= 0f) {
                    page.alpha = 1f
                    page.translationX = 0f
                    page.scaleX = 1f
                    page.scaleY = 1f
                } else if (position <= 1f) {
                    page.translationX = -position * page.width
                    page.alpha = 1f - absPos
                    page.scaleX = 1f - absPos
                    page.scaleY = 1f - absPos
                } else page.alpha = 0f
            }

            SliderAnimations.FADETRANSFORMATION -> {
                page.translationX = -position * page.width
                if (position < -1f || position > 1f) page.alpha = 0f
                else if (position == 0f) page.alpha = 1f
                else page.alpha = if (position <= 0f) position + 1f else 1f - position
            }

            SliderAnimations.FANTRANSFORMATION -> {
                page.translationX = -position * page.width
                page.pivotX = 0f
                page.pivotY = (page.height / 2).toFloat()
                if (position < -1f || position > 1f) page.alpha = 0f
                else {
                    page.alpha = 1f
                    page.rotationY = if (position <= 0f) -120f * absPos else 120f * absPos
                }
            }

            SliderAnimations.FIDGETSPINTRANSFORMATION -> {
                page.translationX = -position * page.width
                page.visibility = if (absPos < 0.5f) View.VISIBLE else View.GONE
                if (position < -1f || position > 1f) page.alpha = 0f
                else {
                    page.alpha = 1f
                    val powerFactor = absPos * absPos * absPos * absPos * absPos * absPos * absPos
                    page.rotation =
                        if (position <= 0f) 36000f * powerFactor else -36000f * powerFactor
                }
            }

            SliderAnimations.HINGETRANSFORMATION -> {
                page.translationX = -position * page.width
                page.pivotX = 0f
                page.pivotY = 0f
                if (position < -1f || position > 1f) page.alpha = 0f
                else if (position <= 0f) {
                    page.rotation = 90f * absPos
                    page.alpha = 1f - absPos
                } else {
                    page.rotation = 0f
                    page.alpha = 1f
                }
            }

            SliderAnimations.HORIZONTALFLIPTRANSFORMATION, SliderAnimations.SPINNERTRANSFORMATION, SliderAnimations.VERTICALFLIPTRANSFORMATION, SliderAnimations.VERTICALSHUTTRANSFORMATION -> {
                page.translationX = -position * page.width
                page.visibility = if (position in -0.5f..0.5f) View.VISIBLE else View.INVISIBLE
                if (position < -1f || position > 1f) page.alpha = 0f
                else {
                    page.alpha = 1f
                    val sign = if (position <= 0f) 1f else -1f
                    val rotationValue = sign * animation.rotationFactor * (2f - absPos)
                    if (animation.useXAxis) page.rotationX = rotationValue else page.rotationY =
                        rotationValue
                }
            }

            SliderAnimations.POPTRANSFORMATION -> {
                page.translationX = -position * page.width
                if (absPos < 0.5f) {
                    page.visibility = View.VISIBLE
                    page.scaleX = 1f - absPos
                    page.scaleY = 1f - absPos
                } else page.visibility = View.GONE
            }

            SliderAnimations.TOSSTRANSFORMATION -> {
                page.translationX = -position * page.width
                page.visibility = if (position in -0.5f..0.5f) View.VISIBLE else View.INVISIBLE
                if (position < -1f || position > 1f) page.alpha = 0f
                else {
                    page.alpha = 1f
                    page.scaleX = max(0.4f, 1f - absPos)
                    page.scaleY = max(0.4f, 1f - absPos)
                    page.rotationX = (if (position <= 0f) 1080f else -1080f) * (2f - absPos)
                    page.translationY = -1000f * absPos
                }
            }

            SliderAnimations.ZOOMOUTTRANSFORMATION -> {
                if (position < -1f || position > 1f) page.alpha = 0f
                else {
                    page.scaleX = max(0.65f, 1f - absPos)
                    page.scaleY = max(0.65f, 1f - absPos)
                    page.alpha = max(0.3f, 1f - absPos)
                }
            }

            else -> {}
        }
    }
}
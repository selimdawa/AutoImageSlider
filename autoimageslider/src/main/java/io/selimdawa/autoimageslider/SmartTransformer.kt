package io.selimdawa.autoimageslider

import android.view.View
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs
import kotlin.math.max

class SmartTransformer(private val animation: SliderAnimations) : ViewPager2.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        val absPos = abs(position)
        val inRange = position in -1f..1f
        if (animation.cameraDist > 0f) page.cameraDistance = animation.cameraDist

        when (animation) {
            SliderAnimations.CUBE_IN_DEPTH, SliderAnimations.CUBE_IN_ROTATION, SliderAnimations.CUBE_IN_SCALING, SliderAnimations.CUBE_OUT_DEPTH, SliderAnimations.CUBE_OUT_ROTATION, SliderAnimations.CUBE_OUT_SCALING, SliderAnimations.GATE -> {
                if (animation.isCubeIn || animation.isCubeOut) {
                    page.alpha = if (inRange) 1f else 0f
                    if (inRange) {
                        page.pivotX = if (position <= 0f) page.width.toFloat() else 0f
                        page.rotationY =
                            (if (position <= 0f) animation.rotationFactor else -animation.rotationFactor) * absPos
                    }
                    if (animation.hasScaleY) page.scaleY =
                        if (animation.scaleMode == 1) max(animation.minScale, 1f - absPos) else max(
                            animation.minScale, absPos
                        )
                }
            }

            SliderAnimations.ANTICLOCKWISE_SPIN, SliderAnimations.CLOCK_SPIN -> {
                page.translationX = -position * page.width
                page.visibility = if (absPos < animation.alphaThreshold) View.VISIBLE else View.GONE
                if (page.isVisible) {
                    page.scaleX = 1f - absPos; page.scaleY = 1f - absPos
                }
                page.alpha = if (inRange) 1f else 0f
                if (inRange) page.rotation =
                    (if (position <= 0f) 360f else -360f) * (if (animation == SliderAnimations.ANTICLOCKWISE_SPIN) 1f - absPos else absPos)
            }

            SliderAnimations.DEPTH -> {
                page.alpha = if (inRange) (if (position <= 0f) 1f else 1f - absPos) else 0f
                if (position in -1f..0f) {
                    page.translationX = 0f; page.scaleX = 1f; page.scaleY = 1f
                } else if (position in 0f..1f) {
                    page.translationX = -position * page.width; page.scaleX =
                        1f - absPos; page.scaleY = 1f - absPos
                }
            }

            SliderAnimations.FADE -> {
                page.translationX = -position * page.width
                page.alpha =
                    if (inRange) (if (position == 0f) 1f else if (position <= 0f) position + 1f else 1f - position) else 0f
            }

            SliderAnimations.FAN -> {
                page.translationX = -position * page.width; page.pivotX = 0f; page.pivotY =
                    page.height / 2f
                page.alpha = if (inRange) 1f else 0f
                if (inRange) page.rotationY =
                    (if (position <= 0f) animation.rotationFactor else -animation.rotationFactor) * absPos
            }

            SliderAnimations.FIDGET_SPIN -> {
                page.translationX = -position * page.width
                page.visibility = if (absPos < animation.alphaThreshold) View.VISIBLE else View.GONE
                page.alpha = if (inRange) 1f else 0f
                if (inRange) page.rotation =
                    (if (position <= 0f) 36000f else -36000f) * (absPos * absPos * absPos * absPos * absPos * absPos * absPos)
            }

            SliderAnimations.HINGE -> {
                page.translationX = -position * page.width; page.pivotX = 0f; page.pivotY = 0f
                page.alpha = if (inRange) (if (position <= 0f) 1f - absPos else 1f) else 0f
                page.rotation = if (position in -1f..0f) animation.rotationFactor * absPos else 0f
            }

            SliderAnimations.HORIZONTAL_FLIP, SliderAnimations.SPINNER, SliderAnimations.VERTICAL_FLIP, SliderAnimations.VERTICAL_SHUT -> {
                page.translationX = -position * page.width
                page.visibility = if (position in -0.5f..0.5f) View.VISIBLE else View.INVISIBLE
                page.alpha = if (inRange) 1f else 0f
                if (inRange) {
                    val rot =
                        (if (position <= 0f) 1f else -1f) * animation.rotationFactor * (2f - absPos)
                    if (animation.useXAxis) page.rotationX = rot else page.rotationY = rot
                }
            }

            SliderAnimations.POP -> {
                page.translationX = -position * page.width
                page.visibility = if (absPos < animation.alphaThreshold) View.VISIBLE else View.GONE
                if (absPos < 0.5f) {
                    page.scaleX = 1f - absPos; page.scaleY = 1f - absPos
                }
            }

            SliderAnimations.TOSS -> {
                page.translationX = -position * page.width
                page.visibility = if (position in -0.5f..0.5f) View.VISIBLE else View.INVISIBLE
                page.alpha = if (inRange) 1f else 0f
                if (inRange) {
                    page.scaleX = max(animation.minScale, 1f - absPos)
                    page.scaleY = max(animation.minScale, 1f - absPos)
                    page.translationY = -1000f * absPos
                    page.rotationX =
                        (if (position <= 0f) animation.rotationFactor else -animation.rotationFactor) * (2f - absPos)
                }
            }

            SliderAnimations.ZOOM_OUT -> {
                page.alpha = if (inRange) max(animation.minAlpha, 1f - absPos) else 0f
                if (inRange) {
                    page.scaleX = max(animation.minScale, 1f - absPos)
                    page.scaleY = max(animation.minScale, 1f - absPos)
                }
            }

            SliderAnimations.ACCORDION -> {
                page.pivotX = if (position < 0) 0f else page.width.toFloat()
                page.scaleX = if (inRange) 1f - absPos else 0f
            }

            SliderAnimations.BACKGROUND_TO_FOREGROUND -> {
                if (inRange) {
                    val s = max(animation.minScale, 1f - absPos)
                    page.scaleX = s; page.scaleY = s
                    page.translationX = -position * page.width
                    page.alpha = if (absPos < animation.alphaThreshold) 1f else 0f
                }
            }

            SliderAnimations.FOREGROUND_TO_BACKGROUND -> {
                if (inRange) {
                    val s = max(animation.minScale, absPos)
                    page.scaleX = s; page.scaleY = s
                    page.translationX = -position * page.width
                    page.alpha = if (absPos < animation.alphaThreshold) 1f else 0f
                }
            }

            SliderAnimations.ROTATE_DOWN -> {
                if (inRange) {
                    page.pivotX = page.width * 0.5f; page.pivotY = page.height.toFloat()
                    page.rotation = animation.rotationFactor * position
                }
            }

            SliderAnimations.ROTATE_UP -> {
                if (inRange) {
                    page.pivotX = page.width * 0.5f; page.pivotY = 0f
                    page.rotation = -animation.rotationFactor * position
                }
            }

            SliderAnimations.TABLET -> {
                if (inRange) {
                    page.pivotX = if (position < 0) 0f else page.width.toFloat()
                    page.pivotY = page.height * 0.5f
                    page.rotationY = animation.rotationFactor * position
                    page.translationX = -position * page.width
                }
            }

            SliderAnimations.ZOOM_IN -> {
                if (inRange) {
                    val s = if (position < 0) position + 1f else 1f - position
                    page.scaleX = s; page.scaleY = s
                    page.pivotX = page.width * 0.5f; page.pivotY = page.height * 0.5f
                    page.alpha = if (absPos < animation.alphaThreshold) 1f else 0f
                }
            }

            SliderAnimations.PARALLAX -> {
                if (inRange) {
                    page.translationX = -position * (page.width * animation.parallaxFactor)
                }
            }

            else -> {}
        }
    }
}
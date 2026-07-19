package io.selimdawa.autoimageslider

import android.view.View
import androidx.core.view.isVisible
import kotlin.math.abs
import kotlin.math.max

class SmartTransformer(private val animation: SliderAnimations) : SliderPager.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        val absPos = abs(position)
        val inRange = position in -1f..1f
        if (animation.cameraDist > 0f) page.cameraDistance = animation.cameraDist

        if (animation.isCubeIn || animation.isCubeOut) {
            page.alpha = if (inRange) 1f else 0f
            if (inRange) {
                page.pivotX = if (position <= 0f) page.width.toFloat() else 0f
                page.rotationY = (if (position <= 0f) animation.rotationFactor else -animation.rotationFactor) * absPos
            }
            if (animation.hasScaleY) page.scaleY = if (animation.scaleMode == 1) max(0.4f, 1f - absPos) else max(0.4f, absPos)
            return
        }

        when (animation) {
            SliderAnimations.ANTICLOCKSPINTRANSFORMATION, SliderAnimations.CLOCK_SPINTRANSFORMATION -> {
                page.translationX = -position * page.width
                page.visibility = if (absPos < 0.5f) View.VISIBLE else View.GONE
                if (page.isVisible) { page.scaleX = 1f - absPos; page.scaleY = 1f - absPos }
                page.alpha = if (inRange) 1f else 0f
                if (inRange) page.rotation = (if (position <= 0f) 360f else -360f) * (if (animation == SliderAnimations.ANTICLOCKSPINTRANSFORMATION) 1f - absPos else absPos)
            }
            SliderAnimations.DEPTHTRANSFORMATION -> {
                page.alpha = if (inRange) (if (position <= 0f) 1f else 1f - absPos) else 0f
                if (position in -1f..0f) { page.translationX = 0f; page.scaleX = 1f; page.scaleY = 1f }
                else if (position in 0f..1f) { page.translationX = -position * page.width; page.scaleX = 1f - absPos; page.scaleY = 1f - absPos }
            }
            SliderAnimations.FADETRANSFORMATION -> {
                page.translationX = -position * page.width
                page.alpha = if (inRange) (if (position == 0f) 1f else if (position <= 0f) position + 1f else 1f - position) else 0f
            }
            SliderAnimations.FANTRANSFORMATION -> {
                page.translationX = -position * page.width; page.pivotX = 0f; page.pivotY = page.height / 2f; page.alpha = if (inRange) 1f else 0f
                if (inRange) page.rotationY = (if (position <= 0f) -120f else 120f) * absPos
            }
            SliderAnimations.FIDGETSPINTRANSFORMATION -> {
                page.translationX = -position * page.width; page.visibility = if (absPos < 0.5f) View.VISIBLE else View.GONE; page.alpha = if (inRange) 1f else 0f
                if (inRange) page.rotation = (if (position <= 0f) 36000f else -36000f) * (absPos * absPos * absPos * absPos * absPos * absPos * absPos)
            }
            SliderAnimations.HINGETRANSFORMATION -> {
                page.translationX = -position * page.width; page.pivotX = 0f; page.pivotY = 0f
                page.alpha = if (inRange) (if (position <= 0f) 1f - absPos else 1f) else 0f
                page.rotation = if (position in -1f..0f) 90f * absPos else 0f
            }
            SliderAnimations.HORIZONTALFLIPTRANSFORMATION, SliderAnimations.SPINNERTRANSFORMATION, SliderAnimations.VERTICALFLIPTRANSFORMATION, SliderAnimations.VERTICALSHUTTRANSFORMATION -> {
                page.translationX = -position * page.width; page.visibility = if (position in -0.5f..0.5f) View.VISIBLE else View.INVISIBLE; page.alpha = if (inRange) 1f else 0f
                if (inRange) {
                    val rot = (if (position <= 0f) 1f else -1f) * animation.rotationFactor * (2f - absPos)
                    if (animation.useXAxis) page.rotationX = rot else page.rotationY = rot
                }
            }
            SliderAnimations.POPTRANSFORMATION -> {
                page.translationX = -position * page.width; page.visibility = if (absPos < 0.5f) View.VISIBLE else View.GONE
                if (absPos < 0.5f) { page.scaleX = 1f - absPos; page.scaleY = 1f - absPos }
            }
            SliderAnimations.TOSSTRANSFORMATION -> {
                page.translationX = -position * page.width; page.visibility = if (position in -0.5f..0.5f) View.VISIBLE else View.INVISIBLE; page.alpha = if (inRange) 1f else 0f
                if (inRange) {
                    page.scaleX = max(0.4f, 1f - absPos); page.scaleY = max(0.4f, 1f - absPos); page.translationY = -1000f * absPos
                    page.rotationX = (if (position <= 0f) 1080f else -1080f) * (2f - absPos)
                }
            }
            SliderAnimations.ZOOMOUTTRANSFORMATION -> {
                page.alpha = if (inRange) max(0.3f, 1f - absPos) else 0f
                if (inRange) { page.scaleX = max(0.65f, 1f - absPos); page.scaleY = max(0.65f, 1f - absPos) }
            }
            else -> {}
        }
    }
}

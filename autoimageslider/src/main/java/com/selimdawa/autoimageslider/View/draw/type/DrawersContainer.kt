package com.selimdawa.autoimageslider.View.draw.type

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.selimdawa.autoimageslider.View.animation.data.ColorAnimationValue
import com.selimdawa.autoimageslider.View.animation.data.DropAnimationValue
import com.selimdawa.autoimageslider.View.animation.data.FillAnimationValue
import com.selimdawa.autoimageslider.View.animation.data.ScaleAnimationValue
import com.selimdawa.autoimageslider.View.animation.data.SlideAnimationValue
import com.selimdawa.autoimageslider.View.animation.data.SwapAnimationValue
import com.selimdawa.autoimageslider.View.animation.data.ThinWormAnimationValue
import com.selimdawa.autoimageslider.View.animation.data.Value
import com.selimdawa.autoimageslider.View.animation.data.WormAnimationValue
import com.selimdawa.autoimageslider.View.animation.type.IndicatorAnimationType
import com.selimdawa.autoimageslider.View.draw.data.Indicator
import com.selimdawa.autoimageslider.View.draw.data.Orientation

open class BaseDrawer(var paint: Paint?, var indicator: Indicator?)

class UniversalDrawer(paint: Paint, indicator: Indicator) : BaseDrawer(paint, indicator) {
    private val strokePaint = Paint().apply { style = Paint.Style.STROKE; isAntiAlias = true }
    private val rect = RectF()

    fun drawBasic(canvas: Canvas, position: Int, isSelected: Boolean, cx: Int, cy: Int) {
        val ind = indicator ?: return
        val pnt = paint ?: return
        val anim = ind.animationType
        var r = ind.radius.toFloat()
        if (anim == IndicatorAnimationType.SCALE && !isSelected || anim == IndicatorAnimationType.SCALE_DOWN && isSelected) r *= ind.scaleFactor
        val isSel = position == ind.selectedPosition
        val tp = if (anim == IndicatorAnimationType.FILL && !isSel) strokePaint.apply {
            strokeWidth = ind.stroke.toFloat()
        } else pnt
        tp.color = if (isSel) ind.selectedColor else ind.unselectedColor
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), r, tp)
    }

    fun drawWithAnimation(canvas: Canvas, value: Value, position: Int, cx: Int, cy: Int) {
        val ind = indicator ?: return
        val pnt = paint ?: return
        val isInt = ind.isInteractiveAnimation
        val isT =
            (isInt && position == ind.selectingPosition) || (!isInt && position == ind.selectedPosition)
        val isR =
            (isInt && position == ind.selectedPosition) || (!isInt && position == ind.lastSelectedPosition)
        val isH = ind.orientation == Orientation.HORIZONTAL

        when (ind.animationType) {
            IndicatorAnimationType.COLOR -> if (value is ColorAnimationValue) {
                pnt.color = when {
                    isT -> value.color; isR -> value.colorReverse; else -> ind.unselectedColor
                }
                canvas.drawCircle(cx.toFloat(), cy.toFloat(), ind.radius.toFloat(), pnt)
            }

            IndicatorAnimationType.SCALE, IndicatorAnimationType.SCALE_DOWN -> if (value is ScaleAnimationValue) {
                pnt.color =
                    if (isT) value.color else if (isR) value.colorReverse else ind.unselectedColor
                canvas.drawCircle(
                    cx.toFloat(),
                    cy.toFloat(),
                    if (isT) value.radius.toFloat() else if (isR) value.radiusReverse.toFloat() else ind.radius.toFloat(),
                    pnt
                )
            }

            IndicatorAnimationType.DROP -> if (value is DropAnimationValue) {
                pnt.color = ind.unselectedColor; canvas.drawCircle(
                    cx.toFloat(), cy.toFloat(), ind.radius.toFloat(), pnt
                ); pnt.color = ind.selectedColor
                canvas.drawCircle(
                    (if (isH) value.width else value.height).toFloat(),
                    (if (isH) value.height else value.width).toFloat(),
                    value.radius.toFloat(),
                    pnt
                )
            }

            IndicatorAnimationType.SLIDE -> if (value is SlideAnimationValue) {
                pnt.color = ind.unselectedColor; canvas.drawCircle(
                    cx.toFloat(), cy.toFloat(), ind.radius.toFloat(), pnt
                ); pnt.color = ind.selectedColor
                canvas.drawCircle(
                    if (isH) value.coordinate.toFloat() else cx.toFloat(),
                    if (isH) cy.toFloat() else value.coordinate.toFloat(),
                    ind.radius.toFloat(),
                    pnt
                )
            }

            IndicatorAnimationType.FILL -> if (value is FillAnimationValue) {
                strokePaint.color = when {
                    isT -> value.color; isR -> value.colorReverse; else -> ind.unselectedColor
                }
                strokePaint.strokeWidth = ind.stroke.toFloat(); canvas.drawCircle(
                    cx.toFloat(), cy.toFloat(), ind.radius.toFloat(), strokePaint
                )
                strokePaint.strokeWidth = (when {
                    isT -> value.stroke; isR -> value.strokeReverse; else -> ind.stroke
                }).toFloat()
                canvas.drawCircle(
                    cx.toFloat(), cy.toFloat(), when {
                        isT -> value.radius.toFloat(); isR -> value.radiusReverse.toFloat(); else -> ind.radius.toFloat()
                    }, strokePaint
                )
            }

            IndicatorAnimationType.SWAP -> if (value is SwapAnimationValue) {
                val isTargetSwap =
                    (ind.isInteractiveAnimation && position == ind.selectingPosition) || (!ind.isInteractiveAnimation && position == ind.lastSelectedPosition)
                val cord =
                    if (isTargetSwap || position == ind.selectedPosition) value.coordinateReverse else value.coordinate
                pnt.color = if (isTargetSwap) ind.selectedColor else ind.unselectedColor
                canvas.drawCircle(
                    (if (isH) cord else cx).toFloat(),
                    (if (isH) cy else cord).toFloat(),
                    ind.radius.toFloat(),
                    pnt
                )
            }

            IndicatorAnimationType.WORM -> if (value is WormAnimationValue) {
                val r = ind.radius.toFloat()
                rect.set(
                    if (isH) value.rectStart.toFloat() else cx - r,
                    if (isH) cy - r else value.rectStart.toFloat(),
                    if (isH) value.rectEnd.toFloat() else cx + r,
                    if (isH) cy + r else value.rectEnd.toFloat()
                )
                pnt.color = ind.unselectedColor; canvas.drawCircle(
                    cx.toFloat(), cy.toFloat(), r, pnt
                ); pnt.color = ind.selectedColor; canvas.drawRoundRect(rect, r, r, pnt)
            }

            IndicatorAnimationType.THIN_WORM -> if (value is ThinWormAnimationValue) {
                val r = ind.radius.toFloat()
                val h = value.height / 2f
                rect.set(
                    if (isH) value.rectStart.toFloat() else cx - h,
                    if (isH) cy - h else value.rectStart.toFloat(),
                    if (isH) value.rectEnd.toFloat() else cx + h,
                    if (isH) cy + h else value.rectEnd.toFloat()
                )
                pnt.color = ind.unselectedColor; canvas.drawCircle(
                    cx.toFloat(), cy.toFloat(), r, pnt
                ); pnt.color = ind.selectedColor; canvas.drawRoundRect(rect, r, r, pnt)
            }

            else -> drawBasic(canvas, position, isT, cx, cy)
        }
    }
}
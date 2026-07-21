package io.selimdawa.autoimageslider.view.draw

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import io.selimdawa.autoimageslider.view.model.ColorAnimationValue
import io.selimdawa.autoimageslider.view.model.DropAnimationValue
import io.selimdawa.autoimageslider.view.model.FillAnimationValue
import io.selimdawa.autoimageslider.view.model.Indicator
import io.selimdawa.autoimageslider.view.model.IndicatorAnimationType
import io.selimdawa.autoimageslider.view.model.IndicatorShape
import io.selimdawa.autoimageslider.view.model.Orientation
import io.selimdawa.autoimageslider.view.model.ScaleAnimationValue
import io.selimdawa.autoimageslider.view.model.SlideAnimationValue
import io.selimdawa.autoimageslider.view.model.SwapAnimationValue
import io.selimdawa.autoimageslider.view.model.ThinWormAnimationValue
import io.selimdawa.autoimageslider.view.model.Value
import io.selimdawa.autoimageslider.view.model.WormAnimationValue
import io.selimdawa.autoimageslider.view.utils.CoordinatesUtils

class IndicatorDrawer(private val indicator: Indicator) {
    private val paint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
    private val strokePaint = Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE }
    private val rect = RectF()
    private var value: Value? = null
    private var clickListener: ClickListener? = null

    interface ClickListener {
        fun onIndicatorClicked(position: Int)
    }

    fun setClickListener(listener: ClickListener?) {
        clickListener = listener
    }

    fun updateValue(value: Value?) {
        this.value = value
    }

    fun touch(event: MotionEvent?) {
        if (event?.action == MotionEvent.ACTION_UP) {
            CoordinatesUtils.getPosition(indicator, event.x, event.y).takeIf { it >= 0 }
                ?.let { clickListener?.onIndicatorClicked(it) }
        }
    }

    fun draw(canvas: Canvas) {
        for (i in 0 until indicator.count) {
            val cx = CoordinatesUtils.getXCoordinate(indicator, i)
            val cy = CoordinatesUtils.getYCoordinate(indicator, i)
            drawIndicator(canvas, i, cx, cy)
        }
    }

    private fun drawIndicator(canvas: Canvas, position: Int, cx: Int, cy: Int) {
        val isInt = indicator.isInteractiveAnimation
        val sel = indicator.selectedPosition
        val isSelected =
            (!isInt && (position == sel || position == indicator.lastSelectedPosition)) ||
                    (isInt && (position == sel || position == indicator.selectingPosition))

        val animValue = value
        if (isSelected && animValue != null) {
            drawWithAnimation(canvas, animValue, position, cx, cy)
        } else {
            drawBasic(canvas, position, isSelected, cx, cy)
        }
    }

    private fun drawBasic(canvas: Canvas, position: Int, isSelected: Boolean, cx: Int, cy: Int) {
        val anim = indicator.animationType
        var r = indicator.radius.toFloat()
        if (anim == IndicatorAnimationType.SCALE && !isSelected || anim == IndicatorAnimationType.SCALE_DOWN && isSelected) {
            r *= indicator.scaleFactor
        }
        val isSel = position == indicator.selectedPosition
        val p = if (anim == IndicatorAnimationType.FILL && !isSel) {
            strokePaint.apply {
                strokeWidth = indicator.stroke.toFloat(); color = indicator.unselectedColor
            }
        } else {
            paint.apply {
                color = if (isSel) indicator.selectedColor else indicator.unselectedColor
            }
        }
        drawShape(canvas, cx.toFloat(), cy.toFloat(), r, p)
    }

    private fun drawShape(canvas: Canvas, cx: Float, cy: Float, r: Float, p: Paint) {
        when (indicator.indicatorShape) {
            IndicatorShape.SQUARE -> canvas.drawRect(cx - r, cy - r, cx + r, cy + r, p)
            IndicatorShape.DASH -> {
                val isH = indicator.orientation == Orientation.HORIZONTAL
                if (isH) rect.set(cx - r * 2, cy - r / 2, cx + r * 2, cy + r / 2)
                else rect.set(cx - r / 2, cy - r * 2, cx + r / 2, cy + r * 2)
                canvas.drawRoundRect(rect, r, r, p)
            }

            else -> canvas.drawCircle(cx, cy, r, p)
        }
    }

    private fun drawWithAnimation(canvas: Canvas, v: Value, position: Int, cx: Int, cy: Int) {
        val isInt = indicator.isInteractiveAnimation
        val isT =
            (isInt && position == indicator.selectingPosition) || (!isInt && position == indicator.selectedPosition)
        val isR =
            (isInt && position == indicator.selectedPosition) || (!isInt && position == indicator.lastSelectedPosition)
        val isH = indicator.orientation == Orientation.HORIZONTAL
        val r = indicator.radius.toFloat()

        when (indicator.animationType) {
            IndicatorAnimationType.COLOR -> if (v is ColorAnimationValue) {
                paint.color = when {
                    isT -> v.color; isR -> v.colorReverse; else -> indicator.unselectedColor
                }
                drawShape(canvas, cx.toFloat(), cy.toFloat(), r, paint)
            }

            IndicatorAnimationType.SCALE, IndicatorAnimationType.SCALE_DOWN -> if (v is ScaleAnimationValue) {
                paint.color =
                    if (isT) v.color else if (isR) v.colorReverse else indicator.unselectedColor
                val rad = if (isT) v.radius.toFloat() else if (isR) v.radiusReverse.toFloat() else r
                drawShape(canvas, cx.toFloat(), cy.toFloat(), rad, paint)
            }

            IndicatorAnimationType.DROP -> if (v is DropAnimationValue) {
                paint.color = indicator.unselectedColor; drawShape(
                    canvas,
                    cx.toFloat(),
                    cy.toFloat(),
                    r,
                    paint
                )
                paint.color = indicator.selectedColor
                val dx = (if (isH) v.width else v.height).toFloat()
                val dy = (if (isH) v.height else v.width).toFloat()
                drawShape(canvas, dx, dy, v.radius.toFloat(), paint)
            }

            IndicatorAnimationType.SLIDE -> if (v is SlideAnimationValue) {
                paint.color = indicator.unselectedColor; drawShape(
                    canvas,
                    cx.toFloat(),
                    cy.toFloat(),
                    r,
                    paint
                )
                paint.color = indicator.selectedColor
                val dx = if (isH) v.coordinate.toFloat() else cx.toFloat()
                val dy = if (isH) cy.toFloat() else v.coordinate.toFloat()
                drawShape(canvas, dx, dy, r, paint)
            }

            IndicatorAnimationType.FILL -> if (v is FillAnimationValue) {
                strokePaint.color = when {
                    isT -> v.color; isR -> v.colorReverse; else -> indicator.unselectedColor
                }
                strokePaint.strokeWidth = indicator.stroke.toFloat()
                drawShape(canvas, cx.toFloat(), cy.toFloat(), r, strokePaint)
                strokePaint.strokeWidth =
                    (if (isT) v.stroke else if (isR) v.strokeReverse else indicator.stroke).toFloat()
                val rad = if (isT) v.radius.toFloat() else if (isR) v.radiusReverse.toFloat() else r
                drawShape(canvas, cx.toFloat(), cy.toFloat(), rad, strokePaint)
            }

            IndicatorAnimationType.SWAP -> if (v is SwapAnimationValue) {
                val isTarget =
                    (isInt && position == indicator.selectingPosition) || (!isInt && position == indicator.lastSelectedPosition)
                val cord =
                    if (isTarget || position == indicator.selectedPosition) v.coordinateReverse else v.coordinate
                paint.color = if (isTarget) indicator.selectedColor else indicator.unselectedColor
                val dx = (if (isH) cord else cx).toFloat()
                val dy = (if (isH) cy else cord).toFloat()
                drawShape(canvas, dx, dy, r, paint)
            }

            IndicatorAnimationType.WORM -> if (v is WormAnimationValue) {
                rect.set(
                    if (isH) v.rectStart.toFloat() else cx - r,
                    if (isH) cy - r else v.rectStart.toFloat(),
                    if (isH) v.rectEnd.toFloat() else cx + r,
                    if (isH) cy + r else v.rectEnd.toFloat()
                )
                paint.color = indicator.unselectedColor; drawShape(
                    canvas,
                    cx.toFloat(),
                    cy.toFloat(),
                    r,
                    paint
                )
                paint.color = indicator.selectedColor; canvas.drawRoundRect(rect, r, r, paint)
            }

            IndicatorAnimationType.THIN_WORM -> if (v is ThinWormAnimationValue) {
                val h = v.height / 2f
                rect.set(
                    if (isH) v.rectStart.toFloat() else cx - h,
                    if (isH) cy - h else v.rectStart.toFloat(),
                    if (isH) v.rectEnd.toFloat() else cx + h,
                    if (isH) cy + h else v.rectEnd.toFloat()
                )
                paint.color = indicator.unselectedColor; drawShape(
                    canvas,
                    cx.toFloat(),
                    cy.toFloat(),
                    r,
                    paint
                )
                paint.color = indicator.selectedColor; canvas.drawRoundRect(rect, r, r, paint)
            }

            else -> drawBasic(canvas, position, isT, cx, cy)
        }
    }
}
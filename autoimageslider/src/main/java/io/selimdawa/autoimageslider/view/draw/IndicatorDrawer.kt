package io.selimdawa.autoimageslider.view.draw

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import io.selimdawa.autoimageslider.view.model.ColorAnimationValue
import io.selimdawa.autoimageslider.view.model.DropAnimationValue
import io.selimdawa.autoimageslider.view.model.FillAnimationValue
import io.selimdawa.autoimageslider.view.model.JumpAnimationValue
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
        val count = indicator.count
        // Pass 1: Draw all dots as basic (unselected)
        for (i in 0 until count) {
            val cx = CoordinatesUtils.getXCoordinate(indicator, i)
            val cy = CoordinatesUtils.getYCoordinate(indicator, i)
            drawBasic(canvas, false, cx, cy)
        }

        // Pass 2: Draw the active animation or selection on top
        val animValue = value
        if (animValue != null) {
            drawAnimation(canvas, animValue)
        } else {
            // Draw static selection if no animation is running
            val sel = indicator.selectedPosition
            if (sel in 0 until count) {
                val cx = CoordinatesUtils.getXCoordinate(indicator, sel)
                val cy = CoordinatesUtils.getYCoordinate(indicator, sel)
                drawBasic(canvas, true, cx, cy)
            }
        }
    }

    private fun drawBasic(canvas: Canvas, isSelected: Boolean, cx: Int, cy: Int) {
        val anim = indicator.animationType
        var r = indicator.radius.toFloat()
        
        // Scale animations affect the radius even in static state for the selected dot
        if (anim == IndicatorAnimationType.SCALE && !isSelected || anim == IndicatorAnimationType.SCALE_DOWN && isSelected) {
            r *= indicator.scaleFactor
        }
        
        val isSel = isSelected // Use the passed parameter instead of indicator.selectedPosition for better control
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

    private fun drawAnimation(canvas: Canvas, v: Value) {
        val animType = indicator.animationType
        val isInt = indicator.isInteractiveAnimation
        val selPos = indicator.selectedPosition
        val targetPos = if (isInt) indicator.selectingPosition else selPos
        val lastPos = if (isInt) selPos else indicator.lastSelectedPosition

        when (animType) {
            IndicatorAnimationType.COLOR -> {
                drawAnimDot(canvas, lastPos, v)
                drawAnimDot(canvas, targetPos, v)
            }
            IndicatorAnimationType.SCALE, IndicatorAnimationType.SCALE_DOWN -> {
                drawAnimDot(canvas, lastPos, v)
                drawAnimDot(canvas, targetPos, v)
            }
            IndicatorAnimationType.FILL -> {
                drawAnimDot(canvas, lastPos, v)
                drawAnimDot(canvas, targetPos, v)
            }
            // Moving animations are drawn once
            IndicatorAnimationType.WORM -> drawWorm(canvas, v as WormAnimationValue)
            IndicatorAnimationType.THIN_WORM -> drawThinWorm(canvas, v as ThinWormAnimationValue)
            IndicatorAnimationType.SLIDE -> drawSlide(canvas, v as SlideAnimationValue)
            IndicatorAnimationType.DROP -> drawDrop(canvas, v as DropAnimationValue)
            IndicatorAnimationType.SWAP -> {
                drawSwap(canvas, lastPos, v as SwapAnimationValue)
                drawSwap(canvas, targetPos, v)
            }
            IndicatorAnimationType.JUMP -> drawJump(canvas, v as JumpAnimationValue)
            else -> { /* Handled by Pass 1 and static check */ }
        }
    }

    private fun drawAnimDot(canvas: Canvas, position: Int, v: Value) {
        if (position !in 0 until indicator.count) return
        val cx = CoordinatesUtils.getXCoordinate(indicator, position).toFloat()
        val cy = CoordinatesUtils.getYCoordinate(indicator, position).toFloat()
        val r = indicator.radius.toFloat()
        val isInt = indicator.isInteractiveAnimation
        val isT = (isInt && position == indicator.selectingPosition) || (!isInt && position == indicator.selectedPosition)
        val isR = (isInt && position == indicator.selectedPosition) || (!isInt && position == indicator.lastSelectedPosition)

        when (indicator.animationType) {
            IndicatorAnimationType.COLOR -> if (v is ColorAnimationValue) {
                paint.color = when {
                    isT -> v.color; isR -> v.colorReverse; else -> indicator.unselectedColor
                }
                drawShape(canvas, cx, cy, r, paint)
            }
            IndicatorAnimationType.SCALE, IndicatorAnimationType.SCALE_DOWN -> if (v is ScaleAnimationValue) {
                paint.color = if (isT) v.color else if (isR) v.colorReverse else indicator.unselectedColor
                val rad = if (isT) v.radius.toFloat() else if (isR) v.radiusReverse.toFloat() else r
                drawShape(canvas, cx, cy, rad, paint)
            }
            IndicatorAnimationType.FILL -> if (v is FillAnimationValue) {
                strokePaint.color = when {
                    isT -> v.color; isR -> v.colorReverse; else -> indicator.unselectedColor
                }
                strokePaint.strokeWidth = indicator.stroke.toFloat()
                drawShape(canvas, cx, cy, r, strokePaint)
                strokePaint.strokeWidth = (if (isT) v.stroke else if (isR) v.strokeReverse else indicator.stroke).toFloat()
                val rad = if (isT) v.radius.toFloat() else if (isR) v.radiusReverse.toFloat() else r
                drawShape(canvas, cx, cy, rad, strokePaint)
            }
            else -> {}
        }
    }

    private fun drawWorm(canvas: Canvas, v: WormAnimationValue) {
        val r = indicator.radius.toFloat()
        val cy = CoordinatesUtils.getYCoordinate(indicator, 0).toFloat()
        val cx = CoordinatesUtils.getXCoordinate(indicator, 0).toFloat()
        val isH = indicator.orientation == Orientation.HORIZONTAL
        
        rect.set(
            if (isH) v.rectStart.toFloat() else cx - r,
            if (isH) cy - r else v.rectStart.toFloat(),
            if (isH) v.rectEnd.toFloat() else cx + r,
            if (isH) cy + r else v.rectEnd.toFloat()
        )
        paint.color = indicator.selectedColor
        canvas.drawRoundRect(rect, r, r, paint)
    }

    private fun drawThinWorm(canvas: Canvas, v: ThinWormAnimationValue) {
        val r = indicator.radius.toFloat()
        val cy = CoordinatesUtils.getYCoordinate(indicator, 0).toFloat()
        val cx = CoordinatesUtils.getXCoordinate(indicator, 0).toFloat()
        val isH = indicator.orientation == Orientation.HORIZONTAL
        val h = v.height / 2f
        
        rect.set(
            if (isH) v.rectStart.toFloat() else cx - h,
            if (isH) cy - h else v.rectStart.toFloat(),
            if (isH) v.rectEnd.toFloat() else cx + h,
            if (isH) cy + h else v.rectEnd.toFloat()
        )
        paint.color = indicator.selectedColor
        canvas.drawRoundRect(rect, h, h, paint)
    }

    private fun drawSlide(canvas: Canvas, v: SlideAnimationValue) {
        val cx = CoordinatesUtils.getXCoordinate(indicator, 0).toFloat()
        val cy = CoordinatesUtils.getYCoordinate(indicator, 0).toFloat()
        val isH = indicator.orientation == Orientation.HORIZONTAL
        
        paint.color = indicator.selectedColor
        val dx = if (isH) v.coordinate.toFloat() else cx
        val dy = if (isH) cy else v.coordinate.toFloat()
        drawShape(canvas, dx, dy, indicator.radius.toFloat(), paint)
    }

    private fun drawDrop(canvas: Canvas, v: DropAnimationValue) {
        val isH = indicator.orientation == Orientation.HORIZONTAL
        paint.color = indicator.selectedColor
        val dx = (if (isH) v.width else v.height).toFloat()
        val dy = (if (isH) v.height else v.width).toFloat()
        drawShape(canvas, dx, dy, v.radius.toFloat(), paint)
    }

    private fun drawSwap(canvas: Canvas, position: Int, v: SwapAnimationValue) {
        if (position !in 0 until indicator.count) return
        val cx = CoordinatesUtils.getXCoordinate(indicator, position).toFloat()
        val cy = CoordinatesUtils.getYCoordinate(indicator, position).toFloat()
        val r = indicator.radius.toFloat()
        val isH = indicator.orientation == Orientation.HORIZONTAL
        val isInt = indicator.isInteractiveAnimation
        
        val isTarget = (isInt && position == indicator.selectingPosition) || (!isInt && position == indicator.lastSelectedPosition)
        val cord = if (isTarget || position == indicator.selectedPosition) v.coordinateReverse else v.coordinate
        
        paint.color = if (isTarget) indicator.selectedColor else indicator.unselectedColor
        val dx = (if (isH) cord else cx.toInt()).toFloat()
        val dy = (if (isH) cy.toInt() else cord).toFloat()
        drawShape(canvas, dx, dy, r, paint)
    }

    private fun drawJump(canvas: Canvas, v: JumpAnimationValue) {
        val cx = CoordinatesUtils.getXCoordinate(indicator, 0).toFloat()
        val cy = CoordinatesUtils.getYCoordinate(indicator, 0).toFloat()
        val isH = indicator.orientation == Orientation.HORIZONTAL
        
        paint.color = indicator.selectedColor
        val dx = if (isH) v.coordinate.toFloat() else cx
        val dy = if (isH) cy - v.radius else v.coordinate.toFloat()
        drawShape(canvas, dx, dy, indicator.radius.toFloat(), paint)
    }
}

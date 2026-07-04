package com.selimdawa.autoimageslider.IndicatorView.draw.drawer.type

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.selimdawa.autoimageslider.IndicatorView.animation.data.Value
import com.selimdawa.autoimageslider.IndicatorView.animation.data.type.WormAnimationValue
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Indicator
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Orientation

open class WormDrawer(paint: Paint, indicator: Indicator) : BaseDrawer(paint, indicator) {
    var rect: RectF = RectF()

    open fun draw(
        canvas: Canvas,
        value: Value,
        coordinateX: Int,
        coordinateY: Int
    ) {
        if (value !is WormAnimationValue) {
            return
        }

        val currentIndicator = indicator ?: return
        val currentPaint = paint ?: return

        val rectStart = value.rectStart
        val rectEnd = value.rectEnd

        val radius = currentIndicator.radius
        val unselectedColor = currentIndicator.unselectedColor
        val selectedColor = currentIndicator.selectedColor

        if (currentIndicator.orientation == Orientation.HORIZONTAL) {
            rect.left = rectStart.toFloat()
            rect.right = rectEnd.toFloat()
            rect.top = (coordinateY - radius).toFloat()
            rect.bottom = (coordinateY + radius).toFloat()
        } else {
            rect.left = (coordinateX - radius).toFloat()
            rect.right = (coordinateX + radius).toFloat()
            rect.top = rectStart.toFloat()
            rect.bottom = rectEnd.toFloat()
        }

        currentPaint.color = unselectedColor
        canvas.drawCircle(
            coordinateX.toFloat(),
            coordinateY.toFloat(),
            radius.toFloat(),
            currentPaint
        )

        currentPaint.color = selectedColor
        canvas.drawRoundRect(rect, radius.toFloat(), radius.toFloat(), currentPaint)
    }
}
package com.selimdawa.autoimageslider.IndicatorView.draw.drawer.type

import android.graphics.Canvas
import android.graphics.Paint
import com.selimdawa.autoimageslider.IndicatorView.animation.data.Value
import com.selimdawa.autoimageslider.IndicatorView.animation.data.type.DropAnimationValue
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Indicator
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Orientation

class DropDrawer(paint: Paint, indicator: Indicator) : BaseDrawer(paint, indicator) {
    fun draw(
        canvas: Canvas,
        value: Value,
        coordinateX: Int,
        coordinateY: Int
    ) {
        if (value !is DropAnimationValue) {
            return
        }

        val currentIndicator = indicator ?: return
        val currentPaint = paint ?: return

        val unselectedColor = currentIndicator.unselectedColor
        val selectedColor = currentIndicator.selectedColor
        val radius = currentIndicator.radius.toFloat()

        currentPaint.color = unselectedColor
        canvas.drawCircle(coordinateX.toFloat(), coordinateY.toFloat(), radius, currentPaint)

        currentPaint.color = selectedColor
        if (currentIndicator.orientation == Orientation.HORIZONTAL) {
            canvas.drawCircle(
                value.width.toFloat(),
                value.height.toFloat(),
                value.radius.toFloat(),
                currentPaint
            )
        } else {
            canvas.drawCircle(
                value.height.toFloat(),
                value.width.toFloat(),
                value.radius.toFloat(),
                currentPaint
            )
        }
    }
}
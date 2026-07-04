package com.selimdawa.autoimageslider.IndicatorView.draw.drawer.type

import android.graphics.Canvas
import android.graphics.Paint
import com.selimdawa.autoimageslider.IndicatorView.animation.data.Value
import com.selimdawa.autoimageslider.IndicatorView.animation.data.type.SlideAnimationValue
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Indicator
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Orientation

class SlideDrawer(paint: Paint, indicator: Indicator) : BaseDrawer(paint, indicator) {
    fun draw(
        canvas: Canvas,
        value: Value,
        coordinateX: Int,
        coordinateY: Int
    ) {
        if (value !is SlideAnimationValue) {
            return
        }

        val currentIndicator = indicator ?: return
        val currentPaint = paint ?: return

        val coordinate = value.coordinate
        val unselectedColor = currentIndicator.unselectedColor
        val selectedColor = currentIndicator.selectedColor
        val radius = currentIndicator.radius

        currentPaint.color = unselectedColor
        canvas.drawCircle(coordinateX.toFloat(), coordinateY.toFloat(), radius.toFloat(), currentPaint)

        currentPaint.color = selectedColor
        if (currentIndicator.orientation == Orientation.HORIZONTAL) {
            canvas.drawCircle(coordinate.toFloat(), coordinateY.toFloat(), radius.toFloat(), currentPaint)
        } else {
            canvas.drawCircle(coordinateX.toFloat(), coordinate.toFloat(), radius.toFloat(), currentPaint)
        }
    }
}
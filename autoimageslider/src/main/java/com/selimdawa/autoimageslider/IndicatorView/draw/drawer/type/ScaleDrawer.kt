package com.selimdawa.autoimageslider.IndicatorView.draw.drawer.type

import android.graphics.Canvas
import android.graphics.Paint
import com.selimdawa.autoimageslider.IndicatorView.animation.data.ScaleAnimationValue
import com.selimdawa.autoimageslider.IndicatorView.animation.data.Value
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Indicator

class ScaleDrawer(paint: Paint, indicator: Indicator) : BaseDrawer(paint, indicator) {
    fun draw(
        canvas: Canvas,
        value: Value,
        position: Int,
        coordinateX: Int,
        coordinateY: Int
    ) {
        if (value !is ScaleAnimationValue) {
            return
        }

        val currentIndicator = indicator ?: return
        val currentPaint = paint ?: return

        var radius = currentIndicator.radius.toFloat()
        var color = currentIndicator.selectedColor

        val selectedPosition = currentIndicator.selectedPosition
        val selectingPosition = currentIndicator.selectingPosition
        val lastSelectedPosition = currentIndicator.lastSelectedPosition

        if (currentIndicator.isInteractiveAnimation) {
            if (position == selectingPosition) {
                radius = value.radius.toFloat()
                color = value.color
            } else if (position == selectedPosition) {
                radius = value.radiusReverse.toFloat()
                color = value.colorReverse
            }
        } else {
            if (position == selectedPosition) {
                radius = value.radius.toFloat()
                color = value.color
            } else if (position == lastSelectedPosition) {
                radius = value.radiusReverse.toFloat()
                color = value.colorReverse
            }
        }

        currentPaint.color = color
        canvas.drawCircle(coordinateX.toFloat(), coordinateY.toFloat(), radius, currentPaint)
    }
}
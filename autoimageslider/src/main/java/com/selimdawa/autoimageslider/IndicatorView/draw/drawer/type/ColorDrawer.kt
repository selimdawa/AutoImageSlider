package com.selimdawa.autoimageslider.IndicatorView.draw.drawer.type

import android.graphics.Canvas
import android.graphics.Paint
import com.selimdawa.autoimageslider.IndicatorView.animation.data.Value
import com.selimdawa.autoimageslider.IndicatorView.animation.data.type.ColorAnimationValue
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Indicator

class ColorDrawer(paint: Paint, indicator: Indicator) : BaseDrawer(paint, indicator) {
    fun draw(
        canvas: Canvas,
        value: Value,
        position: Int,
        coordinateX: Int,
        coordinateY: Int
    ) {
        if (value !is ColorAnimationValue) {
            return
        }

        val currentIndicator = indicator ?: return
        val currentPaint = paint ?: return

        val radius = currentIndicator.radius.toFloat()
        var color = currentIndicator.selectedColor

        val selectedPosition = currentIndicator.selectedPosition
        val selectingPosition = currentIndicator.selectingPosition
        val lastSelectedPosition = currentIndicator.lastSelectedPosition

        if (currentIndicator.isInteractiveAnimation) {
            if (position == selectingPosition) {
                color = value.color
            } else if (position == selectedPosition) {
                color = value.colorReverse
            }
        } else {
            if (position == selectedPosition) {
                color = value.color
            } else if (position == lastSelectedPosition) {
                color = value.colorReverse
            }
        }

        currentPaint.color = color
        canvas.drawCircle(coordinateX.toFloat(), coordinateY.toFloat(), radius, currentPaint)
    }
}
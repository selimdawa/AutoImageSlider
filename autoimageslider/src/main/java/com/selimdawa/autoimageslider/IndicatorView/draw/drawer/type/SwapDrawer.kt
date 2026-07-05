package com.selimdawa.autoimageslider.IndicatorView.draw.drawer.type

import android.graphics.Canvas
import android.graphics.Paint
import com.selimdawa.autoimageslider.IndicatorView.animation.data.SwapAnimationValue
import com.selimdawa.autoimageslider.IndicatorView.animation.data.Value
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Indicator
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Orientation

class SwapDrawer(paint: Paint, indicator: Indicator) : BaseDrawer(paint, indicator) {
    fun draw(
        canvas: Canvas,
        value: Value,
        position: Int,
        coordinateX: Int,
        coordinateY: Int
    ) {
        if (value !is SwapAnimationValue) {
            return
        }

        val currentIndicator = indicator ?: return
        val currentPaint = paint ?: return

        val selectedColor = currentIndicator.selectedColor
        val unselectedColor = currentIndicator.unselectedColor
        val radius = currentIndicator.radius

        val selectedPosition = currentIndicator.selectedPosition
        val selectingPosition = currentIndicator.selectingPosition
        val lastSelectedPosition = currentIndicator.lastSelectedPosition

        var coordinate = value.coordinate
        var color = unselectedColor

        if (currentIndicator.isInteractiveAnimation) {
            if (position == selectingPosition) {
                coordinate = value.coordinate
                color = selectedColor
            } else if (position == selectedPosition) {
                coordinate = value.coordinateReverse
                color = unselectedColor
            }
        } else {
            if (position == lastSelectedPosition) {
                coordinate = value.coordinate
                color = selectedColor
            } else if (position == selectedPosition) {
                coordinate = value.coordinateReverse
                color = unselectedColor
            }
        }

        currentPaint.color = color
        if (currentIndicator.orientation == Orientation.HORIZONTAL) {
            canvas.drawCircle(
                coordinate.toFloat(),
                coordinateY.toFloat(),
                radius.toFloat(),
                currentPaint
            )
        } else {
            canvas.drawCircle(
                coordinateX.toFloat(),
                coordinate.toFloat(),
                radius.toFloat(),
                currentPaint
            )
        }
    }
}
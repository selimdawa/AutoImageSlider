package com.selimdawa.autoimageslider.IndicatorView.draw.drawer.type

import android.graphics.Canvas
import android.graphics.Paint
import com.selimdawa.autoimageslider.IndicatorView.animation.data.Value
import com.selimdawa.autoimageslider.IndicatorView.animation.data.type.FillAnimationValue
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Indicator

class FillDrawer(paint: Paint, indicator: Indicator) : BaseDrawer(paint, indicator) {
    private val strokePaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    fun draw(
        canvas: Canvas,
        value: Value,
        position: Int,
        coordinateX: Int,
        coordinateY: Int
    ) {
        if (value !is FillAnimationValue) {
            return
        }

        val currentIndicator = indicator ?: return

        var color = currentIndicator.unselectedColor
        var radius = currentIndicator.radius.toFloat()
        var stroke = currentIndicator.stroke

        val selectedPosition = currentIndicator.selectedPosition
        val selectingPosition = currentIndicator.selectingPosition
        val lastSelectedPosition = currentIndicator.lastSelectedPosition

        if (currentIndicator.isInteractiveAnimation) {
            if (position == selectingPosition) {
                color = value.color
                radius = value.radius.toFloat()
                stroke = value.stroke
            } else if (position == selectedPosition) {
                color = value.colorReverse
                radius = value.radiusReverse.toFloat()
                stroke = value.strokeReverse
            }
        } else {
            if (position == selectedPosition) {
                color = value.color
                radius = value.radius.toFloat()
                stroke = value.stroke
            } else if (position == lastSelectedPosition) {
                color = value.colorReverse
                radius = value.radiusReverse.toFloat()
                stroke = value.strokeReverse
            }
        }

        strokePaint.color = color
        strokePaint.strokeWidth = currentIndicator.stroke.toFloat()
        canvas.drawCircle(
            coordinateX.toFloat(),
            coordinateY.toFloat(),
            currentIndicator.radius.toFloat(),
            strokePaint
        )

        strokePaint.strokeWidth = stroke.toFloat()
        canvas.drawCircle(coordinateX.toFloat(), coordinateY.toFloat(), radius, strokePaint)
    }
}
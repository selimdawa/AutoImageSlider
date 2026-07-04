package com.selimdawa.autoimageslider.IndicatorView.draw.drawer.type

import android.graphics.Canvas
import android.graphics.Paint
import com.selimdawa.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Indicator

class BasicDrawer(paint: Paint, indicator: Indicator) : BaseDrawer(paint, indicator) {
    private val strokePaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeWidth = indicator.stroke.toFloat()
    }

    fun draw(
        canvas: Canvas,
        position: Int,
        isSelectedItem: Boolean,
        coordinateX: Int,
        coordinateY: Int
    ) {
        val currentIndicator = indicator ?: return
        val currentPaint = paint ?: return

        var radius = currentIndicator.radius.toFloat()
        val strokePx = currentIndicator.stroke
        val scaleFactor = currentIndicator.scaleFactor

        val selectedColor = currentIndicator.selectedColor
        val unselectedColor = currentIndicator.unselectedColor
        val selectedPosition = currentIndicator.selectedPosition
        val animationType = currentIndicator.animationType

        if (animationType == IndicatorAnimationType.SCALE && !isSelectedItem) {
            radius *= scaleFactor
        } else if (animationType == IndicatorAnimationType.SCALE_DOWN && isSelectedItem) {
            radius *= scaleFactor
        }

        val color = if (position == selectedPosition) selectedColor else unselectedColor

        val targetPaint = if (animationType == IndicatorAnimationType.FILL && position != selectedPosition) {
            strokePaint.apply { strokeWidth = strokePx.toFloat() }
        } else {
            currentPaint
        }

        targetPaint.color = color
        canvas.drawCircle(coordinateX.toFloat(), coordinateY.toFloat(), radius, targetPaint)
    }
}
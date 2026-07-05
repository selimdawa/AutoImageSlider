package com.selimdawa.autoimageslider.IndicatorView.draw.drawer.type

import android.graphics.Canvas
import android.graphics.Paint
import com.selimdawa.autoimageslider.IndicatorView.animation.data.ThinWormAnimationValue
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Indicator
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Orientation
import com.selimdawa.autoimageslider.IndicatorView.animation.data.Value

class ThinWormDrawer(paint: Paint, indicator: Indicator) : WormDrawer(paint, indicator) {
    override fun draw(
        canvas: Canvas,
        value: Value,
        coordinateX: Int,
        coordinateY: Int
    ) {
        if (value !is ThinWormAnimationValue) {
            return
        }

        val currentIndicator = indicator ?: return
        val currentPaint = paint ?: return

        val rectStart = value.rectStart
        val rectEnd = value.rectEnd
        val height = value.height / 2

        val radius = currentIndicator.radius
        val unselectedColor = currentIndicator.unselectedColor
        val selectedColor = currentIndicator.selectedColor

        if (currentIndicator.orientation == Orientation.HORIZONTAL) {
            rect.left = rectStart.toFloat()
            rect.right = rectEnd.toFloat()
            rect.top = (coordinateY - height).toFloat()
            rect.bottom = (coordinateY + height).toFloat()
        } else {
            rect.left = (coordinateX - height).toFloat()
            rect.right = (coordinateX + height).toFloat()
            rect.top = rectStart.toFloat()
            rect.bottom = rectEnd.toFloat()
        }

        currentPaint.color = unselectedColor
        canvas.drawCircle(coordinateX.toFloat(), coordinateY.toFloat(), radius.toFloat(), currentPaint)

        currentPaint.color = selectedColor
        canvas.drawRoundRect(rect, radius.toFloat(), radius.toFloat(), currentPaint)
    }
}
package com.selimdawa.autoimageslider.IndicatorView.utils

import android.util.Pair
import com.selimdawa.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Indicator
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Orientation

object CoordinatesUtils {
    fun getCoordinate(indicator: Indicator, position: Int) =
        if (indicator.orientation == Orientation.HORIZONTAL) getXCoordinate(
            indicator, position
        ) else getYCoordinate(indicator, position)

    fun getXCoordinate(indicator: Indicator, position: Int) =
        (if (indicator.orientation == Orientation.HORIZONTAL) getHorizontalCoordinate(
            indicator, position
        ) else getVerticalCoordinate(indicator)) + indicator.paddingLeft

    fun getYCoordinate(indicator: Indicator, position: Int) =
        (if (indicator.orientation == Orientation.HORIZONTAL) getVerticalCoordinate(indicator) else getHorizontalCoordinate(
            indicator, position
        )) + indicator.paddingTop

    fun getPosition(indicator: Indicator, x: Float, y: Float) =
        if (indicator.orientation == Orientation.HORIZONTAL) getFitPosition(
            indicator, x, y
        ) else getFitPosition(indicator, y, x)

    private fun getFitPosition(indicator: Indicator, lenCoord: Float, heightCoord: Float): Int {
        val radius = indicator.radius;
        val stroke = indicator.stroke;
        val padding = indicator.padding
        val height =
            if (indicator.orientation == Orientation.HORIZONTAL) indicator.height else indicator.width
        var length = 0
        for (i in 0..<indicator.count) {
            val start = length
            length += radius * 2 + (stroke / 2) + (if (i > 0) padding else padding / 2)
            if (lenCoord in start.toFloat()..length.toFloat() && heightCoord in 0f..height.toFloat()) return i
        }
        return -1
    }

    private fun getHorizontalCoordinate(indicator: Indicator, position: Int): Int {
        val radius = indicator.radius;
        val stroke = indicator.stroke
        var coordinate = 0
        for (i in 0..<indicator.count) {
            coordinate += radius + (stroke / 2)
            if (position == i) return coordinate
            coordinate += radius + indicator.padding + (stroke / 2)
        }
        return if (indicator.animationType == IndicatorAnimationType.DROP) coordinate + (radius * 2) else coordinate
    }

    private fun getVerticalCoordinate(indicator: Indicator) =
        if (indicator.animationType == IndicatorAnimationType.DROP) indicator.radius * 3 else indicator.radius

    fun getProgress(
        indicator: Indicator, position: Int, positionOffset: Float, isRtl: Boolean
    ): Pair<Int?, Float?> {
        val count = indicator.count
        val targetPos = (if (isRtl) (count - 1) - position else position).coerceIn(
            0, (count - 1).coerceAtLeast(0)
        )
        var selectedPos = indicator.selectedPosition

        if (targetPos > selectedPos || (isRtl && targetPos - 1 < selectedPos) || (!isRtl && targetPos + 1 < selectedPos)) {
            selectedPos = targetPos; indicator.selectedPosition = selectedPos
        }

        val slideToRight = selectedPos == targetPos && positionOffset != 0f
        val selectPos =
            if (slideToRight) (if (isRtl) targetPos - 1 else targetPos + 1) else targetPos
        val selectProgress =
            (if (slideToRight) positionOffset else 1 - positionOffset).coerceIn(0f, 1f)

        return Pair(selectPos, selectProgress)
    }
}
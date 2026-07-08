package io.selimdawa.autoimageslider.View.utils

import android.util.Pair
import com.selimdawa.autoimageslider.View.animation.type.IndicatorAnimationType
import com.selimdawa.autoimageslider.View.draw.data.Indicator
import com.selimdawa.autoimageslider.View.draw.data.Orientation

object CoordinatesUtils {
    fun getCoordinate(ind: Indicator, pos: Int) =
        if (ind.orientation == Orientation.HORIZONTAL) getXCoordinate(ind, pos) else getYCoordinate(
            ind, pos
        )

    fun getXCoordinate(ind: Indicator, pos: Int) =
        (if (ind.orientation == Orientation.HORIZONTAL) getHorizontalCoordinate(
            ind, pos
        ) else getVerticalCoordinate(ind)) + ind.paddingLeft

    fun getYCoordinate(ind: Indicator, pos: Int) =
        (if (ind.orientation == Orientation.HORIZONTAL) getVerticalCoordinate(ind) else getHorizontalCoordinate(
            ind, pos
        )) + ind.paddingTop

    fun getPosition(ind: Indicator, x: Float, y: Float) =
        if (ind.orientation == Orientation.HORIZONTAL) getFitPosition(
            ind, x, y
        ) else getFitPosition(ind, y, x)

    private fun getFitPosition(ind: Indicator, len: Float, hgt: Float): Int {
        val h = if (ind.orientation == Orientation.HORIZONTAL) ind.height else ind.width
        var l = 0
        for (i in 0..<ind.count) {
            val start = l
            l += ind.radius * 2 + (ind.stroke / 2) + if (i > 0) ind.padding else ind.padding / 2
            if (len in start.toFloat()..l.toFloat() && hgt in 0f..h.toFloat()) return i
        }
        return -1
    }

    private fun getHorizontalCoordinate(ind: Indicator, pos: Int): Int {
        var cord = 0
        for (i in 0..<ind.count) {
            cord += ind.radius + (ind.stroke / 2)
            if (pos == i) return cord
            cord += ind.radius + ind.padding + (ind.stroke / 2)
        }
        return if (ind.animationType == IndicatorAnimationType.DROP) cord + (ind.radius * 2) else cord
    }

    private fun getVerticalCoordinate(ind: Indicator) =
        if (ind.animationType == IndicatorAnimationType.DROP) ind.radius * 3 else ind.radius

    fun getProgress(ind: Indicator, pos: Int, offset: Float, isRtl: Boolean): Pair<Int?, Float?> {
        val count = ind.count
        var selPos = ind.selectedPosition
        val target =
            (if (isRtl) (count - 1) - pos else pos).coerceIn(0, (count - 1).coerceAtLeast(0))
        if (target > selPos || (isRtl && target - 1 < selPos) || (!isRtl && target + 1 < selPos)) {
            selPos = target; ind.selectedPosition = selPos
        }
        val slideR = selPos == target && offset != 0f
        return Pair(
            if (slideR) (if (isRtl) target - 1 else target + 1) else target,
            (if (slideR) offset else 1 - offset).coerceIn(0f, 1f)
        )
    }
}
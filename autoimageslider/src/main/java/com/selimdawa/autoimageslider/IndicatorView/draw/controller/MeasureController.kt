package com.selimdawa.autoimageslider.IndicatorView.draw.controller

import android.view.View.MeasureSpec
import com.selimdawa.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Indicator
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Orientation
import kotlin.math.min

class MeasureController {
    fun measureViewSize(
        indicator: Indicator, widthMeasureSpec: Int, heightMeasureSpec: Int
    ): Pair<Int, Int> {
        val count = indicator.count
        val radius = indicator.radius
        val stroke = indicator.stroke
        val isHorizontal = indicator.orientation == Orientation.HORIZONTAL

        var desiredWidth = 0
        var desiredHeight = 0

        if (count != 0) {
            val w =
                (radius * 2 * count) + ((stroke * 2) * count) + (indicator.padding * (count - 1))
            val h = (radius * 2) + stroke
            desiredWidth = if (isHorizontal) w else h
            desiredHeight = if (isHorizontal) h else w
        }

        if (indicator.animationType == IndicatorAnimationType.DROP) {
            if (isHorizontal) desiredHeight *= 2 else desiredWidth *= 2
        }

        desiredWidth += indicator.paddingLeft + indicator.paddingRight
        desiredHeight += indicator.paddingTop + indicator.paddingBottom

        val width = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(widthMeasureSpec)
            MeasureSpec.AT_MOST -> min(desiredWidth, MeasureSpec.getSize(widthMeasureSpec))
            else -> desiredWidth
        }.coerceAtLeast(0)

        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(heightMeasureSpec)
            MeasureSpec.AT_MOST -> min(desiredHeight, MeasureSpec.getSize(heightMeasureSpec))
            else -> desiredHeight
        }.coerceAtLeast(0)

        indicator.width = width
        indicator.height = height
        return Pair(width, height)
    }
}
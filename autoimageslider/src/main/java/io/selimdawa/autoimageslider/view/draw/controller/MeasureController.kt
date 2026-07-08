package io.selimdawa.autoimageslider.view.draw.controller

import android.view.View.MeasureSpec
import io.selimdawa.autoimageslider.view.animation.type.IndicatorAnimationType
import io.selimdawa.autoimageslider.view.draw.data.Indicator
import io.selimdawa.autoimageslider.view.draw.data.Orientation
import kotlin.math.min

class MeasureController {

    fun measureViewSize(indicator: Indicator, widthSpec: Int, heightSpec: Int): Pair<Int, Int> {
        val count = indicator.count
        val r = indicator.radius
        val s = indicator.stroke
        val isH = indicator.orientation == Orientation.HORIZONTAL
        var dW = 0
        var dH = 0
        if (count != 0) {
            val w = (r * 2 * count) + ((s * 2) * count) + (indicator.padding * (count - 1))
            val h = (r * 2) + s
            dW = if (isH) w else h; dH = if (isH) h else w
        }

        if (indicator.animationType == IndicatorAnimationType.DROP) {
            if (isH) dH *= 2 else dW *= 2
        }

        dW += indicator.paddingLeft + indicator.paddingRight; dH += indicator.paddingTop + indicator.paddingBottom

        val w = when (MeasureSpec.getMode(widthSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(widthSpec); MeasureSpec.AT_MOST -> min(
                dW, MeasureSpec.getSize(widthSpec)
            ); else -> dW
        }.coerceAtLeast(0)
        val h = when (MeasureSpec.getMode(heightSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(heightSpec); MeasureSpec.AT_MOST -> min(
                dH, MeasureSpec.getSize(heightSpec)
            ); else -> dH
        }.coerceAtLeast(0)
        indicator.width = w; indicator.height = h
        return Pair(w, h)
    }
}
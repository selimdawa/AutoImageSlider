package com.selimdawa.autoimageslider.IndicatorView.draw.controller

import android.view.View.MeasureSpec
import com.selimdawa.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Indicator
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Orientation
import kotlin.math.min

class MeasureController {
    fun measureViewSize(
        indicator: Indicator,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ): Pair<Int, Int> {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val count = indicator.count
        val radius = indicator.radius
        val stroke = indicator.stroke

        val padding = indicator.padding
        val paddingLeft = indicator.paddingLeft
        val paddingTop = indicator.paddingTop
        val paddingRight = indicator.paddingRight
        val paddingBottom = indicator.paddingBottom

        val circleDiameterPx = radius * 2
        var desiredWidth = 0
        var desiredHeight = 0

        var width: Int
        var height: Int

        val orientation = indicator.orientation
        if (count != 0) {
            val diameterSum = circleDiameterPx * count
            val strokeSum = (stroke * 2) * count

            val paddingSum = padding * (count - 1)
            val w = diameterSum + strokeSum + paddingSum
            val h = circleDiameterPx + stroke

            if (orientation == Orientation.HORIZONTAL) {
                desiredWidth = w
                desiredHeight = h
            } else {
                desiredWidth = h
                desiredHeight = w
            }
        }

        if (indicator.animationType == IndicatorAnimationType.DROP) {
            if (orientation == Orientation.HORIZONTAL) {
                desiredHeight *= 2
            } else {
                desiredWidth *= 2
            }
        }

        val horizontalPadding = paddingLeft + paddingRight
        val verticalPadding = paddingTop + paddingBottom

        desiredWidth += horizontalPadding
        desiredHeight += verticalPadding

        width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(desiredWidth, widthSize)
            else -> desiredWidth
        }

        height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredHeight, heightSize)
            else -> desiredHeight
        }

        if (width < 0) {
            width = 0
        }

        if (height < 0) {
            height = 0
        }

        indicator.width = width
        indicator.height = height

        return Pair(width, height)
    }
}
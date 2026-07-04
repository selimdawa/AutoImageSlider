package com.selimdawa.autoimageslider.IndicatorView.draw.drawer.type

import android.graphics.Paint
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Indicator

open class BaseDrawer(paint: Paint, indicator: Indicator) {
    var paint: Paint? = paint
    var indicator: Indicator? = indicator
}
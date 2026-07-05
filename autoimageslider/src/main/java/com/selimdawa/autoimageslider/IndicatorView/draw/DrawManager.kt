package com.selimdawa.autoimageslider.IndicatorView.draw

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Pair
import android.view.MotionEvent
import com.selimdawa.autoimageslider.IndicatorView.animation.data.Value
import com.selimdawa.autoimageslider.IndicatorView.draw.controller.AttributeController
import com.selimdawa.autoimageslider.IndicatorView.draw.controller.DrawController
import com.selimdawa.autoimageslider.IndicatorView.draw.controller.MeasureController
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Indicator

class DrawManager {
    private val indicator = Indicator()
    private val drawController = DrawController(indicator)
    private val measureController = MeasureController()
    private val attributeController = AttributeController(indicator)

    fun indicator() = indicator
    fun setClickListener(listener: DrawController.ClickListener?) =
        drawController.setClickListener(listener)

    fun touch(event: MotionEvent?) = drawController.touch(event)
    fun updateValue(value: Value?) = drawController.updateValue(value)
    fun draw(canvas: Canvas) = drawController.draw(canvas)
    fun measureViewSize(wSpec: Int, hSpec: Int) =
        measureController.measureViewSize(indicator, wSpec, hSpec).let { Pair(it.first, it.second) }

    fun initAttributes(context: Context, attrs: AttributeSet?) =
        attributeController.init(context, attrs)
}
package com.selimdawa.autoimageslider.IndicatorView.draw

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Pair
import android.view.MotionEvent
import com.selimdawa.autoimageslider.IndicatorView.animation.data.Value
import com.selimdawa.autoimageslider.IndicatorView.draw.controller.AttributeController
import com.selimdawa.autoimageslider.IndicatorView.draw.controller.DrawController
import com.selimdawa.autoimageslider.IndicatorView.draw.controller.DrawController.ClickListener
import com.selimdawa.autoimageslider.IndicatorView.draw.controller.MeasureController
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Indicator

class DrawManager {
    private var indicator: Indicator?
    private val drawController: DrawController
    private val measureController: MeasureController
    private val attributeController: AttributeController

    init {
        this.indicator = Indicator()
        this.drawController = DrawController(indicator!!)
        this.measureController = MeasureController()
        this.attributeController = AttributeController(indicator!!)
    }

    fun indicator(): Indicator {
        if (indicator == null) {
            indicator = Indicator()
        }

        return indicator!!
    }

    fun setClickListener(listener: ClickListener?) {
        drawController.setClickListener(listener)
    }

    fun touch(event: MotionEvent?) {
        drawController.touch(event)
    }

    fun updateValue(value: Value?) {
        drawController.updateValue(value)
    }

    fun draw(canvas: Canvas) {
        drawController.draw(canvas)
    }

    fun measureViewSize(widthMeasureSpec: Int, heightMeasureSpec: Int): Pair<Int, Int> {
        val currentIndicator = indicator ?: return Pair(0, 0)
        val kotlinPair =
            measureController.measureViewSize(currentIndicator, widthMeasureSpec, heightMeasureSpec)

        return Pair(kotlinPair.first, kotlinPair.second)
    }


    fun initAttributes(context: Context, attrs: AttributeSet?) {
        attributeController.init(context, attrs)
    }
}

package com.selimdawa.autoimageslider.IndicatorView.draw.drawer

import android.graphics.Canvas
import android.graphics.Paint
import com.selimdawa.autoimageslider.IndicatorView.animation.data.Value
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Indicator
import com.selimdawa.autoimageslider.IndicatorView.draw.drawer.type.UniversalDrawer

class Drawer(indicator: Indicator) {
    private val paint = Paint().apply { style = Paint.Style.FILL; isAntiAlias = true }
    private val universalDrawer = UniversalDrawer(paint, indicator)

    private var position = 0
    private var coordinateX = 0
    private var coordinateY = 0

    fun setup(position: Int, coordinateX: Int, coordinateY: Int) {
        this.position = position; this.coordinateX = coordinateX; this.coordinateY = coordinateY
    }

    fun drawBasic(canvas: Canvas, isSelected: Boolean) =
        universalDrawer.drawBasic(canvas, position, isSelected, coordinateX, coordinateY)

    fun drawColor(canvas: Canvas, value: Value) =
        universalDrawer.drawWithAnimation(canvas, value, position, coordinateX, coordinateY)

    fun drawScale(canvas: Canvas, value: Value) =
        universalDrawer.drawWithAnimation(canvas, value, position, coordinateX, coordinateY)

    fun drawWorm(canvas: Canvas, value: Value) =
        universalDrawer.drawWithAnimation(canvas, value, position, coordinateX, coordinateY)

    fun drawSlide(canvas: Canvas, value: Value) =
        universalDrawer.drawWithAnimation(canvas, value, position, coordinateX, coordinateY)

    fun drawFill(canvas: Canvas, value: Value) =
        universalDrawer.drawWithAnimation(canvas, value, position, coordinateX, coordinateY)

    fun drawThinWorm(canvas: Canvas, value: Value) =
        universalDrawer.drawWithAnimation(canvas, value, position, coordinateX, coordinateY)

    fun drawDrop(canvas: Canvas, value: Value) =
        universalDrawer.drawWithAnimation(canvas, value, position, coordinateX, coordinateY)

    fun drawSwap(canvas: Canvas, value: Value) =
        universalDrawer.drawWithAnimation(canvas, value, position, coordinateX, coordinateY)

    fun drawScaleDown(canvas: Canvas, value: Value) =
        universalDrawer.drawWithAnimation(canvas, value, position, coordinateX, coordinateY)
}
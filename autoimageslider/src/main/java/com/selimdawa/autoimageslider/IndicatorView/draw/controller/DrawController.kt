package com.selimdawa.autoimageslider.IndicatorView.draw.controller

import android.graphics.Canvas
import android.view.MotionEvent
import com.selimdawa.autoimageslider.IndicatorView.animation.data.Value
import com.selimdawa.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Indicator
import com.selimdawa.autoimageslider.IndicatorView.draw.drawer.Drawer
import com.selimdawa.autoimageslider.IndicatorView.utils.CoordinatesUtils

class DrawController(private val indicator: Indicator) {
    private var value: Value? = null
    private val drawer: Drawer = Drawer(indicator)
    private var listener: ClickListener? = null

    interface ClickListener {
        fun onIndicatorClicked(position: Int)
    }

    fun updateValue(value: Value?) {
        this.value = value
    }

    fun setClickListener(listener: ClickListener?) {
        this.listener = listener
    }

    fun touch(event: MotionEvent?) {
        if (event == null) {
            return
        }

        when (event.action) {
            MotionEvent.ACTION_UP -> onIndicatorTouched(event.x, event.y)
            else -> {}
        }
    }

    private fun onIndicatorTouched(x: Float, y: Float) {
        val currentListener = listener
        if (currentListener != null) {
            val position = CoordinatesUtils.getPosition(indicator, x, y)
            if (position >= 0) {
                currentListener.onIndicatorClicked(position)
            }
        }
    }

    fun draw(canvas: Canvas) {
        val count = indicator.count

        for (position in 0..<count) {
            val coordinateX = CoordinatesUtils.getXCoordinate(indicator, position)
            val coordinateY = CoordinatesUtils.getYCoordinate(indicator, position)
            drawIndicator(canvas, position, coordinateX, coordinateY)
        }
    }

    private fun drawIndicator(
        canvas: Canvas,
        position: Int,
        coordinateX: Int,
        coordinateY: Int
    ) {
        val interactiveAnimation = indicator.isInteractiveAnimation
        val selectedPosition = indicator.selectedPosition
        val selectingPosition = indicator.selectingPosition
        val lastSelectedPosition = indicator.lastSelectedPosition

        val selectedItem =
            !interactiveAnimation && (position == selectedPosition || position == lastSelectedPosition)
        val selectingItem =
            interactiveAnimation && (position == selectedPosition || position == selectingPosition)
        val isSelectedItem = selectedItem or selectingItem
        drawer.setup(position, coordinateX, coordinateY)

        val currentValue = value
        if (currentValue != null && isSelectedItem) {
            drawWithAnimation(canvas, currentValue)
        } else {
            drawer.drawBasic(canvas, isSelectedItem)
        }
    }

    private fun drawWithAnimation(canvas: Canvas, animatedValue: Value) {
        when (indicator.animationType) {
            IndicatorAnimationType.NONE, null -> drawer.drawBasic(canvas, true)
            IndicatorAnimationType.COLOR -> drawer.drawColor(canvas, animatedValue)
            IndicatorAnimationType.SCALE -> drawer.drawScale(canvas, animatedValue)
            IndicatorAnimationType.WORM -> drawer.drawWorm(canvas, animatedValue)
            IndicatorAnimationType.SLIDE -> drawer.drawSlide(canvas, animatedValue)
            IndicatorAnimationType.FILL -> drawer.drawFill(canvas, animatedValue)
            IndicatorAnimationType.THIN_WORM -> drawer.drawThinWorm(canvas, animatedValue)
            IndicatorAnimationType.DROP -> drawer.drawDrop(canvas, animatedValue)
            IndicatorAnimationType.SWAP -> drawer.drawSwap(canvas, animatedValue)
            IndicatorAnimationType.SCALE_DOWN -> drawer.drawScaleDown(canvas, animatedValue)
        }
    }
}
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
    private val drawer = Drawer(indicator)
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
        if (event?.action == MotionEvent.ACTION_UP) {
            val position = CoordinatesUtils.getPosition(indicator, event.x, event.y)
            if (position >= 0) listener?.onIndicatorClicked(position)
        }
    }

    fun draw(canvas: Canvas) {
        for (position in 0..<indicator.count) {
            drawIndicator(
                canvas,
                position,
                CoordinatesUtils.getXCoordinate(indicator, position),
                CoordinatesUtils.getYCoordinate(indicator, position)
            )
        }
    }

    private fun drawIndicator(canvas: Canvas, position: Int, coordinateX: Int, coordinateY: Int) {
        val isInteractive = indicator.isInteractiveAnimation
        val selected = indicator.selectedPosition
        val isSelected =
            (!isInteractive && (position == selected || position == indicator.lastSelectedPosition)) || (isInteractive && (position == selected || position == indicator.selectingPosition))

        drawer.setup(position, coordinateX, coordinateY)
        value?.takeIf { isSelected }?.let { drawWithAnimation(canvas, it) } ?: drawer.drawBasic(
            canvas, isSelected
        )
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
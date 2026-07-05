package com.selimdawa.autoimageslider.View.draw.controller

import android.graphics.Canvas
import android.view.MotionEvent
import com.selimdawa.autoimageslider.View.animation.data.Value
import com.selimdawa.autoimageslider.View.animation.type.IndicatorAnimationType
import com.selimdawa.autoimageslider.View.draw.data.Indicator
import com.selimdawa.autoimageslider.View.draw.type.Drawer
import com.selimdawa.autoimageslider.View.utils.CoordinatesUtils

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
            CoordinatesUtils.getPosition(indicator, event.x, event.y).takeIf { it >= 0 }
                ?.let { listener?.onIndicatorClicked(it) }
        }
    }

    fun draw(canvas: Canvas) {
        for (i in 0..<indicator.count) drawIndicator(
            canvas,
            i,
            CoordinatesUtils.getXCoordinate(indicator, i),
            CoordinatesUtils.getYCoordinate(indicator, i)
        )
    }

    private fun drawIndicator(canvas: Canvas, position: Int, cx: Int, cy: Int) {
        val isInt = indicator.isInteractiveAnimation
        val sel = indicator.selectedPosition
        val isSelected =
            (!isInt && (position == sel || position == indicator.lastSelectedPosition)) || (isInt && (position == sel || position == indicator.selectingPosition))
        drawer.setup(position, cx, cy)
        value?.takeIf { isSelected }?.let { drawWithAnimation(canvas, it) } ?: drawer.drawBasic(
            canvas, isSelected
        )
    }

    private fun drawWithAnimation(canvas: Canvas, animValue: Value) {
        when (indicator.animationType) {
            IndicatorAnimationType.NONE, null -> drawer.drawBasic(canvas, true)
            IndicatorAnimationType.COLOR -> drawer.drawColor(canvas, animValue)
            IndicatorAnimationType.SCALE -> drawer.drawScale(canvas, animValue)
            IndicatorAnimationType.WORM -> drawer.drawWorm(canvas, animValue)
            IndicatorAnimationType.SLIDE -> drawer.drawSlide(canvas, animValue)
            IndicatorAnimationType.FILL -> drawer.drawFill(canvas, animValue)
            IndicatorAnimationType.THIN_WORM -> drawer.drawThinWorm(canvas, animValue)
            IndicatorAnimationType.DROP -> drawer.drawDrop(canvas, animValue)
            IndicatorAnimationType.SWAP -> drawer.drawSwap(canvas, animValue)
            IndicatorAnimationType.SCALE_DOWN -> drawer.drawScaleDown(canvas, animValue)
        }
    }
}
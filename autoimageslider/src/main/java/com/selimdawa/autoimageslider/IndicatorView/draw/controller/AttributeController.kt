package com.selimdawa.autoimageslider.IndicatorView.draw.controller

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.toColorInt
import com.selimdawa.autoimageslider.IndicatorView.animation.type.BaseAnimation
import com.selimdawa.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Indicator
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Orientation
import com.selimdawa.autoimageslider.IndicatorView.draw.data.RtlMode
import com.selimdawa.autoimageslider.IndicatorView.utils.DensityUtils
import com.selimdawa.autoimageslider.R

class AttributeController(private val indicator: Indicator) {

    fun init(context: Context, attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, R.styleable.PageIndicatorView, 0, 0) {
            initCountAttribute(this)
            initColorAttribute(this)
            initAnimationAttribute(this)
            initSizeAttribute(this)
        }
    }

    private fun initCountAttribute(typedArray: TypedArray) {
        val viewPagerId =
            typedArray.getResourceId(R.styleable.PageIndicatorView_piv_viewPager, View.NO_ID)
        val autoVisibility =
            typedArray.getBoolean(R.styleable.PageIndicatorView_piv_autoVisibility, true)
        val dynamicCount =
            typedArray.getBoolean(R.styleable.PageIndicatorView_piv_dynamicCount, false)
        var count = typedArray.getInt(
            R.styleable.PageIndicatorView_piv_count, Indicator.COUNT_NONE
        )

        if (count == Indicator.COUNT_NONE) {
            count = Indicator.DEFAULT_COUNT
        }

        var position = typedArray.getInt(R.styleable.PageIndicatorView_piv_select, 0)
        if (position < 0) {
            position = 0
        } else if (count > 0 && position > count - 1) {
            position = count - 1
        }

        indicator.viewPagerId = viewPagerId
        indicator.isAutoVisibility = autoVisibility
        indicator.isDynamicCount = dynamicCount
        indicator.count = count

        indicator.selectedPosition = position
        indicator.selectingPosition = position
        indicator.lastSelectedPosition = position
    }

    private fun initColorAttribute(typedArray: TypedArray) {
        val unselectedColor = typedArray.getColor(
            R.styleable.PageIndicatorView_piv_unselectedColor, "#33ffffff".toColorInt()
        )
        val selectedColor = typedArray.getColor(
            R.styleable.PageIndicatorView_piv_selectedColor, "#ffffff".toColorInt()
        )

        indicator.unselectedColor = unselectedColor
        indicator.selectedColor = selectedColor
    }

    private fun initAnimationAttribute(typedArray: TypedArray) {
        val interactiveAnimation =
            typedArray.getBoolean(R.styleable.PageIndicatorView_piv_interactiveAnimation, false)
        var animationDuration = typedArray.getInt(
            R.styleable.PageIndicatorView_piv_animationDuration,
            BaseAnimation.DEFAULT_ANIMATION_TIME
        )
        if (animationDuration < 0) {
            animationDuration = 0
        }

        val animIndex = typedArray.getInt(
            R.styleable.PageIndicatorView_piv_animationType, IndicatorAnimationType.NONE.ordinal
        )
        val animationType = getAnimationType(animIndex)

        val rtlIndex =
            typedArray.getInt(R.styleable.PageIndicatorView_piv_rtl_mode, RtlMode.Off.ordinal)
        val rtlMode: RtlMode = getRtlMode(rtlIndex)

        indicator.animationDuration = animationDuration.toLong()
        indicator.isInteractiveAnimation = interactiveAnimation
        indicator.animationType = animationType
        indicator.rtlMode = rtlMode
    }

    private fun initSizeAttribute(typedArray: TypedArray) {
        val orientationIndex = typedArray.getInt(
            R.styleable.PageIndicatorView_piv_orientation, Orientation.HORIZONTAL.ordinal
        )
        val orientation =
            if (orientationIndex == 0) Orientation.HORIZONTAL else Orientation.VERTICAL

        var radius = typedArray.getDimension(
            R.styleable.PageIndicatorView_piv_radius, DensityUtils.dpToPx(6).toFloat()
        ).toInt()
        if (radius < 0) radius = 0

        var padding = typedArray.getDimension(
            R.styleable.PageIndicatorView_piv_padding, DensityUtils.dpToPx(8).toFloat()
        ).toInt()
        if (padding < 0) padding = 0

        var scaleFactor = typedArray.getFloat(R.styleable.PageIndicatorView_piv_scaleFactor, 0.7f)
        if (scaleFactor < 0.3f) scaleFactor = 0.3f
        else if (scaleFactor > 1.0f) scaleFactor = 1.0f

        var stroke = typedArray.getDimension(
            R.styleable.PageIndicatorView_piv_strokeWidth, DensityUtils.dpToPx(1).toFloat()
        ).toInt()
        if (stroke > radius) stroke = radius
        if (indicator.animationType != IndicatorAnimationType.FILL) stroke = 0

        indicator.radius = radius
        indicator.orientation = orientation
        indicator.padding = padding
        indicator.scaleFactor = scaleFactor
        indicator.stroke = stroke
    }

    private fun getAnimationType(index: Int): IndicatorAnimationType {
        return when (index) {
            0 -> IndicatorAnimationType.NONE
            1 -> IndicatorAnimationType.COLOR
            2 -> IndicatorAnimationType.SCALE
            3 -> IndicatorAnimationType.WORM
            4 -> IndicatorAnimationType.SLIDE
            5 -> IndicatorAnimationType.FILL
            6 -> IndicatorAnimationType.THIN_WORM
            7 -> IndicatorAnimationType.DROP
            8 -> IndicatorAnimationType.SWAP
            9 -> IndicatorAnimationType.SCALE_DOWN
            else -> IndicatorAnimationType.NONE
        }
    }

    companion object {
        fun getRtlMode(index: Int): RtlMode {
            return when (index) {
                0 -> RtlMode.On
                1 -> RtlMode.Off
                2 -> RtlMode.Auto
                else -> RtlMode.Auto
            }
        }
    }
}
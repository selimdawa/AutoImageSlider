package io.selimdawa.autoimageslider.view.draw.controller

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.toColorInt
import io.selimdawa.autoimageslider.R
import io.selimdawa.autoimageslider.view.animation.type.BaseAnimation
import io.selimdawa.autoimageslider.view.animation.type.IndicatorAnimationType
import io.selimdawa.autoimageslider.view.draw.data.Indicator
import io.selimdawa.autoimageslider.view.draw.data.IndicatorShape
import io.selimdawa.autoimageslider.view.draw.data.Orientation
import io.selimdawa.autoimageslider.view.draw.data.RtlMode
import io.selimdawa.autoimageslider.view.utils.DensityUtils

class AttributeController(private val indicator: Indicator) {

    fun init(context: Context, attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, R.styleable.PageIndicatorView, 0, 0) {
            val count = getInt(
                R.styleable.PageIndicatorView_piv_count, Indicator.COUNT_NONE
            ).let { if (it == Indicator.COUNT_NONE) Indicator.DEFAULT_COUNT else it }
            val position = getInt(R.styleable.PageIndicatorView_piv_select, 0).coerceIn(
                0, (count - 1).coerceAtLeast(0)
            )
            val radius = getDimension(
                R.styleable.PageIndicatorView_piv_radius, DensityUtils.dpToPx(6).toFloat()
            ).toInt().coerceAtLeast(0)
            val stroke = getDimension(
                R.styleable.PageIndicatorView_piv_strokeWidth, DensityUtils.dpToPx(1).toFloat()
            ).toInt().coerceIn(0, radius)

            indicator.apply {
                viewPagerId = getResourceId(R.styleable.PageIndicatorView_piv_viewPager, View.NO_ID)
                isAutoVisibility =
                    getBoolean(R.styleable.PageIndicatorView_piv_autoVisibility, true)
                isDynamicCount = getBoolean(R.styleable.PageIndicatorView_piv_dynamicCount, false)
                this.count = count
                selectedPosition = position; selectingPosition = position; lastSelectedPosition =
                position
                unselectedColor = getColor(
                    R.styleable.PageIndicatorView_piv_unselectedColor, "#33ffffff".toColorInt()
                )
                selectedColor = getColor(
                    R.styleable.PageIndicatorView_piv_selectedColor, "#ffffff".toColorInt()
                )
                animationDuration = getInt(
                    R.styleable.PageIndicatorView_piv_animationDuration,
                    BaseAnimation.DEFAULT_ANIMATION_TIME
                ).coerceAtLeast(0).toLong()
                isInteractiveAnimation =
                    getBoolean(R.styleable.PageIndicatorView_piv_interactiveAnimation, false)
                animationType = IndicatorAnimationType.entries.getOrNull(
                    getInt(
                        R.styleable.PageIndicatorView_piv_animationType,
                        IndicatorAnimationType.NONE.ordinal
                    )
                ) ?: IndicatorAnimationType.NONE
                rtlMode = RtlMode.entries.getOrNull(
                    getInt(
                        R.styleable.PageIndicatorView_piv_rtl_mode, RtlMode.Off.ordinal
                    )
                ) ?: RtlMode.Auto
                indicatorShape = IndicatorShape.entries.getOrNull(
                    getInt(
                        R.styleable.PageIndicatorView_piv_indicatorShape,
                        IndicatorShape.CIRCLE.ordinal
                    )
                ) ?: IndicatorShape.CIRCLE
                this.radius = radius
                orientation = if (getInt(
                        R.styleable.PageIndicatorView_piv_orientation,
                        Orientation.HORIZONTAL.ordinal
                    ) == 0
                ) Orientation.HORIZONTAL else Orientation.VERTICAL
                padding = getDimension(
                    R.styleable.PageIndicatorView_piv_padding, DensityUtils.dpToPx(8).toFloat()
                ).toInt().coerceAtLeast(0)
                scaleFactor =
                    getFloat(R.styleable.PageIndicatorView_piv_scaleFactor, 0.7f).coerceIn(
                        0.3f, 1.0f
                    )
                this.stroke = if (animationType == IndicatorAnimationType.FILL) stroke else 0
            }
        }
    }
}
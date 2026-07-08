package io.selimdawa.autoimageslider.view.draw.data

import android.view.View
import io.selimdawa.autoimageslider.view.animation.type.IndicatorAnimationType

class Indicator {
    var height = 0
    var width = 0
    var radius = 0
    var padding = 0
    var paddingLeft = 0
    var paddingTop = 0
    var paddingRight = 0
    var paddingBottom = 0
    var stroke = 0
    var scaleFactor = 0f
    var unselectedColor = 0
    var selectedColor = 0
    var isInteractiveAnimation = false
    var isAutoVisibility = false
    var isDynamicCount = false
    var animationDuration = 0L
    var count = DEFAULT_COUNT
    var selectedPosition = 0
    var selectingPosition = 0
    var lastSelectedPosition = 0
    var viewPagerId = View.NO_ID

    var orientation: Orientation? = null
        get() = field ?: Orientation.HORIZONTAL
    var animationType: IndicatorAnimationType? = null
        get() = field ?: IndicatorAnimationType.NONE
    var rtlMode: RtlMode? = null
        get() = field ?: RtlMode.Off

    companion object {
        const val DEFAULT_COUNT = 3
        const val MIN_COUNT = 1
        const val COUNT_NONE = -1
        const val DEFAULT_RADIUS_DP = 6
        const val DEFAULT_PADDING_DP = 8
    }
}
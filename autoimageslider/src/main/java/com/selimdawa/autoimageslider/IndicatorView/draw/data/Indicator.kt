package com.selimdawa.autoimageslider.IndicatorView.draw.data

import android.view.View
import com.selimdawa.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType

class Indicator {
    var height: Int = 0
    var width: Int = 0
    var radius: Int = 0

    var padding: Int = 0
    var paddingLeft: Int = 0
    var paddingTop: Int = 0
    var paddingRight: Int = 0
    var paddingBottom: Int = 0

    var stroke: Int = 0 //For "Fill" animation only
    var scaleFactor: Float = 0f //For "Scale" animation only

    var unselectedColor: Int = 0
    var selectedColor: Int = 0

    var isInteractiveAnimation: Boolean = false
    var isAutoVisibility: Boolean = false
    var isDynamicCount: Boolean = false

    var animationDuration: Long = 0
    var count: Int = DEFAULT_COUNT

    var selectedPosition: Int = 0
    var selectingPosition: Int = 0
    var lastSelectedPosition: Int = 0

    var viewPagerId: Int = View.NO_ID

    var orientation: Orientation? = null
        get() {
            if (field == null) {
                field = Orientation.HORIZONTAL
            }
            return field
        }
    var animationType: IndicatorAnimationType? = null
        get() {
            if (field == null) {
                field = IndicatorAnimationType.NONE
            }
            return field
        }
    var rtlMode: RtlMode? = null
        get() {
            if (field == null) {
                field = RtlMode.Off
            }
            return field
        }

    companion object {
        const val DEFAULT_COUNT: Int = 3
        const val MIN_COUNT: Int = 1
        const val COUNT_NONE: Int = -1

        const val DEFAULT_RADIUS_DP: Int = 6
        const val DEFAULT_PADDING_DP: Int = 8
    }
}

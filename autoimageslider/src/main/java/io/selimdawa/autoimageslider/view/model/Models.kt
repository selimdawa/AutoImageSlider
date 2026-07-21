package io.selimdawa.autoimageslider.view.model

import android.os.Parcel
import android.os.Parcelable
import android.view.View
import io.selimdawa.autoimageslider.view.utils.DensityUtils

// --- Enums ---

enum class IndicatorShape { CIRCLE, SQUARE, DASH }

enum class Orientation { HORIZONTAL, VERTICAL }

enum class RtlMode { On, Off, Auto }

enum class IndicatorAnimationType {
    NONE, COLOR, SCALE, WORM, SLIDE, FILL, THIN_WORM, DROP, SWAP, SCALE_DOWN, JUMP
}

// --- Data Classes ---

interface Value

open class ColorAnimationValue(var color: Int = 0, var colorReverse: Int = 0) : Value

data class DropAnimationValue(var width: Int = 0, var height: Int = 0, var radius: Int = 0) : Value

open class ScaleAnimationValue(var radius: Int = 0, var radiusReverse: Int = 0) : ColorAnimationValue()

data class FillAnimationValue(
    var radius: Int = 0, var radiusReverse: Int = 0, var stroke: Int = 0, var strokeReverse: Int = 0
) : ColorAnimationValue()

data class SlideAnimationValue(var coordinate: Int = 0) : Value

data class SwapAnimationValue(var coordinate: Int = 0, var coordinateReverse: Int = 0) : Value

open class WormAnimationValue(var rectStart: Int = 0, var rectEnd: Int = 0) : Value

data class ThinWormAnimationValue(var height: Int = 0) : WormAnimationValue()

data class JumpAnimationValue(var coordinate: Int = 0, var radius: Int = 0) : Value

// --- Core Indicator Data ---

class Indicator {
    var height = 0
    var width = 0
    var radius = DensityUtils.dpToPx(DEFAULT_RADIUS_DP)
    var padding = DensityUtils.dpToPx(DEFAULT_PADDING_DP)
    var paddingLeft = 0
    var paddingTop = 0
    var paddingRight = 0
    var paddingBottom = 0
    var stroke = 0
    var scaleFactor = 0f
    var unselectedColor = 0
    var selectedColor = 0
    var isInteractiveAnimation = true
    var isAutoVisibility = false
    var isDynamicCount = false
    var animationDuration = 0L
    var count = DEFAULT_COUNT
    var selectedPosition = 0
    var selectingPosition = 0
    var lastSelectedPosition = 0
    var viewPagerId = View.NO_ID

    var indicatorShape: IndicatorShape? = null
        get() = field ?: IndicatorShape.CIRCLE
    var orientation: Orientation? = null
        get() = field ?: Orientation.HORIZONTAL
    var animationType: IndicatorAnimationType? = null
        get() = field ?: IndicatorAnimationType.NONE
    var rtlMode: RtlMode? = null
        get() = field ?: RtlMode.Off

    companion object {
        const val DEFAULT_COUNT = 3
        const val COUNT_NONE = -1
        const val DEFAULT_RADIUS_DP = 4
        const val DEFAULT_PADDING_DP = 6
    }
}

class PositionSavedState : View.BaseSavedState {
    var selectedPosition = 0
    var selectingPosition = 0
    var lastSelectedPosition = 0

    constructor(superState: Parcelable?) : super(superState)
    private constructor(`in`: Parcel) : super(`in`) {
        selectedPosition = `in`.readInt(); selectingPosition =
            `in`.readInt(); lastSelectedPosition = `in`.readInt()
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeInt(selectedPosition); out.writeInt(selectingPosition); out.writeInt(
            lastSelectedPosition
        )
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<PositionSavedState?> {
            override fun createFromParcel(`in`: Parcel) = PositionSavedState(`in`)
            override fun newArray(size: Int) = arrayOfNulls<PositionSavedState?>(size)
        }
    }
}
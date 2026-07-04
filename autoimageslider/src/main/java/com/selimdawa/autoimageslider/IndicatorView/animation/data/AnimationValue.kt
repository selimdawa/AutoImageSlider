package com.selimdawa.autoimageslider.IndicatorView.animation.data

import com.selimdawa.autoimageslider.IndicatorView.animation.data.type.ColorAnimationValue
import com.selimdawa.autoimageslider.IndicatorView.animation.data.type.DropAnimationValue
import com.selimdawa.autoimageslider.IndicatorView.animation.data.type.FillAnimationValue
import com.selimdawa.autoimageslider.IndicatorView.animation.data.type.ScaleAnimationValue
import com.selimdawa.autoimageslider.IndicatorView.animation.data.type.SwapAnimationValue
import com.selimdawa.autoimageslider.IndicatorView.animation.data.type.ThinWormAnimationValue
import com.selimdawa.autoimageslider.IndicatorView.animation.data.type.WormAnimationValue

class AnimationValue {
    var colorAnimationValue: ColorAnimationValue? = null
        get() {
            if (field == null) {
                field = ColorAnimationValue()
            }
            return field
        }
        private set
    var scaleAnimationValue: ScaleAnimationValue? = null
        get() {
            if (field == null) {
                field = ScaleAnimationValue()
            }
            return field
        }
        private set
    var wormAnimationValue: WormAnimationValue? = null
        get() {
            if (field == null) {
                field = WormAnimationValue()
            }
            return field
        }
        private set
    var fillAnimationValue: FillAnimationValue? = null
        get() {
            if (field == null) {
                field = FillAnimationValue()
            }
            return field
        }
        private set
    var thinWormAnimationValue: ThinWormAnimationValue? = null
        get() {
            if (field == null) {
                field = ThinWormAnimationValue()
            }
            return field
        }
        private set
    var dropAnimationValue: DropAnimationValue? = null
        get() {
            if (field == null) {
                field = DropAnimationValue()
            }
            return field
        }
        private set
    var swapAnimationValue: SwapAnimationValue? = null
        get() {
            if (field == null) {
                field = SwapAnimationValue()
            }
            return field
        }
        private set
}

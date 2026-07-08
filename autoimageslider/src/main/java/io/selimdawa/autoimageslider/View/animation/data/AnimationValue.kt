package io.selimdawa.autoimageslider.View.animation.data

interface Value

open class ColorAnimationValue(var color: Int = 0, var colorReverse: Int = 0) : Value
data class DropAnimationValue(var width: Int = 0, var height: Int = 0, var radius: Int = 0) : Value
open class ScaleAnimationValue(var radius: Int = 0, var radiusReverse: Int = 0) :
    ColorAnimationValue()

class ScaleDownAnimationValue : ScaleAnimationValue()
data class FillAnimationValue(
    var radius: Int = 0, var radiusReverse: Int = 0, var stroke: Int = 0, var strokeReverse: Int = 0
) : ColorAnimationValue()

data class SlideAnimationValue(var coordinate: Int = 0) : Value
data class SwapAnimationValue(var coordinate: Int = 0, var coordinateReverse: Int = 0) : Value
open class WormAnimationValue(var rectStart: Int = 0, var rectEnd: Int = 0) : Value
data class ThinWormAnimationValue(var height: Int = 0) : WormAnimationValue()
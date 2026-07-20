package io.selimdawa.autoimageslider

enum class SliderAnimations(
    val cameraDist: Float = 0f,
    val rotationFactor: Float = 0f,
    val useXAxis: Boolean = false,
    val isCubeIn: Boolean = false,
    val isCubeOut: Boolean = false,
    val hasScaleY: Boolean = false,
    val scaleMode: Int = 0
) {
    ANTICLOCK_SPIN, CLOCK_SPIN, CUBE_IN_DEPTH(
        cameraDist = 20000f, rotationFactor = 90f, isCubeIn = true, hasScaleY = true, scaleMode = 1
    ),
    CUBE_IN_ROTATION(
        cameraDist = 20000f, rotationFactor = 90f, isCubeIn = true
    ),
    CUBE_IN_SCALING(
        cameraDist = 20000f, rotationFactor = 90f, isCubeIn = true, hasScaleY = true, scaleMode = 2
    ),
    CUBE_OUT_DEPTH(
        rotationFactor = -90f, isCubeOut = true, hasScaleY = true, scaleMode = 1
    ),
    CUBE_OUT_ROTATION(
        rotationFactor = -90f, isCubeOut = true
    ),
    CUBE_OUT_SCALING(
        rotationFactor = -90f, isCubeOut = true, hasScaleY = true, scaleMode = 2
    ),
    DEPTH, FADE, FAN(
        cameraDist = 20000f, rotationFactor = -120f
    ),
    FIDGET_SPIN, GATE(
        rotationFactor = 90f, isCubeIn = true
    ),
    HINGE, HORIZONTAL_FLIP(
        cameraDist = 20000f, rotationFactor = 180f, useXAxis = true
    ),
    POP, SIMPLE, SPINNER(
        cameraDist = 12000f, rotationFactor = 900f
    ),
    TOSS(
        cameraDist = 20000f, rotationFactor = 1080f, useXAxis = true
    ),
    VERTICAL_FLIP(
        cameraDist = 12000f, rotationFactor = 180f
    ),
    VERTICAL_SHUT(
        cameraDist = 1.0E9f, rotationFactor = 180f, useXAxis = true
    ),
    ZOOM_OUT
}
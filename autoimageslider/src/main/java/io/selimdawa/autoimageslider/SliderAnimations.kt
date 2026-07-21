package io.selimdawa.autoimageslider

enum class SliderAnimations(
    val cameraDist: Float = 0f,
    val rotationFactor: Float = 0f,
    val useXAxis: Boolean = false,
    val isCubeIn: Boolean = false,
    val isCubeOut: Boolean = false,
    val hasScaleY: Boolean = false,
    val scaleMode: Int = 0,
    val minScale: Float = 1.0f,
    val minAlpha: Float = 1.0f,
    val alphaThreshold: Float = 1.0f,
    val parallaxFactor: Float = 1.0f
) {
    ANTICLOCKWISE_SPIN(alphaThreshold = 0.5f),
    CLOCK_SPIN(alphaThreshold = 0.5f),
    CUBE_IN_DEPTH(
        cameraDist = 20000f,
        rotationFactor = 90f,
        isCubeIn = true,
        hasScaleY = true,
        scaleMode = 1,
        minScale = 0.4f
    ),
    CUBE_IN_ROTATION(
        cameraDist = 20000f, rotationFactor = 90f, isCubeIn = true
    ),
    CUBE_IN_SCALING(
        cameraDist = 20000f,
        rotationFactor = 90f,
        isCubeIn = true,
        hasScaleY = true,
        scaleMode = 2,
        minScale = 0.4f
    ),
    CUBE_OUT_DEPTH(
        rotationFactor = -90f, isCubeOut = true, hasScaleY = true, scaleMode = 1, minScale = 0.4f
    ),
    CUBE_OUT_ROTATION(
        rotationFactor = -90f, isCubeOut = true
    ),
    CUBE_OUT_SCALING(
        rotationFactor = -90f, isCubeOut = true, hasScaleY = true, scaleMode = 2, minScale = 0.4f
    ),
    DEPTH,
    FADE,
    FAN(
        cameraDist = 20000f, rotationFactor = -120f
    ),
    FIDGET_SPIN(alphaThreshold = 0.5f),
    GATE(
        rotationFactor = 90f, isCubeIn = true
    ),
    HINGE(rotationFactor = 90f),
    HORIZONTAL_FLIP(
        cameraDist = 20000f, rotationFactor = 180f, useXAxis = true
    ),
    POP(alphaThreshold = 0.5f, minScale = 0.4f),
    SIMPLE,
    SPINNER(
        cameraDist = 12000f, rotationFactor = 900f
    ),
    TOSS(
        cameraDist = 20000f, rotationFactor = 1080f, useXAxis = true, minScale = 0.4f
    ),
    VERTICAL_FLIP(
        cameraDist = 12000f, rotationFactor = 180f
    ),
    VERTICAL_SHUT(
        cameraDist = 1.0E9f, rotationFactor = 180f, useXAxis = true
    ),
    ZOOM_OUT(minAlpha = 0.3f, minScale = 0.65f),
    ACCORDION,
    BACKGROUND_TO_FOREGROUND(alphaThreshold = 0.5f, minScale = 0.4f),
    FOREGROUND_TO_BACKGROUND(alphaThreshold = 0.5f, minScale = 0.4f),
    ROTATE_DOWN(rotationFactor = 20f),
    ROTATE_UP(rotationFactor = 20f),
    TABLET(rotationFactor = 45f),
    ZOOM_IN(alphaThreshold = 0.5f),
    PARALLAX(parallaxFactor = 0.5f)
}
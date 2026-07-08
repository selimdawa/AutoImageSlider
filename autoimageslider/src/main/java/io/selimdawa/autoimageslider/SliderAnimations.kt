package io.selimdawa.autoimageslider

@Suppress("unused")
enum class SliderAnimations(
    val cameraDist: Float = 0f,
    val rotationFactor: Float = 0f,
    val useXAxis: Boolean = false,
    val isCubeIn: Boolean = false,
    val isCubeOut: Boolean = false,
    val hasScaleY: Boolean = false,
    val scaleMode: Int = 0
) {
    ANTICLOCKSPINTRANSFORMATION, CLOCK_SPINTRANSFORMATION, CUBEINDEPTHTRANSFORMATION(
        cameraDist = 20000f, rotationFactor = 90f, isCubeIn = true, hasScaleY = true, scaleMode = 1
    ),
    CUBEINROTATIONTRANSFORMATION(
        cameraDist = 20000f, rotationFactor = 90f, isCubeIn = true
    ),
    CUBEINSCALINGTRANSFORMATION(
        cameraDist = 20000f, rotationFactor = 90f, isCubeIn = true, hasScaleY = true, scaleMode = 2
    ),
    CUBEOUTDEPTHTRANSFORMATION(
        rotationFactor = -90f, isCubeOut = true, hasScaleY = true, scaleMode = 1
    ),
    CUBEOUTROTATIONTRANSFORMATION(
        rotationFactor = -90f, isCubeOut = true
    ),
    CUBEOUTSCALINGTRANSFORMATION(
        rotationFactor = -90f, isCubeOut = true, hasScaleY = true, scaleMode = 2
    ),
    DEPTHTRANSFORMATION, FADETRANSFORMATION, FANTRANSFORMATION(
        cameraDist = 20000f, rotationFactor = -120f
    ),
    FIDGETSPINTRANSFORMATION, GATETRANSFORMATION(
        rotationFactor = 90f, isCubeIn = true
    ),
    HINGETRANSFORMATION, HORIZONTALFLIPTRANSFORMATION(
        cameraDist = 20000f, rotationFactor = 180f, useXAxis = true
    ),
    POPTRANSFORMATION, SIMPLETRANSFORMATION, SPINNERTRANSFORMATION(
        cameraDist = 12000f, rotationFactor = 900f
    ),
    TOSSTRANSFORMATION(
        cameraDist = 20000f, rotationFactor = 1080f, useXAxis = true
    ),
    VERTICALFLIPTRANSFORMATION(
        cameraDist = 12000f, rotationFactor = 180f
    ),
    VERTICALSHUTTRANSFORMATION(
        cameraDist = 1.0E9f, rotationFactor = 180f, useXAxis = true
    ),
    ZOOMOUTTRANSFORMATION
}
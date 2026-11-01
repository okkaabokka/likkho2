package com.likkho.nativeui.model

enum class CanvasLayer {
    FOREGROUND,
    BACKGROUND
}

data class FloatingCanvasImage(
    val id: String,
    val uri: String,
    val x: Float = 60f,
    val y: Float = 120f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val layer: CanvasLayer = CanvasLayer.FOREGROUND,
    val baseWidthDp: Float = 180f,
    val baseHeightDp: Float = 180f,
)

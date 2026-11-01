package com.likkho.nativeui.model

/**
 * Represents a styled font range within the body text.
 */
data class FontSpan(
    val start: Int,
    val end: Int,
    val fontId: String,
)

/**
 * Represents a font item available in the app (either built-in or custom uploaded).
 */
data class FontItem(
    val id: String,
    val name: String,
    val filePath: String? = null,
    val isBuiltin: Boolean = false,
)

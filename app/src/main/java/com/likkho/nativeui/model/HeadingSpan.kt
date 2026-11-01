package com.likkho.nativeui.model

/**
 * Represents a heading level (H1, H2, H3) applied to a line or paragraph range.
 * level: 1 (H1), 2 (H2), 3 (H3)
 */
data class HeadingSpan(
    val level: Int,
    val start: Int,
    val end: Int,
)

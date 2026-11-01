package com.likkho.nativeui.model

import androidx.compose.ui.graphics.Color

/** Square 1:1 "PFP" shown in the list and workspace header. */
data class Avatar(
    val label: String = "Likkho",
    val labelSp: Int = 10,
    val sub: String? = null,
    val bg: Color = Color.White,
    val fg: Color = Color.Black,
    val subColor: Color = Color.Black,
    val frame: Color? = null,
) {
    companion object {
        fun monogram(heading: String): Avatar {
            val letter = heading.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "L"
            return Avatar(
                label = letter,
                labelSp = 24,
                bg = Color(0xFFEAF3FF),
                fg = Color(0xFF3366CC),
            )
        }
    }
}

data class DiaryLog(
    val id: String,
    val heading: String,
    val body: String,
    val createdAt: Long,
    val updatedAt: Long,
    val pinned: Boolean = false,
    /** Per-diary passcode (demo only — real encryption comes later). */
    val lockPin: String? = null,
    val reminderAt: Long? = null,
    val avatar: Avatar = Avatar(),
    val fontSpans: List<FontSpan> = emptyList(),
    val boldSpans: List<FormatSpan> = emptyList(),
    val italicSpans: List<FormatSpan> = emptyList(),
    val underlineSpans: List<FormatSpan> = emptyList(),
    val strikethroughSpans: List<FormatSpan> = emptyList(),
    val subscriptSpans: List<FormatSpan> = emptyList(),
    val superscriptSpans: List<FormatSpan> = emptyList(),
    val codeSpans: List<FormatSpan> = emptyList(),
    val quoteSpans: List<FormatSpan> = emptyList(),
    val headingSpans: List<HeadingSpan> = emptyList(),
    val bgImageUri: String? = null,
    val bgOpacity: Float = 0.35f,
    val bgFitMode: String = "cover",
    val floatingImages: List<FloatingCanvasImage> = emptyList(),
) {
    val preview: String
        get() = body.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() }.orEmpty()
}

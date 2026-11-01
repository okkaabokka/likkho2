package com.likkho.nativeui.util

import com.likkho.nativeui.model.FontSpan
import com.likkho.nativeui.model.FormatSpan
import com.likkho.nativeui.model.HeadingSpan

/** One contiguous edit: [removed] chars at [start] were replaced by [inserted] chars. */
data class TextEdit(val start: Int, val removed: Int, val inserted: Int)

/** Result of shifting every span list through one text edit. */
data class EditedSpans(
    val fontSpans: List<FontSpan>,
    val boldSpans: List<FormatSpan>,
    val italicSpans: List<FormatSpan>,
    val underlineSpans: List<FormatSpan>,
    val strikethroughSpans: List<FormatSpan>,
    val subscriptSpans: List<FormatSpan>,
    val superscriptSpans: List<FormatSpan>,
    val codeSpans: List<FormatSpan>,
    val quoteSpans: List<FormatSpan>,
    val headingSpans: List<HeadingSpan>,
)

object TextEditDiff {

    /** Finds the single changed block between two strings (common prefix / suffix). */
    fun compute(old: String, new: String): TextEdit {
        val minLen = minOf(old.length, new.length)
        var prefix = 0
        while (prefix < minLen && old[prefix] == new[prefix]) prefix++
        var suffix = 0
        while (suffix < minLen - prefix &&
            old[old.length - 1 - suffix] == new[new.length - 1 - suffix]
        ) suffix++
        return TextEdit(
            start = prefix,
            removed = old.length - prefix - suffix,
            inserted = new.length - prefix - suffix,
        )
    }

    private fun formatStep(
        spans: List<FormatSpan>,
        edit: TextEdit,
        oldLen: Int,
        typingOn: Boolean,
    ): List<FormatSpan> {
        // Replacing text that was fully formatted keeps the formatting.
        val inherit = edit.removed > 0 &&
            FormatSpanManager.isRangeFullyCovered(spans, edit.start, edit.start + edit.removed)
        var out = spans
        if (edit.removed > 0) {
            out = FormatSpanManager.updateSpansOnTextEdit(out, oldLen, oldLen - edit.removed, edit.start, false)
        }
        if (edit.inserted > 0) {
            val mid = oldLen - edit.removed
            out = FormatSpanManager.updateSpansOnTextEdit(
                out, mid, mid + edit.inserted, edit.start + edit.inserted, typingOn || inherit,
            )
        }
        return out
    }

    /**
     * Moves font / bold / italic / underline / strikethrough / heading spans
     * through the edit that turned [oldText] into [newText]. Works for typing,
     * backspace, paste, and replacing a selection.
     */
    fun applyToSpans(
        oldText: String,
        newText: String,
        fontSpans: List<FontSpan>,
        boldSpans: List<FormatSpan>,
        italicSpans: List<FormatSpan>,
        underlineSpans: List<FormatSpan>,
        strikethroughSpans: List<FormatSpan>,
        subscriptSpans: List<FormatSpan>,
        superscriptSpans: List<FormatSpan>,
        codeSpans: List<FormatSpan>,
        quoteSpans: List<FormatSpan>,
        headingSpans: List<HeadingSpan>,
        activeFontId: String?,
        boldOn: Boolean,
        italicOn: Boolean,
        underlineOn: Boolean,
        strikethroughOn: Boolean,
        subscriptOn: Boolean,
        superscriptOn: Boolean,
        codeOn: Boolean,
        quoteOn: Boolean,
        headingLevel: Int?,
    ): EditedSpans {
        val edit = compute(oldText, newText)
        if (edit.removed == 0 && edit.inserted == 0) {
            return EditedSpans(
                fontSpans, boldSpans, italicSpans, underlineSpans, strikethroughSpans,
                subscriptSpans, superscriptSpans, codeSpans, quoteSpans, headingSpans,
            )
        }
        val oldLen = oldText.length
        val mid = oldLen - edit.removed
        val midText = if (edit.removed > 0) oldText.removeRange(edit.start, edit.start + edit.removed) else oldText

        // ---- fonts
        val inheritedFont = if (edit.removed > 0) {
            fontSpans.firstOrNull { it.start <= edit.start && it.end >= edit.start + edit.removed }?.fontId
        } else null
        var fonts = fontSpans
        if (edit.removed > 0) {
            fonts = CustomFontManager.updateSpansOnTextEdit(fonts, oldLen, mid, edit.start, null)
        }
        if (edit.inserted > 0) {
            fonts = CustomFontManager.updateSpansOnTextEdit(
                fonts, mid, mid + edit.inserted, edit.start + edit.inserted, activeFontId ?: inheritedFont,
            )
        }

        // ---- headings
        val inheritedHeading = if (edit.removed > 0) {
            headingSpans.firstOrNull { it.start <= edit.start && it.end >= edit.start + edit.removed }?.level
        } else null
        var heads = headingSpans
        if (edit.removed > 0) {
            heads = HeadingSpanManager.updateSpansOnTextEdit(heads, oldText, midText, edit.start, null)
        }
        if (edit.inserted > 0) {
            heads = HeadingSpanManager.updateSpansOnTextEdit(
                heads, midText, newText, edit.start + edit.inserted, headingLevel ?: inheritedHeading,
            )
        }

        return EditedSpans(
            fontSpans = fonts,
            boldSpans = formatStep(boldSpans, edit, oldLen, boldOn),
            italicSpans = formatStep(italicSpans, edit, oldLen, italicOn),
            underlineSpans = formatStep(underlineSpans, edit, oldLen, underlineOn),
            strikethroughSpans = formatStep(strikethroughSpans, edit, oldLen, strikethroughOn),
            subscriptSpans = formatStep(subscriptSpans, edit, oldLen, subscriptOn),
            superscriptSpans = formatStep(superscriptSpans, edit, oldLen, superscriptOn),
            codeSpans = formatStep(codeSpans, edit, oldLen, codeOn),
            quoteSpans = formatStep(quoteSpans, edit, oldLen, quoteOn),
            headingSpans = heads,
        )
    }
}

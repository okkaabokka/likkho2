package com.likkho.nativeui.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import com.likkho.nativeui.model.FloatingCanvasImage
import com.likkho.nativeui.model.FontSpan
import com.likkho.nativeui.model.FormatSpan
import com.likkho.nativeui.model.HeadingSpan

/**
 * Snapshot of canvas text, formatting spans, active heading, background, and floating images.
 */
data class CanvasTextState(
    val body: TextFieldValue,
    val heading: TextFieldValue,
    val fontSpans: List<FontSpan> = emptyList(),
    val activeFontId: String? = null,
    val boldSpans: List<FormatSpan> = emptyList(),
    val italicSpans: List<FormatSpan> = emptyList(),
    val underlineSpans: List<FormatSpan> = emptyList(),
    val strikethroughSpans: List<FormatSpan> = emptyList(),
    val subscriptSpans: List<FormatSpan> = emptyList(),
    val superscriptSpans: List<FormatSpan> = emptyList(),
    val codeSpans: List<FormatSpan> = emptyList(),
    val quoteSpans: List<FormatSpan> = emptyList(),
    val headingSpans: List<HeadingSpan> = emptyList(),
    val isBoldActive: Boolean = false,
    val isItalicActive: Boolean = false,
    val isUnderlineActive: Boolean = false,
    val isStrikethroughActive: Boolean = false,
    val isSubscriptActive: Boolean = false,
    val isSuperscriptActive: Boolean = false,
    val isCodeActive: Boolean = false,
    val isQuoteActive: Boolean = false,
    val activeHeadingLevel: Int? = null,
    val bgImageUri: String? = null,
    val bgOpacity: Float = 0.35f,
    val bgFitMode: String = "cover",
    val floatingImages: List<FloatingCanvasImage> = emptyList(),
)

/**
 * Undo and Redo manager for canvas text, styles, font spans, active modes, cursor changes, and floating images.
 */
class CanvasUndoRedoManager(
    initialBody: TextFieldValue = TextFieldValue(),
    initialHeading: TextFieldValue = TextFieldValue(),
    initialFontSpans: List<FontSpan> = emptyList(),
    initialActiveFontId: String? = null,
    initialBoldSpans: List<FormatSpan> = emptyList(),
    initialItalicSpans: List<FormatSpan> = emptyList(),
    initialUnderlineSpans: List<FormatSpan> = emptyList(),
    initialStrikethroughSpans: List<FormatSpan> = emptyList(),
    initialSubscriptSpans: List<FormatSpan> = emptyList(),
    initialSuperscriptSpans: List<FormatSpan> = emptyList(),
    initialCodeSpans: List<FormatSpan> = emptyList(),
    initialQuoteSpans: List<FormatSpan> = emptyList(),
    initialHeadingSpans: List<HeadingSpan> = emptyList(),
    initialIsBoldActive: Boolean = false,
    initialIsItalicActive: Boolean = false,
    initialIsUnderlineActive: Boolean = false,
    initialIsStrikethroughActive: Boolean = false,
    initialIsSubscriptActive: Boolean = false,
    initialIsSuperscriptActive: Boolean = false,
    initialIsCodeActive: Boolean = false,
    initialIsQuoteActive: Boolean = false,
    initialActiveHeadingLevel: Int? = null,
    initialBgImageUri: String? = null,
    initialBgOpacity: Float = 0.35f,
    initialBgFitMode: String = "cover",
    initialFloatingImages: List<FloatingCanvasImage> = emptyList(),
    private val maxHistory: Int = 500,
) {
    private val history = mutableListOf<CanvasTextState>()

    var currentIndex by mutableIntStateOf(0)
        private set

    var historyCount by mutableIntStateOf(1)
        private set

    var isApplyingSnapshot: Boolean = false
        private set

    init {
        history.add(
            CanvasTextState(
                body = initialBody,
                heading = initialHeading,
                fontSpans = initialFontSpans,
                activeFontId = initialActiveFontId,
                boldSpans = initialBoldSpans,
                italicSpans = initialItalicSpans,
                underlineSpans = initialUnderlineSpans,
                strikethroughSpans = initialStrikethroughSpans,
                subscriptSpans = initialSubscriptSpans,
                superscriptSpans = initialSuperscriptSpans,
                codeSpans = initialCodeSpans,
                quoteSpans = initialQuoteSpans,
                headingSpans = initialHeadingSpans,
                isBoldActive = initialIsBoldActive,
                isItalicActive = initialIsItalicActive,
                isUnderlineActive = initialIsUnderlineActive,
                isStrikethroughActive = initialIsStrikethroughActive,
                isSubscriptActive = initialIsSubscriptActive,
                isSuperscriptActive = initialIsSuperscriptActive,
                isCodeActive = initialIsCodeActive,
                isQuoteActive = initialIsQuoteActive,
                activeHeadingLevel = initialActiveHeadingLevel,
                bgImageUri = initialBgImageUri,
                bgOpacity = initialBgOpacity,
                bgFitMode = initialBgFitMode,
                floatingImages = initialFloatingImages,
            )
        )
        currentIndex = 0
        historyCount = 1
    }

    val canUndo: Boolean
        get() = currentIndex > 0

    val canRedo: Boolean
        get() = currentIndex < historyCount - 1

    /**
     * Records a new state change (text, cursor, fonts, bold, italic, underline, strikethrough, subscript, superscript, code, quote, headings, active modes, background, floating images).
     */
    fun record(
        body: TextFieldValue,
        heading: TextFieldValue,
        fontSpans: List<FontSpan> = emptyList(),
        activeFontId: String? = null,
        boldSpans: List<FormatSpan> = emptyList(),
        italicSpans: List<FormatSpan> = emptyList(),
        underlineSpans: List<FormatSpan> = emptyList(),
        strikethroughSpans: List<FormatSpan> = emptyList(),
        subscriptSpans: List<FormatSpan> = emptyList(),
        superscriptSpans: List<FormatSpan> = emptyList(),
        codeSpans: List<FormatSpan> = emptyList(),
        quoteSpans: List<FormatSpan> = emptyList(),
        headingSpans: List<HeadingSpan> = emptyList(),
        isBoldActive: Boolean = false,
        isItalicActive: Boolean = false,
        isUnderlineActive: Boolean = false,
        isStrikethroughActive: Boolean = false,
        isSubscriptActive: Boolean = false,
        isSuperscriptActive: Boolean = false,
        isCodeActive: Boolean = false,
        isQuoteActive: Boolean = false,
        activeHeadingLevel: Int? = null,
        bgImageUri: String? = null,
        bgOpacity: Float = 0.35f,
        bgFitMode: String = "cover",
        floatingImages: List<FloatingCanvasImage> = emptyList(),
    ) {
        if (isApplyingSnapshot) return

        val current = history.getOrNull(currentIndex)
        if (current != null &&
            current.body.text == body.text &&
            current.heading.text == heading.text &&
            current.fontSpans == fontSpans &&
            current.boldSpans == boldSpans &&
            current.italicSpans == italicSpans &&
            current.underlineSpans == underlineSpans &&
            current.strikethroughSpans == strikethroughSpans &&
            current.subscriptSpans == subscriptSpans &&
            current.superscriptSpans == superscriptSpans &&
            current.codeSpans == codeSpans &&
            current.quoteSpans == quoteSpans &&
            current.headingSpans == headingSpans &&
            current.bgImageUri == bgImageUri &&
            current.bgOpacity == bgOpacity &&
            current.bgFitMode == bgFitMode &&
            current.floatingImages == floatingImages
        ) {
            // Only the cursor / selection / typing-mode toggles changed.
            // Refresh the current snapshot instead of adding a new undo step,
            // so Undo goes back through real edits, not cursor taps.
            history[currentIndex] = current.copy(
                body = body,
                heading = heading,
                activeFontId = activeFontId,
                isBoldActive = isBoldActive,
                isItalicActive = isItalicActive,
                isUnderlineActive = isUnderlineActive,
                isStrikethroughActive = isStrikethroughActive,
                isSubscriptActive = isSubscriptActive,
                isSuperscriptActive = isSuperscriptActive,
                isCodeActive = isCodeActive,
                isQuoteActive = isQuoteActive,
                activeHeadingLevel = activeHeadingLevel,
            )
            return
        }

        // If user undid some steps and then made a new change, drop subsequent redo steps
        if (currentIndex < history.size - 1) {
            val toRemove = history.size - 1 - currentIndex
            repeat(toRemove) {
                history.removeAt(history.size - 1)
            }
        }

        history.add(
            CanvasTextState(
                body = body,
                heading = heading,
                fontSpans = fontSpans,
                activeFontId = activeFontId,
                boldSpans = boldSpans,
                italicSpans = italicSpans,
                underlineSpans = underlineSpans,
                strikethroughSpans = strikethroughSpans,
                subscriptSpans = subscriptSpans,
                superscriptSpans = superscriptSpans,
                codeSpans = codeSpans,
                quoteSpans = quoteSpans,
                headingSpans = headingSpans,
                isBoldActive = isBoldActive,
                isItalicActive = isItalicActive,
                isUnderlineActive = isUnderlineActive,
                isStrikethroughActive = isStrikethroughActive,
                isSubscriptActive = isSubscriptActive,
                isSuperscriptActive = isSuperscriptActive,
                isCodeActive = isCodeActive,
                isQuoteActive = isQuoteActive,
                activeHeadingLevel = activeHeadingLevel,
                bgImageUri = bgImageUri,
                bgOpacity = bgOpacity,
                bgFitMode = bgFitMode,
                floatingImages = floatingImages,
            )
        )
        if (history.size > maxHistory) {
            history.removeAt(0)
        } else {
            currentIndex++
        }
        // keeps canUndo / canRedo in sync (Redo stayed disabled without this)
        historyCount = history.size
    }

    /**
     * Reverts to the previous snapshot, restoring text, cursor, and all styles.
     */
    fun undo(): CanvasTextState? {
        if (!canUndo) return null
        isApplyingSnapshot = true
        currentIndex--
        val snapshot = history[currentIndex]
        isApplyingSnapshot = false
        return snapshot
    }

    /**
     * Advances to the next snapshot, restoring text, cursor, and all styles.
     */
    fun redo(): CanvasTextState? {
        if (!canRedo) return null
        isApplyingSnapshot = true
        currentIndex++
        val snapshot = history[currentIndex]
        isApplyingSnapshot = false
        return snapshot
    }

    /**
     * Resets history to a new initial snapshot.
     */
    fun reset(
        body: TextFieldValue,
        heading: TextFieldValue,
        fontSpans: List<FontSpan> = emptyList(),
        activeFontId: String? = null,
        boldSpans: List<FormatSpan> = emptyList(),
        italicSpans: List<FormatSpan> = emptyList(),
        underlineSpans: List<FormatSpan> = emptyList(),
        strikethroughSpans: List<FormatSpan> = emptyList(),
        headingSpans: List<HeadingSpan> = emptyList(),
        isBoldActive: Boolean = false,
        isItalicActive: Boolean = false,
        isUnderlineActive: Boolean = false,
        isStrikethroughActive: Boolean = false,
        activeHeadingLevel: Int? = null,
    ) {
        history.clear()
        history.add(
            CanvasTextState(
                body = body,
                heading = heading,
                fontSpans = fontSpans,
                activeFontId = activeFontId,
                boldSpans = boldSpans,
                italicSpans = italicSpans,
                underlineSpans = underlineSpans,
                strikethroughSpans = strikethroughSpans,
                headingSpans = headingSpans,
                isBoldActive = isBoldActive,
                isItalicActive = isItalicActive,
                isUnderlineActive = isUnderlineActive,
                isStrikethroughActive = isStrikethroughActive,
                activeHeadingLevel = activeHeadingLevel,
            )
        )
        currentIndex = 0
        historyCount = 1
    }
}

package com.likkho.nativeui.util

import com.likkho.nativeui.model.HeadingSpan

object HeadingSpanManager {

    /**
     * Finds the start and end indices of the line(s) covering [selectionMin, selectionMax].
     */
    fun findLineRange(text: String, selectionMin: Int, selectionMax: Int): Pair<Int, Int> {
        val len = text.length
        if (len == 0) return Pair(0, 0)
        val clampedMin = selectionMin.coerceIn(0, len)
        val clampedMax = selectionMax.coerceIn(clampedMin, len)

        val start = if (clampedMin == 0) {
            0
        } else {
            val idx = text.lastIndexOf('\n', clampedMin - 1)
            if (idx == -1) 0 else idx + 1
        }

        val searchFrom = if (clampedMax > clampedMin && clampedMax > 0 && clampedMax <= len && text[clampedMax - 1] == '\n') {
            clampedMax - 1
        } else {
            clampedMax
        }
        val end = text.indexOf('\n', searchFrom).let {
            if (it == -1) len else it
        }

        return Pair(start, end)
    }

    /**
     * Checks if the selection range [min, max] is fully covered by a heading of [level].
     */
    fun isRangeCoveredByLevel(
        spans: List<HeadingSpan>,
        min: Int,
        max: Int,
        level: Int,
    ): Boolean {
        if (min >= max) return false
        return spans.any { it.level == level && it.start <= min && it.end >= max }
    }

    /**
     * Gets the heading level (1, 2, 3) at the cursor position.
     */
    fun getHeadingAtCursor(spans: List<HeadingSpan>, cursor: Int): Int? {
        if (cursor > 0) {
            val span = spans.firstOrNull { it.start <= cursor - 1 && it.end >= cursor }
            if (span != null) return span.level
        }
        return spans.firstOrNull { it.start <= cursor && it.end > cursor }?.level
    }

    /**
     * Toggles a heading level on the selected text range [start, end].
     * If the range is already covered by [targetLevel], it is removed (toggled off).
     * Otherwise, any existing heading on [start, end] is replaced with [targetLevel].
     */
    fun toggleHeadingRange(
        spans: List<HeadingSpan>,
        start: Int,
        end: Int,
        targetLevel: Int,
    ): List<HeadingSpan> {
        if (start >= end) return spans

        val isAlreadyTarget = isRangeCoveredByLevel(spans, start, end, targetLevel)

        val remaining = mutableListOf<HeadingSpan>()
        for (span in spans) {
            if (span.end <= start || span.start >= end) {
                // No overlap
                remaining.add(span)
            } else {
                // Overlap: slice parts outside [start, end]
                if (span.start < start) {
                    remaining.add(HeadingSpan(span.level, span.start, start))
                }
                if (span.end > end) {
                    remaining.add(HeadingSpan(span.level, end, span.end))
                }
            }
        }

        if (!isAlreadyTarget) {
            remaining.add(HeadingSpan(targetLevel, start, end))
        }

        return mergeContiguousSpans(remaining)
    }

    /**
     * Gets the heading level active on the line containing the selection.
     */
    fun getActiveHeadingLevel(
        spans: List<HeadingSpan>,
        text: String,
        selectionMin: Int,
        selectionMax: Int,
    ): Int? {
        val (lineStart, lineEnd) = findLineRange(text, selectionMin, selectionMax)
        return spans.firstOrNull { it.start <= lineStart && it.end >= lineEnd }?.level
            ?: spans.firstOrNull { maxOf(it.start, lineStart) < minOf(it.end, lineEnd) }?.level
    }

    /**
     * Toggles a heading level on the entire line containing the selection.
     */
    fun toggleHeading(
        spans: List<HeadingSpan>,
        text: String,
        selectionMin: Int,
        selectionMax: Int,
        targetLevel: Int,
    ): List<HeadingSpan> {
        val (lineStart, lineEnd) = findLineRange(text, selectionMin, selectionMax)
        if (lineStart >= lineEnd) {
            val existing = spans.firstOrNull { it.start <= lineStart && it.end >= lineEnd }
            return if (existing != null && existing.level == targetLevel) {
                spans.filter { it != existing }
            } else {
                spans.filter { it.start > lineEnd || it.end < lineStart } + HeadingSpan(targetLevel, lineStart, lineEnd)
            }
        }

        return toggleHeadingRange(spans, lineStart, lineEnd, targetLevel)
    }

    /**
     * Updates heading spans when text changes (typing, backspacing, pressing Enter).
     * If [activeHeadingLevel] is set, newly inserted characters inherit that heading level.
     */
    fun updateSpansOnTextEdit(
        spans: List<HeadingSpan>,
        oldText: String,
        newText: String,
        editIndex: Int,
        activeHeadingLevel: Int? = null,
    ): List<HeadingSpan> {
        val oldLen = oldText.length
        val newLen = newText.length
        val delta = newLen - oldLen
        if (delta == 0) return spans

        val result = mutableListOf<HeadingSpan>()

        if (delta > 0) {
            // Insertion
            val insertedStart = editIndex - delta
            val insertedEnd = editIndex
            val insertedChar = newText.getOrNull(insertedStart)
            val isEnter = insertedChar == '\n'

            for (span in spans) {
                when {
                    span.end < insertedStart -> {
                        result.add(span)
                    }
                    span.start >= insertedStart -> {
                        result.add(HeadingSpan(span.level, span.start + delta, span.end + delta))
                    }
                    else -> {
                        if (isEnter) {
                            // User pressed Enter: heading ends at the newline
                            result.add(HeadingSpan(span.level, span.start, insertedStart))
                        } else {
                            // Regular typing within heading: expand span
                            result.add(HeadingSpan(span.level, span.start, span.end + delta))
                        }
                    }
                }
            }

            // If a heading is active for typing, and we did not insert a newline
            if (activeHeadingLevel != null && !isEnter) {
                val existingIdx = result.indexOfFirst { it.level == activeHeadingLevel && it.end == insertedStart }
                if (existingIdx >= 0) {
                    val ex = result[existingIdx]
                    result[existingIdx] = HeadingSpan(ex.level, ex.start, insertedEnd)
                } else {
                    val alreadyInside = result.any { it.level == activeHeadingLevel && it.start <= insertedStart && it.end >= insertedEnd }
                    if (!alreadyInside) {
                        result.add(HeadingSpan(activeHeadingLevel, insertedStart, insertedEnd))
                    }
                }
            }
        } else {
            // Deletion
            val deleteStart = editIndex
            val deleteEnd = editIndex - delta

            for (span in spans) {
                when {
                    span.end <= deleteStart -> {
                        result.add(span)
                    }
                    span.start >= deleteEnd -> {
                        result.add(HeadingSpan(span.level, span.start + delta, span.end + delta))
                    }
                    span.start >= deleteStart && span.end <= deleteEnd -> {
                        // Completely deleted
                    }
                    span.start < deleteStart && span.end > deleteEnd -> {
                        val newEnd = span.end + delta
                        if (newEnd > span.start) {
                            result.add(HeadingSpan(span.level, span.start, newEnd))
                        }
                    }
                    span.start < deleteStart && span.end <= deleteEnd -> {
                        val newEnd = deleteStart
                        if (newEnd > span.start) {
                            result.add(HeadingSpan(span.level, span.start, newEnd))
                        }
                    }
                    else -> {
                        val newStart = deleteStart
                        val newEnd = span.end + delta
                        if (newEnd > newStart) {
                            result.add(HeadingSpan(span.level, newStart, newEnd))
                        }
                    }
                }
            }
        }

        return mergeContiguousSpans(result)
    }

    private fun mergeContiguousSpans(spans: List<HeadingSpan>): List<HeadingSpan> {
        val valid = spans.filter { it.end > it.start }.sortedBy { it.start }
        if (valid.size <= 1) return valid
        val merged = mutableListOf<HeadingSpan>()
        var curr = valid[0]
        for (i in 1 until valid.size) {
            val next = valid[i]
            if (next.level == curr.level && next.start <= curr.end) {
                curr = HeadingSpan(curr.level, curr.start, maxOf(curr.end, next.end))
            } else {
                merged.add(curr)
                curr = next
            }
        }
        merged.add(curr)
        return merged
    }
}

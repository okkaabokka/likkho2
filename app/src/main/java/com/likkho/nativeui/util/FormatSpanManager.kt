package com.likkho.nativeui.util

import com.likkho.nativeui.model.FormatSpan

object FormatSpanManager {

    /**
     * Checks if a given range [start, end] is completely covered by spans.
     */
    fun isRangeFullyCovered(spans: List<FormatSpan>, start: Int, end: Int): Boolean {
        if (start >= end) return false
        var current = start
        val relevant = spans.filter { it.end > start && it.start < end }.sortedBy { it.start }
        for (span in relevant) {
            if (span.start > current) return false
            if (span.end >= end) return true
            current = maxOf(current, span.end)
        }
        return current >= end
    }

    /**
     * Checks if a cursor position is currently covered by a span.
     */
    fun isCursorCovered(spans: List<FormatSpan>, cursor: Int): Boolean {
        if (spans.isEmpty()) return false
        return spans.any { span ->
            if (cursor == 0) span.start == 0 && span.end > 0
            else (cursor in (span.start + 1)..span.end) || (cursor == span.start && span.end > span.start)
        }
    }

    /**
     * Removes formatting from a specific range [start, end].
     */
    fun removeRange(spans: List<FormatSpan>, start: Int, end: Int): List<FormatSpan> {
        if (start >= end || spans.isEmpty()) return spans
        val result = mutableListOf<FormatSpan>()
        for (span in spans) {
            if (span.end <= start || span.start >= end) {
                result.add(span)
            } else {
                if (span.start < start) {
                    result.add(FormatSpan(span.start, start))
                }
                if (span.end > end) {
                    result.add(FormatSpan(end, span.end))
                }
            }
        }
        return result.filter { it.end > it.start }.sortedBy { it.start }
    }

    /**
     * Toggles formatting on a selected range [start, end].
     * If the range is already fully covered, removes it (toggles off).
     * Otherwise, applies formatting to the range.
     */
    fun toggleRange(spans: List<FormatSpan>, start: Int, end: Int): List<FormatSpan> {
        if (start >= end) return spans

        if (isRangeFullyCovered(spans, start, end)) {
            return removeRange(spans, start, end)
        } else {
            // Add [start, end] and merge
            val all = (spans + FormatSpan(start, end)).sortedBy { it.start }
            val merged = mutableListOf<FormatSpan>()
            for (s in all) {
                if (merged.isEmpty()) {
                    merged.add(s)
                } else {
                    val last = merged.last()
                    if (s.start <= last.end) {
                        merged[merged.size - 1] = FormatSpan(last.start, maxOf(last.end, s.end))
                    } else {
                        merged.add(s)
                    }
                }
            }
            return merged
        }
    }

    /**
     * Updates spans when text is inserted or deleted.
     */
    fun updateSpansOnTextEdit(
        spans: List<FormatSpan>,
        oldTextLength: Int,
        newTextLength: Int,
        editIndex: Int,
        isEnabledForTyping: Boolean,
    ): List<FormatSpan> {
        val delta = newTextLength - oldTextLength
        if (delta == 0) return spans

        val updated = mutableListOf<FormatSpan>()

        if (delta > 0) {
            // Insertion of `delta` characters
            val insertStart = editIndex - delta
            val insertEnd = editIndex

            for (span in spans) {
                when {
                    span.end <= insertStart -> {
                        updated.add(span)
                    }
                    span.start >= insertStart -> {
                        updated.add(FormatSpan(span.start + delta, span.end + delta))
                    }
                    else -> {
                        // Insert happened inside the span
                        if (isEnabledForTyping) {
                            updated.add(FormatSpan(span.start, span.end + delta))
                        } else {
                            // Split span around insert
                            updated.add(FormatSpan(span.start, insertStart))
                            updated.add(FormatSpan(insertEnd, span.end + delta))
                        }
                    }
                }
            }

            if (isEnabledForTyping) {
                val existingIndex = updated.indexOfFirst { it.end == insertStart }
                if (existingIndex >= 0) {
                    updated[existingIndex] = FormatSpan(updated[existingIndex].start, insertEnd)
                } else {
                    updated.add(FormatSpan(insertStart, insertEnd))
                }
            }
        } else {
            // Deletion of characters
            val deleteStart = editIndex
            val deleteEnd = editIndex - delta

            for (span in spans) {
                when {
                    span.end <= deleteStart -> {
                        updated.add(span)
                    }
                    span.start >= deleteEnd -> {
                        updated.add(FormatSpan(span.start + delta, span.end + delta))
                    }
                    else -> {
                        val newStart = if (span.start < deleteStart) span.start else deleteStart
                        val newEnd = if (span.end > deleteEnd) span.end + delta else deleteStart
                        if (newEnd > newStart) {
                            updated.add(FormatSpan(newStart, newEnd))
                        }
                    }
                }
            }
        }

        // Clean up and merge
        val cleaned = updated.filter { it.end > it.start }.sortedBy { it.start }
        val merged = mutableListOf<FormatSpan>()
        for (s in cleaned) {
            if (merged.isEmpty()) {
                merged.add(s)
            } else {
                val last = merged.last()
                if (s.start <= last.end) {
                    merged[merged.size - 1] = FormatSpan(last.start, maxOf(last.end, s.end))
                } else {
                    merged.add(s)
                }
            }
        }
        return merged
    }
}

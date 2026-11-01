package com.likkho.nativeui.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

object ListAndFormatUtils {

    /**
     * Toggles Numbered List on current line or all lines spanning selection.
     */
    fun toggleNumberedList(value: TextFieldValue): TextFieldValue {
        val text = value.text
        val sel = value.selection
        val (lineStart, lineEnd) = getSelectedLineSpan(text, sel.min, sel.max)
        val selectedBlock = text.substring(lineStart, lineEnd)
        val lines = selectedBlock.split('\n')

        val numberRegex = Regex("""^\s*\d+\.\s+""")
        val allNumbered = lines.all { it.isBlank() || numberRegex.containsMatchIn(it) }

        val newLines = if (allNumbered) {
            lines.map { numberRegex.replace(it, "") }
        } else {
            var num = 1
            lines.map { line ->
                if (line.isBlank()) line
                else {
                    val clean = stripAnyListPrefix(line)
                    "${num++}. $clean"
                }
            }
        }

        val replacement = newLines.joinToString("\n")
        val newText = text.substring(0, lineStart) + replacement + text.substring(lineEnd)
        val newEnd = lineStart + replacement.length
        return TextFieldValue(text = newText, selection = TextRange(newEnd))
    }

    /**
     * Toggles Bullet List on current line or all lines spanning selection.
     */
    fun toggleBulletList(value: TextFieldValue): TextFieldValue {
        val text = value.text
        val sel = value.selection
        val (lineStart, lineEnd) = getSelectedLineSpan(text, sel.min, sel.max)
        val selectedBlock = text.substring(lineStart, lineEnd)
        val lines = selectedBlock.split('\n')

        val bulletRegex = Regex("""^\s*•\s+""")
        val allBulleted = lines.all { it.isBlank() || bulletRegex.containsMatchIn(it) }

        val newLines = if (allBulleted) {
            lines.map { bulletRegex.replace(it, "") }
        } else {
            lines.map { line ->
                if (line.isBlank()) line
                else {
                    val clean = stripAnyListPrefix(line)
                    "• $clean"
                }
            }
        }

        val replacement = newLines.joinToString("\n")
        val newText = text.substring(0, lineStart) + replacement + text.substring(lineEnd)
        val newEnd = lineStart + replacement.length
        return TextFieldValue(text = newText, selection = TextRange(newEnd))
    }

    /**
     * Toggles To-Do Checkbox on current line or all lines spanning selection using box symbol ☐.
     */
    fun toggleTodoCheckbox(value: TextFieldValue): TextFieldValue {
        val text = value.text
        val sel = value.selection
        val (lineStart, lineEnd) = getSelectedLineSpan(text, sel.min, sel.max)
        val selectedBlock = text.substring(lineStart, lineEnd)
        val lines = selectedBlock.split('\n')

        val todoRegex = Regex("""^\s*([☐☑]|\[[ xX]\])\s+""")
        val allTodo = lines.all { it.isBlank() || todoRegex.containsMatchIn(it) }

        val newLines = if (allTodo) {
            lines.map { todoRegex.replace(it, "") }
        } else {
            lines.map { line ->
                if (line.isBlank()) line
                else {
                    val clean = stripAnyListPrefix(line)
                    "☐ $clean"
                }
            }
        }

        val replacement = newLines.joinToString("\n")
        val newText = text.substring(0, lineStart) + replacement + text.substring(lineEnd)
        val newEnd = lineStart + replacement.length
        return TextFieldValue(text = newText, selection = TextRange(newEnd))
    }

    /**
     * Toggles Quotation (Blockquote) on current line or all lines spanning selection.
     */
    fun toggleQuotation(value: TextFieldValue): TextFieldValue {
        val text = value.text
        val sel = value.selection
        val (lineStart, lineEnd) = getSelectedLineSpan(text, sel.min, sel.max)
        val selectedBlock = text.substring(lineStart, lineEnd)
        val lines = selectedBlock.split('\n')

        val quoteRegex = Regex("""^\s*>\s+""")
        val allQuoted = lines.all { it.isBlank() || quoteRegex.containsMatchIn(it) }

        val newLines = if (allQuoted) {
            lines.map { quoteRegex.replace(it, "") }
        } else {
            lines.map { line ->
                if (line.isBlank()) line
                else {
                    val clean = stripAnyListPrefix(line)
                    "> $clean"
                }
            }
        }

        val replacement = newLines.joinToString("\n")
        val newText = text.substring(0, lineStart) + replacement + text.substring(lineEnd)
        val newEnd = lineStart + replacement.length
        return TextFieldValue(text = newText, selection = TextRange(newEnd))
    }

    /**
     * Auto-continues or terminates lists / todos / quotes when Enter is pressed.
     */
    fun handleEnterKey(
        oldText: String,
        newText: String,
        editIndex: Int,
    ): TextFieldValue? {
        val delta = newText.length - oldText.length
        if (delta != 1 || editIndex <= 0) return null
        val insertPos = editIndex - 1
        if (newText.getOrNull(insertPos) != '\n') return null

        val prevLine = oldText.substring(0, insertPos).substringAfterLast('\n')

        // 1. Numbered List
        val numMatch = Regex("""^\s*(\d+)\.\s*(.*)$""").find(prevLine)
        if (numMatch != null) {
            val num = numMatch.groupValues[1].toIntOrNull() ?: 1
            val content = numMatch.groupValues[2].trim()
            return if (content.isEmpty()) {
                // User pressed enter on an empty item -> exit list
                val lineStart = insertPos - prevLine.length
                val textWithoutPrefix = newText.substring(0, lineStart) + "\n" + newText.substring(editIndex)
                TextFieldValue(textWithoutPrefix, selection = TextRange(lineStart + 1))
            } else {
                // Auto-continue numbering
                val nextPrefix = "${num + 1}. "
                val autoText = newText.substring(0, editIndex) + nextPrefix + newText.substring(editIndex)
                TextFieldValue(autoText, selection = TextRange(editIndex + nextPrefix.length))
            }
        }

        // 2. Bullet List
        val bulletMatch = Regex("""^\s*•\s*(.*)$""").find(prevLine)
        if (bulletMatch != null) {
            val content = bulletMatch.groupValues[1].trim()
            return if (content.isEmpty()) {
                val lineStart = insertPos - prevLine.length
                val textWithoutPrefix = newText.substring(0, lineStart) + "\n" + newText.substring(editIndex)
                TextFieldValue(textWithoutPrefix, selection = TextRange(lineStart + 1))
            } else {
                val nextPrefix = "• "
                val autoText = newText.substring(0, editIndex) + nextPrefix + newText.substring(editIndex)
                TextFieldValue(autoText, selection = TextRange(editIndex + nextPrefix.length))
            }
        }

        // 3. To-Do Checkbox (☐ and ☑ or legacy brackets)
        val todoMatch = Regex("""^\s*([☐☑]|\[[ xX]\])\s*(.*)$""").find(prevLine)
        if (todoMatch != null) {
            val content = todoMatch.groupValues[2].trim()
            return if (content.isEmpty()) {
                val lineStart = insertPos - prevLine.length
                val textWithoutPrefix = newText.substring(0, lineStart) + "\n" + newText.substring(editIndex)
                TextFieldValue(textWithoutPrefix, selection = TextRange(lineStart + 1))
            } else {
                val nextPrefix = "☐ "
                val autoText = newText.substring(0, editIndex) + nextPrefix + newText.substring(editIndex)
                TextFieldValue(autoText, selection = TextRange(editIndex + nextPrefix.length))
            }
        }

        return null
    }

    /**
     * Toggles a checkbox on a specific line index (used for interactive to-do tapping in Reading Mode & Edit Mode).
     */
    fun toggleCheckboxOnLine(fullText: String, lineCharIndex: Int): String {
        val lineStart = if (lineCharIndex <= 0) 0 else fullText.lastIndexOf('\n', lineCharIndex - 1) + 1
        val lineEnd = fullText.indexOf('\n', lineCharIndex).let { if (it == -1) fullText.length else it }
        val line = fullText.substring(lineStart, lineEnd)

        val updatedLine = when {
            line.contains("☐") -> line.replaceFirst("☐", "☑")
            line.contains("☑") -> line.replaceFirst("☑", "☐")
            line.contains("[ ]") -> line.replaceFirst("[ ]", "☑")
            line.contains("[x]") -> line.replaceFirst("[x]", "☐")
            line.contains("[X]") -> line.replaceFirst("[X]", "☐")
            else -> line
        }

        return fullText.substring(0, lineStart) + updatedLine + fullText.substring(lineEnd)
    }

    fun getSelectedLineSpan(text: String, start: Int, end: Int): Pair<Int, Int> {
        val s = start.coerceIn(0, text.length)
        val e = end.coerceIn(s, text.length)
        val lineStart = if (s == 0) 0 else text.lastIndexOf('\n', s - 1) + 1
        val lineEnd = if (e >= text.length) text.length else {
            val nextNewline = text.indexOf('\n', e)
            if (nextNewline == -1) text.length else nextNewline
        }
        return Pair(lineStart, lineEnd)
    }

    fun stripAnyListPrefix(line: String): String {
        return line
            .replaceFirst(Regex("""^\s*\d+\.\s+"""), "")
            .replaceFirst(Regex("""^\s*•\s+"""), "")
            .replaceFirst(Regex("""^\s*([☐☑]|\[[ xX]\])\s+"""), "")
            .replaceFirst(Regex("""^\s*>\s+"""), "")
    }
}

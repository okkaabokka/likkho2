package com.likkho.nativeui.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Typeface
import com.likkho.nativeui.model.FontItem
import com.likkho.nativeui.model.FontSpan
import com.likkho.nativeui.ui.theme.WikiFonts
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object CustomFontManager {

    private const val PREFS_NAME = "likkho_custom_fonts_prefs"
    private const val KEY_CUSTOM_FONTS = "custom_fonts_list"

    val customFonts = mutableStateListOf<FontItem>()
    private val loadedFamilies = mutableMapOf<String, FontFamily>()

    fun init(context: Context) {
        if (customFonts.isNotEmpty()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_CUSTOM_FONTS, null) ?: return
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                val filePath = obj.getString("filePath")
                val file = File(filePath)
                if (file.exists() && file.length() > 0) {
                    customFonts.add(FontItem(id = id, name = name, filePath = filePath, isBuiltin = false))
                }
            }
        } catch (_: Exception) {}
    }

    fun saveFontFromUri(context: Context, uri: Uri): FontItem? {
        val resolver = context.contentResolver
        var fileName = "CustomFont_${System.currentTimeMillis()}"

        try {
            resolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            }
        } catch (_: Exception) {}

        val cleanName = fileName.substringBeforeLast(".")
        val fontsDir = File(context.filesDir, "custom_fonts").apply { if (!exists()) mkdirs() }
        val ext = if (fileName.contains(".")) fileName.substringAfterLast(".").lowercase() else "ttf"
        val fontId = "font_${System.currentTimeMillis()}_${cleanName.take(12).replace(Regex("[^a-zA-Z0-9]"), "_")}"
        val destFile = File(fontsDir, "$fontId.$ext")

        try {
            resolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (_: Exception) {
            return null
        }

        if (!destFile.exists() || destFile.length() == 0L) {
            return null
        }

        val item = FontItem(id = fontId, name = cleanName, filePath = destFile.absolutePath, isBuiltin = false)
        customFonts.add(item)
        persist(context)
        return item
    }

    private fun persist(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val array = JSONArray()
        for (f in customFonts) {
            val obj = JSONObject().apply {
                put("id", f.id)
                put("name", f.name)
                put("filePath", f.filePath.orEmpty())
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_CUSTOM_FONTS, array.toString()).apply()
    }

    fun resolveFontFamily(fontId: String, wikiFonts: WikiFonts): FontFamily? {
        if (fontId == "builtin_default") return wikiFonts.prose
        if (fontId == "builtin_serif") return wikiFonts.serif
        if (fontId == "builtin_short_stack" || fontId == "builtin_prose") return wikiFonts.shortStack
        if (fontId == "builtin_sans") return wikiFonts.sans
        if (fontId == "builtin_mono") return wikiFonts.mono

        loadedFamilies[fontId]?.let { return it }

        val fontItem = customFonts.firstOrNull { it.id == fontId } ?: return null
        val path = fontItem.filePath ?: return null
        val file = File(path)
        if (!file.exists()) return null

        return try {
            val androidTypeface = android.graphics.Typeface.createFromFile(file)
            val composeFamily = FontFamily(Typeface(androidTypeface))
            loadedFamilies[fontId] = composeFamily
            composeFamily
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Applies a font to selected text range, or toggles it off if the whole range already has this font.
     */
    fun applyFontToRange(
        spans: List<FontSpan>,
        start: Int,
        end: Int,
        targetFontId: String,
    ): List<FontSpan> {
        if (start >= end) return spans

        val isAlreadyFullyTarget = spans.any { it.fontId == targetFontId && it.start <= start && it.end >= end }

        val remaining = mutableListOf<FontSpan>()
        for (span in spans) {
            if (span.end <= start || span.start >= end) {
                // No overlap
                remaining.add(span)
            } else {
                // Overlaps: slice parts outside [start, end]
                if (span.start < start) {
                    remaining.add(span.copy(end = start))
                }
                if (span.end > end) {
                    remaining.add(span.copy(start = end))
                }
            }
        }

        if (!isAlreadyFullyTarget && targetFontId != "builtin_default") {
            remaining.add(FontSpan(start = start, end = end, fontId = targetFontId))
        }

        return remaining.sortedBy { it.start }
    }

    /**
     * Adjusts existing spans when text is typed or deleted.
     */
    fun updateSpansOnTextEdit(
        spans: List<FontSpan>,
        oldTextLength: Int,
        newTextLength: Int,
        editIndex: Int,
        activeFontId: String?,
    ): List<FontSpan> {
        val delta = newTextLength - oldTextLength
        if (delta == 0) return spans

        val updated = mutableListOf<FontSpan>()

        if (delta > 0) {
            // Insertion
            val insertStart = editIndex - delta
            val insertEnd = editIndex

            for (span in spans) {
                when {
                    span.end <= insertStart -> {
                        updated.add(span)
                    }
                    span.start >= insertStart -> {
                        updated.add(span.copy(start = span.start + delta, end = span.end + delta))
                    }
                    else -> {
                        // Insert happened inside the span
                        if (activeFontId == null || activeFontId == span.fontId) {
                            updated.add(span.copy(end = span.end + delta))
                        } else {
                            // Split span around insert
                            updated.add(span.copy(end = insertStart))
                            updated.add(span.copy(start = insertEnd, end = span.end + delta))
                        }
                    }
                }
            }

            if (activeFontId != null && activeFontId != "builtin_default") {
                val existing = updated.indexOfFirst { it.fontId == activeFontId && it.end == insertStart }
                if (existing >= 0) {
                    updated[existing] = updated[existing].copy(end = insertEnd)
                } else {
                    updated.add(FontSpan(start = insertStart, end = insertEnd, fontId = activeFontId))
                }
            }
        } else {
            // Deletion
            val deleteStart = editIndex
            val deleteEnd = editIndex - delta // delta is negative

            for (span in spans) {
                when {
                    span.end <= deleteStart -> {
                        updated.add(span)
                    }
                    span.start >= deleteEnd -> {
                        updated.add(span.copy(start = span.start + delta, end = span.end + delta))
                    }
                    else -> {
                        // Deletion overlaps span
                        val newStart = if (span.start < deleteStart) span.start else deleteStart
                        val newEnd = if (span.end > deleteEnd) span.end + delta else deleteStart
                        if (newEnd > newStart) {
                            updated.add(span.copy(start = newStart, end = newEnd))
                        }
                    }
                }
            }
        }

        return updated.filter { it.end > it.start }.sortedBy { it.start }
    }
}

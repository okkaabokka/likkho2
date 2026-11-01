package com.likkho.nativeui.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object CanvasBackgroundManager {

    fun saveBackgroundFromUri(context: Context, uri: Uri): String? {
        return try {
            val bgDir = File(context.filesDir, "canvas_bg")
            if (!bgDir.exists()) bgDir.mkdirs()

            val targetFile = File(bgDir, "bg_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

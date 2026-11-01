package com.likkho.nativeui.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object CanvasImageManager {

    fun saveImageFromUri(context: Context, uri: Uri): String? {
        return try {
            val imgDir = File(context.filesDir, "canvas_images")
            if (!imgDir.exists()) imgDir.mkdirs()

            val targetFile = File(imgDir, "img_${System.currentTimeMillis()}_${(1000..9999).random()}.jpg")
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

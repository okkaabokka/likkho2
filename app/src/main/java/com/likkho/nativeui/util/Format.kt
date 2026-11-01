package com.likkho.nativeui.util

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Fmt {
    private val locale = Locale("en", "IN")

    fun date(ms: Long): String = SimpleDateFormat("dd MMM yyyy", locale).format(Date(ms))

    fun time(ms: Long): String = SimpleDateFormat("hh:mm a", locale).format(Date(ms))

    fun dateTime(ms: Long): String = SimpleDateFormat("dd MMM, hh:mm a", locale).format(Date(ms))
}

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

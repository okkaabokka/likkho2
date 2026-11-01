package com.likkho.nativeui.model

import androidx.compose.ui.graphics.Color

/** Demo entries so every list-row variant (pinned, locked, reminder) is visible. */
object SampleData {
    fun starterLogs(): List<DiaryLog> {
        val now = System.currentTimeMillis()
        val min = 60_000L
        return listOf(
            DiaryLog(
                id = "log_starter_guide",
                heading = "Likkho — Official Guide",
                body = "Welcome to Likkho. This is a native UI preview: tap Create to write a new entry, " +
                    "use the 3-dot menu to pin, share, export or delete, and open Settings from the menu button.\n\n" +
                    "लिखो में आपका स्वागत है। नई प्रविष्टि लिखने के लिए Create दबाइए।",
                createdAt = now - 15 * min,
                updatedAt = now - 15 * min,
                pinned = true,
                avatar = Avatar(
                    label = "लिखो",
                    labelSp = 13,
                    sub = "LIKKHO",
                    bg = Color(0xFF1E293B),
                    fg = Color.White,
                    subColor = Color(0xFF93C5FD),
                    frame = Color(0xFF60A5FA),
                ),
            ),
            DiaryLog(
                id = "log_morning_pages",
                heading = "Morning Pages",
                body = "Woke up early today. The air smelled like rain and the city was quiet.\n\n" +
                    "Three things I want to finish this week: the draft, the garden, the call.",
                createdAt = now - 26 * 60 * min,
                updatedAt = now - 3 * 60 * min,
                reminderAt = now + 2 * 60 * min,
                avatar = Avatar.monogram("Morning Pages"),
            ),
            DiaryLog(
                id = "log_private",
                heading = "Private Notes",
                body = "This diary is locked. Demo passcode: 1234",
                createdAt = now - 3 * 24 * 60 * min,
                updatedAt = now - 2 * 24 * 60 * min,
                lockPin = "1234",
                avatar = Avatar(
                    label = "P",
                    labelSp = 24,
                    bg = Color(0xFFFDECEC),
                    fg = Color(0xFFB32424),
                ),
            ),
            DiaryLog(
                id = "log_reading_list",
                heading = "Reading List",
                body = "1. The Origin of Species\n2. Godaan\n3. Sapiens\n4. A short history of nearly everything",
                createdAt = now - 9 * 24 * 60 * min,
                updatedAt = now - 9 * 24 * 60 * min,
                avatar = Avatar.monogram("Reading List"),
            ),
        )
    }
}

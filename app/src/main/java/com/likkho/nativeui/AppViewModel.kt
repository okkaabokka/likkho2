package com.likkho.nativeui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.likkho.nativeui.model.DiaryLog
import com.likkho.nativeui.model.SampleData

sealed interface Route {
    data object Home : Route
    data class Workspace(val logId: String?) : Route
    data object Settings : Route
    data object Backup : Route
    data object Legal : Route
}

data class MicSettings(
    val format: String = "wav",
    val sampleRate: Int = 44100,
    val bitRate: Int = 128000,
)

/**
 * UI state only. Logs live in memory (sample data); storage, encryption,
 * backup etc. are meant to be plugged in here later.
 */
class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("likkho_ui", Context.MODE_PRIVATE)

    var darkMode by mutableStateOf(prefs.getBoolean("dark", false))
        private set
    var fullScreen by mutableStateOf(prefs.getBoolean("fullscreen", false))
        private set

    var biometrics by mutableStateOf(false)
    var passcode by mutableStateOf<String?>(null)
        private set
    var isLocked by mutableStateOf(false)
        private set

    var micSettings by mutableStateOf(MicSettings())

    var route by mutableStateOf<Route>(Route.Home)
        private set

    val logs = mutableStateListOf<DiaryLog>().apply { addAll(SampleData.starterLogs()) }

    // ---- navigation -------------------------------------------------
    fun navigate(target: Route) {
        route = target
    }

    fun back() {
        route = when (route) {
            Route.Backup, Route.Legal -> Route.Settings
            else -> Route.Home
        }
    }

    // ---- preferences ------------------------------------------------
    fun toggleDark() {
        darkMode = !darkMode
        prefs.edit().putBoolean("dark", darkMode).apply()
    }

    fun toggleFullScreen() {
        fullScreen = !fullScreen
        prefs.edit().putBoolean("fullscreen", fullScreen).apply()
    }

    // ---- lock -------------------------------------------------------
    fun updatePasscode(pin: String?) {
        passcode = pin
        if (pin == null) {
            biometrics = false
            isLocked = false
        }
    }

    fun lock() {
        if (passcode != null) isLocked = true
    }

    fun unlock() {
        isLocked = false
    }

    // ---- logs -------------------------------------------------------
    fun upsert(log: DiaryLog) {
        val i = logs.indexOfFirst { it.id == log.id }
        if (i >= 0) logs[i] = log else logs.add(0, log)
    }

    fun delete(id: String) {
        logs.removeAll { it.id == id }
    }

    fun togglePin(id: String) {
        val i = logs.indexOfFirst { it.id == id }
        if (i >= 0) logs[i] = logs[i].copy(pinned = !logs[i].pinned)
    }

    fun clearAll() {
        logs.clear()
        micSettings = MicSettings()
        updatePasscode(null)
    }
}

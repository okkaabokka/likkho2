package com.likkho.nativeui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.FullscreenExit
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.likkho.nativeui.AppViewModel
import com.likkho.nativeui.MicSettings
import com.likkho.nativeui.Route
import com.likkho.nativeui.ui.components.FieldLabel
import com.likkho.nativeui.ui.components.HairlineDivider
import com.likkho.nativeui.ui.components.OptionGrid
import com.likkho.nativeui.ui.components.ScreenHeader
import com.likkho.nativeui.ui.components.SectionCard
import com.likkho.nativeui.ui.components.SettingRow
import com.likkho.nativeui.ui.components.StatusBanner
import com.likkho.nativeui.ui.components.WikiButton
import com.likkho.nativeui.ui.components.WikiButtonStyle
import com.likkho.nativeui.ui.components.WikiDialog
import com.likkho.nativeui.ui.components.WikiField
import com.likkho.nativeui.ui.components.WikiSwitch
import com.likkho.nativeui.ui.theme.Wiki
import com.likkho.nativeui.util.toast

@Composable
fun SettingsScreen(vm: AppViewModel) {
    val c = Wiki.colors
    val ctx = LocalContext.current
    var showPasscode by remember { mutableStateOf(false) }
    var showMic by remember { mutableStateOf(false) }
    val mic = vm.micSettings

    Column(
        Modifier
            .fillMaxSize()
            .background(c.bg)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        ScreenHeader("Settings", onBack = vm::back)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
        ) {
            SectionCard {
                SettingRow(
                    icon = if (vm.darkMode) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                    iconTint = if (vm.darkMode) c.link else c.sun,
                    title = "Dark Theme",
                    subtitle = if (vm.darkMode) "Dark Mode" else "Light Mode",
                    onClick = { vm.toggleDark() },
                ) { WikiSwitch(vm.darkMode) }
                HairlineDivider()

                SettingRow(
                    icon = if (vm.fullScreen) Icons.Outlined.FullscreenExit else Icons.Outlined.Fullscreen,
                    iconTint = c.primary,
                    title = "Full Screen Mode",
                    subtitle = if (vm.fullScreen) "Immersive full screen active" else "Hide status & navigation bars",
                    onClick = { vm.toggleFullScreen() },
                ) { WikiSwitch(vm.fullScreen) }
                HairlineDivider()

                SettingRow(
                    icon = Icons.Outlined.VpnKey,
                    iconTint = c.primary,
                    title = "Set Passcode",
                    subtitle = if (vm.passcode != null) "Passcode enabled" else "Set a passcode for lock",
                    onClick = { showPasscode = true },
                ) { Chevron() }
                HairlineDivider()

                SettingRow(
                    icon = Icons.Outlined.Fingerprint,
                    iconTint = c.primary,
                    title = "Biometric Unlock",
                    subtitle = if (vm.biometrics) "Fingerprint / Face unlock enabled" else "Use device biometrics with passcode",
                    onClick = {
                        if (vm.passcode == null) ctx.toast("Set a passcode first.")
                        else vm.biometrics = !vm.biometrics
                    },
                ) { WikiSwitch(vm.biometrics) }
                HairlineDivider()

                SettingRow(
                    icon = Icons.Outlined.FolderOpen,
                    iconTint = c.primary,
                    title = "Backup & Restore",
                    subtitle = "Backup and restore diary text & media",
                    onClick = { vm.navigate(Route.Backup) },
                ) { Chevron() }
                HairlineDivider()

                SettingRow(
                    icon = Icons.Outlined.BookmarkBorder,
                    iconTint = c.primary,
                    title = "Saved Word Analysis",
                    subtitle = "View saved words in alphabetical order (A–Z)",
                    onClick = { ctx.toast("Saved Word Analysis — to be wired later") },
                ) { Chevron() }
                HairlineDivider()

                SettingRow(
                    icon = Icons.Outlined.Mic,
                    iconTint = c.primary,
                    title = "Audio Recording Settings",
                    subtitle = "Format: .${mic.format} · ${formatKhz(mic.sampleRate)} kHz · ${mic.bitRate / 1000} kbps",
                    onClick = { showMic = true },
                ) { Chevron() }
                HairlineDivider()

                SettingRow(
                    icon = Icons.Outlined.Description,
                    iconTint = c.primary,
                    title = "Legal, About & Data Notice",
                    subtitle = "Privacy Policy, Terms, About, Licenses, Export & Clear Data",
                    onClick = { vm.navigate(Route.Legal) },
                ) { Chevron() }
            }
        }
    }

    if (showPasscode) {
        SetPasscodeDialog(
            hasPasscode = vm.passcode != null,
            onDismiss = { showPasscode = false },
            onSave = { pin ->
                vm.updatePasscode(pin)
                showPasscode = false
                ctx.toast(if (pin == null) "Passcode removed" else "Passcode set")
            },
        )
    }
    if (showMic) {
        MicSettingsDialog(
            current = mic,
            onDismiss = { showMic = false },
            onSave = { vm.micSettings = it; showMic = false },
        )
    }
}

private fun formatKhz(hz: Int): String {
    val k = hz / 1000f
    return if (k % 1f == 0f) k.toInt().toString() else "%.1f".format(k)
}

@Composable
private fun Chevron() {
    Icon(Icons.Outlined.ChevronRight, null, tint = Wiki.colors.muted, modifier = Modifier.size(20.dp))
}

@Composable
private fun SetPasscodeDialog(
    hasPasscode: Boolean,
    onDismiss: () -> Unit,
    onSave: (String?) -> Unit,
) {
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    fun submit() {
        when {
            pin.length !in 4..8 -> error = "Use 4 to 8 digits."
            pin != confirm -> error = "Passcodes do not match."
            else -> onSave(pin)
        }
    }

    WikiDialog(
        onDismiss = onDismiss,
        title = "Set Passcode",
        icon = Icons.Outlined.VpnKey,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (error != null) StatusBanner(error!!, isError = true)
            Column {
                FieldLabel("New Passcode")
                WikiField(
                    value = pin,
                    onValueChange = { pin = it.filter(Char::isDigit).take(8); error = null },
                    placeholder = "4–8 digits",
                    password = true,
                    keyboardType = KeyboardType.NumberPassword,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Column {
                FieldLabel("Confirm Passcode")
                WikiField(
                    value = confirm,
                    onValueChange = { confirm = it.filter(Char::isDigit).take(8); error = null },
                    placeholder = "Repeat passcode",
                    password = true,
                    keyboardType = KeyboardType.NumberPassword,
                    modifier = Modifier.fillMaxWidth(),
                    onDone = { submit() },
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (hasPasscode) {
                    WikiButton("Remove", { onSave(null) }, style = WikiButtonStyle.DangerOutline)
                }
                Spacer(Modifier.weight(1f))
                WikiButton("Cancel", onDismiss, style = WikiButtonStyle.Outline)
                Spacer(Modifier.size(8.dp))
                WikiButton("Save", { submit() })
            }
        }
    }
}

@Composable
private fun MicSettingsDialog(
    current: MicSettings,
    onDismiss: () -> Unit,
    onSave: (MicSettings) -> Unit,
) {
    var draft by remember { mutableStateOf(current) }

    WikiDialog(
        onDismiss = onDismiss,
        title = "Audio Recording Settings",
        icon = Icons.Outlined.Mic,
    ) {
        Column(
            Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column {
                FieldLabel("Format")
                OptionGrid(
                    options = listOf("wav", "flac", "m4a", "aac", "mp3", "ogg", "webm").map { it to ".$it" },
                    selected = draft.format,
                    columns = 4,
                    onSelect = { draft = draft.copy(format = it) },
                )
            }
            Column {
                FieldLabel("Sample Rate")
                OptionGrid(
                    options = listOf(
                        8000 to "8,000 Hz", 16000 to "16,000 Hz", 22050 to "22,050 Hz",
                        44100 to "44,100 Hz", 48000 to "48,000 Hz",
                    ),
                    selected = draft.sampleRate,
                    columns = 3,
                    onSelect = { draft = draft.copy(sampleRate = it) },
                )
            }
            Column {
                FieldLabel("Bit Rate")
                OptionGrid(
                    options = listOf(
                        64000 to "64 kbps", 128000 to "128 kbps", 192000 to "192 kbps",
                        256000 to "256 kbps", 320000 to "320 kbps",
                    ),
                    selected = draft.bitRate,
                    columns = 3,
                    onSelect = { draft = draft.copy(bitRate = it) },
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                WikiButton("Cancel", onDismiss, style = WikiButtonStyle.Outline)
                WikiButton("Save", { onSave(draft) })
            }
        }
    }
}

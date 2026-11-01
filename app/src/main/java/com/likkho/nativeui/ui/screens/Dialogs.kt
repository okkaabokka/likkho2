package com.likkho.nativeui.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import com.likkho.nativeui.model.DiaryLog
import com.likkho.nativeui.ui.components.FieldLabel
import com.likkho.nativeui.ui.components.WikiButton
import com.likkho.nativeui.ui.components.WikiButtonStyle
import com.likkho.nativeui.ui.components.WikiDialog
import com.likkho.nativeui.ui.components.WikiField
import com.likkho.nativeui.ui.theme.Wiki
import com.likkho.nativeui.util.Fmt
import kotlinx.coroutines.delay
import java.util.Calendar

// ------------------------------------------------------------- delete

@Composable
fun DeleteEntryDialog(log: DiaryLog, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val c = Wiki.colors
    val f = Wiki.fonts
    WikiDialog(
        onDismiss = onDismiss,
        title = "Delete Entry?",
        icon = Icons.Outlined.Warning,
        iconTint = c.danger,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = buildAnnotatedString {
                    append("Are you sure you want to permanently delete ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("\"${log.heading}\"") }
                    append("? This action cannot be undone.")
                },
                style = TextStyle(fontFamily = f.sans, fontSize = 12.sp, lineHeight = 18.sp),
            )
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                WikiButton("Cancel", onDismiss, style = WikiButtonStyle.Outline)
                WikiButton("Delete", onConfirm, icon = Icons.Outlined.Delete, style = WikiButtonStyle.Danger)
            }
        }
    }
}

// ------------------------------------------------------------- export

private val EXPORT_FORMATS = listOf(
    ".txt" to "Plain Text Document",
    ".md" to "Markdown Document",
    ".pdf" to "PDF Printable Document (Full Canvas)",
    ".rtf" to "Rich Text Format",
    ".html" to "Web Page Archive",
    ".docx" to "Microsoft Word Document (Full Canvas)",
    ".json" to "Structured JSON Data",
    ".csv" to "Comma-Separated Values",
    ".tsv" to "Tab-Separated Values",
    ".xml" to "Extensible Markup Language",
)

@Composable
fun ExportDialog(log: DiaryLog, onDismiss: () -> Unit, onPick: (String) -> Unit) {
    val c = Wiki.colors
    val f = Wiki.fonts
    WikiDialog(
        onDismiss = onDismiss,
        title = "Export \"${log.heading}\"",
        icon = Icons.Outlined.FileDownload,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Choose a file format to save to Downloads/Likkho:",
                color = c.muted,
                style = TextStyle(fontFamily = f.sans, fontSize = 12.sp),
            )
            Spacer(Modifier.height(12.dp))
            EXPORT_FORMATS.chunked(2).forEach { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pair.forEach { (ext, desc) ->
                        Column(
                            Modifier
                                .weight(1f)
                                .background(c.surface)
                                .border(1.dp, c.border)
                                .clickable { onPick(ext) }
                                .padding(10.dp),
                        ) {
                            Text(
                                text = ext,
                                color = c.primary,
                                style = TextStyle(fontFamily = f.mono, fontSize = 12.sp, fontWeight = FontWeight.Bold),
                            )
                            Text(
                                text = desc,
                                color = c.muted,
                                style = TextStyle(fontFamily = f.sans, fontSize = 10.sp, lineHeight = 13.sp),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ------------------------------------------------------------- locked diary

@Composable
fun UnlockDiaryDialog(log: DiaryLog, onDismiss: () -> Unit, onUnlocked: () -> Unit) {
    val c = Wiki.colors
    val f = Wiki.fonts
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var failures by remember { mutableStateOf(0) }
    var cooldown by remember { mutableStateOf(0) }
    val focus = remember { FocusRequester() }

    LaunchedEffect(Unit) { focus.requestFocus() }
    LaunchedEffect(cooldown > 0) {
        while (cooldown > 0) {
            delay(1000)
            cooldown -= 1
        }
    }

    fun submit() {
        if (cooldown > 0) return
        if (input.trim() == log.lockPin) {
            onUnlocked()
        } else {
            failures += 1
            if (failures % 3 == 0) {
                cooldown = 15
                error = null
            } else {
                error = "Incorrect diary passcode."
            }
        }
    }

    WikiDialog(
        onDismiss = onDismiss,
        title = "Unlock \"${log.heading}\"",
        icon = Icons.Outlined.Lock,
        iconTint = c.danger,
        maxWidth = 320.dp,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (cooldown > 0) {
                Text(
                    text = "Locked for ${cooldown}s due to continuous wrong attempts.",
                    color = c.danger,
                    style = TextStyle(fontFamily = f.sans, fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                )
            } else if (error != null) {
                Text(
                    text = error!!,
                    color = c.danger,
                    style = TextStyle(fontFamily = f.sans, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                )
            }
            Column {
                FieldLabel("Enter Diary Passcode")
                WikiField(
                    value = input,
                    onValueChange = { input = it; error = null },
                    placeholder = "Passcode...",
                    password = true,
                    enabled = cooldown == 0,
                    height = 36.dp,
                    focusRequester = focus,
                    modifier = Modifier.fillMaxWidth(),
                    onDone = { submit() },
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                WikiButton("Cancel", onDismiss, style = WikiButtonStyle.Outline, height = 36.dp)
                WikiButton(
                    text = if (cooldown > 0) "Wait ${cooldown}s" else "Unlock",
                    onClick = { submit() },
                    enabled = cooldown == 0,
                    height = 36.dp,
                )
            }
        }
    }
}

// ------------------------------------------------------------- reminder

@Composable
fun ReminderDialog(
    current: Long?,
    darkTheme: Boolean,
    onDismiss: () -> Unit,
    onSave: (Long?) -> Unit,
) {
    val c = Wiki.colors
    val f = Wiki.fonts
    val ctx = LocalContext.current
    var millis by remember {
        mutableLongStateOf(current ?: (System.currentTimeMillis() + 5 * 60_000L))
    }
    var error by remember { mutableStateOf("") }
    val themeRes = if (darkTheme) {
        android.R.style.Theme_Material_Dialog_Alert
    } else {
        android.R.style.Theme_Material_Light_Dialog_Alert
    }

    fun pickDate() {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        DatePickerDialog(
            ctx, themeRes,
            { _, y, m, d ->
                val next = Calendar.getInstance().apply {
                    timeInMillis = millis
                    set(y, m, d)
                }
                millis = next.timeInMillis
                error = ""
            },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
        ).show()
    }

    fun pickTime() {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        TimePickerDialog(
            ctx, themeRes,
            { _, h, min ->
                val next = Calendar.getInstance().apply {
                    timeInMillis = millis
                    set(Calendar.HOUR_OF_DAY, h)
                    set(Calendar.MINUTE, min)
                    set(Calendar.SECOND, 0)
                }
                millis = next.timeInMillis
                error = ""
            },
            cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false,
        ).show()
    }

    WikiDialog(
        onDismiss = onDismiss,
        title = "Set Date & Time",
        icon = Icons.Outlined.NotificationsActive,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Date", color = c.muted,
                        modifier = Modifier.padding(bottom = 4.dp),
                        style = TextStyle(fontFamily = f.sans, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                    )
                    PickerBox(Fmt.date(millis)) { pickDate() }
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        "Time", color = c.muted,
                        modifier = Modifier.padding(bottom = 4.dp),
                        style = TextStyle(fontFamily = f.sans, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                    )
                    PickerBox(Fmt.time(millis)) { pickTime() }
                }
            }
            if (error.isNotEmpty()) {
                Text(
                    text = error,
                    color = c.danger,
                    style = TextStyle(fontFamily = f.sans, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                )
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.hairline))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (current != null) {
                    WikiButton(
                        "Clear",
                        onClick = { onSave(null); onDismiss() },
                        icon = Icons.Outlined.Delete,
                        style = WikiButtonStyle.DangerOutline,
                    )
                }
                Spacer(Modifier.weight(1f))
                WikiButton("Cancel", onDismiss, style = WikiButtonStyle.Outline)
                Spacer(Modifier.width(8.dp))
                WikiButton(
                    "Set",
                    onClick = {
                        if (millis <= System.currentTimeMillis()) {
                            error = "Please choose a future time."
                        } else {
                            onSave(millis)
                            onDismiss()
                        }
                    },
                    icon = Icons.Outlined.Notifications,
                )
            }
        }
    }
}

@Composable
private fun PickerBox(text: String, onClick: () -> Unit) {
    val c = Wiki.colors
    val f = Wiki.fonts
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(c.surface)
            .border(1.dp, c.border)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(text, style = TextStyle(fontFamily = f.mono, fontSize = 12.sp))
    }
}

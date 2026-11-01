package com.likkho.nativeui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.likkho.nativeui.AppViewModel
import com.likkho.nativeui.ui.components.HairlineDivider
import com.likkho.nativeui.ui.components.ScreenHeader
import com.likkho.nativeui.ui.components.SectionCard
import com.likkho.nativeui.ui.components.WikiButton
import com.likkho.nativeui.ui.components.WikiButtonStyle
import com.likkho.nativeui.ui.components.WikiDialog
import com.likkho.nativeui.ui.theme.Wiki
import com.likkho.nativeui.util.toast

private val SECTIONS = listOf(
    "About" to "Likkho is a Wikipedia-style writing and diary app. This build is the native (Kotlin / Jetpack Compose) UI shell.",
    "Privacy" to "Placeholder text. Replace with your final privacy policy once storage and encryption are connected.",
    "Terms" to "Placeholder text. Replace with your final terms of use.",
    "Open-source licenses" to "Jetpack Compose, AndroidX (Apache 2.0). Cormorant Garamond, Lora, Plus Jakarta Sans, IBM Plex Mono (SIL Open Font License 1.1).",
    "Data notice" to "Entries in this preview live only in memory and disappear when the app is closed.",
)

@Composable
fun LegalScreen(vm: AppViewModel) {
    val c = Wiki.colors
    val f = Wiki.fonts
    val ctx = LocalContext.current
    var confirm by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(c.bg)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        ScreenHeader("Legal, About & Data Notice", onBack = vm::back)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            SectionCard {
                SECTIONS.forEachIndexed { i, (title, text) ->
                    if (i > 0) HairlineDivider()
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(
                            title,
                            style = TextStyle(fontFamily = f.serif, fontSize = 17.sp, fontWeight = FontWeight.Bold),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text,
                            color = c.muted,
                            style = TextStyle(fontFamily = f.prose, fontSize = 13.sp, lineHeight = 20.sp),
                        )
                    }
                }
            }

            WikiButton(
                text = "Delete All App Data",
                icon = Icons.Outlined.Delete,
                style = WikiButtonStyle.DangerOutline,
                modifier = Modifier.fillMaxWidth(),
                onClick = { confirm = true },
            )
        }
    }

    if (confirm) {
        WikiDialog(
            onDismiss = { confirm = false },
            title = "Delete All Data?",
            icon = Icons.Outlined.Warning,
            iconTint = c.danger,
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "All diary entries and settings will be permanently removed. This cannot be undone.",
                    style = TextStyle(fontFamily = f.sans, fontSize = 12.sp, lineHeight = 18.sp),
                )
                androidx.compose.foundation.layout.Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, androidx.compose.ui.Alignment.End),
                ) {
                    WikiButton("Cancel", { confirm = false }, style = WikiButtonStyle.Outline)
                    WikiButton(
                        "Delete",
                        {
                            confirm = false
                            vm.clearAll()
                            ctx.toast("All application data has been deleted.")
                        },
                        icon = Icons.Outlined.Delete,
                        style = WikiButtonStyle.Danger,
                    )
                }
            }
        }
    }
}

package com.likkho.nativeui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.likkho.nativeui.AppViewModel
import com.likkho.nativeui.ui.components.FieldLabel
import com.likkho.nativeui.ui.components.ScreenHeader
import com.likkho.nativeui.ui.components.SectionCard
import com.likkho.nativeui.ui.components.SquareIconButton
import com.likkho.nativeui.ui.components.StatusBanner
import com.likkho.nativeui.ui.components.WikiButton
import com.likkho.nativeui.ui.components.WikiButtonStyle
import com.likkho.nativeui.ui.components.WikiField
import com.likkho.nativeui.ui.theme.Wiki
import com.likkho.nativeui.util.toast

/**
 * UI shell only — key hashing, AES encryption, file picking and restore
 * are intentionally NOT implemented here.
 */
@Composable
fun BackupScreen(vm: AppViewModel) {
    val c = Wiki.colors
    val ctx = LocalContext.current

    var showKey by remember { mutableStateOf(false) }
    var newKey by remember { mutableStateOf("") }
    var confirmKey by remember { mutableStateOf("") }
    var keySet by remember { mutableStateOf(false) }
    var backupKey by remember { mutableStateOf("") }
    var restoreKey by remember { mutableStateOf("") }
    var banner by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .background(c.bg)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        ScreenHeader("Backup & Restore", onBack = vm::back)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            banner?.let { (text, isError) -> StatusBanner(text, isError) }

            // 1. Set encryption key
            SectionCard(Modifier.padding(0.dp)) {
                Column(Modifier.padding(16.dp)) {
                    CardTitle(Icons.Outlined.VpnKey, "Set Encryption Key") {
                        if (keySet) {
                            Text(
                                "✓ Key Set",
                                color = c.success,
                                style = TextStyle(fontFamily = Wiki.fonts.sans, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column {
                            FieldLabel("Encryption Key")
                            WikiField(
                                value = newKey,
                                onValueChange = { newKey = it },
                                placeholder = "Enter encryption key",
                                password = !showKey,
                                modifier = Modifier.fillMaxWidth(),
                                trailing = {
                                    SquareIconButton(
                                        icon = if (showKey) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = "Toggle visibility",
                                        onClick = { showKey = !showKey },
                                        size = 32.dp,
                                        bordered = false,
                                        tint = c.muted,
                                        iconSize = 16.dp,
                                    )
                                },
                            )
                        }
                        Column {
                            FieldLabel("Confirm Encryption Key")
                            WikiField(
                                value = confirmKey,
                                onValueChange = { confirmKey = it },
                                placeholder = "Confirm encryption key",
                                password = !showKey,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        WikiButton(
                            text = "Save Key",
                            icon = Icons.Outlined.Shield,
                            style = WikiButtonStyle.Outline,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                banner = when {
                                    newKey.length < 6 -> "Encryption key must be at least 6 characters." to true
                                    newKey != confirmKey -> "Keys do not match." to true
                                    else -> {
                                        keySet = true
                                        newKey = ""
                                        confirmKey = ""
                                        "Encryption key saved (UI demo — hashing comes later)." to false
                                    }
                                }
                            },
                        )
                    }
                }
            }

            // 2. Backup
            SectionCard {
                Column(Modifier.padding(16.dp)) {
                    CardTitle(Icons.Outlined.CloudDownload, "Backup") {}
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column {
                            FieldLabel("Encryption Key")
                            WikiField(
                                value = backupKey,
                                onValueChange = { backupKey = it },
                                placeholder = "Enter encryption key",
                                password = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        WikiButton(
                            text = "Backup",
                            icon = Icons.Outlined.Lock,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                if (backupKey.isEmpty()) banner = "Enter your encryption key." to true
                                else ctx.toast("Backup — to be wired later")
                            },
                        )
                    }
                }
            }

            // 3. Restore
            SectionCard {
                Column(Modifier.padding(16.dp)) {
                    CardTitle(Icons.Outlined.Restore, "Restore") {}
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column {
                            FieldLabel("Encryption Key")
                            WikiField(
                                value = restoreKey,
                                onValueChange = { restoreKey = it },
                                placeholder = "Enter encryption key",
                                password = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        WikiButton(
                            text = "Choose Backup File",
                            icon = Icons.Outlined.FolderOpen,
                            style = WikiButtonStyle.Outline,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                if (restoreKey.isEmpty()) banner = "Enter your encryption key." to true
                                else ctx.toast("Restore — to be wired later")
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardTitle(icon: ImageVector, title: String, trailing: @Composable () -> Unit) {
    val c = Wiki.colors
    val f = Wiki.fonts
    Column {
        Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = c.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.size(8.dp))
            Text(
                title,
                modifier = Modifier.weight(1f),
                style = TextStyle(fontFamily = f.serif, fontSize = 16.sp, fontWeight = FontWeight.Bold),
            )
            trailing()
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.hairline))
    }
}

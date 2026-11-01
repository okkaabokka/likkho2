package com.likkho.nativeui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.likkho.nativeui.ui.components.PressableKey
import com.likkho.nativeui.ui.theme.Wiki
import com.likkho.nativeui.util.toast
import kotlinx.coroutines.delay

@Composable
fun PasscodeScreen(
    pin: String,
    biometricsEnabled: Boolean,
    onUnlock: () -> Unit,
) {
    val c = Wiki.colors
    val f = Wiki.fonts
    val ctx = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var entered by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var failures by remember { mutableIntStateOf(0) }
    var cooldown by remember { mutableIntStateOf(0) }

    LaunchedEffect(cooldown > 0) {
        while (cooldown > 0) {
            delay(1000)
            cooldown -= 1
        }
    }
    LaunchedEffect(error) {
        if (error) {
            delay(350)
            entered = ""
            error = false
        }
    }

    fun onDigit(d: String) {
        if (cooldown > 0 || error) return
        val next = (entered + d).take(pin.length)
        entered = next
        if (next == pin) {
            onUnlock()
        } else if (next.length >= pin.length) {
            error = true
            failures += 1
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            if (failures % 5 == 0) cooldown = 30
        }
    }

    fun onBackspace() {
        if (cooldown > 0 || error) return
        entered = entered.dropLast(1)
    }

    val cooling = cooldown > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Top identity
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 16.dp)) {
            Box(
                modifier = Modifier.size(56.dp).background(c.surface).border(1.dp, c.border),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Lock, null, tint = c.primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.size(16.dp))
            Text(
                text = "Likkho Locked",
                style = TextStyle(fontFamily = f.serif, fontSize = 24.sp, fontWeight = FontWeight.Bold),
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = when {
                    cooling -> "Too many failed attempts. Try again in ${cooldown}s"
                    biometricsEnabled -> "Unlock with Biometrics or enter your passcode"
                    else -> "Enter your passcode to unlock"
                },
                color = c.muted,
                textAlign = TextAlign.Center,
                style = TextStyle(fontFamily = f.sans, fontSize = 12.sp),
            )
        }

        // Dots
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(pin.length) { i ->
                    val filled = i < entered.length
                    val tone = when {
                        error || cooling -> c.danger
                        filled -> c.primary
                        else -> c.border
                    }
                    Box(
                        Modifier
                            .size(16.dp)
                            .graphicsLayer {
                                val s = if (filled && !error) 1.1f else 1f
                                scaleX = s
                                scaleY = s
                            }
                            .background(if (filled || error || cooling) tone else Color.Transparent, CircleShape)
                            .border(1.dp, tone, CircleShape),
                    )
                }
            }
            Spacer(Modifier.size(12.dp))
            if (cooling) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Shield, null, tint = c.danger, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = "Locked for $cooldown seconds due to wrong attempts",
                        color = c.danger,
                        style = TextStyle(fontFamily = f.sans, fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                    )
                }
            } else if (error) {
                Text(
                    text = "Incorrect passcode",
                    color = c.danger,
                    style = TextStyle(fontFamily = f.sans, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                )
            } else {
                Spacer(Modifier.size(16.dp))
            }
        }

        // Keypad
        Column(
            modifier = Modifier.widthIn(max = 280.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9")).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { d ->
                        PressableKey(Modifier.weight(1f), enabled = !cooling, onClick = { onDigit(d) }) { fg ->
                            Text(
                                text = d,
                                color = fg,
                                style = TextStyle(fontFamily = f.serif, fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
                            )
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (biometricsEnabled) {
                    PressableKey(Modifier.weight(1f), enabled = !cooling, onClick = {
                        ctx.toast("Biometric prompt — to be wired later")
                    }) { fg ->
                        Icon(Icons.Outlined.Fingerprint, "Unlock with biometrics", tint = if (fg == Color.White) fg else c.primary, modifier = Modifier.size(24.dp))
                    }
                } else {
                    Spacer(Modifier.weight(1f))
                }
                PressableKey(Modifier.weight(1f), enabled = !cooling, onClick = { onDigit("0") }) { fg ->
                    Text(
                        text = "0",
                        color = fg,
                        style = TextStyle(fontFamily = f.serif, fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
                    )
                }
                PressableKey(Modifier.weight(1f), enabled = !cooling, onClick = { onBackspace() }) { fg ->
                    Icon(Icons.Outlined.Backspace, "Backspace", tint = if (fg == Color.White) fg else c.muted, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

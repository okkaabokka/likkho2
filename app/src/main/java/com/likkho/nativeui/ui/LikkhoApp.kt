package com.likkho.nativeui.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.likkho.nativeui.AppViewModel
import com.likkho.nativeui.Route
import com.likkho.nativeui.ui.screens.BackupScreen
import com.likkho.nativeui.ui.screens.HomeScreen
import com.likkho.nativeui.ui.screens.LegalScreen
import com.likkho.nativeui.ui.screens.PasscodeScreen
import com.likkho.nativeui.ui.screens.SettingsScreen
import com.likkho.nativeui.ui.screens.WorkspaceScreen
import com.likkho.nativeui.ui.theme.Wiki

@Composable
fun LikkhoApp(vm: AppViewModel) {
    val c = Wiki.colors
    val locked = vm.isLocked && vm.passcode != null

    // Home has its own BackHandler-less root; Workspace handles its own back (auto-save).
    BackHandler(enabled = !locked && (vm.route == Route.Backup || vm.route == Route.Legal || vm.route == Route.Settings)) {
        vm.back()
    }

    Column(Modifier.fillMaxSize().background(c.bg)) {
        // Status-bar strip painted with the header colour (just like the web app's theme-color).
        Spacer(
            Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(if (locked) c.bg else c.surface),
        )
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (locked) {
                PasscodeScreen(
                    pin = vm.passcode.orEmpty(),
                    biometricsEnabled = vm.biometrics,
                    onUnlock = { vm.unlock() },
                )
            } else {
                Crossfade(
                    targetState = vm.route,
                    animationSpec = tween(140),
                    label = "route",
                ) { route ->
                    when (route) {
                        Route.Home -> HomeScreen(vm)
                        is Route.Workspace -> WorkspaceScreen(vm, route.logId)
                        Route.Settings -> SettingsScreen(vm)
                        Route.Backup -> BackupScreen(vm)
                        Route.Legal -> LegalScreen(vm)
                    }
                }
            }
        }
    }
}

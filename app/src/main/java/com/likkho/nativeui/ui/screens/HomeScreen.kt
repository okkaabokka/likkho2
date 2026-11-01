package com.likkho.nativeui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.likkho.nativeui.AppViewModel
import com.likkho.nativeui.Route
import com.likkho.nativeui.model.Avatar
import com.likkho.nativeui.model.DiaryLog
import com.likkho.nativeui.ui.components.HairlineDivider
import com.likkho.nativeui.ui.components.SquareIconButton
import com.likkho.nativeui.ui.components.WikiField
import com.likkho.nativeui.ui.components.WikiHeader
import com.likkho.nativeui.ui.theme.Wiki
import com.likkho.nativeui.util.Fmt
import com.likkho.nativeui.util.toast

private enum class UnlockAction { Open, Export }

private data class UnlockRequest(val log: DiaryLog, val action: UnlockAction)

@Composable
fun HomeScreen(vm: AppViewModel) {
    val c = Wiki.colors
    val f = Wiki.fonts
    val ctx = LocalContext.current

    var searchOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var menuLogId by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<DiaryLog?>(null) }
    var exporting by remember { mutableStateOf<DiaryLog?>(null) }
    var pendingUnlock by remember { mutableStateOf<UnlockRequest?>(null) }
    val searchFocus = remember { FocusRequester() }

    val q = query.trim().lowercase()
    val shown = vm.logs
        .filter { log ->
            q.isEmpty() ||
                log.heading.lowercase().contains(q) ||
                (log.lockPin == null && log.body.lowercase().contains(q))
        }
        .sortedWith(compareByDescending<DiaryLog> { it.pinned }.thenByDescending { it.updatedAt })

    fun openLog(log: DiaryLog) {
        if (log.lockPin != null) pendingUnlock = UnlockRequest(log, UnlockAction.Open)
        else vm.navigate(Route.Workspace(log.id))
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(c.bg)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        Column(Modifier.fillMaxSize()) {
            // ---------------------------------------------- header
            WikiHeader {
                if (searchOpen) {
                    LaunchedEffect(Unit) { searchFocus.requestFocus() }
                    Icon(Icons.Outlined.Search, null, tint = c.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    WikiField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = "Search diaries...",
                        height = 36.dp,
                        focusRequester = searchFocus,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    SquareIconButton(
                        icon = Icons.Outlined.Close,
                        contentDescription = "Close search",
                        onClick = { searchOpen = false; query = "" },
                        size = 36.dp,
                        tint = c.muted,
                    )
                } else {
                    Text(
                        text = "Likkho",
                        modifier = Modifier.weight(1f),
                        style = TextStyle(
                            fontFamily = f.serif,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.6).sp,
                        ),
                    )
                    SquareIconButton(
                        icon = Icons.Outlined.Search,
                        contentDescription = "Search diaries",
                        onClick = { searchOpen = true },
                    )
                    Spacer(Modifier.width(6.dp))
                    // Instant Lock
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .background(c.bg)
                            .border(1.dp, c.border)
                            .clickable {
                                if (vm.passcode == null) ctx.toast("Set a passcode first (Settings)")
                                else vm.lock()
                            }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(Icons.Outlined.Lock, "Instant Lock", tint = c.primary, modifier = Modifier.size(16.dp))
                        Text(
                            "Lock",
                            style = TextStyle(fontFamily = f.sans, fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    SquareIconButton(
                        icon = Icons.Outlined.Menu,
                        contentDescription = "Open Settings",
                        onClick = { vm.navigate(Route.Settings) },
                    )
                }
            }

            // ---------------------------------------------- list
            if (shown.isEmpty()) {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier.size(48.dp).background(c.surface).border(1.dp, c.border),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.MenuBook, null, tint = c.muted, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (q.isNotEmpty()) "No Matching Diaries Found" else "No Logs Yet",
                        style = TextStyle(fontFamily = f.serif, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (q.isNotEmpty()) "No diary entry contains \"$query\"."
                        else "Tap the create button below to start writing.",
                        color = c.muted,
                        textAlign = TextAlign.Center,
                        style = TextStyle(fontFamily = f.sans, fontSize = 12.sp, lineHeight = 18.sp),
                    )
                }
            } else {
                LazyColumn(Modifier.weight(1f)) {
                    items(shown, key = { it.id }) { log ->
                        LogRow(
                            log = log,
                            menuOpen = menuLogId == log.id,
                            onOpenMenu = { menuLogId = if (menuLogId == log.id) null else log.id },
                            onDismissMenu = { menuLogId = null },
                            onClick = { openLog(log) },
                            onPin = { menuLogId = null; vm.togglePin(log.id) },
                            onShare = { menuLogId = null; ctx.toast("Share (.likkho) — to be wired later") },
                            onExport = {
                                menuLogId = null
                                if (log.lockPin != null) pendingUnlock = UnlockRequest(log, UnlockAction.Export)
                                else exporting = log
                            },
                            onDelete = { menuLogId = null; pendingDelete = log },
                        )
                        HairlineDivider()
                    }
                    item { Spacer(Modifier.height(96.dp)) }
                }
            }
        }

        // ---------------------------------------------- FAB
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .height(56.dp)
                .background(c.primary)
                .border(1.dp, c.primaryPressed)
                .clickable { vm.navigate(Route.Workspace(null)) }
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Outlined.Add, "Create new diary log", tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(20.dp))
            Text(
                "Create",
                color = androidx.compose.ui.graphics.Color.White,
                style = TextStyle(
                    fontFamily = f.serif,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.4.sp,
                ),
            )
        }
    }

    // ---------------------------------------------- dialogs
    pendingUnlock?.let { req ->
        UnlockDiaryDialog(
            log = req.log,
            onDismiss = { pendingUnlock = null },
            onUnlocked = {
                pendingUnlock = null
                when (req.action) {
                    UnlockAction.Open -> vm.navigate(Route.Workspace(req.log.id))
                    UnlockAction.Export -> exporting = req.log
                }
            },
        )
    }
    pendingDelete?.let { log ->
        DeleteEntryDialog(
            log = log,
            onDismiss = { pendingDelete = null },
            onConfirm = { vm.delete(log.id); pendingDelete = null },
        )
    }
    exporting?.let { log ->
        ExportDialog(
            log = log,
            onDismiss = { exporting = null },
            onPick = { ext ->
                exporting = null
                ctx.toast("Export $ext — to be wired later")
            },
        )
    }
}

@Composable
private fun LogRow(
    log: DiaryLog,
    menuOpen: Boolean,
    onOpenMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onClick: () -> Unit,
    onPin: () -> Unit,
    onShare: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
) {
    val c = Wiki.colors
    val f = Wiki.fonts
    val stampStyle = TextStyle(
        fontFamily = f.mono,
        fontSize = 10.sp,
        color = c.muted.copy(alpha = 0.85f),
        fontWeight = FontWeight.Normal,
        lineHeight = 14.sp,
    )
    val stampLabel = stampStyle.copy(
        fontWeight = FontWeight.Normal,
        color = c.text.copy(alpha = 0.80f),
    )
    val now = System.currentTimeMillis()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DiaryAvatar(log.avatar, 52.dp)
        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (log.pinned) {
                    Icon(Icons.Outlined.PushPin, "Pinned", tint = c.primary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                }
                if (log.lockPin != null) {
                    Icon(Icons.Outlined.Lock, "Locked diary", tint = c.danger, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = log.heading,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = f.serif, fontSize = 19.sp, fontWeight = FontWeight.Bold),
                )
            }
            Text(
                text = if (log.lockPin != null) "" else log.preview,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = c.muted.copy(alpha = 0.85f),
                modifier = Modifier.padding(top = 2.dp).heightIn(min = 18.dp),
                style = TextStyle(fontFamily = f.prose, fontSize = 13.5.sp, fontWeight = FontWeight.Normal),
            )
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Created:", style = stampLabel)
                Text("${Fmt.date(log.createdAt)} · ${Fmt.time(log.createdAt)}", style = stampStyle)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Modified:", style = stampLabel)
                Text("${Fmt.date(log.updatedAt)} · ${Fmt.time(log.updatedAt)}", style = stampStyle)
                val r = log.reminderAt
                if (r != null && r > now) {
                    Text("·", style = stampStyle)
                    Icon(Icons.Outlined.NotificationsActive, null, tint = c.primary, modifier = Modifier.size(12.dp))
                    Text(Fmt.time(r), style = stampStyle.copy(color = c.primary))
                }
            }
        }

        // 3-dot menu
        Box {
            SquareIconButton(
                icon = Icons.Outlined.MoreVert,
                contentDescription = "Log options",
                onClick = onOpenMenu,
                bordered = false,
                tint = c.muted,
            )
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = onDismissMenu,
                shape = RectangleShape,
                containerColor = c.bg,
                tonalElevation = 0.dp,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, c.border),
            ) {
                MenuItem(if (log.pinned) "Unpin from Top" else "Pin to Top", Icons.Outlined.PushPin, c.primary, c.text, onPin)
                MenuItem("Share (.likkho)", Icons.Outlined.Share, c.primary, c.text, onShare)
                MenuItem("Export", Icons.Outlined.FileDownload, c.primary, c.text, onExport)
                MenuItem("Delete", Icons.Outlined.Delete, c.danger, c.danger, onDelete)
            }
        }
    }
}

@Composable
private fun MenuItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    val f = Wiki.fonts
    DropdownMenuItem(
        text = {
            Text(
                label,
                color = textColor,
                style = TextStyle(fontFamily = f.sans, fontSize = 13.sp, fontWeight = FontWeight.Medium),
            )
        },
        leadingIcon = { Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp)) },
        onClick = onClick,
    )
}

@Composable
fun DiaryAvatar(avatar: Avatar, size: Dp) {
    val c = Wiki.colors
    val f = Wiki.fonts
    Box(
        modifier = Modifier
            .size(size)
            .background(avatar.bg)
            .border(1.dp, c.border),
        contentAlignment = Alignment.Center,
    ) {
        if (avatar.frame != null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(size * 0.05f)
                    .border(1.dp, avatar.frame),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = avatar.label,
                color = avatar.fg,
                maxLines = 1,
                style = TextStyle(
                    fontFamily = f.serif,
                    fontSize = (avatar.labelSp * size.value / 52f).sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            if (avatar.sub != null) {
                Text(
                    text = avatar.sub,
                    color = avatar.subColor,
                    maxLines = 1,
                    style = TextStyle(
                        fontFamily = f.serif,
                        fontSize = (8f * size.value / 52f).sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
    }
}

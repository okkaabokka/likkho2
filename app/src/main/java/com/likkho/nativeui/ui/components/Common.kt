package com.likkho.nativeui.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.likkho.nativeui.ui.theme.Wiki

// ---------------------------------------------------------------- header

@Composable
fun WikiHeader(
    height: Dp = 56.dp,
    horizontal: Dp = 16.dp,
    content: @Composable RowScope.() -> Unit,
) {
    val c = Wiki.colors
    Column(Modifier.fillMaxWidth().background(c.surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = height)
                .padding(horizontal = horizontal),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
    }
}

@Composable
fun ScreenHeader(title: String, onBack: () -> Unit) {
    val f = Wiki.fonts
    WikiHeader {
        SquareIconButton(
            icon = Icons.Outlined.ArrowBack,
            contentDescription = "Back",
            onClick = onBack,
            bordered = false,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = title,
            style = TextStyle(
                fontFamily = f.serif,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

// ---------------------------------------------------------------- buttons

@Composable
fun SquareIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    bordered: Boolean = true,
    tint: Color = Wiki.colors.text,
    background: Color = if (bordered) Wiki.colors.bg else Color.Transparent,
    iconSize: Dp = 20.dp,
) {
    val c = Wiki.colors
    Box(
        modifier = modifier
            .size(size)
            .background(background)
            .then(if (bordered) Modifier.border(1.dp, c.border) else Modifier)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize),
        )
    }
}

enum class WikiButtonStyle { Primary, Outline, Danger, DangerOutline }

@Composable
fun WikiButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    style: WikiButtonStyle = WikiButtonStyle.Primary,
    enabled: Boolean = true,
    height: Dp = 40.dp,
) {
    val c = Wiki.colors
    val f = Wiki.fonts
    val bg: Color
    val fg: Color
    val line: Color
    when (style) {
        WikiButtonStyle.Primary -> { bg = c.primary; fg = Color.White; line = c.primaryPressed }
        WikiButtonStyle.Outline -> { bg = c.surface; fg = c.text; line = c.border }
        WikiButtonStyle.Danger -> { bg = c.danger; fg = Color.White; line = c.dangerPressed }
        WikiButtonStyle.DangerOutline -> { bg = Color.Transparent; fg = c.danger; line = c.danger.copy(alpha = 0.4f) }
    }
    Row(
        modifier = modifier
            .height(height)
            .alpha(if (enabled) 1f else 0.5f)
            .background(bg)
            .border(1.dp, line)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
        }
        Text(
            text = text,
            color = fg,
            style = TextStyle(fontFamily = f.sans, fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
        )
    }
}

// ---------------------------------------------------------------- switch

@Composable
fun WikiSwitch(checked: Boolean) {
    val c = Wiki.colors
    val offset by animateDpAsState(if (checked) 20.dp else 0.dp, label = "thumb")
    Box(
        modifier = Modifier
            .width(44.dp)
            .height(24.dp)
            .background(if (checked) c.primary else c.surface, CircleShape)
            .border(1.dp, if (checked) c.primary else c.border, CircleShape)
            .padding(horizontal = 3.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .offset(x = offset)
                .size(16.dp)
                .background(Color.White, CircleShape),
        )
    }
}

// ---------------------------------------------------------------- text field

@Composable
fun WikiField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    password: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    mono: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    height: Dp = 40.dp,
    focusRequester: FocusRequester? = null,
    trailing: (@Composable () -> Unit)? = null,
    onDone: (() -> Unit)? = null,
) {
    val c = Wiki.colors
    val f = Wiki.fonts
    var focused by remember { mutableStateOf(false) }
    val kb = if (password && keyboardType == KeyboardType.Text) KeyboardType.Password else keyboardType

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = singleLine,
        textStyle = TextStyle(
            fontFamily = if (mono) f.mono else f.sans,
            fontSize = 13.sp,
            color = c.text,
        ),
        cursorBrush = SolidColor(c.primary),
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = kb, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
        modifier = modifier
            .height(height)
            .alpha(if (enabled) 1f else 0.5f)
            .background(c.surface)
            .border(1.dp, if (focused) c.primary else c.border)
            .onFocusChanged { focused = it.isFocused }
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
        decorationBox = { inner ->
            Row(
                Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = c.muted.copy(alpha = 0.7f),
                            style = TextStyle(fontFamily = f.sans, fontSize = 13.sp),
                        )
                    }
                    inner()
                }
                if (trailing != null) trailing()
            }
        },
    )
}

@Composable
fun FieldLabel(text: String) {
    val f = Wiki.fonts
    Text(
        text = text,
        modifier = Modifier.padding(bottom = 4.dp),
        style = TextStyle(fontFamily = f.sans, fontSize = 12.sp, fontWeight = FontWeight.Medium),
    )
}

// ---------------------------------------------------------------- dialog

@Composable
fun WikiDialog(
    onDismiss: () -> Unit,
    title: String,
    icon: ImageVector? = null,
    iconTint: Color = Wiki.colors.primary,
    maxWidth: Dp = 384.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = Wiki.colors
    val f = Wiki.fonts
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                Modifier
                    .widthIn(max = maxWidth)
                    .fillMaxWidth()
                    .background(c.bg)
                    .border(1.dp, c.border),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.surface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (icon != null) {
                        Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = title,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                        style = TextStyle(
                            fontFamily = f.serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(c.hairline))
                content()
            }
        }
    }
}

// ---------------------------------------------------------------- list rows

@Composable
fun HairlineDivider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(Wiki.colors.hairline))
}

@Composable
fun SectionCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val c = Wiki.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(c.bg)
            .border(1.dp, c.border),
        content = content,
    )
}

@Composable
fun IconBox(icon: ImageVector, tint: Color, size: Dp = 40.dp) {
    val c = Wiki.colors
    Box(
        modifier = Modifier.size(size).background(c.surface).border(1.dp, c.border),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun SettingRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit,
) {
    val f = Wiki.fonts
    val c = Wiki.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBox(icon, iconTint)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = TextStyle(fontFamily = f.serif, fontSize = 16.sp, fontWeight = FontWeight.Bold),
            )
            Text(
                text = subtitle,
                color = c.muted.copy(alpha = 0.85f),
                style = TextStyle(fontFamily = f.sans, fontSize = 12.sp, fontWeight = FontWeight.Normal),
            )
        }
        Spacer(Modifier.width(12.dp))
        trailing()
    }
}

/** Banner used for success / error messages (green or red tinted box). */
@Composable
fun StatusBanner(text: String, isError: Boolean) {
    val c = Wiki.colors
    val f = Wiki.fonts
    val tone = if (isError) c.danger else c.success
    Text(
        text = text,
        color = if (isError) c.danger else c.text,
        modifier = Modifier
            .fillMaxWidth()
            .background(tone.copy(alpha = 0.10f))
            .border(1.dp, tone)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        style = TextStyle(fontFamily = f.sans, fontSize = 12.sp, fontWeight = FontWeight.Medium),
    )
}

/** Selectable chips laid out in a fixed number of columns (no experimental FlowRow). */
@Composable
fun <T> OptionGrid(
    options: List<Pair<T, String>>,
    selected: T,
    columns: Int,
    onSelect: (T) -> Unit,
) {
    val c = Wiki.colors
    val f = Wiki.fonts
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(columns).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { (value, label) ->
                    val isSel = value == selected
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .background(if (isSel) c.primary.copy(alpha = 0.12f) else c.surface)
                            .border(1.dp, if (isSel) c.primary else c.border)
                            .clickable { onSelect(value) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label,
                            color = if (isSel) c.primary else c.text,
                            style = TextStyle(
                                fontFamily = f.mono,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                            ),
                        )
                    }
                }
                repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun PressableKey(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable (Color) -> Unit,
) {
    val c = Wiki.colors
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val fg = if (pressed) Color.White else c.text
    Box(
        modifier = modifier
            .height(56.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .background(if (pressed) c.primary else c.surface)
            .border(1.dp, c.border)
            .clickable(
                interactionSource = source,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content(fg)
    }
}

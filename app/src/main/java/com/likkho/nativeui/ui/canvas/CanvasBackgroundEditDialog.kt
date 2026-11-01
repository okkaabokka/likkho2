package com.likkho.nativeui.ui.canvas

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File

/**
 * Edit & Image Compression Pop-up Modal for Canvas Background.
 */
@Composable
fun CanvasBackgroundEditDialog(
    bgImageUri: String,
    currentOpacity: Float,
    currentFitMode: String,
    primaryColor: Color,
    surfaceColor: Color,
    textColor: Color,
    mutedColor: Color,
    borderColor: Color,
    onApply: (opacity: Float, fitMode: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val ctx = LocalContext.current
    var opacity by remember { mutableFloatStateOf(currentOpacity) }
    var fitMode by remember { mutableStateOf(currentFitMode) }
    var compressionLevel by remember { mutableFloatStateOf(85f) }
    var selectedFilter by remember { mutableStateOf("Original") }

    val file = remember(bgImageUri) { File(bgImageUri) }
    val fileSizeKb = remember(bgImageUri) {
        if (file.exists()) (file.length() / 1024).toString() + " KB" else "Unknown"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(16.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = surfaceColor,
            tonalElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(primaryColor.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.Tune, null, tint = primaryColor, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(
                                "Background Image Tools",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    fontFamily = FontFamily.SansSerif,
                                ),
                            )
                            Text(
                                "Edit appearance & compression",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    color = mutedColor,
                                    fontFamily = FontFamily.SansSerif,
                                ),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.Close, "Close", tint = mutedColor, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Image Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.04f))
                        .border(1.dp, borderColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    val scale = when (fitMode) {
                        "contain", "fit" -> ContentScale.Fit
                        "fill", "stretch" -> ContentScale.FillBounds
                        else -> ContentScale.Crop
                    }
                    AsyncImage(
                        model = ImageRequest.Builder(ctx)
                            .data(if (file.exists()) file else bgImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Background Preview",
                        contentScale = scale,
                        modifier = Modifier.matchParentSize(),
                        alpha = opacity,
                    )

                    // File info badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                    ) {
                        Text(
                            text = fileSizeKb,
                            style = TextStyle(fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Medium),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Section 1: Opacity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(Icons.Outlined.Opacity, null, tint = primaryColor, modifier = Modifier.size(16.dp))
                        Text(
                            "Opacity",
                            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor),
                        )
                    }
                    Text(
                        "${(opacity * 100).toInt()}%",
                        style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = primaryColor),
                    )
                }
                Slider(
                    value = opacity,
                    onValueChange = { opacity = it },
                    valueRange = 0.05f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = primaryColor,
                        activeTrackColor = primaryColor,
                        inactiveTrackColor = borderColor.copy(alpha = 0.4f),
                    ),
                )

                Spacer(Modifier.height(8.dp))

                // Section 2: Fit Mode
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 8.dp),
                ) {
                    Icon(Icons.Outlined.Crop, null, tint = primaryColor, modifier = Modifier.size(16.dp))
                    Text(
                        "Canvas Fit Mode",
                        style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val modes = listOf(
                        "cover" to "Cover",
                        "contain" to "Contain",
                        "fill" to "Fill",
                    )
                    modes.forEach { (modeKey, modeTitle) ->
                        val isSelected = fitMode == modeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) primaryColor else primaryColor.copy(alpha = 0.08f))
                                .border(
                                    1.dp,
                                    if (isSelected) primaryColor else borderColor.copy(alpha = 0.35f),
                                    RoundedCornerShape(8.dp),
                                )
                                .clickable { fitMode = modeKey }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = modeTitle,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else textColor,
                                ),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Section 3: Image Compressing & Optimization Tools (Placeholder for upcoming tools)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(Icons.Outlined.Compress, null, tint = primaryColor, modifier = Modifier.size(16.dp))
                        Text(
                            "Compression Quality",
                            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor),
                        )
                    }
                    Text(
                        "${compressionLevel.toInt()}%",
                        style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = primaryColor),
                    )
                }

                Slider(
                    value = compressionLevel,
                    onValueChange = { compressionLevel = it },
                    valueRange = 20f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = primaryColor,
                        activeTrackColor = primaryColor,
                        inactiveTrackColor = borderColor.copy(alpha = 0.4f),
                    ),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("High Compression (Smaller File)", style = TextStyle(fontSize = 10.sp, color = mutedColor))
                    Text("Original (100%)", style = TextStyle(fontSize = 10.sp, color = mutedColor))
                }

                Spacer(Modifier.height(20.dp))

                // Action Buttons (Cancel / Apply)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("Cancel", style = TextStyle(fontSize = 13.sp, color = textColor))
                    }
                    Button(
                        onClick = {
                            onApply(opacity, fitMode)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    ) {
                        Icon(Icons.Outlined.Check, null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Apply", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
                    }
                }
            }
        }
    }
}

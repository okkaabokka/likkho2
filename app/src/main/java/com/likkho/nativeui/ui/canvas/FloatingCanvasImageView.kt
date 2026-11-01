package com.likkho.nativeui.ui.canvas

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.RotateRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.likkho.nativeui.model.CanvasLayer
import com.likkho.nativeui.model.FloatingCanvasImage
import java.io.File
import kotlin.math.atan2
import kotlin.math.roundToInt

/**
 * Renders a floating canvas image with native single-finger pan, 2-finger pinch resize,
 * rotation, layer toggle (Foreground/Background), and selection controls.
 */
@Composable
fun FloatingCanvasImageView(
    image: FloatingCanvasImage,
    isSelected: Boolean,
    isReadingMode: Boolean,
    primaryColor: Color,
    surfaceColor: Color,
    textColor: Color,
    onSelect: () -> Unit,
    onDeselect: () -> Unit,
    onLiveTransform: (x: Float, y: Float, scale: Float, rotation: Float) -> Unit,
    onCommitTransform: (FloatingCanvasImage) -> Unit,
    onToggleLayer: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current
    val density = LocalDensity.current

    val baseWidthPx = with(density) { image.baseWidthDp.dp.toPx() }
    val baseHeightPx = with(density) { image.baseHeightDp.dp.toPx() }

    Box(
        modifier = modifier
            .offset { IntOffset(image.x.roundToInt(), image.y.roundToInt()) }
    ) {
        // Rotated & scaled visual image content
        Box(
            modifier = Modifier
                .size(image.baseWidthDp.dp, image.baseHeightDp.dp)
                .graphicsLayer {
                    scaleX = image.scale
                    scaleY = image.scale
                    rotationZ = image.rotation
                }
                .then(
                    if (!isReadingMode) {
                        Modifier
                            // Single tap to select
                            .pointerInput(image.id) {
                                detectTapGestures(
                                    onTap = {
                                        onSelect()
                                    }
                                )
                            }
                            // Native single-finger pan, 2-finger pinch-zoom, and 2-finger rotation
                            .pointerInput(image.id) {
                                detectTransformGestures(panZoomLock = false) { _, pan, zoom, rotation ->
                                    onSelect()
                                    val newX = image.x + pan.x
                                    val newY = image.y + pan.y
                                    val newScale = (image.scale * zoom).coerceIn(0.2f, 5.0f)
                                    val newRotation = (image.rotation + rotation) % 360f

                                    onLiveTransform(newX, newY, newScale, newRotation)
                                }
                            }
                    } else Modifier
                )
        ) {
            // Actual Image
            AsyncImage(
                model = ImageRequest.Builder(ctx)
                    .data(File(image.uri).takeIf { it.exists() } ?: image.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Canvas floating image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(8.dp))
                    .then(
                        if (isSelected && !isReadingMode) {
                            Modifier.border(2.dp, primaryColor, RoundedCornerShape(8.dp))
                        } else {
                            Modifier.border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        }
                    )
            )

            // Selection Handles (shown when selected in Edit mode)
            if (isSelected && !isReadingMode) {
                // Top-Left Corner Indicator
                CornerDot(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset((-5).dp, (-5).dp),
                    color = primaryColor,
                )

                // Top-Right Corner Handle: 1-finger drag to rotate
                HandleControl(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(6.dp, (-6).dp)
                        .pointerInput(image.id) {
                            detectDragGestures(
                                onDragEnd = { onCommitTransform(image) }
                            ) { change, dragAmount ->
                                change.consume()
                                val centerX = baseWidthPx / 2f
                                val centerY = baseHeightPx / 2f
                                val currentAngle = atan2(dragAmount.y, dragAmount.x) * (180f / Math.PI.toFloat())
                                val newRotation = (image.rotation + currentAngle * 0.15f) % 360f
                                onLiveTransform(image.x, image.y, image.scale, newRotation)
                            }
                        },
                    icon = Icons.Outlined.RotateRight,
                    primaryColor = primaryColor,
                    contentDescription = "Rotate image",
                )

                // Bottom-Left Corner Indicator
                CornerDot(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset((-5).dp, 5.dp),
                    color = primaryColor,
                )

                // Bottom-Right Corner Handle: 1-finger drag to pinch/scale
                HandleControl(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(6.dp, 6.dp)
                        .pointerInput(image.id) {
                            detectDragGestures(
                                onDragEnd = { onCommitTransform(image) }
                            ) { change, dragAmount ->
                                change.consume()
                                val delta = (dragAmount.x + dragAmount.y) / (baseWidthPx * 0.8f)
                                val newScale = (image.scale + delta).coerceIn(0.25f, 4.5f)
                                onLiveTransform(image.x, image.y, newScale, image.rotation)
                            }
                        },
                    icon = Icons.Outlined.AspectRatio,
                    primaryColor = primaryColor,
                    contentDescription = "Resize image",
                )
            }
        }

        // Floating action toolbar directly attached above selected image
        if (isSelected && !isReadingMode) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-44).dp)
                    .shadow(4.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                color = surfaceColor,
                tonalElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.12f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // Layer switch button (Foreground vs Background)
                    ActionPillButton(
                        icon = Icons.Outlined.Layers,
                        label = if (image.layer == CanvasLayer.FOREGROUND) "FG" else "BG",
                        active = image.layer == CanvasLayer.FOREGROUND,
                        tint = primaryColor,
                        onClick = onToggleLayer,
                    )

                    // Reset Rotation button
                    ActionIconButton(
                        icon = Icons.Outlined.RotateRight,
                        contentDescription = "Reset rotation to 0",
                        tint = textColor,
                        onClick = {
                            val reset = image.copy(rotation = 0f)
                            onCommitTransform(reset)
                        },
                    )

                    // Reset Scale button (1x)
                    ActionIconButton(
                        icon = Icons.Outlined.AspectRatio,
                        contentDescription = "Reset scale to 1.0",
                        tint = textColor,
                        onClick = {
                            val reset = image.copy(scale = 1.0f)
                            onCommitTransform(reset)
                        },
                    )

                    // Delete button
                    ActionIconButton(
                        icon = Icons.Outlined.Delete,
                        contentDescription = "Delete image",
                        tint = Color(0xFFDC2626),
                        onClick = onDelete,
                    )

                    // Done / Deselect button
                    ActionIconButton(
                        icon = Icons.Outlined.Check,
                        contentDescription = "Done editing image",
                        tint = primaryColor,
                        onClick = onDeselect,
                    )
                }
            }
        }
    }
}

@Composable
private fun CornerDot(
    modifier: Modifier = Modifier,
    color: Color,
) {
    Box(
        modifier = modifier
            .size(10.dp)
            .background(Color.White, CircleShape)
            .border(2.dp, color, CircleShape)
    )
}

@Composable
private fun HandleControl(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    primaryColor: Color,
    contentDescription: String,
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .shadow(2.dp, CircleShape)
            .background(Color.White, CircleShape)
            .border(1.5.dp, primaryColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = primaryColor,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun ActionPillButton(
    icon: ImageVector,
    label: String,
    active: Boolean,
    tint: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) tint.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(14.dp))
        Text(
            text = label,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = tint,
            ),
        )
    }
}

@Composable
private fun ActionIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(16.dp),
        )
    }
}

/**
 * Compact floating layer bar shown in Edit Mode when canvas contains floating images.
 * Gives instant access to select any image (FG/BG) and see layer status at a glance.
 */
@Composable
fun FloatingImagesLayerBar(
    images: List<FloatingCanvasImage>,
    selectedImageId: String?,
    primaryColor: Color,
    surfaceColor: Color,
    onSelect: (String) -> Unit,
    onAddMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.shadow(3.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = surfaceColor.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.10f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                Icons.Outlined.PhotoLibrary,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(15.dp),
            )
            Text(
                text = "${images.size} ${if (images.size == 1) "Image" else "Images"}",
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif,
                    color = primaryColor,
                ),
            )

            images.forEachIndexed { index, img ->
                val isSelected = (selectedImageId == img.id)
                val badge = if (img.layer == CanvasLayer.FOREGROUND) "FG" else "BG"
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) primaryColor else primaryColor.copy(alpha = 0.10f),
                    modifier = Modifier.clickable { onSelect(img.id) },
                ) {
                    Text(
                        text = "#${index + 1} $badge",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            color = if (isSelected) Color.White else primaryColor,
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    )
                }
            }

            // + Add more button
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.12f))
                    .clickable(onClick = onAddMore),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = "Add image",
                    tint = primaryColor,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}


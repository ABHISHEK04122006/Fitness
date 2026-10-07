package com.example.fitness.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.fitness.core.components.FitnessIcons
import com.example.fitness.core.designsystem.FitTheme
import com.example.fitness.cropImageBytes
import com.example.fitness.decodeByteArrayToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Dedicated profile photo cropping dialog matching ATHGYM design.
 *
 * Provides:
 * - 1:1 fixed square crop viewport.
 * - Circular avatar preview overlay guideline.
 * - Buttery-smooth pan (drag) and zoom (pinch + slider).
 * - Aspect-ratio locked, distortion-free rendering.
 * - [Cancel], [Reset], and [Crop & Save] actions.
 * - Generates high-quality square cropped JPEG byte array for upload.
 */
@Composable
fun ProfilePhotoCropDialog(
    imageBytes: ByteArray,
    onConfirmCrop: (croppedBytes: ByteArray) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Decode image bitmap to get natural dimensions
    val imageBitmap = remember(imageBytes) {
        decodeByteArrayToBitmap(imageBytes)
    }

    var zoom by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }
    var actualViewportPx by remember { mutableFloatStateOf(0f) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = {
            if (!isProcessing) onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isProcessing,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = Color(0xFF000000)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // ==========================================
                // 1. TOP HEADER: Back/Cancel, Title, Reset
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Close / Cancel icon
                    Surface(
                        onClick = { if (!isProcessing) onDismiss() },
                        shape = CircleShape,
                        color = Color(0xFF161E24),
                        border = BorderStroke(1.dp, Color(0xFF2B3A42)),
                        modifier = Modifier
                            .size(40.dp)
                            .semantics { contentDescription = "Cancel Crop" }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = FitnessIcons.Close,
                                contentDescription = "Cancel",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Crop Photo",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Square 1:1 Avatar",
                            fontSize = 11.sp,
                            color = FitTheme.colors.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Reset action
                    Surface(
                        onClick = {
                            zoom = 1.0f
                            panOffsetX = 0f
                            panOffsetY = 0f
                            errorMessage = null
                        },
                        enabled = !isProcessing && (zoom != 1.0f || panOffsetX != 0f || panOffsetY != 0f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF161E24),
                        border = BorderStroke(1.dp, Color(0xFF2B3A42)),
                        modifier = Modifier.semantics { contentDescription = "Reset Crop" }
                    ) {
                        Text(
                            text = "Reset",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (zoom != 1.0f || panOffsetX != 0f || panOffsetY != 0f) {
                                Color.White
                            } else {
                                Color(0xFF5A6E7B)
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ==========================================
                // 2. MAIN CROP VIEWPORT STAGING AREA
                // ==========================================
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageBitmap == null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Unable to load selected image",
                                color = Color(0xFFFF5252),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "The file format may not be supported.",
                                color = Color(0xFF8E9BA4),
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        // Calculate viewport size (fixed square 1:1)
                        val maxAvailableWidth = maxWidth - 24.dp
                        val maxAvailableHeight = maxHeight - 120.dp
                        val viewportSizeDp = minOf(maxAvailableWidth, maxAvailableHeight, 320.dp)
                        val viewportSizePx = with(density) { viewportSizeDp.toPx() }
                        actualViewportPx = viewportSizePx

                        val (baseWidthDp, baseHeightDp) = remember(imageBitmap.width, imageBitmap.height, viewportSizeDp) {
                            val (bw, bh) = ProfilePhotoCropCalculator.computeBaseDimensions(
                                imageWidth = imageBitmap.width,
                                imageHeight = imageBitmap.height,
                                viewportSize = viewportSizePx
                            )
                            Pair(
                                with(density) { bw.toDp() },
                                with(density) { bh.toDp() }
                            )
                        }

                        // Max pan bounds in pixels
                        val (maxPanX, maxPanY) = ProfilePhotoCropCalculator.computeMaxPanOffsets(
                            imageWidth = imageBitmap.width,
                            imageHeight = imageBitmap.height,
                            viewportSize = viewportSizePx,
                            zoom = zoom
                        )

                        // Clamp offsets whenever zoom changes
                        panOffsetX = panOffsetX.coerceIn(-maxPanX, maxPanX)
                        panOffsetY = panOffsetY.coerceIn(-maxPanY, maxPanY)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Fixed Square Crop Viewport
                            Box(
                                modifier = Modifier
                                    .size(viewportSizeDp)
                                    .clip(RectangleShape)
                                    .background(Color(0xFF050709))
                                    .pointerInput(imageBitmap) {
                                        detectTransformGestures { _, pan, zoomFactor, _ ->
                                            zoom = (zoom * zoomFactor).coerceIn(1.0f, 4.0f)
                                            val (currMaxX, currMaxY) = ProfilePhotoCropCalculator.computeMaxPanOffsets(
                                                imageWidth = imageBitmap.width,
                                                imageHeight = imageBitmap.height,
                                                viewportSize = viewportSizePx,
                                                zoom = zoom
                                            )
                                            panOffsetX = (panOffsetX + pan.x).coerceIn(-currMaxX, currMaxX)
                                            panOffsetY = (panOffsetY + pan.y).coerceIn(-currMaxY, currMaxY)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                // 1. Rendered Image with GPU-accelerated graphicsLayer
                                Image(
                                    bitmap = imageBitmap,
                                    contentDescription = "Profile Photo Crop Preview",
                                    modifier = Modifier
                                        .size(baseWidthDp, baseHeightDp)
                                        .graphicsLayer {
                                            scaleX = zoom
                                            scaleY = zoom
                                            translationX = panOffsetX
                                            translationY = panOffsetY
                                        }
                                )

                                // 2. Faint 3x3 Rule-of-Thirds Grid
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val stepW = size.width / 3f
                                    val stepH = size.height / 3f
                                    val gridColor = Color.White.copy(alpha = 0.15f)

                                    drawLine(gridColor, Offset(stepW, 0f), Offset(stepW, size.height), strokeWidth = 1f)
                                    drawLine(gridColor, Offset(stepW * 2, 0f), Offset(stepW * 2, size.height), strokeWidth = 1f)
                                    drawLine(gridColor, Offset(0f, stepH), Offset(size.width, stepH), strokeWidth = 1f)
                                    drawLine(gridColor, Offset(0f, stepH * 2), Offset(size.width, stepH * 2), strokeWidth = 1f)
                                }

                                // 3. Circular Avatar Preview Guideline
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(
                                            border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.45f)),
                                            shape = CircleShape
                                        )
                                )

                                // 4. Neon-Green Square Crop Boundary Frame
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(
                                            border = BorderStroke(2.dp, FitTheme.colors.primary.copy(alpha = 0.9f)),
                                            shape = RectangleShape
                                        )
                                )

                                // 5. L-shaped Corner Accent Markers
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeW = 4.dp.toPx()
                                    val cornerLen = 18.dp.toPx()
                                    val accentColor = Color(0xFFA8FF3E)

                                    // Top-Left
                                    drawLine(accentColor, Offset(0f, 0f), Offset(cornerLen, 0f), strokeWidth = strokeW)
                                    drawLine(accentColor, Offset(0f, 0f), Offset(0f, cornerLen), strokeWidth = strokeW)

                                    // Top-Right
                                    drawLine(accentColor, Offset(size.width, 0f), Offset(size.width - cornerLen, 0f), strokeWidth = strokeW)
                                    drawLine(accentColor, Offset(size.width, 0f), Offset(size.width, cornerLen), strokeWidth = strokeW)

                                    // Bottom-Left
                                    drawLine(accentColor, Offset(0f, size.height), Offset(cornerLen, size.height), strokeWidth = strokeW)
                                    drawLine(accentColor, Offset(0f, size.height), Offset(0f, size.height - cornerLen), strokeWidth = strokeW)

                                    // Bottom-Right
                                    drawLine(accentColor, Offset(size.width, size.height), Offset(size.width - cornerLen, size.height), strokeWidth = strokeW)
                                    drawLine(accentColor, Offset(size.width, size.height), Offset(size.width, size.height - cornerLen), strokeWidth = strokeW)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Gesture Hint Text
                            Text(
                                text = "Drag to position • Pinch or slide to zoom",
                                fontSize = 12.sp,
                                color = Color(0xFF8E9BA4),
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // ==========================================
                            // 3. ZOOM SLIDER CONTROLS
                            // ==========================================
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Zoom Out Button
                                Surface(
                                    onClick = { zoom = (zoom - 0.25f).coerceIn(1.0f, 4.0f) },
                                    enabled = !isProcessing && zoom > 1.0f,
                                    shape = CircleShape,
                                    color = Color(0xFF161E24),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "−",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (zoom > 1.0f) Color.White else Color(0xFF5A6E7B)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Slider
                                Slider(
                                    value = zoom,
                                    onValueChange = { zoom = it },
                                    valueRange = 1.0f..4.0f,
                                    modifier = Modifier.weight(1f),
                                    enabled = !isProcessing,
                                    colors = SliderDefaults.colors(
                                        thumbColor = FitTheme.colors.primary,
                                        activeTrackColor = FitTheme.colors.primary,
                                        inactiveTrackColor = Color(0xFF1E262C)
                                    )
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                // Zoom In Button
                                Surface(
                                    onClick = { zoom = (zoom + 0.25f).coerceIn(1.0f, 4.0f) },
                                    enabled = !isProcessing && zoom < 4.0f,
                                    shape = CircleShape,
                                    color = Color(0xFF161E24),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "+",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (zoom < 4.0f) Color.White else Color(0xFF5A6E7B)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Percentage Label
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF161E24),
                                    border = BorderStroke(1.dp, Color(0xFF26333D))
                                ) {
                                    Text(
                                        text = "${(zoom * 100).toInt()}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FitTheme.colors.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF2B1216),
                        border = BorderStroke(1.dp, Color(0xFF5A1E24)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            fontSize = 12.sp,
                            color = Color(0xFFFF5252),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ==========================================
                // 4. ACTION BUTTONS: Cancel & Crop & Save
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    Surface(
                        onClick = { if (!isProcessing) onDismiss() },
                        enabled = !isProcessing,
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF161E24),
                        border = BorderStroke(1.dp, Color(0xFF2B3A42)),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .semantics { contentDescription = "Cancel Crop" }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Cancel",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Crop & Save Button
                    Surface(
                        onClick = {
                            if (imageBitmap == null) {
                                onDismiss()
                                return@Surface
                            }
                            isProcessing = true
                            errorMessage = null

                            coroutineScope.launch {
                                try {
                                    val croppedBytes = withContext(Dispatchers.Default) {
                                        val effectiveViewportPx = if (actualViewportPx > 0f) actualViewportPx else with(density) { 300.dp.toPx() }
                                        val normRect = ProfilePhotoCropCalculator.computeNormalizedCropRect(
                                            imageWidth = imageBitmap.width,
                                            imageHeight = imageBitmap.height,
                                            viewportSize = effectiveViewportPx,
                                            zoom = zoom,
                                            panOffsetX = panOffsetX,
                                            panOffsetY = panOffsetY
                                        )
                                        cropImageBytes(
                                            sourceBytes = imageBytes,
                                            cropRect = normRect,
                                            targetMaxDimension = 720
                                        )
                                    }

                                    if (croppedBytes != null && croppedBytes.isNotEmpty()) {
                                        onConfirmCrop(croppedBytes)
                                    } else {
                                        errorMessage = "Image crop failed. Please try again."
                                        isProcessing = false
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Error during cropping: ${e.message ?: "Unknown error"}"
                                    isProcessing = false
                                }
                            }
                        },
                        enabled = !isProcessing && imageBitmap != null,
                        shape = RoundedCornerShape(14.dp),
                        color = FitTheme.colors.primary,
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp)
                            .semantics { contentDescription = "Crop & Save" }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isProcessing) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.Black,
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Cropping...",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            } else {
                                Text(
                                    text = "Crop & Save",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

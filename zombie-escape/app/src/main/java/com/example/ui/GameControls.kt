package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Direction
import com.example.ui.theme.CautionAmber
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.StaminaCyan
import com.example.ui.theme.ToxicGreen
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Feature 12 & Touch Response Enhancement:
 * Ultra-responsive touch controls with zero-latency pointer input,
 * continuous thumb tracking, and smooth direction switching.
 */
@Composable
fun GameTouchControls(
    modifier: Modifier = Modifier,
    onDirectionChange: (Direction) -> Unit,
    isSprinting: Boolean,
    onSprintToggle: (Boolean) -> Unit,
    decoysRemaining: Int,
    onThrowDecoy: () -> Unit,
    flashlightOn: Boolean,
    onToggleFlashlight: () -> Unit,
    sprintLabel: String,
    decoyLabel: String
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // High-responsiveness Virtual D-Pad / Thumbpad
        ResponsiveDPad(
            onDirectionChange = onDirectionChange,
            modifier = Modifier.testTag("virtual_dpad")
        )

        // Action Buttons (Flashlight, Decoy Flare, Sprint)
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Flashlight Toggle Button (Zero latency down event)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (flashlightOn) CautionAmber.copy(alpha = 0.25f) else DarkSurfaceElevated)
                    .border(
                        1.5.dp,
                        if (flashlightOn) CautionAmber else Color.Gray.copy(alpha = 0.4f),
                        CircleShape
                    )
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown()
                            onToggleFlashlight()
                        }
                    }
                    .testTag("flashlight_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlashlightOn,
                    contentDescription = "Flashlight",
                    tint = if (flashlightOn) CautionAmber else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Throw Decoy Flare Button (Instant down trigger)
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = if (decoysRemaining > 0) CautionAmber else Color.Gray,
                        contentColor = Color.Black
                    ) {
                        Text(
                            text = "$decoysRemaining",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (decoysRemaining > 0) CautionAmber.copy(alpha = 0.2f) else DarkSurfaceElevated)
                        .border(
                            2.dp,
                            if (decoysRemaining > 0) CautionAmber else Color.Gray.copy(alpha = 0.3f),
                            CircleShape
                        )
                        .pointerInput(decoysRemaining) {
                            if (decoysRemaining > 0) {
                                awaitEachGesture {
                                    awaitFirstDown()
                                    onThrowDecoy()
                                }
                            }
                        }
                        .testTag("decoy_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = decoyLabel,
                            tint = if (decoysRemaining > 0) CautionAmber else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = decoyLabel,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (decoysRemaining > 0) CautionAmber else Color.Gray
                        )
                    }
                }
            }

            // Sprint Button (Hold down with zero-latency gesture tracking)
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSprinting)
                            Brush.radialGradient(listOf(StaminaCyan, Color(0xFF0284C7)))
                        else
                            Brush.radialGradient(listOf(DarkSurfaceElevated, Color(0xFF0F172A)))
                    )
                    .border(
                        2.5.dp,
                        if (isSprinting) Color.White else StaminaCyan.copy(alpha = 0.7f),
                        CircleShape
                    )
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            onSprintToggle(true)
                            // Wait for pointer release or cancel
                            do {
                                val event = awaitPointerEvent()
                            } while (event.changes.any { it.pressed })
                            onSprintToggle(false)
                        }
                    }
                    .testTag("sprint_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                        contentDescription = sprintLabel,
                        tint = if (isSprinting) Color.Black else StaminaCyan,
                        modifier = Modifier.size(30.dp)
                    )
                    Text(
                        text = sprintLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isSprinting) Color.Black else StaminaCyan
                    )
                }
            }
        }
    }
}

/**
 * High-precision Virtual D-Pad:
 * - 0ms down latency
 * - Continuous thumb dragging across directions
 * - Active direction highlighting
 */
@Composable
fun ResponsiveDPad(
    modifier: Modifier = Modifier,
    onDirectionChange: (Direction) -> Unit
) {
    var activeDirection by remember { mutableStateOf(Direction.NONE) }
    val dpadSize = 148.dp
    val buttonSize = 44.dp

    Box(
        modifier = modifier
            .size(dpadSize)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1E293B).copy(alpha = 0.95f),
                        Color(0xFF0F172A).copy(alpha = 0.95f)
                    )
                )
            )
            .border(2.dp, Color(0xFF334155), CircleShape)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val centerPx = size.width / 2f

                    fun updateDirection(change: PointerInputChange) {
                        val dx = change.position.x - centerPx
                        val dy = change.position.y - centerPx
                        val dist = sqrt(dx * dx + dy * dy)

                        val newDir = if (dist < 16f) {
                            Direction.NONE
                        } else {
                            val angle = atan2(dy, dx) * (180f / PI.toFloat())
                            when {
                                angle in -45f..45f -> Direction.RIGHT
                                angle in 45f..135f -> Direction.DOWN
                                angle in -135f..-45f -> Direction.UP
                                else -> Direction.LEFT
                            }
                        }

                        if (newDir != activeDirection) {
                            activeDirection = newDir
                            onDirectionChange(newDir)
                        }
                    }

                    updateDirection(down)

                    // Track drag movement across D-pad continuously
                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull()
                        if (change != null && change.pressed) {
                            updateDirection(change)
                            change.consume()
                        }
                    } while (event.changes.any { it.pressed })

                    activeDirection = Direction.NONE
                    onDirectionChange(Direction.NONE)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // UP Button
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp)
                .size(buttonSize)
                .clip(RoundedCornerShape(8.dp))
                .background(if (activeDirection == Direction.UP) ToxicGreen else Color(0xFF1E293B))
                .border(1.dp, if (activeDirection == Direction.UP) Color.White else Color(0xFF475569), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Up",
                tint = if (activeDirection == Direction.UP) Color.Black else Color.White,
                modifier = Modifier.size(30.dp)
            )
        }

        // DOWN Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp)
                .size(buttonSize)
                .clip(RoundedCornerShape(8.dp))
                .background(if (activeDirection == Direction.DOWN) ToxicGreen else Color(0xFF1E293B))
                .border(1.dp, if (activeDirection == Direction.DOWN) Color.White else Color(0xFF475569), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Down",
                tint = if (activeDirection == Direction.DOWN) Color.Black else Color.White,
                modifier = Modifier.size(30.dp)
            )
        }

        // LEFT Button
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp)
                .size(buttonSize)
                .clip(RoundedCornerShape(8.dp))
                .background(if (activeDirection == Direction.LEFT) ToxicGreen else Color(0xFF1E293B))
                .border(1.dp, if (activeDirection == Direction.LEFT) Color.White else Color(0xFF475569), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Left",
                tint = if (activeDirection == Direction.LEFT) Color.Black else Color.White,
                modifier = Modifier.size(30.dp)
            )
        }

        // RIGHT Button
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
                .size(buttonSize)
                .clip(RoundedCornerShape(8.dp))
                .background(if (activeDirection == Direction.RIGHT) ToxicGreen else Color(0xFF1E293B))
                .border(1.dp, if (activeDirection == Direction.RIGHT) Color.White else Color(0xFF475569), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Right",
                tint = if (activeDirection == Direction.RIGHT) Color.Black else Color.White,
                modifier = Modifier.size(30.dp)
            )
        }

        // Center Thumb indicator
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F172A))
                .border(1.5.dp, if (activeDirection != Direction.NONE) ToxicGreen else Color(0xFF475569), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (activeDirection != Direction.NONE) ToxicGreen else Color.Gray)
            )
        }
    }
}

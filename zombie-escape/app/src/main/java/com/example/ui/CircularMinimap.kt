package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.Direction
import com.example.model.GameLevel
import com.example.model.Player
import com.example.model.TileCodes
import com.example.model.Zombie
import com.example.model.ZombieState
import com.example.ui.theme.BloodRed
import com.example.ui.theme.CautionAmber
import com.example.ui.theme.KeyGold
import com.example.ui.theme.StaminaCyan
import com.example.ui.theme.ToxicGreen
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Circular Tactical Radar Mini Map displayed at the top right corner:
 * - Circular HUD with tactical radar grid, concentric range rings, and rotating sweep line.
 * - Displays local maze geometry (walls, corridors).
 * - Real-time tracking of player position and facing direction.
 * - Real-time tracking of zombies (patrol vs alerted/chasing blips).
 * - Objective indicators: Key item (gold), Escape door (green/red), Medkits (white/red), Boosters (cyan).
 */
@Composable
fun CircularMinimap(
    level: GameLevel,
    player: Player,
    zombies: List<Zombie>,
    pulseAnim: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(86.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xF00A1622),
                        Color(0xF5050C13)
                    )
                )
            )
            .border(2.dp, ToxicGreen.copy(alpha = 0.85f), CircleShape)
            .testTag("circular_minimap"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val radarRadius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // 1. Radar Background Grid & Concentric Rings
            drawRadarBackground(this, center, radarRadius, pulseAnim)

            // 2. Maze Walls (Local area around player, ~8.5 tiles radius)
            val viewRadiusTiles = 8.5f
            val scale = (radarRadius - 4f) / viewRadiusTiles
            drawMinimapWalls(this, level, player, center, radarRadius, scale)

            // 3. Objectives & Items (Exit Door, Key, Medkits, Boosters)
            drawMinimapItems(this, level, player, center, radarRadius, scale, pulseAnim)

            // 4. Zombies (Red / Amber radar blips with alert halos)
            drawMinimapZombies(this, zombies, player, center, radarRadius, scale, pulseAnim)

            // 5. Player Survivor Blip (Center with direction cone)
            drawMinimapPlayer(this, player, center, radarRadius)

            // 6. Tactical Bezel Accents & North Indicator
            drawRadarBezel(this, center, radarRadius)
        }
    }
}

private fun drawRadarBackground(
    scope: DrawScope,
    center: Offset,
    radius: Float,
    pulseAnim: Float
) {
    // Concentric range rings
    scope.drawCircle(
        color = ToxicGreen.copy(alpha = 0.18f),
        radius = radius * 0.72f,
        center = center,
        style = Stroke(width = 1f)
    )
    scope.drawCircle(
        color = ToxicGreen.copy(alpha = 0.22f),
        radius = radius * 0.40f,
        center = center,
        style = Stroke(width = 1f)
    )

    // Crosshairs
    scope.drawLine(
        color = ToxicGreen.copy(alpha = 0.14f),
        start = Offset(center.x - radius, center.y),
        end = Offset(center.x + radius, center.y),
        strokeWidth = 1f
    )
    scope.drawLine(
        color = ToxicGreen.copy(alpha = 0.14f),
        start = Offset(center.x, center.y - radius),
        end = Offset(center.x, center.y + radius),
        strokeWidth = 1f
    )

    // Rotating Radar Sweep Beam
    val sweepAngleRad = (pulseAnim * 2f * PI).toFloat()
    val sweepEndX = center.x + cos(sweepAngleRad) * radius
    val sweepEndY = center.y + sin(sweepAngleRad) * radius

    // Faint sweep beam line
    scope.drawLine(
        color = ToxicGreen.copy(alpha = 0.45f),
        start = center,
        end = Offset(sweepEndX, sweepEndY),
        strokeWidth = 1.5f
    )

    // Soft trail sector behind sweep line
    val trailAngleRad = sweepAngleRad - 0.35f
    val trailPath = Path().apply {
        moveTo(center.x, center.y)
        lineTo(sweepEndX, sweepEndY)
        lineTo(center.x + cos(trailAngleRad) * radius, center.y + sin(trailAngleRad) * radius)
        close()
    }
    scope.drawPath(
        path = trailPath,
        color = ToxicGreen.copy(alpha = 0.08f),
        style = Fill
    )
}

private fun drawMinimapWalls(
    scope: DrawScope,
    level: GameLevel,
    player: Player,
    center: Offset,
    radarRadius: Float,
    scale: Float
) {
    val grid = level.grid
    val width = level.width
    val height = level.height

    val px = player.renderX
    val py = player.renderY

    val minX = (px - 9).toInt().coerceAtLeast(0)
    val maxX = (px + 9).toInt().coerceAtMost(width - 1)
    val minY = (py - 9).toInt().coerceAtLeast(0)
    val maxY = (py + 9).toInt().coerceAtMost(height - 1)

    val wallSize = scale * 0.95f
    val maxDistSq = (radarRadius - 4f) * (radarRadius - 4f)

    for (y in minY..maxY) {
        for (x in minX..maxX) {
            if (grid[y][x] == TileCodes.WALL) {
                val dx = (x.toFloat() - px) * scale
                val dy = (y.toFloat() - py) * scale
                val distSq = dx * dx + dy * dy

                if (distSq < maxDistSq) {
                    scope.drawRect(
                        color = Color(0xFF475569).copy(alpha = 0.85f),
                        topLeft = Offset(center.x + dx - wallSize * 0.5f, center.y + dy - wallSize * 0.5f),
                        size = Size(wallSize, wallSize)
                    )
                }
            }
        }
    }
}

private fun drawMinimapItems(
    scope: DrawScope,
    level: GameLevel,
    player: Player,
    center: Offset,
    radarRadius: Float,
    scale: Float,
    pulseAnim: Float
) {
    val px = player.renderX
    val py = player.renderY
    val boundaryRadius = radarRadius - 8f

    // 1. Key Item (if not collected yet)
    if (!player.hasKey) {
        val dx = (level.keyPos.x.toFloat() - px) * scale
        val dy = (level.keyPos.y.toFloat() - py) * scale
        val dist = sqrt(dx * dx + dy * dy)

        val (finalX, finalY) = if (dist > boundaryRadius) {
            val ratio = boundaryRadius / dist
            Pair(center.x + dx * ratio, center.y + dy * ratio)
        } else {
            Pair(center.x + dx, center.y + dy)
        }

        val keySize = 4.5f + 1.5f * sin(pulseAnim * 6f)
        // Diamond Key Marker
        val keyPath = Path().apply {
            moveTo(finalX, finalY - keySize)
            lineTo(finalX + keySize, finalY)
            lineTo(finalX, finalY + keySize)
            lineTo(finalX - keySize, finalY)
            close()
        }
        scope.drawPath(keyPath, color = KeyGold, style = Fill)
        scope.drawCircle(
            color = KeyGold.copy(alpha = 0.4f),
            radius = keySize * 1.6f,
            center = Offset(finalX, finalY),
            style = Stroke(width = 1f)
        )
    }

    // 2. Exit Door (Green if unlocked, Red if locked)
    val edx = (level.exitPos.x.toFloat() - px) * scale
    val edy = (level.exitPos.y.toFloat() - py) * scale
    val edist = sqrt(edx * edx + edy * edy)

    val (exitX, exitY) = if (edist > boundaryRadius) {
        val ratio = boundaryRadius / edist
        Pair(center.x + edx * ratio, center.y + edy * ratio)
    } else {
        Pair(center.x + edx, center.y + edy)
    }

    val exitColor = if (player.hasKey) ToxicGreen else BloodRed
    val exitSize = 5f
    scope.drawRect(
        color = exitColor,
        topLeft = Offset(exitX - exitSize * 0.5f, exitY - exitSize * 0.5f),
        size = Size(exitSize, exitSize)
    )
    scope.drawCircle(
        color = exitColor.copy(alpha = 0.5f + 0.3f * sin(pulseAnim * 5f)),
        radius = exitSize * 1.5f,
        center = Offset(exitX, exitY),
        style = Stroke(width = 1.2f)
    )

    // 3. Medkits (White/Red crosses)
    for (mPos in level.medkitPositions) {
        val mdx = (mPos.x.toFloat() - px) * scale
        val mdy = (mPos.y.toFloat() - py) * scale
        if (mdx * mdx + mdy * mdy < boundaryRadius * boundaryRadius) {
            scope.drawCircle(
                color = Color.White,
                radius = 2.5f,
                center = Offset(center.x + mdx, center.y + mdy)
            )
            scope.drawCircle(
                color = BloodRed,
                radius = 1.4f,
                center = Offset(center.x + mdx, center.y + mdy)
            )
        }
    }

    // 4. Stamina Boosters (Cyan dots)
    for (sPos in level.staminaBoosterPositions) {
        val sdx = (sPos.x.toFloat() - px) * scale
        val sdy = (sPos.y.toFloat() - py) * scale
        if (sdx * sdx + sdy * sdy < boundaryRadius * boundaryRadius) {
            scope.drawCircle(
                color = StaminaCyan,
                radius = 2.5f,
                center = Offset(center.x + sdx, center.y + sdy)
            )
        }
    }
}

private fun drawMinimapZombies(
    scope: DrawScope,
    zombies: List<Zombie>,
    player: Player,
    center: Offset,
    radarRadius: Float,
    scale: Float,
    pulseAnim: Float
) {
    val px = player.renderX
    val py = player.renderY
    val boundaryRadius = radarRadius - 6f
    val boundaryRadiusSq = boundaryRadius * boundaryRadius

    for (zombie in zombies) {
        val zdx = (zombie.renderX - px) * scale
        val zdy = (zombie.renderY - py) * scale
        val distSq = zdx * zdx + zdy * zdy

        if (distSq <= boundaryRadiusSq) {
            val zx = center.x + zdx
            val zy = center.y + zdy

            when (zombie.state) {
                ZombieState.CHASING -> {
                    // Flashing alert halo
                    scope.drawCircle(
                        color = BloodRed.copy(alpha = 0.45f + 0.35f * sin(pulseAnim * 8f)),
                        radius = 6.5f,
                        center = Offset(zx, zy),
                        style = Stroke(width = 1.2f)
                    )
                    scope.drawCircle(
                        color = BloodRed,
                        radius = 3.5f,
                        center = Offset(zx, zy)
                    )
                }
                ZombieState.SEARCHING -> {
                    scope.drawCircle(
                        color = CautionAmber,
                        radius = 3.0f,
                        center = Offset(zx, zy)
                    )
                }
                ZombieState.IDLE -> {
                    scope.drawCircle(
                        color = Color(0xFFEF4444).copy(alpha = 0.8f),
                        radius = 2.6f,
                        center = Offset(zx, zy)
                    )
                }
            }
        }
    }
}

private fun drawMinimapPlayer(
    scope: DrawScope,
    player: Player,
    center: Offset,
    radarRadius: Float
) {
    // Player Center Dot (Bright Neon Blue / Cyan)
    scope.drawCircle(
        color = Color(0xFF38BDF8),
        radius = 3.6f,
        center = center
    )
    scope.drawCircle(
        color = Color.White,
        radius = 1.6f,
        center = center
    )

    // Heading indicator cone / arrowhead
    val coneDist = 6.5f
    val coneOffset = when (player.facing) {
        Direction.UP -> Offset(0f, -coneDist)
        Direction.DOWN -> Offset(0f, coneDist)
        Direction.LEFT -> Offset(-coneDist, 0f)
        Direction.RIGHT -> Offset(coneDist, 0f)
        Direction.NONE -> Offset(0f, -coneDist)
    }

    val arrowPath = Path().apply {
        when (player.facing) {
            Direction.UP -> {
                moveTo(center.x, center.y - coneDist)
                lineTo(center.x - 3f, center.y - 2f)
                lineTo(center.x + 3f, center.y - 2f)
            }
            Direction.DOWN -> {
                moveTo(center.x, center.y + coneDist)
                lineTo(center.x - 3f, center.y + 2f)
                lineTo(center.x + 3f, center.y + 2f)
            }
            Direction.LEFT -> {
                moveTo(center.x - coneDist, center.y)
                lineTo(center.x - 2f, center.y - 3f)
                lineTo(center.x - 2f, center.y + 3f)
            }
            Direction.RIGHT, Direction.NONE -> {
                moveTo(center.x + coneDist, center.y)
                lineTo(center.x + 2f, center.y - 3f)
                lineTo(center.x + 2f, center.y + 3f)
            }
        }
        close()
    }
    scope.drawPath(arrowPath, color = Color(0xFF38BDF8), style = Fill)
}

private fun drawRadarBezel(
    scope: DrawScope,
    center: Offset,
    radius: Float
) {
    // Outer tick marks at 0°, 90°, 180°, 270°
    val tickLen = 4f
    scope.drawLine(
        color = ToxicGreen,
        start = Offset(center.x, center.y - radius),
        end = Offset(center.x, center.y - radius + tickLen),
        strokeWidth = 2f
    )
    scope.drawLine(
        color = ToxicGreen,
        start = Offset(center.x, center.y + radius - tickLen),
        end = Offset(center.x, center.y + radius),
        strokeWidth = 2f
    )
    scope.drawLine(
        color = ToxicGreen,
        start = Offset(center.x - radius, center.y),
        end = Offset(center.x - radius + tickLen, center.y),
        strokeWidth = 2f
    )
    scope.drawLine(
        color = ToxicGreen,
        start = Offset(center.x + radius - tickLen, center.y),
        end = Offset(center.x + radius, center.y),
        strokeWidth = 2f
    )

    // North Indicator Arrow at top
    val northPath = Path().apply {
        moveTo(center.x, center.y - radius + 2f)
        lineTo(center.x - 3f, center.y - radius + 7f)
        lineTo(center.x + 3f, center.y - radius + 7f)
        close()
    }
    scope.drawPath(northPath, color = Color(0xFF38BDF8), style = Fill)
}

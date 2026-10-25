package com.example.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.model.DecoyItem
import com.example.model.Direction
import com.example.model.GameLevel
import com.example.model.Player
import com.example.model.TileCodes
import com.example.model.Zombie
import com.example.model.ZombieState
import com.example.ui.theme.BloodRed
import com.example.ui.theme.CautionAmber
import com.example.ui.theme.KeyGold
import com.example.ui.theme.MetalGray
import com.example.ui.theme.PathFloor
import com.example.ui.theme.StaminaCyan
import com.example.ui.theme.ToxicGreen
import com.example.ui.theme.WallDark
import com.example.ui.theme.WallHighlight
import com.example.ui.theme.ZombieDark
import com.example.ui.theme.ZombieFlesh
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pixel Art canvas renderer:
 * 1. 2D Array maze rendering with pixel brick textures and paths.
 * 8. Animation: Smooth transition between tile coordinates (using renderX/renderY).
 * 9. UI Overlay: Zombie health bar and detection status balloon (! / ? / zzz).
 */
object PixelArtRenderer {

    fun drawMaze(
        scope: DrawScope,
        level: GameLevel,
        tileSize: Float,
        offsetX: Float,
        offsetY: Float,
        playerHasKey: Boolean,
        pulseAnim: Float
    ) {
        val grid = level.grid
        val height = level.height
        val width = level.width

        for (y in 0 until height) {
            for (x in 0 until width) {
                val tileX = offsetX + x * tileSize
                val tileY = offsetY + y * tileSize

                // Viewport culling for high performance
                if (tileX + tileSize < 0 || tileX > scope.size.width ||
                    tileY + tileSize < 0 || tileY > scope.size.height
                ) {
                    continue
                }

                when (grid[y][x]) {
                    TileCodes.WALL -> drawWallTile(scope, tileX, tileY, tileSize, x, y)
                    TileCodes.PATH -> drawPathTile(scope, tileX, tileY, tileSize, x, y)
                    TileCodes.EXIT -> drawExitTile(scope, tileX, tileY, tileSize, playerHasKey, pulseAnim)
                    TileCodes.KEY -> {
                        drawPathTile(scope, tileX, tileY, tileSize, x, y)
                        drawKeyItem(scope, tileX, tileY, tileSize, pulseAnim)
                    }
                    TileCodes.MEDKIT -> {
                        drawPathTile(scope, tileX, tileY, tileSize, x, y)
                        drawMedkitItem(scope, tileX, tileY, tileSize, pulseAnim)
                    }
                    TileCodes.STAMINA_BOOSTER -> {
                        drawPathTile(scope, tileX, tileY, tileSize, x, y)
                        drawStaminaBoosterItem(scope, tileX, tileY, tileSize, pulseAnim)
                    }
                }

                // Feature 8: Faint chunk boundary grid indicator (every 8 tiles)
                if (x % 8 == 0 && grid[y][x] != TileCodes.WALL) {
                    scope.drawLine(
                        color = Color(0xFF38BDF8).copy(alpha = 0.18f),
                        start = Offset(tileX, tileY),
                        end = Offset(tileX, tileY + tileSize),
                        strokeWidth = 1.5f
                    )
                }
                if (y % 8 == 0 && grid[y][x] != TileCodes.WALL) {
                    scope.drawLine(
                        color = Color(0xFF38BDF8).copy(alpha = 0.18f),
                        start = Offset(tileX, tileY),
                        end = Offset(tileX + tileSize, tileY),
                        strokeWidth = 1.5f
                    )
                }
            }
        }
    }

    private fun drawWallTile(
        scope: DrawScope,
        x: Float,
        y: Float,
        size: Float,
        gx: Int,
        gy: Int
    ) {
        // Base Wall
        scope.drawRect(
            color = WallDark,
            topLeft = Offset(x, y),
            size = Size(size, size)
        )

        // Pixel bevel highlight
        val bevel = size * 0.12f
        scope.drawRect(
            color = WallHighlight,
            topLeft = Offset(x, y),
            size = Size(size, bevel)
        )
        scope.drawRect(
            color = WallHighlight,
            topLeft = Offset(x, y),
            size = Size(bevel, size)
        )

        // Dark bevel shadow on bottom/right
        scope.drawRect(
            color = Color(0xFF0F1418),
            topLeft = Offset(x, y + size - bevel),
            size = Size(size, bevel)
        )
        scope.drawRect(
            color = Color(0xFF0F1418),
            topLeft = Offset(x + size - bevel, y),
            size = Size(bevel, size)
        )

        // Subtle brick line pattern for pixel aesthetic
        val midY = y + size * 0.5f
        scope.drawLine(
            color = Color(0xFF141A1E),
            start = Offset(x + bevel, midY),
            end = Offset(x + size - bevel, midY),
            strokeWidth = size * 0.06f
        )
        val mortarX = if ((gx + gy) % 2 == 0) x + size * 0.5f else x + size * 0.25f
        scope.drawLine(
            color = Color(0xFF141A1E),
            start = Offset(mortarX, y + bevel),
            end = Offset(mortarX, midY),
            strokeWidth = size * 0.05f
        )
    }

    private fun drawPathTile(
        scope: DrawScope,
        x: Float,
        y: Float,
        size: Float,
        gx: Int,
        gy: Int
    ) {
        val isAlternate = (gx + gy) % 2 == 0
        val floorColor = if (isAlternate) PathFloor else Color(0xFF1F292E)
        scope.drawRect(
            color = floorColor,
            topLeft = Offset(x, y),
            size = Size(size, size)
        )

        // Subtle grid border
        scope.drawRect(
            color = Color(0xFF161E22),
            topLeft = Offset(x, y),
            size = Size(size, size),
            style = Stroke(width = 1f)
        )
    }

    private fun drawKeyItem(
        scope: DrawScope,
        x: Float,
        y: Float,
        size: Float,
        pulse: Float
    ) {
        val bobOffset = sin(pulse * 2f * PI.toFloat()) * (size * 0.1f)
        val cx = x + size * 0.5f
        val cy = y + size * 0.5f + bobOffset
        val r = size * 0.22f

        // Glow ring
        scope.drawCircle(
            color = KeyGold.copy(alpha = 0.35f),
            radius = r * 1.6f,
            center = Offset(cx, cy)
        )

        // Key head
        scope.drawCircle(
            color = KeyGold,
            radius = r,
            center = Offset(cx, cy - r * 0.4f),
            style = Stroke(width = size * 0.08f)
        )

        // Key shaft
        scope.drawLine(
            color = KeyGold,
            start = Offset(cx, cy - r * 0.1f),
            end = Offset(cx, cy + r * 1.2f),
            strokeWidth = size * 0.08f
        )

        // Key teeth
        scope.drawLine(
            color = KeyGold,
            start = Offset(cx, cy + r * 0.7f),
            end = Offset(cx + r * 0.6f, cy + r * 0.7f),
            strokeWidth = size * 0.08f
        )
        scope.drawLine(
            color = KeyGold,
            start = Offset(cx, cy + r * 1.1f),
            end = Offset(cx + r * 0.5f, cy + r * 1.1f),
            strokeWidth = size * 0.08f
        )
    }

    private fun drawMedkitItem(
        scope: DrawScope,
        x: Float,
        y: Float,
        size: Float,
        pulse: Float
    ) {
        val cx = x + size * 0.5f
        val cy = y + size * 0.5f
        val boxW = size * 0.6f
        val boxH = size * 0.48f

        // Red glow
        scope.drawRoundRect(
            color = BloodRed.copy(alpha = 0.25f + 0.15f * sin(pulse * 3f)),
            topLeft = Offset(cx - boxW * 0.6f, cy - boxH * 0.6f),
            size = Size(boxW * 1.2f, boxH * 1.2f),
            cornerRadius = CornerRadius(size * 0.1f)
        )

        // White kit box
        scope.drawRoundRect(
            color = Color(0xFFF8FAFC),
            topLeft = Offset(cx - boxW * 0.5f, cy - boxH * 0.5f),
            size = Size(boxW, boxH),
            cornerRadius = CornerRadius(size * 0.08f)
        )

        // Red cross
        val crossThickness = size * 0.08f
        val crossLength = size * 0.26f
        scope.drawRect(
            color = BloodRed,
            topLeft = Offset(cx - crossLength * 0.5f, cy - crossThickness * 0.5f),
            size = Size(crossLength, crossThickness)
        )
        scope.drawRect(
            color = BloodRed,
            topLeft = Offset(cx - crossThickness * 0.5f, cy - crossLength * 0.5f),
            size = Size(crossThickness, crossLength)
        )
    }

    private fun drawStaminaBoosterItem(
        scope: DrawScope,
        x: Float,
        y: Float,
        size: Float,
        pulse: Float
    ) {
        val cx = x + size * 0.5f
        val cy = y + size * 0.5f
        val radius = size * 0.28f

        // Cyan electric pulse glow
        scope.drawCircle(
            color = StaminaCyan.copy(alpha = 0.35f + 0.20f * sin(pulse * 4f)),
            radius = radius * 1.5f,
            center = Offset(cx, cy)
        )

        // Energy capsule body
        scope.drawCircle(
            color = Color(0xFF0C4A6E),
            radius = radius,
            center = Offset(cx, cy)
        )
        scope.drawCircle(
            color = StaminaCyan,
            radius = radius,
            center = Offset(cx, cy),
            style = Stroke(width = size * 0.06f)
        )

        // Lightning bolt icon
        val boltPath = Path().apply {
            moveTo(cx + size * 0.04f, cy - size * 0.18f)
            lineTo(cx - size * 0.10f, cy + size * 0.02f)
            lineTo(cx - size * 0.01f, cy + size * 0.02f)
            lineTo(cx - size * 0.05f, cy + size * 0.18f)
            lineTo(cx + size * 0.12f, cy - size * 0.02f)
            lineTo(cx + size * 0.02f, cy - size * 0.02f)
            close()
        }
        scope.drawPath(boltPath, color = Color.White, style = Fill)
    }

    private fun drawExitTile(
        scope: DrawScope,
        x: Float,
        y: Float,
        size: Float,
        isUnlocked: Boolean,
        pulse: Float
    ) {
        // Base door frame
        scope.drawRect(
            color = Color(0xFF111827),
            topLeft = Offset(x, y),
            size = Size(size, size)
        )

        val activeColor = if (isUnlocked) ToxicGreen else BloodRed
        val glowAlpha = if (isUnlocked) 0.35f + 0.25f * sin(pulse * 4f) else 0.25f

        // Glow background
        scope.drawRect(
            color = activeColor.copy(alpha = glowAlpha),
            topLeft = Offset(x + size * 0.1f, y + size * 0.1f),
            size = Size(size * 0.8f, size * 0.8f)
        )

        // Door frame border
        scope.drawRect(
            color = activeColor,
            topLeft = Offset(x + size * 0.08f, y + size * 0.08f),
            size = Size(size * 0.84f, size * 0.84f),
            style = Stroke(width = size * 0.08f)
        )

        if (isUnlocked) {
            // Unlocked portal arrows
            val cx = x + size * 0.5f
            val cy = y + size * 0.5f
            val arrowPath = Path().apply {
                moveTo(cx - size * 0.2f, cy + size * 0.15f)
                lineTo(cx, cy - size * 0.2f)
                lineTo(cx + size * 0.2f, cy + size * 0.15f)
            }
            scope.drawPath(arrowPath, color = ToxicGreen, style = Stroke(width = size * 0.08f))
        } else {
            // Locked cross bars
            scope.drawLine(
                color = BloodRed,
                start = Offset(x + size * 0.15f, y + size * 0.15f),
                end = Offset(x + size * 0.85f, y + size * 0.85f),
                strokeWidth = size * 0.07f
            )
            scope.drawLine(
                color = BloodRed,
                start = Offset(x + size * 0.85f, y + size * 0.15f),
                end = Offset(x + size * 0.15f, y + size * 0.85f),
                strokeWidth = size * 0.07f
            )
        }
    }

    /**
     * Draws the player survivor character with walking bob and flashlight.
     */
    fun drawPlayer(
        scope: DrawScope,
        player: Player,
        tileSize: Float,
        offsetX: Float,
        offsetY: Float,
        pulseAnim: Float
    ) {
        val now = System.currentTimeMillis()
        val isInvulnerable = now < player.invulnerableUntilMs
        if (isInvulnerable && ((now / 120) % 2 == 0L)) {
            // Flash off during invulnerability
            return
        }

        val px = offsetX + player.renderX * tileSize + tileSize * 0.5f
        val py = offsetY + player.renderY * tileSize + tileSize * 0.5f

        // Flashlight beam
        if (player.flashlightOn) {
            drawFlashlightCone(scope, px, py, player.facing, tileSize)
        }

        val charRadius = tileSize * 0.32f
        val isWalking = player.isWalking
        val cycle = player.walkCycle

        // Animated Survivor Boots (Smooth walking strides)
        val strideDist = if (isWalking) sin(cycle) * (charRadius * 0.40f) else 0f
        val bootW = charRadius * 0.32f
        val bootH = charRadius * 0.50f
        val bootColor = Color(0xFF0F172A)
        val bootSole = Color(0xFF334155)

        // Feet stride offset relative to facing direction
        val (leftFootOffset, rightFootOffset) = when (player.facing) {
            Direction.UP, Direction.DOWN -> {
                val lateral = charRadius * 0.42f
                Pair(
                    Offset(-lateral, strideDist),
                    Offset(lateral, -strideDist)
                )
            }
            Direction.LEFT, Direction.RIGHT -> {
                val lateral = charRadius * 0.42f
                Pair(
                    Offset(strideDist, -lateral),
                    Offset(-strideDist, lateral)
                )
            }
            Direction.NONE -> {
                val lateral = charRadius * 0.42f
                Pair(
                    Offset(-lateral, strideDist),
                    Offset(lateral, -strideDist)
                )
            }
        }

        // Left Boot
        scope.drawRoundRect(
            color = bootColor,
            topLeft = Offset(px + leftFootOffset.x - bootW * 0.5f, py + leftFootOffset.y - bootH * 0.5f),
            size = Size(bootW, bootH),
            cornerRadius = CornerRadius(bootW * 0.35f)
        )
        scope.drawRoundRect(
            color = bootSole,
            topLeft = Offset(px + leftFootOffset.x - bootW * 0.35f, py + leftFootOffset.y - bootH * 0.25f),
            size = Size(bootW * 0.7f, bootH * 0.5f),
            cornerRadius = CornerRadius(bootW * 0.25f)
        )

        // Right Boot
        scope.drawRoundRect(
            color = bootColor,
            topLeft = Offset(px + rightFootOffset.x - bootW * 0.5f, py + rightFootOffset.y - bootH * 0.5f),
            size = Size(bootW, bootH),
            cornerRadius = CornerRadius(bootW * 0.35f)
        )
        scope.drawRoundRect(
            color = bootSole,
            topLeft = Offset(px + rightFootOffset.x - bootW * 0.35f, py + rightFootOffset.y - bootH * 0.25f),
            size = Size(bootW * 0.7f, bootH * 0.5f),
            cornerRadius = CornerRadius(bootW * 0.25f)
        )

        // Subtle natural body sway during walking (weight shifting left/right)
        val sway = if (isWalking) cos(cycle) * (charRadius * 0.07f) else 0f
        val (swayX, swayY) = when (player.facing) {
            Direction.UP, Direction.DOWN -> Pair(sway, 0f)
            Direction.LEFT, Direction.RIGHT -> Pair(0f, sway)
            Direction.NONE -> Pair(0f, 0f)
        }

        val bodyX = px + swayX
        val bodyY = py + swayY

        // Stamina boost active cyan aura
        if (player.staminaBoostDurationSec > 0f) {
            scope.drawCircle(
                color = StaminaCyan.copy(alpha = 0.45f + 0.25f * sin(pulseAnim * 6f)),
                radius = charRadius * 1.55f,
                center = Offset(bodyX, bodyY),
                style = Stroke(width = tileSize * 0.06f)
            )
        }

        // Flashlight glow around feet
        scope.drawCircle(
            color = Color(0xFFFFFBEB).copy(alpha = 0.18f),
            radius = tileSize * 0.75f,
            center = Offset(px, py)
        )

        // Survivor Body (Navy survivor jacket with smooth walking posture)
        scope.drawCircle(
            color = Color(0xFF1E3A8A),
            radius = charRadius,
            center = Offset(bodyX, bodyY)
        )

        // Survivor Backpack
        val backpackOffset = when (player.facing) {
            Direction.UP -> Offset(0f, charRadius * 0.7f)
            Direction.DOWN -> Offset(0f, -charRadius * 0.7f)
            Direction.LEFT -> Offset(charRadius * 0.7f, 0f)
            Direction.RIGHT -> Offset(-charRadius * 0.7f, 0f)
            Direction.NONE -> Offset(0f, -charRadius * 0.7f)
        }
        scope.drawCircle(
            color = Color(0xFF78350F), // Leather brown pack
            radius = charRadius * 0.45f,
            center = Offset(bodyX + backpackOffset.x, bodyY + backpackOffset.y)
        )

        // Survivor Head / Cap
        scope.drawCircle(
            color = Color(0xFFFDE047), // Yellow survival helmet
            radius = charRadius * 0.55f,
            center = Offset(bodyX, bodyY - charRadius * 0.1f)
        )

        // Direction visor
        val visorOffset = when (player.facing) {
            Direction.UP -> Offset(0f, -charRadius * 0.45f)
            Direction.DOWN -> Offset(0f, charRadius * 0.45f)
            Direction.LEFT -> Offset(-charRadius * 0.45f, 0f)
            Direction.RIGHT -> Offset(charRadius * 0.45f, 0f)
            Direction.NONE -> Offset(0f, charRadius * 0.45f)
        }
        scope.drawCircle(
            color = Color(0xFF0F172A),
            radius = charRadius * 0.22f,
            center = Offset(bodyX + visorOffset.x, bodyY + visorOffset.y)
        )

        // If player has key, subtle golden aura
        if (player.hasKey) {
            scope.drawCircle(
                color = KeyGold.copy(alpha = 0.35f + 0.15f * sin(pulseAnim * 4f)),
                radius = charRadius * 1.35f,
                center = Offset(bodyX, bodyY),
                style = Stroke(width = tileSize * 0.04f)
            )
        }
    }

    private fun drawFlashlightCone(
        scope: DrawScope,
        px: Float,
        py: Float,
        facing: Direction,
        tileSize: Float
    ) {
        val beamLength = tileSize * 3.8f
        val coneWidth = tileSize * 1.5f

        val (targetX, targetY, p1, p2) = when (facing) {
            Direction.UP -> QuadPoints(
                px, py - beamLength,
                Offset(px - coneWidth * 0.5f, py - beamLength),
                Offset(px + coneWidth * 0.5f, py - beamLength)
            )
            Direction.DOWN -> QuadPoints(
                px, py + beamLength,
                Offset(px - coneWidth * 0.5f, py + beamLength),
                Offset(px + coneWidth * 0.5f, py + beamLength)
            )
            Direction.LEFT -> QuadPoints(
                px - beamLength, py,
                Offset(px - beamLength, py - coneWidth * 0.5f),
                Offset(px - beamLength, py + coneWidth * 0.5f)
            )
            Direction.RIGHT, Direction.NONE -> QuadPoints(
                px + beamLength, py,
                Offset(px + beamLength, py - coneWidth * 0.5f),
                Offset(px + beamLength, py + coneWidth * 0.5f)
            )
        }

        val conePath = Path().apply {
            moveTo(px, py)
            lineTo(p1.x, p1.y)
            lineTo(p2.x, p2.y)
            close()
        }

        scope.drawPath(
            path = conePath,
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFFAED).copy(alpha = 0.28f), Color.Transparent),
                center = Offset(px, py),
                radius = beamLength * 1.1f
            )
        )
    }

    private data class QuadPoints(val tx: Float, val ty: Float, val p1: Offset, val p2: Offset)

    /**
     * Draws zombie with:
     * Feature 8: Smooth movement animation between tile coordinates
     * Feature 9: UI Overlay displaying health bar and detection status (! / ? / zzz)
     */
    fun drawZombie(
        scope: DrawScope,
        zombie: Zombie,
        tileSize: Float,
        offsetX: Float,
        offsetY: Float,
        textMeasurer: TextMeasurer,
        pulseAnim: Float
    ) {
        // Feature 8: renderX / renderY interpolates smoothly between prevPos and targetPos
        val zx = offsetX + zombie.renderX * tileSize + tileSize * 0.5f
        val zy = offsetY + zombie.renderY * tileSize + tileSize * 0.5f

        val charRadius = tileSize * 0.32f
        val isWalking = zombie.animProgress < 0.95f
        val walkBob = if (isWalking) sin(zombie.animProgress * PI.toFloat() * 2f) * (tileSize * 0.08f) else 0f

        // Zombie body (decayed green flesh)
        scope.drawCircle(
            color = ZombieDark,
            radius = charRadius * 1.05f,
            center = Offset(zx, zy + walkBob)
        )
        scope.drawCircle(
            color = ZombieFlesh,
            radius = charRadius,
            center = Offset(zx, zy + walkBob)
        )

        // Torn tattered shirt
        scope.drawCircle(
            color = Color(0xFF475569),
            radius = charRadius * 0.65f,
            center = Offset(zx, zy + walkBob)
        )

        // Zombie Head
        scope.drawCircle(
            color = ZombieFlesh,
            radius = charRadius * 0.5f,
            center = Offset(zx, zy + walkBob - charRadius * 0.2f)
        )

        // Menacing glowing red eyes
        val eyeOffset = when (zombie.facing) {
            Direction.UP -> Offset(0f, -charRadius * 0.35f)
            Direction.DOWN -> Offset(0f, charRadius * 0.35f)
            Direction.LEFT -> Offset(-charRadius * 0.35f, 0f)
            Direction.RIGHT, Direction.NONE -> Offset(charRadius * 0.35f, 0f)
        }
        val eyeColor = if (zombie.state == ZombieState.CHASING) BloodRed else CautionAmber
        scope.drawCircle(
            color = eyeColor,
            radius = charRadius * 0.15f,
            center = Offset(zx + eyeOffset.x - charRadius * 0.15f, zy + walkBob + eyeOffset.y)
        )
        scope.drawCircle(
            color = eyeColor,
            radius = charRadius * 0.15f,
            center = Offset(zx + eyeOffset.x + charRadius * 0.15f, zy + walkBob + eyeOffset.y)
        )

        // Reaching zombie arms
        val armSwing = if (isWalking) sin(zombie.animProgress * PI.toFloat() * 4f) * (tileSize * 0.1f) else 0f
        val arm1 = Offset(zx - charRadius * 0.7f, zy + walkBob - charRadius * 0.2f + armSwing)
        val arm2 = Offset(zx + charRadius * 0.7f, zy + walkBob - charRadius * 0.2f - armSwing)
        scope.drawCircle(color = ZombieFlesh, radius = charRadius * 0.25f, center = arm1)
        scope.drawCircle(color = ZombieFlesh, radius = charRadius * 0.25f, center = arm2)

        // -------------------------------------------------------------
        // Feature 9: UI OVERLAY (Zombie Health Bar & Detection Status)
        // -------------------------------------------------------------
        val overlayBaseY = zy + walkBob - charRadius * 1.6f

        // 1. Health Bar
        val barWidth = tileSize * 0.8f
        val barHeight = tileSize * 0.12f
        val barX = zx - barWidth * 0.5f
        val barY = overlayBaseY

        // Health bar background
        scope.drawRoundRect(
            color = Color(0xFF0F172A),
            topLeft = Offset(barX, barY),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(2f)
        )

        // Health bar fill
        val healthFraction = (zombie.health.toFloat() / zombie.maxHealth.toFloat()).coerceIn(0f, 1f)
        val healthColor = when {
            healthFraction > 0.6f -> ToxicGreen
            healthFraction > 0.3f -> CautionAmber
            else -> BloodRed
        }
        if (healthFraction > 0f) {
            scope.drawRoundRect(
                color = healthColor,
                topLeft = Offset(barX + 1f, barY + 1f),
                size = Size((barWidth - 2f) * healthFraction, barHeight - 2f),
                cornerRadius = CornerRadius(1.5f)
            )
        }

        // 2. Detection Status Indicator Balloon (! / ? / zzz)
        val balloonRadius = tileSize * 0.28f
        val balloonCenterY = overlayBaseY - balloonRadius * 1.4f
        val balloonColor = zombie.state.color

        // Balloon background
        scope.drawCircle(
            color = Color(0xFF0F172A),
            radius = balloonRadius + 2f,
            center = Offset(zx, balloonCenterY)
        )
        scope.drawCircle(
            color = balloonColor,
            radius = balloonRadius,
            center = Offset(zx, balloonCenterY)
        )

        // Draw Status Icon Text inside balloon
        val statusSymbol = zombie.state.icon
        val textLayout = textMeasurer.measure(
            text = statusSymbol,
            style = TextStyle(
                color = Color.Black,
                fontSize = (balloonRadius * 1.25f).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        )
        scope.drawText(
            textLayoutResult = textLayout,
            topLeft = Offset(
                zx - textLayout.size.width * 0.5f,
                balloonCenterY - textLayout.size.height * 0.5f
            )
        )
    }

    /**
     * Draws active decoy item (flashing flare).
     */
    fun drawDecoy(
        scope: DrawScope,
        decoy: DecoyItem,
        tileSize: Float,
        offsetX: Float,
        offsetY: Float,
        pulseAnim: Float
    ) {
        val dx = offsetX + decoy.pos.x * tileSize + tileSize * 0.5f
        val dy = offsetY + decoy.pos.y * tileSize + tileSize * 0.5f

        // Sound / pulse ripple circles attracting BFS
        val waveRadius = (tileSize * 0.4f) + (pulseAnim * tileSize * 1.5f) % (tileSize * 1.8f)
        val waveAlpha = (1.0f - (waveRadius / (tileSize * 1.8f))).coerceIn(0f, 1f) * 0.6f
        scope.drawCircle(
            color = CautionAmber.copy(alpha = waveAlpha),
            radius = waveRadius,
            center = Offset(dx, dy),
            style = Stroke(width = 2f)
        )

        // Flare beacon
        scope.drawCircle(
            color = BloodRed,
            radius = tileSize * 0.2f,
            center = Offset(dx, dy)
        )
        scope.drawCircle(
            color = Color.White,
            radius = tileSize * 0.1f,
            center = Offset(dx, dy)
        )
    }
}

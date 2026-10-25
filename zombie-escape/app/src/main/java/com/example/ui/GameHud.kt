package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.LocalizedStrings
import com.example.model.GameLevel
import com.example.model.Player
import com.example.model.Zombie
import com.example.model.ZombieState
import com.example.ui.theme.BloodRed
import com.example.ui.theme.CautionAmber
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.KeyGold
import com.example.ui.theme.StaminaCyan
import com.example.ui.theme.ToxicGreen

/**
 * Feature 9: UI Overlay with Top-Right Circular Mini Map
 * Displays zombie detection status, health, stamina, lives, score, level timer, and real-time circular radar.
 */
@Composable
fun GameHud(
    modifier: Modifier = Modifier,
    level: GameLevel,
    player: Player,
    zombies: List<Zombie>,
    score: Int = 0,
    levelTimeElapsedSec: Int = 0,
    currentChunk: String = "0_0",
    pulseAnim: Float = 0f,
    localized: LocalizedStrings,
    onPauseClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Left Column: Stats, Health, Stamina, and Status Banners
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Top Stats Row: Lives, Score, Timer, Key Badge, Pause
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Lives Hearts
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, BloodRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    repeat(player.maxLives) { index ->
                        val hasLife = index < player.lives
                        Text(
                            text = if (hasLife) "❤️" else "🖤",
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 0.5.dp)
                        )
                    }
                }

                // Score Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, KeyGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "PTS $score",
                        color = KeyGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Timer Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, CautionAmber.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 5.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${levelTimeElapsedSec}s",
                        color = CautionAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Pause Button
                IconButton(
                    onClick = onPauseClick,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF475569), CircleShape)
                        .testTag("pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Key Status Chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (player.hasKey) KeyGold.copy(alpha = 0.22f) else Color(0xFF1E293B))
                    .border(
                        1.dp,
                        if (player.hasKey) KeyGold else Color.Gray.copy(alpha = 0.35f),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = "Key Status",
                    tint = if (player.hasKey) KeyGold else Color.Gray,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (player.hasKey) localized.exitUnlocked else localized.keyRequired,
                    color = if (player.hasKey) KeyGold else Color.LightGray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Player Status Bars (Health & Stamina)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Health Bar
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = localized.health,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = BloodRed
                        )
                        Text(
                            text = "${player.health}%",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(1.dp))
                    LinearProgressIndicator(
                        progress = { (player.health.toFloat() / player.maxHealth.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = BloodRed,
                        trackColor = Color(0xFF331111)
                    )
                }

                // Stamina Bar
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (player.staminaBoostDurationSec > 0f) "⚡ BOOST (${player.staminaBoostDurationSec.toInt()}s)" else localized.stamina,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = StaminaCyan
                        )
                        Text(
                            text = "${player.stamina.toInt()}%",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(1.dp))
                    LinearProgressIndicator(
                        progress = { (player.stamina / player.maxStamina).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = StaminaCyan,
                        trackColor = Color(0xFF0D252E)
                    )
                }
            }

            // Feature 9: Zombie Detection Status overlay banner
            val highestAlertZombie = zombies.maxByOrNull {
                when (it.state) {
                    ZombieState.CHASING -> 3
                    ZombieState.SEARCHING -> 2
                    ZombieState.IDLE -> 1
                }
            }

            if (highestAlertZombie != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (highestAlertZombie.state) {
                                ZombieState.CHASING -> BloodRed.copy(alpha = 0.25f)
                                ZombieState.SEARCHING -> CautionAmber.copy(alpha = 0.20f)
                                ZombieState.IDLE -> ToxicGreen.copy(alpha = 0.15f)
                            }
                        )
                        .border(
                            1.dp,
                            highestAlertZombie.state.color.copy(alpha = 0.6f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${highestAlertZombie.type.displayName} (${highestAlertZombie.state.icon})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = highestAlertZombie.state.color
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (highestAlertZombie.state) {
                                ZombieState.CHASING -> localized.zombieChasing
                                ZombieState.SEARCHING -> localized.zombieSearching
                                ZombieState.IDLE -> localized.zombieIdle
                            },
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "HP: ${highestAlertZombie.health}%",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = highestAlertZombie.state.color
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Top-Right Corner: Circular Radar Mini Map
        CircularMinimap(
            level = level,
            player = player,
            zombies = zombies,
            pulseAnim = pulseAnim,
            modifier = Modifier.size(84.dp)
        )
    }
}

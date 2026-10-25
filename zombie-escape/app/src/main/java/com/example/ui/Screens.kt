package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AnalyticsRecord
import com.example.localization.AppLanguage
import com.example.localization.LocalizedStrings
import com.example.model.FpsMode
import com.example.ui.theme.BloodRed
import com.example.ui.theme.CautionAmber
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.KeyGold
import com.example.ui.theme.MetalGray
import com.example.ui.theme.StaminaCyan
import com.example.ui.theme.ToxicGreen

@Composable
fun MainMenuScreen(
    localized: LocalizedStrings,
    currentLanguage: AppLanguage,
    onLanguageSelect: (AppLanguage) -> Unit,
    isSoundMuted: Boolean,
    onToggleSound: () -> Unit,
    onStartGame: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHowToPlay: () -> Unit,
    onOpenAnalytics: () -> Unit
) {
    var languageMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F1A12),
                        Color(0xFF090E0B)
                    )
                )
            )
            .padding(24.dp)
    ) {
        // Top right controls: Language selector, Sound toggle, and Settings shortcut
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopEnd),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Settings Shortcut
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(DarkSurfaceElevated)
                    .testTag("top_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = localized.settings,
                    tint = ToxicGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Sound Toggle
            IconButton(
                onClick = onToggleSound,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(DarkSurfaceElevated)
                    .testTag("sound_toggle_button")
            ) {
                Icon(
                    imageVector = if (isSoundMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                    contentDescription = if (isSoundMuted) localized.soundOff else localized.soundOn,
                    tint = if (isSoundMuted) Color.Gray else ToxicGreen
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Language picker
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, MetalGray, RoundedCornerShape(8.dp))
                        .clickable { languageMenuExpanded = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = localized.languageSelect,
                        tint = ToxicGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentLanguage.nativeName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                DropdownMenu(
                    expanded = languageMenuExpanded,
                    onDismissRequest = { languageMenuExpanded = false },
                    modifier = Modifier.background(DarkSurfaceElevated)
                ) {
                    AppLanguage.values().forEach { lang ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${lang.nativeName} (${lang.displayName})",
                                    color = if (lang == currentLanguage) ToxicGreen else Color.White
                                )
                            },
                            onClick = {
                                onLanguageSelect(lang)
                                languageMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Center Content: Pixel Art Title & Navigation
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pixel Art Logo / Title Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E2922))
                    .border(2.dp, ToxicGreen, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = localized.appTitle,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = ToxicGreen,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "TOP-DOWN SURVIVAL • BFS PATHFINDING AI",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CautionAmber,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Start Game / Level 1 Button
            Button(
                onClick = { onStartGame(1) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ToxicGreen,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(52.dp)
                    .testTag("start_game_button")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = localized.startEscape,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Settings Button (FPS control 30/60 FPS, Audio)
            OutlinedButton(
                onClick = onOpenSettings,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ToxicGreen),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, ToxicGreen),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(46.dp)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = localized.settings,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // How To Play Button
            OutlinedButton(
                onClick = onOpenHowToPlay,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(46.dp)
                    .testTag("how_to_play_button")
            ) {
                Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = localized.howToPlay, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Analytics & Balance Button
            OutlinedButton(
                onClick = onOpenAnalytics,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CautionAmber),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(46.dp)
                    .testTag("analytics_button")
            ) {
                Icon(imageVector = Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = localized.analytics, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Bottom Footer info
        Text(
            text = "4 ESCAPE ZONES • GRAPH BFS ALGORITHM • REAL-TIME RADAR",
            color = Color.Gray,
            fontSize = 9.sp,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun HowToPlayDialog(
    localized: LocalizedStrings,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ToxicGreen)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = localized.howToPlay,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = ToxicGreen
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = localized.howToPlayContent,
                    fontSize = 12.sp,
                    color = Color(0xFFE2E8F0),
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "OK", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Feature 2: Setting option for switching the game FPS control (30 FPS, 60 FPS) and audio
 */
@Composable
fun SettingsDialog(
    localized: LocalizedStrings,
    currentFpsMode: FpsMode,
    onSelectFpsMode: (FpsMode) -> Unit,
    isSoundMuted: Boolean,
    onToggleSound: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ToxicGreen)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = ToxicGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = localized.settings,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }

                HorizontalDivider(color = MetalGray.copy(alpha = 0.5f))

                // FPS Control Section (Feature 2)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = localized.fpsControl,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CautionAmber
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Toggle target rendering rate for battery saving or highest smoothness.",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 60 FPS Option
                    FpsOptionCard(
                        title = "60 FPS",
                        subtitle = localized.fps60,
                        isSelected = currentFpsMode == FpsMode.FPS_60,
                        onClick = { onSelectFpsMode(FpsMode.FPS_60) },
                        testTag = "fps_60_option"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 30 FPS Option
                    FpsOptionCard(
                        title = "30 FPS",
                        subtitle = localized.fps30,
                        isSelected = currentFpsMode == FpsMode.FPS_30,
                        onClick = { onSelectFpsMode(FpsMode.FPS_30) },
                        testTag = "fps_30_option"
                    )
                }

                HorizontalDivider(color = MetalGray.copy(alpha = 0.5f))

                // Sound Toggle Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E293B))
                        .clickable { onToggleSound() }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isSoundMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = if (isSoundMuted) Color.Gray else ToxicGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isSoundMuted) localized.soundOff else localized.soundOn,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Switch(
                        checked = !isSoundMuted,
                        onCheckedChange = { onToggleSound() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = ToxicGreen,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color(0xFF334155)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "DONE", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun FpsOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) ToxicGreen.copy(alpha = 0.15f) else Color(0xFF1E293B))
            .border(
                1.5.dp,
                if (isSelected) ToxicGreen else Color(0xFF334155),
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = if (isSelected) ToxicGreen else Color.White
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = if (isSelected) Color(0xFFCBD5E1) else Color.Gray
            )
        }

        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = ToxicGreen,
                unselectedColor = Color.Gray
            )
        )
    }
}

@Composable
fun PauseDialog(
    localized: LocalizedStrings,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onOpenSettings: () -> Unit,
    onMainMenu: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, MetalGray)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "PAUSED",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = CautionAmber
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen, contentColor = Color.Black)
                ) {
                    Text(text = localized.resume, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ToxicGreen)
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = localized.settings, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(text = localized.restart, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onMainMenu,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BloodRed)
                ) {
                    Text(text = localized.mainMenu, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LevelClearedDialog(
    localized: LocalizedStrings,
    levelNumber: Int,
    timeElapsedSec: Int,
    score: Int = 0,
    livesRemaining: Int = 3,
    onNextLevel: () -> Unit,
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("level_cleared_dialog"),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, ToxicGreen)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = KeyGold,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = localized.levelCleared,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = ToxicGreen
                )
                Text(
                    text = "Sector $levelNumber Escaped in ${timeElapsedSec}s!",
                    fontSize = 13.sp,
                    color = Color.LightGray
                )

                // Stats summary row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F1A12))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "SCORE", fontSize = 10.sp, color = Color.Gray)
                        Text(text = "$score", fontSize = 14.sp, fontWeight = FontWeight.Black, color = KeyGold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "LIVES", fontSize = 10.sp, color = Color.Gray)
                        Text(text = "❤️ x$livesRemaining", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BloodRed)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Requirement 6: Button 1 - Next Level
                Button(
                    onClick = onNextLevel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("next_level_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen, contentColor = Color.Black)
                ) {
                    Text(text = localized.nextLevel, fontWeight = FontWeight.Black)
                }

                // Requirement 6: Button 2 - Play Again (restarts the level again)
                OutlinedButton(
                    onClick = onPlayAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("play_again_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ToxicGreen)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Play Again", fontWeight = FontWeight.Bold)
                }

                // Requirement 6: Button 3 - Main Menu
                OutlinedButton(
                    onClick = onMainMenu,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("main_menu_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(text = localized.mainMenu, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun GameOverDialog(
    localized: LocalizedStrings,
    levelNumber: Int,
    onRetry: () -> Unit,
    onMainMenu: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, BloodRed)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = localized.gameOver,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = BloodRed
                )
                Text(
                    text = "Zombie caught you in Sector $levelNumber!",
                    fontSize = 13.sp,
                    color = Color.LightGray
                )
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BloodRed, contentColor = Color.White)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = localized.tryAgain, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onMainMenu,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(text = localized.mainMenu, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Feature 15: No of level are 4 after game get over.
 * Final Victory Screen displayed upon escaping Level 4!
 */
@Composable
fun VictoryScreen(
    localized: LocalizedStrings,
    totalTimeSec: Int,
    score: Int = 0,
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit,
    onOpenAnalytics: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0E2415),
                        Color(0xFF08120B)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = KeyGold,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = localized.victoryTitle,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = ToxicGreen,
                textAlign = TextAlign.Center
            )
            Text(
                text = localized.victoryDesc,
                fontSize = 13.sp,
                color = Color(0xFFCBD5E1),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Total Escape Time: ${totalTimeSec}s Across 3 Sectors • Final Score: $score",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = CautionAmber,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onPlayAgain,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ToxicGreen, contentColor = Color.Black)
            ) {
                Text(text = "Play Again", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onMainMenu,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text(text = localized.mainMenu, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onOpenAnalytics,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CautionAmber)
            ) {
                Text(text = localized.analytics, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Feature 14: Analytics Screen
 * Displays tracked player behavior (escapes, deaths, times, steps, alerts) to help evaluate difficulty balance.
 */
@Composable
fun AnalyticsScreen(
    localized: LocalizedStrings,
    records: List<AnalyticsRecord>,
    escapeCount: Int,
    deathCount: Int,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(DarkSurfaceElevated)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = localized.analyticsTitle,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = CautionAmber
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Balance Overview Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Escapes
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = localized.totalEscapes, fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = "$escapeCount",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ToxicGreen
                    )
                }
            }
            // Deaths
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = localized.totalDeaths, fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = "$deathCount",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = BloodRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Difficulty Balance Verdict Card
        val winRate = if (escapeCount + deathCount > 0) {
            (escapeCount.toFloat() / (escapeCount + deathCount).toFloat()) * 100f
        } else {
            50f
        }

        val balanceVerdict = when {
            winRate in 35f..65f -> "Fair & Well Balanced (35-65% survival rate)"
            winRate > 65f -> "Accessible / Mastered (Player out-maneuvering BFS AI)"
            else -> "High Lethality / Hardcore (Tight corridors and aggressive pathfinding)"
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            border = androidx.compose.foundation.BorderStroke(1.dp, MetalGray)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = localized.balanceRating,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CautionAmber
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = balanceVerdict,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = localized.balanceHint,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Recent Survival Runs",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Session list
        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recorded runs yet. Play a round to log behavioral analytics!",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(records) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (record.escaped) Color(0xFF14241B) else Color(0xFF241515)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Sector ${record.levelNumber}: ${if (record.escaped) "ESCAPED" else "INFECTED"}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (record.escaped) ToxicGreen else BloodRed
                                )
                                Text(
                                    text = "Time: ${record.timeElapsedSec}s • Steps: ${record.stepsTaken} • Alerts: ${record.zombieAlertTriggers}",
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                            }
                            Text(
                                text = record.difficultyRating,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CautionAmber
                            )
                        }
                    }
                }
            }
        }
    }
}

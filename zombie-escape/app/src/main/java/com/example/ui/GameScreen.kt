package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.example.game.GameViewModel
import com.example.model.Direction
import com.example.model.GameScreenState

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val localized = uiState.localized
    val textMeasurer = rememberTextMeasurer()
    var showInGameSettings by remember { mutableStateOf(false) }

    when (uiState.screenState) {
        GameScreenState.MAIN_MENU -> {
            MainMenuScreen(
                localized = localized,
                currentLanguage = uiState.language,
                onLanguageSelect = { viewModel.setLanguage(it) },
                isSoundMuted = uiState.isSoundMuted,
                onToggleSound = { viewModel.toggleSound() },
                onStartGame = { lvl -> viewModel.startLevel(lvl) },
                onOpenSettings = { viewModel.openSettings() },
                onOpenHowToPlay = { viewModel.openHowToPlay() },
                onOpenAnalytics = { viewModel.openAnalytics() }
            )
        }
        GameScreenState.SETTINGS -> {
            MainMenuScreen(
                localized = localized,
                currentLanguage = uiState.language,
                onLanguageSelect = { viewModel.setLanguage(it) },
                isSoundMuted = uiState.isSoundMuted,
                onToggleSound = { viewModel.toggleSound() },
                onStartGame = { lvl -> viewModel.startLevel(lvl) },
                onOpenSettings = {},
                onOpenHowToPlay = { viewModel.openHowToPlay() },
                onOpenAnalytics = { viewModel.openAnalytics() }
            )
            SettingsDialog(
                localized = localized,
                currentFpsMode = uiState.fpsMode,
                onSelectFpsMode = { viewModel.setFpsMode(it) },
                isSoundMuted = uiState.isSoundMuted,
                onToggleSound = { viewModel.toggleSound() },
                onDismiss = { viewModel.closeSettings() }
            )
        }
        GameScreenState.HOW_TO_PLAY -> {
            MainMenuScreen(
                localized = localized,
                currentLanguage = uiState.language,
                onLanguageSelect = { viewModel.setLanguage(it) },
                isSoundMuted = uiState.isSoundMuted,
                onToggleSound = { viewModel.toggleSound() },
                onStartGame = { lvl -> viewModel.startLevel(lvl) },
                onOpenSettings = { viewModel.openSettings() },
                onOpenHowToPlay = {},
                onOpenAnalytics = { viewModel.openAnalytics() }
            )
            HowToPlayDialog(
                localized = localized,
                onDismiss = { viewModel.closeHowToPlay() }
            )
        }
        GameScreenState.ANALYTICS -> {
            AnalyticsScreen(
                localized = localized,
                records = uiState.analyticsRecords,
                escapeCount = uiState.totalEscapes,
                deathCount = uiState.totalDeaths,
                onBack = { viewModel.closeAnalytics() }
            )
        }
        GameScreenState.VICTORY -> {
            VictoryScreen(
                localized = localized,
                totalTimeSec = uiState.totalGameTimeSec,
                score = uiState.score,
                onPlayAgain = { viewModel.startLevel(1) },
                onMainMenu = { viewModel.returnToMainMenu() },
                onOpenAnalytics = { viewModel.openAnalytics() }
            )
        }
        GameScreenState.PLAYING,
        GameScreenState.PAUSED,
        GameScreenState.LEVEL_CLEARED,
        GameScreenState.GAME_OVER -> {
            BoxWithConstraints(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color(0xFF090E0B))
                    .testTag("game_viewport_screen")
            ) {
                val density = LocalDensity.current
                val widthPx = constraints.maxWidth.toFloat()
                val heightPx = constraints.maxHeight.toFloat()

                // Optimal tile size for responsive mobile scaling
                val baseTileSize = with(density) { 48.dp.toPx() }

                // Smooth camera following with fluid interpolation
                val cameraX = uiState.cameraTargetX * baseTileSize + baseTileSize * 0.5f
                val cameraY = uiState.cameraTargetY * baseTileSize + baseTileSize * 0.5f

                val offsetX = widthPx * 0.5f - cameraX
                val offsetY = heightPx * 0.44f - cameraY

                // Pixel Art Canvas World with 0ms touch response
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(baseTileSize, offsetX, offsetY) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                fun steerToPointer(px: Float, py: Float) {
                                    val tapTileX = (px - offsetX) / baseTileSize
                                    val tapTileY = (py - offsetY) / baseTileSize
                                    val dx = tapTileX - (uiState.player.renderX + 0.5f)
                                    val dy = tapTileY - (uiState.player.renderY + 0.5f)
                                    if (kotlin.math.abs(dx) > 0.35f || kotlin.math.abs(dy) > 0.35f) {
                                        if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                                            viewModel.onDirectionInput(if (dx > 0) Direction.RIGHT else Direction.LEFT)
                                        } else {
                                            viewModel.onDirectionInput(if (dy > 0) Direction.DOWN else Direction.UP)
                                        }
                                    }
                                }
                                steerToPointer(down.position.x, down.position.y)
                                do {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull()
                                    if (change != null && change.pressed) {
                                        steerToPointer(change.position.x, change.position.y)
                                    }
                                } while (event.changes.any { it.pressed })
                                viewModel.onDirectionInput(Direction.NONE)
                            }
                        }
                ) {
                    // 1. Draw 2D Array Maze (walls, paths, items, exit)
                    PixelArtRenderer.drawMaze(
                        scope = this,
                        level = uiState.currentLevel,
                        tileSize = baseTileSize,
                        offsetX = offsetX,
                        offsetY = offsetY,
                        playerHasKey = uiState.player.hasKey,
                        pulseAnim = uiState.pulseAnim
                    )

                    // 2. Draw Active Decoys (flashing lures)
                    uiState.activeDecoy?.let { decoy ->
                        PixelArtRenderer.drawDecoy(
                            scope = this,
                            decoy = decoy,
                            tileSize = baseTileSize,
                            offsetX = offsetX,
                            offsetY = offsetY,
                            pulseAnim = uiState.pulseAnim
                        )
                    }

                    // 3. Draw Player Survivor (smooth sub-tile movement)
                    PixelArtRenderer.drawPlayer(
                        scope = this,
                        player = uiState.player,
                        tileSize = baseTileSize,
                        offsetX = offsetX,
                        offsetY = offsetY,
                        pulseAnim = uiState.pulseAnim
                    )

                    // 4. Draw Zombies (Feature 8 smooth animation + Feature 9 UI Overlay)
                    for (zombie in uiState.zombies) {
                        PixelArtRenderer.drawZombie(
                            scope = this,
                            zombie = zombie,
                            tileSize = baseTileSize,
                            offsetX = offsetX,
                            offsetY = offsetY,
                            textMeasurer = textMeasurer,
                            pulseAnim = uiState.pulseAnim
                        )
                    }
                }

                // Top UI Overlay: Health, Stamina, Lives, Score, Timer, Key, Zombie AI Status Banner (No Level L1-L4 UI)
                GameHud(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding(),
                    level = uiState.currentLevel,
                    player = uiState.player,
                    zombies = uiState.zombies,
                    score = uiState.score,
                    levelTimeElapsedSec = uiState.levelTimeElapsedSec,
                    currentChunk = uiState.currentChunk,
                    pulseAnim = uiState.pulseAnim,
                    localized = localized,
                    onPauseClick = { viewModel.pauseGame() }
                )

                // Bottom Mobile Touch Controls (High-response Virtual D-Pad & Actions)
                GameTouchControls(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding(),
                    onDirectionChange = { dir -> viewModel.onDirectionInput(dir) },
                    isSprinting = uiState.player.isSprinting,
                    onSprintToggle = { sprinting -> viewModel.onSprintToggle(sprinting) },
                    decoysRemaining = uiState.player.decoysRemaining,
                    onThrowDecoy = { viewModel.throwDecoy() },
                    flashlightOn = uiState.player.flashlightOn,
                    onToggleFlashlight = { viewModel.toggleFlashlight() },
                    sprintLabel = localized.sprint,
                    decoyLabel = localized.decoy
                )

                // Dialog Modals
                when (uiState.screenState) {
                    GameScreenState.PAUSED -> {
                        PauseDialog(
                            localized = localized,
                            onResume = { viewModel.resumeGame() },
                            onRestart = { viewModel.restartCurrentLevel() },
                            onOpenSettings = { showInGameSettings = true },
                            onMainMenu = { viewModel.returnToMainMenu() }
                        )
                    }
                    GameScreenState.LEVEL_CLEARED -> {
                        LevelClearedDialog(
                            localized = localized,
                            levelNumber = uiState.currentLevelNumber,
                            timeElapsedSec = uiState.levelTimeElapsedSec,
                            score = uiState.score,
                            livesRemaining = uiState.player.lives,
                            onNextLevel = { viewModel.nextLevel() },
                            onPlayAgain = { viewModel.restartCurrentLevel() },
                            onMainMenu = { viewModel.returnToMainMenu() }
                        )
                    }
                    GameScreenState.GAME_OVER -> {
                        GameOverDialog(
                            localized = localized,
                            levelNumber = uiState.currentLevelNumber,
                            onRetry = { viewModel.restartCurrentLevel() },
                            onMainMenu = { viewModel.returnToMainMenu() }
                        )
                    }
                    else -> {}
                }

                // In-Game Settings Dialog (FPS control 30/60 FPS while paused)
                if (showInGameSettings) {
                    SettingsDialog(
                        localized = localized,
                        currentFpsMode = uiState.fpsMode,
                        onSelectFpsMode = { viewModel.setFpsMode(it) },
                        isSoundMuted = uiState.isSoundMuted,
                        onToggleSound = { viewModel.toggleSound() },
                        onDismiss = { showInGameSettings = false }
                    )
                }
            }
        }
    }
}

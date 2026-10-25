package com.example.game

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.GameSoundEngine
import com.example.data.AnalyticsDao
import com.example.data.AnalyticsRecord
import com.example.data.ZombieEscapeDatabase
import com.example.localization.AppLanguage
import com.example.localization.GameLocalization
import com.example.localization.LocalizedStrings
import com.example.model.DecoyItem
import com.example.model.Direction
import com.example.model.FpsMode
import com.example.model.GameLevel
import com.example.model.GameScreenState
import com.example.model.GridPos
import com.example.model.LevelData
import com.example.model.Player
import com.example.model.TileCodes
import com.example.model.Zombie
import com.example.model.ZombieState
import com.example.model.ZombieType
import com.example.pathfinding.BfsPathfinder
import com.example.pathfinding.GridGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

data class GameUiState(
    val screenState: GameScreenState = GameScreenState.MAIN_MENU,
    val currentLevelNumber: Int = 1,
    val currentLevel: GameLevel = LevelData.getLevel(1),
    val player: Player = Player(pos = LevelData.getLevel(1).playerSpawn),
    val zombies: List<Zombie> = emptyList(),
    val activeDecoy: DecoyItem? = null,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val isSoundMuted: Boolean = false,
    val fpsMode: FpsMode = FpsMode.FPS_60,
    val pulseAnim: Float = 0f,
    val levelTimeElapsedSec: Int = 0,
    val totalGameTimeSec: Int = 0,
    val score: Int = 0,
    val maxLevels: Int = 3,
    val currentChunk: String = "0_0",
    val stepsTaken: Int = 0,
    val alertsTriggered: Int = 0,
    val damageTaken: Int = 0,
    val sprintDurationSec: Float = 0f,
    val decoysUsed: Int = 0,
    val analyticsRecords: List<AnalyticsRecord> = emptyList(),
    val totalEscapes: Int = 0,
    val totalDeaths: Int = 0,
    val cameraTargetX: Float = 0f,
    val cameraTargetY: Float = 0f
) {
    val localized: LocalizedStrings
        get() = GameLocalization.getStrings(language)
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val soundEngine = GameSoundEngine(viewModelScope)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var analyticsDao: AnalyticsDao? = null
    private var gameLoopJob: Job? = null

    private var currentGraph: GridGraph? = null
    private var pathfinder: BfsPathfinder? = null

    // Touch input direction buffer
    private var desiredDirection: Direction = Direction.NONE

    init {
        // Initialize Room Database in background
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = ZombieEscapeDatabase.getDatabase(application)
                val dao = db.analyticsDao()
                analyticsDao = dao

                val recent = dao.getRecentRecords(20)
                val escapes = dao.getEscapeCount()
                val deaths = dao.getDeathCount()

                _uiState.update {
                    it.copy(
                        analyticsRecords = recent,
                        totalEscapes = escapes,
                        totalDeaths = deaths
                    )
                }
            } catch (_: Exception) {
                // Ignore DB initialization failures gracefully
            }
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _uiState.update { it.copy(language = lang) }
    }

    fun toggleSound() {
        val newMuted = !_uiState.value.isSoundMuted
        soundEngine.isMuted = newMuted
        _uiState.update { it.copy(isSoundMuted = newMuted) }
    }

    fun openSettings() {
        _uiState.update { it.copy(screenState = GameScreenState.SETTINGS) }
    }

    fun closeSettings() {
        _uiState.update { it.copy(screenState = GameScreenState.MAIN_MENU) }
    }

    fun setFpsMode(mode: FpsMode) {
        _uiState.update { it.copy(fpsMode = mode) }
    }

    fun openHowToPlay() {
        _uiState.update { it.copy(screenState = GameScreenState.HOW_TO_PLAY) }
    }

    fun closeHowToPlay() {
        _uiState.update { it.copy(screenState = GameScreenState.MAIN_MENU) }
    }

    fun openAnalytics() {
        refreshAnalytics()
        _uiState.update { it.copy(screenState = GameScreenState.ANALYTICS) }
    }

    fun closeAnalytics() {
        _uiState.update { it.copy(screenState = GameScreenState.MAIN_MENU) }
    }

    fun pauseGame() {
        if (_uiState.value.screenState == GameScreenState.PLAYING) {
            _uiState.update { it.copy(screenState = GameScreenState.PAUSED) }
        }
    }

    fun resumeGame() {
        if (_uiState.value.screenState == GameScreenState.PAUSED) {
            _uiState.update { it.copy(screenState = GameScreenState.PLAYING) }
        }
    }

    fun returnToMainMenu() {
        gameLoopJob?.cancel()
        _uiState.update { it.copy(screenState = GameScreenState.MAIN_MENU) }
    }

    fun startLevel(levelNum: Int) {
        val level = LevelData.getLevel(levelNum)
        val graph = GridGraph.buildFromGrid(level.grid, level.width, level.height)
        currentGraph = graph
        pathfinder = BfsPathfinder(graph)

        val carryScore = if (levelNum == 1) 0 else _uiState.value.score
        val carryLives = if (levelNum == 1) 3 else _uiState.value.player.lives.coerceAtLeast(1)

        val initialPlayer = Player(
            pos = level.playerSpawn,
            prevPos = level.playerSpawn,
            targetPos = level.playerSpawn,
            animProgress = 1.0f,
            health = 100,
            lives = carryLives,
            maxLives = 3,
            stamina = 100f,
            hasKey = false,
            decoysRemaining = 2,
            flashlightOn = true,
            invulnerableUntilMs = System.currentTimeMillis() + 1000L,
            exactX = level.playerSpawn.x.toFloat(),
            exactY = level.playerSpawn.y.toFloat(),
            walkCycle = 0f,
            isWalking = false
        )

        // Construct zombies from level spawns with their chunkId
        val initialZombies = level.zombieSpawns.mapIndexed { index, (spawnPos, zType) ->
            val chunkId = "${spawnPos.x / 8}_${spawnPos.y / 8}"
            Zombie(
                id = index + 1,
                currentPos = spawnPos,
                prevPos = spawnPos,
                targetPos = spawnPos,
                animProgress = 1.0f,
                health = 100,
                state = ZombieState.IDLE,
                type = zType,
                chunkId = chunkId,
                patrolWaypoints = listOf(
                    spawnPos,
                    GridPos(
                        (spawnPos.x + 2).coerceAtMost(level.width - 2),
                        spawnPos.y
                    ),
                    spawnPos,
                    GridPos(
                        spawnPos.x,
                        (spawnPos.y + 2).coerceAtMost(level.height - 2)
                    )
                )
            )
        }

        desiredDirection = Direction.NONE
        val startChunk = "${level.playerSpawn.x / 8}_${level.playerSpawn.y / 8}"

        _uiState.update {
            it.copy(
                screenState = GameScreenState.PLAYING,
                currentLevelNumber = levelNum,
                currentLevel = level,
                score = carryScore,
                currentChunk = startChunk,
                player = initialPlayer,
                zombies = initialZombies,
                activeDecoy = null,
                levelTimeElapsedSec = 0,
                stepsTaken = 0,
                alertsTriggered = 0,
                damageTaken = 0,
                sprintDurationSec = 0f,
                decoysUsed = 0,
                cameraTargetX = level.playerSpawn.x.toFloat(),
                cameraTargetY = level.playerSpawn.y.toFloat()
            )
        }

        startGameLoop()
    }

    fun nextLevel() {
        val next = _uiState.value.currentLevelNumber + 1
        if (next <= 3) {
            startLevel(next)
        } else {
            // Completed Level 3 -> Final Victory!
            _uiState.update { it.copy(screenState = GameScreenState.VICTORY) }
        }
    }

    fun restartCurrentLevel() {
        startLevel(_uiState.value.currentLevelNumber)
    }

    // Touch Controls: Ultra-responsive input with smooth walking direction steering
    fun onDirectionInput(dir: Direction) {
        desiredDirection = dir
        if (dir != Direction.NONE) {
            _uiState.update {
                it.copy(player = it.player.copy(facing = dir))
            }
        }
    }

    fun onSprintToggle(sprinting: Boolean) {
        _uiState.update {
            it.copy(player = it.player.copy(isSprinting = sprinting))
        }
    }

    fun toggleFlashlight() {
        _uiState.update {
            it.copy(player = it.player.copy(flashlightOn = !it.player.flashlightOn))
        }
    }

    fun throwDecoy() {
        val current = _uiState.value
        if (current.player.decoysRemaining <= 0 || current.activeDecoy != null) return

        val decoyPos = current.player.pos
        soundEngine.playDecoyThrow()

        _uiState.update {
            it.copy(
                player = it.player.copy(decoysRemaining = it.player.decoysRemaining - 1),
                activeDecoy = DecoyItem(pos = decoyPos, remainingSeconds = 6.0f),
                decoysUsed = it.decoysUsed + 1
            )
        }
    }

    /**
     * Core Game Loop with dynamic FPS control (30 FPS / 60 FPS)
     */
    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch(Dispatchers.Default) {
            var lastTimeNanos = System.nanoTime()
            var secondAccumulator = 0f
            var aiUpdateTimer = 0f

            while (isActive) {
                val now = System.nanoTime()
                val deltaSec = ((now - lastTimeNanos) / 1_000_000_000.0f).coerceIn(0.005f, 0.06f)
                lastTimeNanos = now

                val currentState = _uiState.value
                if (currentState.screenState == GameScreenState.PLAYING) {
                    secondAccumulator += deltaSec
                    if (secondAccumulator >= 1.0f) {
                        secondAccumulator -= 1.0f
                        _uiState.update {
                            it.copy(
                                levelTimeElapsedSec = it.levelTimeElapsedSec + 1,
                                totalGameTimeSec = it.totalGameTimeSec + 1
                            )
                        }
                    }

                    // Update pulse animation for UI shimmer
                    _uiState.update {
                        it.copy(pulseAnim = (it.pulseAnim + deltaSec * 1.5f) % 1.0f)
                    }

                    // 1. Update Decoy timer
                    updateDecoy(deltaSec)

                    // 2. Update Player movement and stamina with smooth physics
                    updatePlayer(deltaSec)

                    // 3. Update Zombie BFS and movement
                    aiUpdateTimer += deltaSec
                    if (aiUpdateTimer >= 0.18f) {
                        aiUpdateTimer = 0f
                        updateZombiePathfinding()
                    }
                    updateZombieAnimation(deltaSec)

                    // 4. Check collisions and damage
                    checkCollisionsAndObjectives()
                }

                // Dynamic FPS rate control (60 FPS = ~16ms, 30 FPS = ~33ms)
                delay(_uiState.value.fpsMode.frameDelayMs)
            }
        }
    }

    private fun updateDecoy(deltaSec: Float) {
        val decoy = _uiState.value.activeDecoy ?: return
        val remaining = decoy.remainingSeconds - deltaSec
        if (remaining <= 0f) {
            _uiState.update { it.copy(activeDecoy = null) }
        } else {
            _uiState.update { it.copy(activeDecoy = decoy.copy(remainingSeconds = remaining)) }
        }
    }

    private var stepDistanceAccumulator: Float = 0f

    private fun collidesWithWalls(
        px: Float,
        py: Float,
        grid: Array<IntArray>,
        width: Int,
        height: Int,
        radius: Float = 0.28f
    ): Boolean {
        // Safe boundaries check
        if (px - radius < 0.5f || px + radius > (width - 1) + 0.5f ||
            py - radius < 0.5f || py + radius > (height - 1) + 0.5f) {
            return true
        }

        val minGx = kotlin.math.floor(px - radius).toInt().coerceIn(0, width - 1)
        val maxGx = kotlin.math.ceil(px + radius).toInt().coerceIn(0, width - 1)
        val minGy = kotlin.math.floor(py - radius).toInt().coerceIn(0, height - 1)
        val maxGy = kotlin.math.ceil(py + radius).toInt().coerceIn(0, height - 1)

        for (gy in minGy..maxGy) {
            for (gx in minGx..maxGx) {
                if (grid[gy][gx] == TileCodes.WALL) {
                    val closestX = px.coerceIn(gx.toFloat() - 0.5f, gx.toFloat() + 0.5f)
                    val closestY = py.coerceIn(gy.toFloat() - 0.5f, gy.toFloat() + 0.5f)
                    val diffX = px - closestX
                    val diffY = py - closestY
                    if (diffX * diffX + diffY * diffY < radius * radius) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * Smooth walking player movement:
     * - Continuous sub-tile floating-point coordinates (no tile jumping)
     * - Smooth wall sliding along corridors
     * - Corner alignment assist to glide effortlessly through openings
     * - Striding footsteps and animated walk cycle
     */
    private fun updatePlayer(deltaSec: Float) {
        val state = _uiState.value
        val player = state.player
        val level = state.currentLevel
        val grid = level.grid
        val width = level.width
        val height = level.height

        // Stamina handling with booster support
        var stamina = player.stamina
        val staminaBoost = (player.staminaBoostDurationSec - deltaSec).coerceAtLeast(0f)
        val isSprinting = player.isSprinting && (stamina > 5f || staminaBoost > 0f)
        if (isSprinting) {
            if (staminaBoost <= 0f) {
                stamina = (stamina - deltaSec * 18f).coerceAtLeast(0f)
            }
            _uiState.update { it.copy(sprintDurationSec = it.sprintDurationSec + deltaSec) }
        } else {
            val regenRate = if (staminaBoost > 0f) 28f else 18f
            stamina = (stamina + deltaSec * regenRate).coerceAtMost(player.maxStamina)
        }

        val speed = if (isSprinting) 6.4f else 4.2f // Tiles per second
        var curX = player.exactX
        var curY = player.exactY
        val startX = curX
        val startY = curY
        var facing = player.facing

        val playerRadius = 0.28f

        if (desiredDirection != Direction.NONE) {
            facing = desiredDirection
            val moveX = desiredDirection.dx.toFloat()
            val moveY = desiredDirection.dy.toFloat()

            val dx = moveX * speed * deltaSec
            val dy = moveY * speed * deltaSec

            // 1. Move along X axis with wall collision
            if (dx != 0f) {
                if (!collidesWithWalls(curX + dx, curY, grid, width, height, playerRadius)) {
                    curX += dx
                }
            }

            // 2. Move along Y axis with wall collision
            if (dy != 0f) {
                if (!collidesWithWalls(curX, curY + dy, grid, width, height, playerRadius)) {
                    curY += dy
                }
            }

            // 3. Smooth Corridor Alignment Assist (prevents snagging on wall corners)
            if (dy != 0f && dx == 0f) {
                val corridorCenterX = kotlin.math.round(curX)
                if (kotlin.math.abs(curX - corridorCenterX) < 0.35f) {
                    val nudge = (corridorCenterX - curX) * (10f * deltaSec)
                    if (!collidesWithWalls(curX + nudge, curY, grid, width, height, playerRadius)) {
                        curX += nudge
                    }
                }
            } else if (dx != 0f && dy == 0f) {
                val corridorCenterY = kotlin.math.round(curY)
                if (kotlin.math.abs(curY - corridorCenterY) < 0.35f) {
                    val nudge = (corridorCenterY - curY) * (10f * deltaSec)
                    if (!collidesWithWalls(curX, curY + nudge, grid, width, height, playerRadius)) {
                        curY += nudge
                    }
                }
            }
        }

        val distMoved = kotlin.math.sqrt((curX - startX) * (curX - startX) + (curY - startY) * (curY - startY))
        val isWalking = distMoved > 0.0005f
        var walkCycle = player.walkCycle

        if (isWalking) {
            walkCycle += distMoved * 9.5f
            stepDistanceAccumulator += distMoved
            if (stepDistanceAccumulator >= 0.70f) {
                stepDistanceAccumulator = 0f
                soundEngine.playFootstep()
                _uiState.update { it.copy(stepsTaken = it.stepsTaken + 1) }
            }
        } else {
            walkCycle = (walkCycle % (2f * Math.PI.toFloat()))
        }

        val gridX = curX.roundToInt().coerceIn(0, width - 1)
        val gridY = curY.roundToInt().coerceIn(0, height - 1)
        val currentGridPos = GridPos(gridX, gridY)
        val playerChunk = "${gridX / 8}_${gridY / 8}"

        val updatedPlayer = player.copy(
            pos = currentGridPos,
            prevPos = currentGridPos,
            targetPos = currentGridPos,
            animProgress = 1.0f,
            exactX = curX,
            exactY = curY,
            walkCycle = walkCycle,
            isWalking = isWalking,
            stamina = stamina,
            staminaBoostDurationSec = staminaBoost,
            isSprinting = isSprinting,
            facing = facing
        )

        _uiState.update {
            it.copy(
                player = updatedPlayer,
                currentChunk = playerChunk,
                cameraTargetX = curX,
                cameraTargetY = curY
            )
        }
    }

    /**
     * Feature 11: Performance-Optimized Multi-Agent BFS Pathfinding
     * Runs single reverse-BFS flow field from player / target so all zombies query in O(1).
     */
    private fun updateZombiePathfinding() {
        val state = _uiState.value
        val player = state.player
        val pf = pathfinder ?: return
        val activeDecoy = state.activeDecoy

        // Target is Decoy if active, otherwise Player
        val targetPos = activeDecoy?.pos ?: player.pos

        // Feature 11: Single reverse-BFS pass from target
        val flowField = pf.computeMultiAgentFlowField(targetPos)

        val playerChunk = "${player.pos.x / 8}_${player.pos.y / 8}"

        val updatedZombies = state.zombies.map { zombie ->
            val isZombieInPlayerChunk = zombie.chunkId == playerChunk
            val distToPlayer = zombie.currentPos.manhattanDistance(player.pos)
            val distToDecoy = activeDecoy?.let { zombie.currentPos.manhattanDistance(it.pos) } ?: 999

            // Detection check
            var newState = zombie.state
            var newAlert = zombie.alertMeter
            var lastKnown = zombie.lastKnownPlayerPos

            // Requirement 8: Zombies only chase within their own chunk!
            if (isZombieInPlayerChunk) {
                val isPlayerDetected = distToPlayer <= zombie.type.detectionRadius
                val isDecoyDetected = distToDecoy <= (zombie.type.detectionRadius + 2)

                if (isDecoyDetected) {
                    newState = ZombieState.CHASING
                    newAlert = 1.0f
                    lastKnown = activeDecoy!!.pos
                } else if (isPlayerDetected) {
                    if (zombie.state != ZombieState.CHASING) {
                        // Feature 10: Play Zombie detection alert cue!
                        soundEngine.playZombieAlert()
                        triggerHaptic(50)
                        _uiState.update { it.copy(alertsTriggered = it.alertsTriggered + 1) }
                    }
                    newState = ZombieState.CHASING
                    newAlert = 1.0f
                    lastKnown = player.pos
                } else if (zombie.state == ZombieState.CHASING) {
                    // Lost sight within chunk, switch to SEARCHING last known position
                    newState = ZombieState.SEARCHING
                    newAlert = 0.5f
                } else if (zombie.state == ZombieState.SEARCHING && zombie.currentPos == lastKnown) {
                    // Arrived at last known, return to IDLE patrol
                    newState = ZombieState.IDLE
                    newAlert = 0.0f
                }
            } else {
                // Player is outside this zombie's chunk: break pursuit immediately!
                newState = ZombieState.IDLE
                newAlert = 0.0f
                lastKnown = null
            }

            // Determine next step using BFS
            var nextStep: GridPos? = null

            if (newState == ZombieState.CHASING) {
                // Query O(1) flow field directly
                nextStep = flowField.nextStepMatrix[zombie.currentPos.y][zombie.currentPos.x]
            } else if (newState == ZombieState.SEARCHING && lastKnown != null) {
                // Pathfind to last known player position
                val path = pf.findPath(zombie.currentPos, lastKnown)
                nextStep = path.firstOrNull()
            } else {
                // IDLE: Patrol waypoints within its chunk
                val currentWp = zombie.patrolWaypoints.getOrNull(zombie.currentWaypointIndex) ?: zombie.currentPos
                if (zombie.currentPos == currentWp) {
                    val nextWpIndex = (zombie.currentWaypointIndex + 1) % max(1, zombie.patrolWaypoints.size)
                    val nextWp = zombie.patrolWaypoints.getOrNull(nextWpIndex) ?: zombie.currentPos
                    val path = pf.findPath(zombie.currentPos, nextWp)
                    nextStep = path.firstOrNull()
                    zombie.copy(currentWaypointIndex = nextWpIndex)
                } else {
                    val path = pf.findPath(zombie.currentPos, currentWp)
                    nextStep = path.firstOrNull()
                }
            }

            // Calculate facing direction
            val facing = if (nextStep != null) {
                when {
                    nextStep.x > zombie.currentPos.x -> Direction.RIGHT
                    nextStep.x < zombie.currentPos.x -> Direction.LEFT
                    nextStep.y > zombie.currentPos.y -> Direction.DOWN
                    nextStep.y < zombie.currentPos.y -> Direction.UP
                    else -> zombie.facing
                }
            } else zombie.facing

            zombie.copy(
                state = newState,
                alertMeter = newAlert,
                lastKnownPlayerPos = lastKnown,
                facing = facing,
                targetPos = nextStep ?: zombie.currentPos
            )
        }

        _uiState.update { it.copy(zombies = updatedZombies) }
    }

    /**
     * Feature 8: Smooth Zombie Movement Animation between tile coordinates
     */
    private fun updateZombieAnimation(deltaSec: Float) {
        val zombies = _uiState.value.zombies
        val updated = zombies.map { zombie ->
            // Requirement 3: Decreased zombie speed
            val speed = zombie.type.baseSpeed * when (zombie.state) {
                ZombieState.CHASING -> 1.35f
                ZombieState.SEARCHING -> 1.0f
                ZombieState.IDLE -> 0.65f
            }

            var progress = zombie.animProgress + deltaSec * speed
            var currentPos = zombie.currentPos
            var prevPos = zombie.prevPos

            if (progress >= 1.0f) {
                progress = 1.0f
                currentPos = zombie.targetPos
                prevPos = zombie.targetPos
            }

            zombie.copy(
                currentPos = currentPos,
                prevPos = prevPos,
                animProgress = progress
            )
        }
        _uiState.update { it.copy(zombies = updated) }
    }

    /**
     * Check Item Pickups (Key, Medkit), Exit condition, and Zombie attack collisions.
     */
    private fun checkCollisionsAndObjectives() {
        val state = _uiState.value
        val player = state.player
        val level = state.currentLevel
        val grid = level.grid

        // 1. Check Key Item pickup
        if (!player.hasKey && grid[player.pos.y][player.pos.x] == TileCodes.KEY) {
            grid[player.pos.y][player.pos.x] = TileCodes.PATH
            soundEngine.playPickupKey()
            triggerHaptic(100)
            val newScore = state.score + 500
            _uiState.update {
                it.copy(
                    score = newScore,
                    player = it.player.copy(hasKey = true)
                )
            }
        }

        // 2. Check Medkit pickup (Health Regenerator)
        if (grid[player.pos.y][player.pos.x] == TileCodes.MEDKIT) {
            grid[player.pos.y][player.pos.x] = TileCodes.PATH
            soundEngine.playPickupMedkit()
            triggerHaptic(80)
            val newScore = state.score + 150
            val newHealth = (player.health + 40).coerceAtMost(100)
            _uiState.update {
                it.copy(
                    score = newScore,
                    player = it.player.copy(health = newHealth)
                )
            }
        }

        // 3. Check Stamina Booster pickup (Requirement 7)
        if (grid[player.pos.y][player.pos.x] == TileCodes.STAMINA_BOOSTER) {
            grid[player.pos.y][player.pos.x] = TileCodes.PATH
            soundEngine.playPickupKey()
            triggerHaptic(80)
            val newScore = state.score + 150
            _uiState.update {
                it.copy(
                    score = newScore,
                    player = it.player.copy(
                        stamina = 100f,
                        staminaBoostDurationSec = 8.0f
                    )
                )
            }
        }

        // 4. Check Exit reached with Key
        if (player.hasKey && grid[player.pos.y][player.pos.x] == TileCodes.EXIT) {
            soundEngine.playLevelComplete()
            triggerHaptic(200)

            // Log successful escape in Analytics
            logRunAnalytics(escaped = true)

            val levelBonus = 1000 + (player.lives * 250) + max(0, 300 - state.levelTimeElapsedSec * 5)
            val newScore = state.score + levelBonus

            // 3 levels total (Requirement 2 & 6)
            if (state.currentLevelNumber >= 3) {
                _uiState.update { it.copy(score = newScore, screenState = GameScreenState.VICTORY) }
            } else {
                _uiState.update { it.copy(score = newScore, screenState = GameScreenState.LEVEL_CLEARED) }
            }
            return
        }

        // 5. Zombie attack check
        val now = System.currentTimeMillis()
        for (zombie in state.zombies) {
            val dist = player.pos.euclideanDistance(zombie.currentPos)

            // Audio pulse when zombie is close
            if (dist < 3.0f && zombie.state == ZombieState.CHASING) {
                soundEngine.playHeartbeatPulse()
            }

            // Requirement 9: Decreased zombie attack effect (10 HP damage, 1200ms invulnerability cooldown)
            if (dist < 0.75f && now > player.invulnerableUntilMs && now - player.lastDamageTimeMs > 1200L) {
                val damage = 10
                val newHealth = player.health - damage
                soundEngine.playPlayerDamage()
                triggerHaptic(150)

                if (newHealth <= 0) {
                    if (player.lives > 1) {
                        // Lose a life, respawn at starting spawn with 100 HP and temporary invulnerability!
                        val respawnPos = level.playerSpawn
                        _uiState.update {
                            it.copy(
                                damageTaken = it.damageTaken + damage,
                                player = it.player.copy(
                                    pos = respawnPos,
                                    prevPos = respawnPos,
                                    targetPos = respawnPos,
                                    animProgress = 1.0f,
                                    exactX = respawnPos.x.toFloat(),
                                    exactY = respawnPos.y.toFloat(),
                                    walkCycle = 0f,
                                    isWalking = false,
                                    health = 100,
                                    lives = it.player.lives - 1,
                                    invulnerableUntilMs = now + 2500L,
                                    lastDamageTimeMs = now
                                ),
                                cameraTargetX = respawnPos.x.toFloat(),
                                cameraTargetY = respawnPos.y.toFloat()
                            )
                        }
                    } else {
                        // 0 lives left -> Game Over!
                        soundEngine.playGameOver()
                        logRunAnalytics(escaped = false)
                        _uiState.update {
                            it.copy(
                                damageTaken = it.damageTaken + damage,
                                player = it.player.copy(
                                    health = 0,
                                    lives = 0,
                                    lastDamageTimeMs = now
                                ),
                                screenState = GameScreenState.GAME_OVER
                            )
                        }
                        return
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            damageTaken = it.damageTaken + damage,
                            player = it.player.copy(
                                health = newHealth,
                                invulnerableUntilMs = now + 1200L,
                                lastDamageTimeMs = now
                            )
                        )
                    }
                }
            }
        }
    }

    private fun logRunAnalytics(escaped: Boolean) {
        val state = _uiState.value
        val verdict = when {
            escaped && state.levelTimeElapsedSec < 25 -> "Speedrunner"
            escaped && state.alertsTriggered <= 1 -> "Sneak Specialist"
            escaped -> "Fair & Balanced"
            state.damageTaken >= 75 -> "High Lethality"
            else -> "Hardcore Challenge"
        }

        val record = AnalyticsRecord(
            levelNumber = state.currentLevelNumber,
            escaped = escaped,
            timeElapsedSec = state.levelTimeElapsedSec,
            stepsTaken = state.stepsTaken,
            zombieAlertTriggers = state.alertsTriggered,
            damageTaken = state.damageTaken,
            sprintSeconds = state.sprintDurationSec,
            decoysUsed = state.decoysUsed,
            difficultyRating = verdict
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                analyticsDao?.insert(record)
                val recent = analyticsDao?.getRecentRecords(20) ?: emptyList()
                val escapes = analyticsDao?.getEscapeCount() ?: 0
                val deaths = analyticsDao?.getDeathCount() ?: 0
                _uiState.update {
                    it.copy(
                        analyticsRecords = recent,
                        totalEscapes = escapes,
                        totalDeaths = deaths
                    )
                }
            } catch (e: Exception) {
                // Ignore DB write errors
            }
        }
    }

    private fun refreshAnalytics() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val recent = analyticsDao?.getRecentRecords(20) ?: emptyList()
                val escapes = analyticsDao?.getEscapeCount() ?: 0
                val deaths = analyticsDao?.getDeathCount() ?: 0
                _uiState.update {
                    it.copy(
                        analyticsRecords = recent,
                        totalEscapes = escapes,
                        totalDeaths = deaths
                    )
                }
            } catch (e: Exception) {}
        }
    }

    private fun triggerHaptic(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (e: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }
}

package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CautionAmber
import com.example.ui.theme.ToxicGreen
import com.example.ui.theme.BloodRed

/**
 * Represents coordinate position on the grid.
 */
data class GridPos(val x: Int, val y: Int) {
    fun manhattanDistance(other: GridPos): Int =
        kotlin.math.abs(x - other.x) + kotlin.math.abs(y - other.y)

    fun euclideanDistance(other: GridPos): Float =
        kotlin.math.sqrt(((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y)).toFloat())
}

enum class Direction(val dx: Int, val dy: Int) {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0),
    NONE(0, 0)
}

enum class TileType(val symbol: Char) {
    WALL('#'),
    PATH('.'),
    PLAYER_SPAWN('P'),
    ZOMBIE_SPAWN('Z'),
    EXIT_DOOR('E'),
    KEY_ITEM('K'),
    MEDKIT('M'),
    DISTRACTION('D')
}

enum class ZombieState(val label: String, val icon: String, val color: Color) {
    IDLE("Patrolling", "zzz", ToxicGreen),
    SEARCHING("Alerted", "?", CautionAmber),
    CHASING("Chasing", "!", BloodRed)
}

enum class ZombieType(val displayName: String, val baseSpeed: Float, val detectionRadius: Int) {
    WALKER("Shambler", 0.65f, 6),
    RUNNER("Runner", 0.95f, 7),
    STALKER("Stalker", 0.80f, 8)
}

data class Zombie(
    val id: Int,
    val currentPos: GridPos,
    val prevPos: GridPos = currentPos,
    val targetPos: GridPos = currentPos,
    val animProgress: Float = 1.0f, // 0.0 to 1.0 smooth interpolation between prevPos and targetPos
    val health: Int = 100,
    val maxHealth: Int = 100,
    val state: ZombieState = ZombieState.IDLE,
    val alertMeter: Float = 0f, // 0.0 to 1.0
    val type: ZombieType = ZombieType.WALKER,
    val facing: Direction = Direction.DOWN,
    val chunkId: String = "${currentPos.x / 8}_${currentPos.y / 8}",
    val patrolWaypoints: List<GridPos> = emptyList(),
    val currentWaypointIndex: Int = 0,
    val lastKnownPlayerPos: GridPos? = null,
    val lastStepTimeMs: Long = 0L
) {
    val renderX: Float
        get() = prevPos.x + (targetPos.x - prevPos.x) * animProgress

    val renderY: Float
        get() = prevPos.y + (targetPos.y - prevPos.y) * animProgress
}

data class Player(
    val pos: GridPos,
    val prevPos: GridPos = pos,
    val targetPos: GridPos = pos,
    val animProgress: Float = 1.0f,
    val health: Int = 100,
    val maxHealth: Int = 100,
    val lives: Int = 3,
    val maxLives: Int = 3,
    val stamina: Float = 100f,
    val maxStamina: Float = 100f,
    val staminaBoostDurationSec: Float = 0f,
    val hasKey: Boolean = false,
    val isSprinting: Boolean = false,
    val facing: Direction = Direction.DOWN,
    val flashlightOn: Boolean = true,
    val decoysRemaining: Int = 2,
    val lastDamageTimeMs: Long = 0L,
    val invulnerableUntilMs: Long = 0L,
    val exactX: Float = pos.x.toFloat(),
    val exactY: Float = pos.y.toFloat(),
    val walkCycle: Float = 0f,
    val isWalking: Boolean = false
) {
    val renderX: Float
        get() = exactX

    val renderY: Float
        get() = exactY
}

data class DecoyItem(
    val pos: GridPos,
    val remainingSeconds: Float = 5.0f
)

enum class FpsMode(val targetFps: Int, val frameDelayMs: Long) {
    FPS_30(30, 33L),
    FPS_60(60, 16L)
}

enum class GameScreenState {
    MAIN_MENU,
    PLAYING,
    PAUSED,
    LEVEL_CLEARED,
    GAME_OVER,
    VICTORY,
    ANALYTICS,
    HOW_TO_PLAY,
    SETTINGS
}

data class GameLevel(
    val levelNumber: Int,
    val name: String,
    val description: String,
    val width: Int,
    val height: Int,
    val grid: Array<IntArray>, // 0: PATH, 1: WALL, 2: EXIT, 3: KEY, 4: MEDKIT
    val playerSpawn: GridPos,
    val zombieSpawns: List<Pair<GridPos, ZombieType>>,
    val exitPos: GridPos,
    val keyPos: GridPos,
    val medkitPositions: List<GridPos>,
    val staminaBoosterPositions: List<GridPos> = emptyList()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GameLevel) return false
        return levelNumber == other.levelNumber
    }

    override fun hashCode(): Int = levelNumber
}

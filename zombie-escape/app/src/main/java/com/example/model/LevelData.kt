package com.example.model

/**
 * Grid tile numeric constants:
 * 0: PATH
 * 1: WALL
 * 2: EXIT
 * 3: KEY
 * 4: MEDKIT (Health Regenerator)
 * 5: STAMINA_BOOSTER (Stamina Booster)
 */
object TileCodes {
    const val PATH = 0
    const val WALL = 1
    const val EXIT = 2
    const val KEY = 3
    const val MEDKIT = 4
    const val STAMINA_BOOSTER = 5
}

object LevelData {

    fun getLevel(levelNumber: Int): GameLevel {
        return when (levelNumber) {
            1 -> level1
            2 -> level2
            3 -> level3
            else -> level1
        }
    }

    private fun parseMaze(
        rawLines: List<String>,
        playerMarker: Char = 'P',
        zombieMarker: Char = 'Z',
        keyMarker: Char = 'K',
        exitMarker: Char = 'E',
        medkitMarker: Char = 'M',
        staminaMarker: Char = 'S'
    ): Pair<Array<IntArray>, ParsedEntities> {
        val height = rawLines.size
        val width = rawLines[0].length
        val grid = Array(height) { IntArray(width) }

        var playerSpawn = GridPos(1, 1)
        val zombieSpawns = mutableListOf<GridPos>()
        var keyPos = GridPos(width - 2, height - 2)
        var exitPos = GridPos(width - 2, 1)
        val medkits = mutableListOf<GridPos>()
        val staminaBoosters = mutableListOf<GridPos>()

        for (y in 0 until height) {
            val line = rawLines[y]
            for (x in 0 until width) {
                val c = if (x < line.length) line[x] else '#'
                when (c) {
                    '#' -> grid[y][x] = TileCodes.WALL
                    '.' -> grid[y][x] = TileCodes.PATH
                    playerMarker -> {
                        grid[y][x] = TileCodes.PATH
                        playerSpawn = GridPos(x, y)
                    }
                    zombieMarker -> {
                        grid[y][x] = TileCodes.PATH
                        zombieSpawns.add(GridPos(x, y))
                    }
                    keyMarker -> {
                        grid[y][x] = TileCodes.KEY
                        keyPos = GridPos(x, y)
                    }
                    exitMarker -> {
                        grid[y][x] = TileCodes.EXIT
                        exitPos = GridPos(x, y)
                    }
                    medkitMarker -> {
                        grid[y][x] = TileCodes.MEDKIT
                        medkits.add(GridPos(x, y))
                    }
                    staminaMarker -> {
                        grid[y][x] = TileCodes.STAMINA_BOOSTER
                        staminaBoosters.add(GridPos(x, y))
                    }
                    else -> grid[y][x] = TileCodes.PATH
                }
            }
        }
        return Pair(grid, ParsedEntities(playerSpawn, zombieSpawns, exitPos, keyPos, medkits, staminaBoosters))
    }

    private data class ParsedEntities(
        val playerSpawn: GridPos,
        val zombieSpawns: List<GridPos>,
        val exitPos: GridPos,
        val keyPos: GridPos,
        val medkits: List<GridPos>,
        val staminaBoosters: List<GridPos>
    )

    // Level 1: Abandoned Hospital (23x23)
    private val rawLevel1 = listOf(
        "#######################",
        "#P#...........#.#....E#",
        "#.###..####.#.#...#.#.#",
        "#...#.....#.#.#.......#",
        "###.#.#....M#.#.###.#.#",
        "#...#S#...#.#.#.....#.#",
        "#...###.###.#.#.###.#.#",
        "#.............#Z....#.#",
        "#.##.####.#####.###.###",
        "#.........#...#..M....#",
        "#####.#.#....##.#.###.#",
        "#.S...#Z..#.......#...#",
        "#.###...#.#.#####.#..##",
        "#...#...#...#..S......#",
        "#.#.###.#S#.#.###.#.#.#",
        "#.#...#.......#.......#",
        "###M#.#.#.#.#########Z#",
        "#...#.#..S#M#.......#.#",
        "#.#.#.#.#.#Z..###.#.#M#",
        "#.#...#.#...#.#...#...#",
        "#.#####.#####.#.#.##..#",
        "#.............#.#....K#",
        "#######################"
    )

    val level1: GameLevel by lazy {
        val (grid, parsed) = parseMaze(rawLevel1)
        GameLevel(
            levelNumber = 1,
            name = "Abandoned Hospital",
            description = "Quarantine Sector - Navigate the ward wings, locate the Key Card, and evacuate through the Emergency Bay.",
            width = rawLevel1[0].length,
            height = rawLevel1.size,
            grid = grid,
            playerSpawn = parsed.playerSpawn,
            zombieSpawns = parsed.zombieSpawns.mapIndexed { idx, pos ->
                val zType = if (idx % 2 == 0) ZombieType.WALKER else ZombieType.RUNNER
                pos to zType
            },
            exitPos = parsed.exitPos,
            keyPos = parsed.keyPos,
            medkitPositions = parsed.medkits,
            staminaBoosterPositions = parsed.staminaBoosters
        )
    }

    // Level 2: Underground Metro (25x25)
    private val rawLevel2 = listOf(
        "#########################",
        "#P......#...#..........E#",
        "#####.#.#.###.##.######.#",
        "#..S....#...#.#..M....#.#",
        "#.###...###.#.Z.#####...#",
        "#.....#...#.#.#.....#...#",
        "#..M#.#...#.#.##.##.#####",
        "#.......#.#.#.#...#.#...#",
        "#.#.#####.#.#.M...#.#...#",
        "#.#...#...#S..#.#.....#.#",
        "#.###...#.#.#...#.#.#.#.#",
        "#...#Z..#.M.....#.#...#Z#",
        "###.#####...#####.#####.#",
        "#...#......S..#.#.....#.#",
        "#M#######.#.....#####.#.#",
        "#...#...#.#.S.#.....#...#",
        "###.#...#.#..##..######Z#",
        "#.....#.#.#..M#.......#S#",
        "#.#.#.#.#.###.#.###.###.#",
        "#.#...#.#.....#.........#",
        "#.#####.#.#######.#######",
        "#.#...#.....#..Z#.......#",
        "#.#.#....##.#.#####...#.#",
        "#..Z#S......#.........#K#",
        "#########################"
    )

    val level2: GameLevel by lazy {
        val (grid, parsed) = parseMaze(rawLevel2)
        GameLevel(
            levelNumber = 2,
            name = "Underground Metro",
            description = "Deep Tunnels - Multiple subway tracks and maintenance corridors crawling with undead runners.",
            width = rawLevel2[0].length,
            height = rawLevel2.size,
            grid = grid,
            playerSpawn = parsed.playerSpawn,
            zombieSpawns = parsed.zombieSpawns.mapIndexed { idx, pos ->
                val zType = when (idx % 3) { 0 -> ZombieType.WALKER; 1 -> ZombieType.RUNNER; else -> ZombieType.STALKER }
                pos to zType
            },
            exitPos = parsed.exitPos,
            keyPos = parsed.keyPos,
            medkitPositions = parsed.medkits,
            staminaBoosterPositions = parsed.staminaBoosters
        )
    }

    // Level 3: Bio-Research Lab (27x27)
    private val rawLevel3 = listOf(
        "###########################",
        "#P#.......#...........#..E#",
        "#.#.#####.#..######.#.#S#.#",
        "#.#....M..#..M#.S...#.#.#.#",
        "#.#.#..##.###.#.##.##...#.#",
        "#.#.#...#...#.#.#.......#.#",
        "#.#.#.....#...#.##..#####.#",
        "#.#.....#.#.#.......M.#..Z#",
        "#.#######.###.####..#.###.#",
        "#.....S.#.#...#.....#..Z#.#",
        "#######.#.#.#.#####.###.#.#",
        "#......Z#...#.........#...#",
        "#.#.###...#Z#...##.##..S###",
        "#.#.....#...#.#.#.....#...#",
        "###.#.S##.#.#.....#...###.#",
        "#.M.#...#.#...#.#S#.....#.#",
        "#.###.M.#.###...#M#.#####.#",
        "#...#...#...#...#.#.......#",
        "#.#.#.###.#.###.#.#.#####.#",
        "#.#.#.....#.....#.......#.#",
        "#.#.###.###.#######.#.#.#.#",
        "#.#.M.#..Z#.........#...#.#",
        "###.#####.#.###.###.#Z###.#",
        "#...#...#.#.#.........#...#",
        "#.###.#..Z###.###S..#.#..##",
        "#...Z.#.......#.....#....K#",
        "###########################"
    )

    val level3: GameLevel by lazy {
        val (grid, parsed) = parseMaze(rawLevel3)
        GameLevel(
            levelNumber = 3,
            name = "Bio-Research Lab",
            description = "Contagion Core - High-security quarantine complex. Evade stalking specimens and reach final extraction!",
            width = rawLevel3[0].length,
            height = rawLevel3.size,
            grid = grid,
            playerSpawn = parsed.playerSpawn,
            zombieSpawns = parsed.zombieSpawns.mapIndexed { idx, pos ->
                val zType = when (idx % 3) { 0 -> ZombieType.WALKER; 1 -> ZombieType.RUNNER; else -> ZombieType.STALKER }
                pos to zType
            },
            exitPos = parsed.exitPos,
            keyPos = parsed.keyPos,
            medkitPositions = parsed.medkits,
            staminaBoosterPositions = parsed.staminaBoosters
        )
    }
}

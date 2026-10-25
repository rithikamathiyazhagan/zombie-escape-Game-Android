package com.example.pathfinding

import com.example.model.GridPos
import com.example.model.TileCodes

/**
 * Feature 2: Graph representation
 * Each walkable cell is treated as a node, with adjacent cells connected as edges.
 */
class GridGraph(
    val width: Int,
    val height: Int,
    val nodes: Set<GridPos>,
    val adjacencyList: Map<GridPos, List<GridPos>>
) {
    fun getNeighbors(node: GridPos): List<GridPos> =
        adjacencyList[node] ?: emptyList()

    fun isWalkable(pos: GridPos): Boolean =
        pos in nodes

    companion object {
        private val DIRECTIONS = listOf(
            GridPos(0, -1), // UP
            GridPos(0, 1),  // DOWN
            GridPos(-1, 0), // LEFT
            GridPos(1, 0)   // RIGHT
        )

        /**
         * Builds an unweighted undirected graph from the 2D grid array.
         * Any cell that is not a WALL (value != 1) is a walkable node.
         */
        fun buildFromGrid(grid: Array<IntArray>, width: Int, height: Int): GridGraph {
            val walkableNodes = mutableSetOf<GridPos>()

            // Step 1: Collect all walkable nodes
            for (y in 0 until height) {
                for (x in 0 until width) {
                    if (grid[y][x] != TileCodes.WALL) {
                        walkableNodes.add(GridPos(x, y))
                    }
                }
            }

            // Step 2: Build adjacency list connecting 4-way orthogonal neighbors
            val adjMap = mutableMapOf<GridPos, MutableList<GridPos>>()
            for (pos in walkableNodes) {
                val neighbors = mutableListOf<GridPos>()
                for (dir in DIRECTIONS) {
                    val nx = pos.x + dir.x
                    val ny = pos.y + dir.y
                    if (nx in 0 until width && ny in 0 until height) {
                        val neighborPos = GridPos(nx, ny)
                        if (neighborPos in walkableNodes) {
                            neighbors.add(neighborPos)
                        }
                    }
                }
                adjMap[pos] = neighbors
            }

            return GridGraph(width, height, walkableNodes, adjMap)
        }
    }
}

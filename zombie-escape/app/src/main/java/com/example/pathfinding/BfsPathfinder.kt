package com.example.pathfinding

import com.example.model.GridPos
import java.util.ArrayDeque

/**
 * BFS Pathfinder implementation fulfilling:
 * 3. Queue – java.util.ArrayDeque to explore the maze level by level.
 * 4. Visited Array – Array<BooleanArray> preventing duplicate visits.
 * 5. Parent Array – Array<Array<GridPos?>> storing predecessor for shortest path reconstruction.
 * 6. BFS Algorithm – Shortest path calculation.
 * 7. Pathfinding – Path reconstruction.
 * 11. Performance Optimization – Multi-agent Flow Field / Dijkstra cache.
 */
class BfsPathfinder(
    private val graph: GridGraph
) {
    private val width = graph.width
    private val height = graph.height

    /**
     * Calculates the shortest path between start and goal using standard Breadth-First Search (BFS).
     * Returns an ordered list of GridPos starting with the first step after start, leading to goal.
     */
    fun findPath(start: GridPos, goal: GridPos): List<GridPos> {
        if (start == goal) return emptyList()
        if (!graph.isWalkable(start) || !graph.isWalkable(goal)) return emptyList()

        // 3. Queue: used in BFS to explore maze level by level
        val queue: ArrayDeque<GridPos> = ArrayDeque()

        // 4. Visited Array: 2D boolean array to prevent re-visiting cells
        val visited = Array(height) { BooleanArray(width) }

        // 5. Parent Array: 2D array storing previous cell for path reconstruction
        val parent = Array(height) { arrayOfNulls<GridPos>(width) }

        // Enqueue start cell
        queue.add(start)
        visited[start.y][start.x] = true

        var found = false

        while (queue.isNotEmpty()) {
            val current = queue.poll() ?: break

            if (current == goal) {
                found = true
                break
            }

            // Explore graph neighbors
            for (neighbor in graph.getNeighbors(current)) {
                if (!visited[neighbor.y][neighbor.x]) {
                    visited[neighbor.y][neighbor.x] = true
                    parent[neighbor.y][neighbor.x] = current
                    queue.add(neighbor)
                }
            }
        }

        if (!found) return emptyList()

        // 7. Pathfinding: Reconstruct path from goal back to start using parent array
        val path = mutableListOf<GridPos>()
        var curr: GridPos? = goal

        while (curr != null && curr != start) {
            path.add(curr)
            curr = parent[curr.y][curr.x]
        }

        path.reverse()
        return path
    }

    /**
     * Feature 11: Performance Optimization for Multiple Zombies
     * Computes a multi-agent Flow Field (vector field) in a SINGLE reverse-BFS pass from the target (player).
     *
     * Instead of running N separate BFS passes for N zombies each tick (which would be O(N * (V + E))),
     * this reverse BFS runs once in O(V + E) and computes the optimal step toward the target
     * for EVERY cell in the grid.
     *
     * Any number of zombies (1, 4, 10+) can look up their next optimal move in O(1) time without lag!
     */
    data class MultiAgentFlowField(
        val target: GridPos,
        val distances: Array<IntArray>,
        val nextStepMatrix: Array<Array<GridPos?>>
    )

    fun computeMultiAgentFlowField(target: GridPos): MultiAgentFlowField {
        val distances = Array(height) { IntArray(width) { Int.MAX_VALUE } }
        val nextStep = Array(height) { arrayOfNulls<GridPos>(width) }

        if (!graph.isWalkable(target)) {
            return MultiAgentFlowField(target, distances, nextStep)
        }

        val queue: ArrayDeque<GridPos> = ArrayDeque()
        val visited = Array(height) { BooleanArray(width) }

        queue.add(target)
        visited[target.y][target.x] = true
        distances[target.y][target.x] = 0

        while (queue.isNotEmpty()) {
            val current = queue.poll() ?: break
            val currentDist = distances[current.y][current.x]

            for (neighbor in graph.getNeighbors(current)) {
                if (!visited[neighbor.y][neighbor.x]) {
                    visited[neighbor.y][neighbor.x] = true
                    distances[neighbor.y][neighbor.x] = currentDist + 1
                    // In reverse BFS, neighbor's fastest route to target is through 'current'
                    nextStep[neighbor.y][neighbor.x] = current
                    queue.add(neighbor)
                }
            }
        }

        return MultiAgentFlowField(target, distances, nextStep)
    }
}

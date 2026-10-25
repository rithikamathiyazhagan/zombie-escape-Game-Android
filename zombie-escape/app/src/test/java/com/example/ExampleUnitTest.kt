package com.example

import com.example.model.GridPos
import com.example.model.LevelData
import com.example.pathfinding.BfsPathfinder
import com.example.pathfinding.GridGraph
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testBfsPathfinding() {
    val level = LevelData.getLevel(1)
    val graph = GridGraph.buildFromGrid(level.grid, level.width, level.height)
    val pathfinder = BfsPathfinder(graph)

    val start = level.playerSpawn
    val keyPos = level.keyPos

    val path = pathfinder.findPath(start, keyPos)
    assertTrue("Path to key should exist in level 1", path.isNotEmpty())
    assertEquals("Last step in path should be the key position", keyPos, path.last())
  }

  @Test
  fun testMultiAgentFlowField() {
    val level = LevelData.getLevel(1)
    val graph = GridGraph.buildFromGrid(level.grid, level.width, level.height)
    val pathfinder = BfsPathfinder(graph)

    val flowField = pathfinder.computeMultiAgentFlowField(level.playerSpawn)
    assertNotNull(flowField)

    // Distance at player spawn should be 0
    assertEquals(0, flowField.distances[level.playerSpawn.y][level.playerSpawn.x])
  }
}


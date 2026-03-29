package com.devbyzenith.rabbitskater.game

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ObstacleTest {
    
    private lateinit var obstacle: Obstacle
    
    @Before
    fun setup() {
        obstacle = Obstacle(50f, 80f, 200f)
    }
    
    @Test
    fun `obstacle should have correct initial position`() {
        assertEquals(200f, obstacle.x, 0.01f)
    }
    
    @Test
    fun `obstacle should have correct dimensions`() {
        assertEquals(50f, obstacle.width, 0.01f)
        assertEquals(80f, obstacle.height, 0.01f)
    }
    
    @Test
    fun `update should move obstacle left`() {
        val initialX = obstacle.x
        obstacle.update(5f)
        assertTrue(obstacle.x < initialX)
    }
    
    @Test
    fun `update should move obstacle by speed amount`() {
        val speed = 5f
        obstacle.update(speed)
        assertEquals(200f - speed, obstacle.x, 0.01f)
    }
    
    @Test
    fun `obstacle can be created with explicit x position`() {
        val defaultObstacle = Obstacle(30f, 50f, 0f)
        assertEquals(0f, defaultObstacle.x, 0.01f)
    }
}

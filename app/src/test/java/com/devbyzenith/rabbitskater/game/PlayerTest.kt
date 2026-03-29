package com.devbyzenith.rabbitskater.game

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PlayerTest {
    
    private lateinit var player: Player
    
    @Before
    fun setup() {
        player = Player(100f, 0f)
    }
    
    @Test
    fun `jump should set velocityY to jumpForce when not jumping`() {
        player.jump()
        assertEquals(com.devbyzenith.rabbitskater.GameConfig.Player.JUMP_FORCE, player.velocityY, 0.01f)
    }
    
    @Test
    fun `jump should not set velocityY when already jumping`() {
        player.jump()
        val initialVelocity = player.velocityY
        player.jump()
        assertEquals(initialVelocity, player.velocityY, 0.01f)
    }
    
    @Test
    fun `jump should set isJumping to true`() {
        player.jump()
        assertTrue(player.isJumping)
    }
    
    @Test
    fun `update should apply gravity when in air`() {
        player.jump()
        val initialVelocity = player.velocityY
        player.update()
        assertTrue("Gravity should be applied when player is in air", player.velocityY > initialVelocity)
    }
    
    @Test
    fun `player should reset when hitting ground`() {
        player.jump()
        player.update()
        while (player.y < 0) {
            player.update()
        }
        player.update()
        assertEquals(0f, player.y, 0.01f)
        assertFalse(player.isJumping)
    }
}

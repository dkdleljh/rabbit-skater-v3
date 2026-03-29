package com.devbyzenith.rabbitskater.game

import com.devbyzenith.rabbitskater.GameConfig

/**
 * Player entity representing the rabbit on a skateboard.
 * Handles physics-based movement including jumping and gravity.
 *
 * @property x Horizontal position on screen
 * @property y Vertical position (0 = ground level)
 */
class Player(
    var x: Float,
    var y: Float
) {
    var velocityY = 0f
    var isJumping = false
    var hasDoubleJumped = false
    var isSliding = false
    var hasShield = false
    var magnetActive = false
    var coinMultiplier = 1
    
    private var powerUpTimers = mutableMapOf<PowerUpType, Int>()
    
    private var groundY = 0f
    
    fun jump() {
        if (!isJumping) {
            velocityY = GameConfig.Player.JUMP_FORCE
            isJumping = true
            hasDoubleJumped = false
        } else if (!hasDoubleJumped) {
            velocityY = GameConfig.Player.DOUBLE_JUMP_FORCE
            hasDoubleJumped = true
        }
    }
    
    fun update() {
        if (!isSliding) {
            velocityY += GameConfig.Player.GRAVITY
            y += velocityY
            
            if (y >= groundY) {
                y = groundY
                velocityY = 0f
                isJumping = false
                hasDoubleJumped = false
            }
        } else {
             // Sliding logic would go here. For now, keep y at ground level if sliding.
             if (y < groundY) {
                 velocityY += GameConfig.Player.GRAVITY
                 y += velocityY
                 if (y >= groundY) y = groundY
             }
        }
        
        // Update power-up timers
        val iterator = powerUpTimers.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            entry.setValue(entry.value - 1)
            if (entry.value <= 0) {
                deactivatePowerUp(entry.key)
                iterator.remove()
            }
        }
    }
    
    fun activatePowerUp(type: PowerUpType, durationFrames: Int) {
        when (type) {
            PowerUpType.SHIELD -> hasShield = true
            PowerUpType.MAGNET -> magnetActive = true
            PowerUpType.COIN_MULTIPLIER -> coinMultiplier = 2
        }
        powerUpTimers[type] = durationFrames
    }
    
    private fun deactivatePowerUp(type: PowerUpType) {
         when (type) {
            PowerUpType.SHIELD -> hasShield = false
            PowerUpType.MAGNET -> magnetActive = false
            PowerUpType.COIN_MULTIPLIER -> coinMultiplier = 1
        }
    }

    fun setGroundY(groundY: Float) {
        this.groundY = groundY
    }
}

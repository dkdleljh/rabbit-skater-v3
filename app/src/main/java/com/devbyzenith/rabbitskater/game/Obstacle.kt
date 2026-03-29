package com.devbyzenith.rabbitskater.game

import com.devbyzenith.rabbitskater.GameConfig

/**
 * Obstacle entity that moves from right to left.
 *
 * @property width Width of the obstacle
 * @property height Height of the obstacle
 * @property x Current horizontal position
 */
class Obstacle(
    val width: Float,
    val height: Float,
    var x: Float,
    val isFlying: Boolean = false,
    val flyingHeight: Float = 0f
) {
    var animationFrame = 0
    var animationCounter = 0

    fun update(speed: Float) {
        x -= speed
        if (isFlying) {
            animationCounter++
            if (animationCounter > 10) {
                animationFrame = (animationFrame + 1) % 2
                animationCounter = 0
            }
        }
    }
}

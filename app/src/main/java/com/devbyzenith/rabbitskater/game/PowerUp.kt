package com.devbyzenith.rabbitskater.game

import android.graphics.RectF
import com.devbyzenith.rabbitskater.GameConfig

enum class PowerUpType {
    SHIELD, MAGNET, COIN_MULTIPLIER
}

class PowerUp(
    var x: Float,
    var y: Float,
    val type: PowerUpType,
    val width: Float = 40f,
    val height: Float = 40f
) {
    var isActive = true

    fun update(gameSpeed: Float) {
        x -= gameSpeed
    }
    
    fun getHitbox(): RectF {
        return RectF(x, y, x + width, y + height)
    }
}

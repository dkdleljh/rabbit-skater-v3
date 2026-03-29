package com.devbyzenith.rabbitskater.game

import android.graphics.RectF

class Coin(
    var x: Float,
    var y: Float,
    val width: Float = 30f,
    val height: Float = 30f
) {
    var isCollected = false

    fun update(gameSpeed: Float) {
        x -= gameSpeed
    }

    fun getHitbox(): RectF {
        return RectF(x, y, x + width, y + height)
    }
}

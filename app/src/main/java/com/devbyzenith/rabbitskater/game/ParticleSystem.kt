package com.devbyzenith.rabbitskater.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.random.Random

class ParticleSystem {
    private val particles = mutableListOf<Particle>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#BDBDBD") // Light gray for dust
    }

    fun spawnDust(x: Float, y: Float, count: Int = 10) {
        for (i in 0 until count) {
            particles.add(
                Particle(
                    x = x + Random.nextFloat() * 40 - 20,
                    y = y + Random.nextFloat() * 20,
                    vx = Random.nextFloat() * -4f - 1f, // Move left based on game speed/drift
                    vy = Random.nextFloat() * -5f - 1f, // Move up
                    life = Random.nextInt(20, 40)
                )
            )
        }
    }

    fun update(gameSpeed: Float) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx - (gameSpeed * 0.5f) // Drift back smoothly
            p.y += p.vy
            p.life--

            if (p.life <= 0) {
                iterator.remove()
            }
        }
    }

    fun draw(canvas: Canvas) {
        for (p in particles) {
            paint.alpha = (255 * (p.life / 40f)).toInt().coerceIn(0, 255)
            val radius = p.life / 5f
            canvas.drawCircle(p.x, p.y, radius, paint)
        }
    }

    private data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var life: Int
    )
}

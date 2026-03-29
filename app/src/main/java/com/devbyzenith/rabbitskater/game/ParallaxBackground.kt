package com.devbyzenith.rabbitskater.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import kotlin.random.Random

class ParallaxBackground(private val screenWidth: Int, private val screenHeight: Int, private val groundHeight: Float) {

    private val mountains = mutableListOf<Mountain>()
    private val nearTrees = mutableListOf<Tree>()
    private val farTrees = mutableListOf<Tree>()
    
    // Paints
    private val mountainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1E3A8A") } // Deep Blue
    private val farTreePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#065F46") }  // Dark Green
    private val nearTreePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#047857") } // Lighter Green
    private val trunkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#451A03") }   // Brown

    // Layer Speeds (relative to game speed)
    private val MOUNTAIN_SPEED = 0.1f
    private val FAR_TREE_SPEED = 0.5f
    private val NEAR_TREE_SPEED = 0.8f

    init {
        // Initialize some scenery
        for (i in 0..5) {
            mountains.add(Mountain(i * 300f, Random.nextFloat() * 150 + 100, Random.nextFloat() * 200 + 300))
        }
        for (i in 0..8) {
            farTrees.add(Tree(i * 200f, Random.nextFloat() * 80 + 100, isNear = false))
        }
        for (i in 0..6) {
            nearTrees.add(Tree(i * 350f, Random.nextFloat() * 120 + 150, isNear = true))
        }
    }

    fun update(gameSpeed: Float) {
        // Mountains
        mountains.forEach { it.x -= gameSpeed * MOUNTAIN_SPEED }
        mountains.filter { it.x + it.width < 0 }.forEach { 
            it.x = screenWidth.toFloat() + Random.nextFloat() * 100
        }

        // Far Trees
        farTrees.forEach { it.x -= gameSpeed * FAR_TREE_SPEED }
        farTrees.filter { it.x + 100 < 0 }.forEach {
            it.x = screenWidth.toFloat() + Random.nextFloat() * 100
        }

        // Near Trees
        nearTrees.forEach { it.x -= gameSpeed * NEAR_TREE_SPEED }
        nearTrees.filter { it.x + 150 < 0 }.forEach {
            it.x = screenWidth.toFloat() + Random.nextFloat() * 100
        }
    }

    fun draw(canvas: Canvas) {
        val groundY = screenHeight - groundHeight
        
        // Draw Mountains
        for (m in mountains) {
            canvas.drawOval(
                RectF(m.x, groundY - m.height, m.x + m.width, groundY + 50),
                mountainPaint
            )
        }

        // Draw Far Trees
        for (t in farTrees) {
            drawTree(canvas, t.x, groundY, t.height, farTreePaint)
        }

        // Draw Near Trees
        for (t in nearTrees) {
            drawTree(canvas, t.x, groundY, t.height, nearTreePaint)
        }
    }

    private fun drawTree(canvas: Canvas, x: Float, y: Float, height: Float, leavesPaint: Paint) {
        val trunkWidth = height * 0.15f
        val foliageWidth = height * 0.6f
        
        // Trunk
        canvas.drawRect(x - trunkWidth / 2, y - height, x + trunkWidth / 2, y, trunkPaint)
        
        // Foliage (3 overlapping circles)
        canvas.drawCircle(x, y - height, foliageWidth / 2, leavesPaint)
        canvas.drawCircle(x - foliageWidth / 3, y - height + foliageWidth / 2, foliageWidth / 2, leavesPaint)
        canvas.drawCircle(x + foliageWidth / 3, y - height + foliageWidth / 2, foliageWidth / 2, leavesPaint)
    }

    private data class Mountain(var x: Float, val height: Float, val width: Float)
    private data class Tree(var x: Float, val height: Float, val isNear: Boolean)
}

package com.devbyzenith.rabbitskater

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.RectF
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.devbyzenith.rabbitskater.game.Coin
import com.devbyzenith.rabbitskater.game.Obstacle
import com.devbyzenith.rabbitskater.game.ParallaxBackground
import com.devbyzenith.rabbitskater.game.ParticleSystem
import com.devbyzenith.rabbitskater.game.Player
import com.devbyzenith.rabbitskater.game.PowerUp
import com.devbyzenith.rabbitskater.game.PowerUpType
import kotlin.random.Random

/**
 * Main game view handling all game rendering and logic.
 * Implements SurfaceView for hardware-accelerated 60 FPS rendering.
 *
 * @property context Android context for asset loading
 */
class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    
    private var gameThread: GameThread? = null
    private var isRunning = false
    @Volatile
    private var pendingJump = false
    @Volatile
    private var pendingReset = false
    
    private lateinit var player: Player
    private val obstacles = mutableListOf<Obstacle>()
    private val powerUps = mutableListOf<PowerUp>()
    private val coins = mutableListOf<Coin>()
    private var score = 0
    private var highScore = 0
    private var totalCoins = 0
    
    enum class GameState { MENU, PLAYING, GAME_OVER }
    private var gameState = GameState.MENU
    private var isPaused = false
    private var hasSeenTutorial = false
    
    private val prefs = context.getSharedPreferences("RabbitSkaterPrefs", Context.MODE_PRIVATE)
    
    private var gameSpeed = GameConfig.Game.INITIAL_SPEED
    private var obstacleSpawnTimer = 0
    private var nextSpeedIncreaseScore = GameConfig.Game.SPEED_INCREASE_INTERVAL
    
    private var parallaxBackground: ParallaxBackground? = null
    private val particleSystem = ParticleSystem()
    
    private lateinit var spriteManager: SpriteManager
    private var backgroundSprite: Bitmap? = null
    private var groundSprite: Bitmap? = null
    private var characterSprites = listOf<Bitmap>()
    private var obstacleSprite: Bitmap? = null
    
    private val skyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val groundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { 
        color = Color.parseColor(GameConfig.Colors.GROUND) 
    }
    private val groundLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { 
        color = Color.parseColor(GameConfig.Colors.GROUND_LINE) 
    }
    private val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = GameConfig.UI.SCORE_TEXT_SIZE
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF6B35")
        textSize = GameConfig.UI.TITLE_TEXT_SIZE
        isFakeBoldText = true
    }
    
    private val cloudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = 30
    }
    
    private val obstaclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor(GameConfig.Colors.OBSTACLE)
    }
    private val powerUpPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.CYAN
    }
    private val shieldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8000BCD4") // Semi-transparent cyan
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    private val coinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD700") // Gold
    }
    private val coinHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFF59D")
    }
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }
    
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val earPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFE0B2") }
    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK }
    private val nosePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FF8A80") }
    private val boardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#8D6E63") }
    private val wheelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#424242") }
    private val detailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FF6B35") }
    private val wheelHighlight = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    
    private val overlayPaint = Paint().apply {
        color = Color.BLACK
        alpha = 150
    }
    private val gameOverPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF6B35")
        textSize = GameConfig.UI.GAME_OVER_TEXT_SIZE
        isFakeBoldText = true
    }
    private val scoreTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = GameConfig.UI.SCORE_DISPLAY_TEXT_SIZE
    }
    private val tapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F7C548")
        textSize = GameConfig.UI.TAP_TEXT_SIZE
    }
    
    private var animationFrame = 0
    private var animationCounter = 0
    
    init {
        holder.addCallback(this)
        isFocusable = true
        player = Player(GameConfig.Player.INITIAL_X, 0f)
        spriteManager = SpriteManager()
        
        loadPrefs()
        hasSeenTutorial = prefs.getBoolean("hasSeenTutorial", false)
        loadGameAssets()
    }
    
    private fun loadPrefs() {
        highScore = prefs.getInt("highScore", 0)
        totalCoins = prefs.getInt("totalCoins", 0)
    }
    
    // Call this when returning from shop
    fun refreshPrefs() {
        loadPrefs()
    }
    
    private fun loadGameAssets() {
        try {
            spriteManager.loadAtlas(context, "atlases/atlas_ID107.plist", "atlases/atlas_ID107.png")
            backgroundSprite = spriteManager.getSprite("PTModelSprite_ID2014")
            groundSprite = spriteManager.getSprite("PTModelSprite_ID2029")
            characterSprites = listOfNotNull(
                spriteManager.getSprite("PTModelSprite_ID2506"),
                spriteManager.getSprite("PTModelSprite_ID2511"),
                spriteManager.getSprite("PTModelSprite_ID2519")
            )
            obstacleSprite = spriteManager.getSprite("PTModelSprite_ID2020")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startGameThread() {
        if (gameThread?.isAlive == true) {
            return
        }

        isRunning = true
        gameThread = GameThread(holder, this)
        gameThread?.start()
    }

    private fun stopGameThread() {
        val thread = gameThread ?: return
        thread.requestStop()

        var attempts = 10
        while (thread.isAlive && attempts > 0) {
            try {
                thread.join(100)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                break
            }
            attempts--
        }

        gameThread = null
    }
    
    override fun surfaceCreated(holder: SurfaceHolder) {
        startGameThread()
    }
    
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        val groundY = height - GameConfig.Rendering.PLAYER_Y_OFFSET
        player.setGroundY(groundY)
        player.y = groundY
        parallaxBackground = ParallaxBackground(width, height, GameConfig.Rendering.GROUND_HEIGHT)
        skyPaint.shader = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(
                Color.parseColor(GameConfig.Colors.SKY_TOP),
                Color.parseColor(GameConfig.Colors.SKY_MIDDLE),
                Color.parseColor(GameConfig.Colors.SKY_BOTTOM)
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
    }
    
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isRunning = false
        stopGameThread()
    }
    
    private var touchDownY = 0f
    private var playBtnRect = RectF()
    private var shopBtnRect = RectF()
    private var pauseBtnRect = RectF(20f, 20f, 100f, 100f)
    private var resumeBtnRect = RectF()
    private var adBtnRect = RectF()
    private var adWatchedThisRun = false
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchDownY = event.y
                if (gameState == GameState.MENU) {
                    if (playBtnRect.contains(event.x, event.y)) {
                        gameState = GameState.PLAYING
                        if (!hasSeenTutorial) {
                            hasSeenTutorial = true
                            prefs.edit().putBoolean("hasSeenTutorial", true).apply()
                        }
                    }
                    return true
                }
                
                if (isPaused) {
                    if (resumeBtnRect.contains(event.x, event.y)) {
                        isPaused = false
                    }
                    return true
                }
                if (gameState == GameState.GAME_OVER) {
                    if (shopBtnRect.contains(event.x, event.y)) {
                        context.startActivity(android.content.Intent(context, ShopActivity::class.java))
                        // reset might be pending, but we prevent immediate restart
                    } else if (!adWatchedThisRun && adBtnRect.contains(event.x, event.y)) {
                        (context as? android.app.Activity)?.let { activity ->
                            AdManager.showRewardVideo(activity, onRewarded = { reward ->
                                totalCoins += reward
                                prefs.edit().putInt("totalCoins", totalCoins).apply()
                                adWatchedThisRun = true // only once per game over
                                // Note: we should force a redraw, rendering loop might be paused.
                                // But touching down won't pause the loop in normal case unless activity paused.
                            })
                        }
                    } else {
                        pendingReset = true
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                val deltaY = event.y - touchDownY
                if (gameState == GameState.PLAYING && !isPaused) {
                    if (pauseBtnRect.contains(event.x, event.y)) {
                        isPaused = true
                    } else if (deltaY > 50f) { // Swipe down threshold
                        player.isSliding = true
                        // Slide duration reset or handling could go here
                        // For a simple implementation, we might untoggle it after a certain time in update()
                    } else if (deltaY < -50f || Math.abs(deltaY) < 50f) { // Swipe up or tap
                        pendingJump = true
                    }
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
    
    // Counter for slide duration
    private var slideTimer = 0
    
    fun update() {
        if (pendingReset) {
            pendingReset = false
            if (gameState == GameState.GAME_OVER) {
                resetGame()
            }
        }

        if (isPaused || gameState != GameState.PLAYING) return

        if (pendingJump) {
            pendingJump = false
            particleSystem.spawnDust(player.x + 35f, player.y + 70f) // Spawn dust at feet
            player.jump()
            SoundManager.playJumpSound()
        }
        
        if (player.isSliding) {
            slideTimer++
            if (slideTimer > 40) { // Slide duration frames
                player.isSliding = false
                slideTimer = 0
            }
        }

        // TODO: Handle slide input here when added (e.g. swipe down)

        parallaxBackground?.update(gameSpeed)
        player.update()
        particleSystem.update(gameSpeed)
        
        obstacleSpawnTimer++
        if (obstacleSpawnTimer >= GameConfig.Game.OBSTACLE_SPAWN_INTERVAL) {
            spawnObstacle()
            obstacleSpawnTimer = 0
        }
        
        // Spawn powerups occasionally
        if (Random.nextInt(1000) < 5) { // 0.5% chance per frame
            spawnPowerUp()
        }
        
        // Spawn coins
        if (Random.nextInt(100) < 8) { // 8% chance per frame
            spawnCoin()
        }
        
        val playerRect = RectF(
            player.x, 
            player.y + if (player.isSliding) 40f else -20f, 
            player.x + GameConfig.Player.WIDTH, 
            player.y + GameConfig.Player.HEIGHT
        )
        
        val coinIterator = coins.iterator()
        while (coinIterator.hasNext()) {
            val coin = coinIterator.next()
            coin.update(gameSpeed)
            
            if (coin.x < -coin.width) {
                coinIterator.remove()
                continue
            }
            
            if (player.magnetActive) {
                // simple magnet logic: pull coin towards player
                val dx = player.x - coin.x
                val dy = player.y - coin.y
                val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                if (dist < 300f) {
                    coin.x += dx * 0.1f
                    coin.y += dy * 0.1f
                }
            }
            
            if (RectF.intersects(playerRect, coin.getHitbox())) {
                totalCoins += player.coinMultiplier
                SoundManager.playCoinSound()
                coinIterator.remove()
            }
        }
        
        val puIterator = powerUps.iterator()
        while (puIterator.hasNext()) {
            val pu = puIterator.next()
            pu.update(gameSpeed)
            
            if (pu.x < -pu.width) {
                puIterator.remove()
                continue
            }
            
            val playerPuRect = RectF(
                player.x, 
                player.y + if (player.isSliding) 40f else -20f, 
                player.x + GameConfig.Player.WIDTH, 
                player.y + GameConfig.Player.HEIGHT
            )
            
            if (RectF.intersects(playerPuRect, pu.getHitbox())) {
                player.activatePowerUp(pu.type, 600) // 10 seconds at 60fps
                SoundManager.playPowerupSound()
                puIterator.remove()
            }
        }
        
        val iterator = obstacles.iterator()
        while (iterator.hasNext()) {
            val obstacle = iterator.next()
            obstacle.update(gameSpeed)
            
            if (obstacle.x < -obstacle.width) {
                iterator.remove()
                score += GameConfig.Game.SCORE_PER_OBSTACLE * player.coinMultiplier
            }
            
            if (checkCollision(player, obstacle)) {
                if (player.hasShield) {
                    iterator.remove()
                    player.hasShield = false // Consume shield
                    SoundManager.playHitSound()
                    // Optional: add screen shake or particle effect here
                } else if (!player.isSliding || !obstacle.isFlying) {
                    gameState = GameState.GAME_OVER
                    SoundManager.playHitSound()
                    if (score > highScore) {
                        highScore = score
                        prefs.edit().putInt("highScore", highScore).apply()
                    }
                    prefs.edit().putInt("totalCoins", totalCoins).apply()
                }
            }
        }
        
        while (score >= nextSpeedIncreaseScore && gameSpeed < GameConfig.Game.MAX_SPEED) {
            gameSpeed = (gameSpeed + GameConfig.Game.SPEED_INCREMENT).coerceAtMost(GameConfig.Game.MAX_SPEED)
            nextSpeedIncreaseScore += GameConfig.Game.SPEED_INCREASE_INTERVAL
        }
    }
    
    fun render(canvas: Canvas) {
        if (backgroundSprite != null) {
            val srcRect = android.graphics.Rect(0, 0, backgroundSprite!!.width, backgroundSprite!!.height)
            val dstRect = android.graphics.Rect(0, 0, width, height)
            canvas.drawBitmap(backgroundSprite!!, srcRect, dstRect, null)
        } else {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), skyPaint)
        }
        
        parallaxBackground?.draw(canvas)
        drawClouds(canvas)
        
        val groundY = height - GameConfig.Rendering.GROUND_HEIGHT
        
        if (groundSprite != null) {
            var groundX = 0f
            while (groundX < width) {
                canvas.drawBitmap(groundSprite!!, groundX, groundY - groundSprite!!.height, null)
                groundX += groundSprite!!.width
            }
        } else {
            canvas.drawRect(0f, groundY, width.toFloat(), height.toFloat(), groundPaint)
            canvas.drawLine(0f, groundY, width.toFloat(), groundY, groundLinePaint)
        }
        
        for (obstacle in obstacles) {
            drawObstacle(canvas, obstacle, groundY)
        }
        
        for (pu in powerUps) {
            drawPowerUp(canvas, pu)
        }
        
        for (coin in coins) {
            drawCoin(canvas, coin)
        }
        
        particleSystem.draw(canvas)
        drawPlayer(canvas, player.x, player.y)
        
        if (gameState == GameState.MENU) {
            drawMainMenu(canvas)
            return
        }
        
        // UI overlay
        canvas.drawText("Score: $score", 20f, 160f, scorePaint)
        canvas.drawText("Best: $highScore", 20f, 200f, scorePaint)
        canvas.drawText("Coins: $totalCoins", width - 250f, 60f, scorePaint)
        
        // Draw Pause Button (top left)
        canvas.drawRect(pauseBtnRect, obstaclePaint)
        canvas.drawText("||", 40f, 80f, scoreTextPaint)
        
        if (!hasSeenTutorial && gameState == GameState.PLAYING && score < 30) {
            drawTutorial(canvas)
        }
        
        if (gameState == GameState.GAME_OVER) {
            drawGameOver(canvas)
        } else if (isPaused) {
            drawPauseMenu(canvas)
        }
    }
    
    private fun drawClouds(canvas: Canvas) {
        canvas.drawCircle(100f, 100f, 40f, cloudPaint)
        canvas.drawCircle(140f, 100f, 50f, cloudPaint)
        canvas.drawCircle(180f, 100f, 40f, cloudPaint)
        canvas.drawCircle(400f, 150f, 35f, cloudPaint)
        canvas.drawCircle(440f, 150f, 45f, cloudPaint)
        canvas.drawCircle(480f, 150f, 35f, cloudPaint)
    }
    
    private fun drawObstacle(canvas: Canvas, obstacle: Obstacle, groundY: Float) {
        if (obstacle.isFlying) {
            val yPos = groundY - obstacle.flyingHeight
            // Draw a simple bird/bat representation
            canvas.drawOval(obstacle.x, yPos, obstacle.x + obstacle.width, yPos + obstacle.height, obstaclePaint)
            
            // Draw wings depending on animation frame
            if (obstacle.animationFrame == 0) {
                canvas.drawOval(obstacle.x + obstacle.width / 2 - 15, yPos - 15, obstacle.x + obstacle.width / 2 + 15, yPos + 10, obstaclePaint)
            } else {
                canvas.drawOval(obstacle.x + obstacle.width / 2 - 25, yPos + 10, obstacle.x + obstacle.width / 2 + 25, yPos + 25, obstaclePaint)
            }
        } else if (obstacleSprite != null) {
            val sprite = obstacleSprite!!
            val scale = obstacle.height / sprite.height
            val scaledWidth = sprite.width * scale
            canvas.drawBitmap(sprite,
                android.graphics.Rect(0, 0, sprite.width, sprite.height),
                RectF(obstacle.x, groundY - obstacle.height, obstacle.x + scaledWidth, groundY),
                null)
        } else {
            canvas.drawRect(
                obstacle.x,
                groundY - obstacle.height,
                obstacle.x + obstacle.width,
                groundY,
                obstaclePaint
            )
            canvas.drawRect(
                obstacle.x,
                groundY - obstacle.height,
                obstacle.x + 10f,
                groundY - obstacle.height + 20f,
                highlightPaint
            )
        }
    }
    
    private fun drawCoin(canvas: Canvas, coin: Coin) {
        canvas.drawCircle(coin.x + coin.width / 2, coin.y + coin.height / 2, coin.width / 2, coinPaint)
        canvas.drawCircle(coin.x + coin.width / 2, coin.y + coin.height / 2, coin.width / 3, coinHighlightPaint)
    }

    private fun drawPowerUp(canvas: Canvas, pu: PowerUp) {
        powerUpPaint.color = when (pu.type) {
            PowerUpType.SHIELD -> Color.CYAN
            PowerUpType.MAGNET -> Color.RED
            PowerUpType.COIN_MULTIPLIER -> Color.YELLOW
        }
        canvas.drawCircle(pu.x + pu.width / 2, pu.y + pu.height / 2, pu.width / 2, powerUpPaint)
    }
    
    private fun drawPlayer(canvas: Canvas, x: Float, y: Float) {
        if (characterSprites.isNotEmpty()) {
            animationCounter++
            if (animationCounter > GameConfig.Sprites.ANIMATION_SPEED) {
                animationFrame = (animationFrame + 1) % characterSprites.size
                animationCounter = 0
            }
            val sprite = characterSprites[animationFrame]
            val scale = GameConfig.Sprites.CHARACTER_SCALE / sprite.width
            val dstHeight = if (player.isSliding) sprite.height * scale * 0.6f else sprite.height * scale
            val yOffset = if (player.isSliding) sprite.height * scale * 0.4f else 0f
            canvas.drawBitmap(sprite, 
                android.graphics.Rect(0, 0, sprite.width, sprite.height),
                RectF(x, y + yOffset, x + sprite.width * scale, y + yOffset + dstHeight), 
                null)
        } else {
            if (player.isSliding) {
                // Draw sliding pose (squished)
                canvas.drawOval(x + 10, y + 45, x + 60, y + 75, bodyPaint)
                canvas.drawOval(x + 15, y + 35, x + 35, y + 45, earPaint)
                canvas.drawOval(x + 35, y + 35, x + 55, y + 45, earPaint)
                canvas.drawCircle(x + 50, y + 60, 4f, eyePaint)
                canvas.drawCircle(x + 60, y + 65, 3f, nosePaint)
                canvas.drawRect(x + 5, y + 75, x + 65, y + 85, boardPaint)
                canvas.drawCircle(x + 15, y + 92, 8f, wheelPaint)
                canvas.drawCircle(x + 55, y + 92, 8f, wheelPaint)
            } else {
                // Standing pose
                val skinIndex = prefs.getInt("equippedSkin", 0)
                val paintColor = when (skinIndex) {
                    1 -> Color.DKGRAY // Ninja
                    2 -> Color.YELLOW // Gold
                    else -> Color.WHITE // Default
                }
                bodyPaint.color = paintColor
                canvas.drawCircle(x + 35, y + 35, 28f, bodyPaint)
                canvas.drawOval(x + 20, y - 25, x + 30, y + 15, earPaint)
                canvas.drawOval(x + 40, y - 25, x + 50, y + 15, earPaint)
                canvas.drawCircle(x + 25, y + 30, 4f, eyePaint)
                canvas.drawCircle(x + 45, y + 30, 4f, eyePaint)
                canvas.drawCircle(x + 35, y + 40, 5f, nosePaint)
                canvas.drawRect(x + 5, y + 55, x + 65, y + 65, boardPaint)
                canvas.drawRect(x + 10, y + 58, x + 20, y + 63, detailPaint)
                canvas.drawRect(x + 40, y + 58, x + 50, y + 63, detailPaint)
                canvas.drawCircle(x + 15, y + 72, 8f, wheelPaint)
                canvas.drawCircle(x + 55, y + 72, 8f, wheelPaint)
                canvas.drawCircle(x + 13, y + 70, 3f, wheelHighlight)
                canvas.drawCircle(x + 53, y + 70, 3f, wheelHighlight)
            }
        }
        
        if (player.hasShield) {
            canvas.drawCircle(x + 35, y + 35, 60f, shieldPaint)
        }
    }
    
    private fun drawMainMenu(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
        canvas.drawText("RABBIT SKATER v3", width / 2 - 320f, height / 2 - 80f, titlePaint)
        canvas.drawText("Best Score: $highScore", width / 2 - 120f, height / 2 - 10f, scoreTextPaint)
        
        val playX = width / 2f - 100f
        val playY = height / 2f + 50f
        playBtnRect.set(playX, playY, playX + 200f, playY + 80f)
        canvas.drawRect(playBtnRect, obstaclePaint)
        canvas.drawText("PLAY", playX + 50f, playY + 55f, scoreTextPaint)
    }
    
    private fun drawTutorial(canvas: Canvas) {
        val tutorialPaint = Paint(scoreTextPaint).apply { textSize = 30f; alpha = 200 }
        canvas.drawText("TAP to Jump (Double Tap for Double Jump)", width / 2 - 300f, height / 2 - 50f, tutorialPaint)
        canvas.drawText("SWIPE DOWN to Slide", width / 2 - 180f, height / 2 + 10f, tutorialPaint)
    }
    
    private fun drawGameOver(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
        canvas.drawText("GAME OVER", width / 2 - 220f, height / 2 - 30f, gameOverPaint)
        canvas.drawText("Score: $score", width / 2 - 80f, height / 2 + 30f, scoreTextPaint)
        canvas.drawText("Best: $highScore", width / 2 - 80f, height / 2 + 70f, scoreTextPaint)
        canvas.drawText("Tap to restart", width / 2 - 110f, height / 2 + 130f, tapPaint)
        
        // Shop Button Draw
        val shopX = width / 2f - 200f
        val btnY = height / 2f + 160f
        shopBtnRect.set(shopX, btnY, shopX + 150f, btnY + 60f)
        canvas.drawRect(shopBtnRect, obstaclePaint)
        canvas.drawText("Shop", shopX + 25f, btnY + 40f, scoreTextPaint)
        
        // Ad Button Draw (only if not watched yet)
        if (!adWatchedThisRun) {
            val adX = width / 2f + 50f
            adBtnRect.set(adX, btnY, adX + 250f, btnY + 60f)
            canvas.drawRect(adBtnRect, obstaclePaint)
            val scaledTextPaint = Paint(scoreTextPaint).apply { textSize = 28f }
            canvas.drawText("Watch Ad (+50)", adX + 15f, btnY + 40f, scaledTextPaint)
        }
    }
    
    private fun drawPauseMenu(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
        canvas.drawText("PAUSED", width / 2 - 120f, height / 2 - 50f, gameOverPaint)
        
        val resumeX = width / 2f - 100f
        val resumeY = height / 2f + 50f
        resumeBtnRect.set(resumeX, resumeY, resumeX + 200f, resumeY + 60f)
        canvas.drawRect(resumeBtnRect, obstaclePaint)
        canvas.drawText("Resume", resumeX + 35f, resumeY + 40f, scoreTextPaint)
    }
    
    private fun spawnObstacle() {
        val isFlying = score > 200 && Random.nextFloat() > 0.7f // 30% chance of flying obstacle after 200 score
        
        if (isFlying) {
            val obstacleHeight = 30f
            val obstacleWidth = 40f
            val flyingHeight = 100f // Height above ground to force sliding
            obstacles.add(Obstacle(obstacleWidth, obstacleHeight, width.toFloat() + 120f, true, flyingHeight))
        } else {
            val obstacleHeight = Random.nextFloat() * 80 + 40
            val obstacleWidth = Random.nextFloat() * 40 + 30
            obstacles.add(Obstacle(obstacleWidth, obstacleHeight, width.toFloat() + 120f))
        }
    }
    
    private fun spawnPowerUp() {
        val yPos = Random.nextFloat() * (height - GameConfig.Rendering.GROUND_HEIGHT - 150) + 100
        val type = PowerUpType.values()[Random.nextInt(PowerUpType.values().size)]
        powerUps.add(PowerUp(width.toFloat() + 120f, yPos, type))
    }
    
    private fun spawnCoin() {
        val yPos = Random.nextFloat() * (height - GameConfig.Rendering.GROUND_HEIGHT - 150) + 100
        coins.add(Coin(width.toFloat() + 120f, yPos))
    }
    
    private fun checkCollision(player: Player, obstacle: Obstacle): Boolean {
        val playerRight = player.x + GameConfig.Player.WIDTH
        val playerBottom = player.y + GameConfig.Player.HEIGHT
        // When sliding, top of hitbox is lower
        val playerTopHeightOffset = if (player.isSliding) 40f else -20f
        val playerTop = player.y + playerTopHeightOffset
        val obstacleLeft = obstacle.x
        val obstacleRight = obstacle.x + obstacle.width
        val groundY = height - GameConfig.Rendering.GROUND_HEIGHT
        
        val obstacleTop: Float
        val obstacleBottom: Float
        
        if (obstacle.isFlying) {
            obstacleTop = groundY - obstacle.flyingHeight
            obstacleBottom = obstacleTop + obstacle.height
        } else {
            obstacleTop = groundY - obstacle.height
            obstacleBottom = groundY
        }
        
        return playerRight > obstacleLeft + 10 &&
                player.x < obstacleRight - 10 &&
                playerBottom > obstacleTop + 10 &&
                playerTop < obstacleBottom - 10
    }
    
    private fun resetGame() {
        score = 0
        gameState = GameState.PLAYING
        gameSpeed = GameConfig.Game.INITIAL_SPEED
        nextSpeedIncreaseScore = GameConfig.Game.SPEED_INCREASE_INTERVAL
        obstacles.clear()
        powerUps.clear()
        coins.clear()
        obstacleSpawnTimer = 0
        val groundY = height - GameConfig.Rendering.PLAYER_Y_OFFSET
        player.setGroundY(groundY)
        player.y = groundY
        player.velocityY = 0f
        player.isJumping = false
        player.hasDoubleJumped = false
        player.isSliding = false
        player.hasShield = false
        player.magnetActive = false
        player.coinMultiplier = 1
        pendingJump = false
        pendingReset = false
        adWatchedThisRun = false
        isPaused = false
    }
    
    fun pause() {
        isRunning = false
        stopGameThread()
    }
    
    fun resume() {
        startGameThread()
    }
}

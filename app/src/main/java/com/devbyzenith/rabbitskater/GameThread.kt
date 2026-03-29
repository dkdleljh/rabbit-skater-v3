package com.devbyzenith.rabbitskater

import android.view.SurfaceHolder

/**
 * Game thread responsible for the 60 FPS render loop.
 *
 * @property surfaceHolder Surface holder for canvas locking
 * @property gameView Game view to update and render
 */
class GameThread(
    private val surfaceHolder: SurfaceHolder,
    private val gameView: GameView
) : Thread() {
    @Volatile
    private var isRunning = true

    fun requestStop() {
        isRunning = false
        interrupt()
    }
    
    override fun run() {
        while (isRunning) {
            val canvas = surfaceHolder.lockCanvas()
            if (canvas != null) {
                try {
                    synchronized(surfaceHolder) {
                        gameView.update()
                        gameView.render(canvas)
                    }
                } finally {
                    surfaceHolder.unlockCanvasAndPost(canvas)
                }
            }
            
            try {
                sleep(GameConfig.Rendering.FRAME_DELAY_MS.toLong())
            } catch (e: InterruptedException) {
                if (!isRunning) {
                    break
                }
            }
        }
    }
}

package com.devbyzenith.rabbitskater

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

/**
 * Main activity that launches the game.
 * Configures fullscreen mode and initializes the game view.
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        AdManager.initialize(this) {}
        SoundManager.init(this)
        
        gameView = GameView(this)
        setContentView(gameView)
    }

    override fun onResume() {
        super.onResume()
        gameView.refreshPrefs()
        gameView.resume()
        SoundManager.playBgm()
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
        SoundManager.pauseBgm()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        AdManager.destroy()
        SoundManager.release()
    }
}

package com.devbyzenith.rabbitskater

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log

object SoundManager {
    private var soundPool: SoundPool? = null
    private var bgmPlayer: MediaPlayer? = null
    
    // Sound IDs
    private var jumpSoundId = 0
    private var hitSoundId = 0
    private var coinSoundId = 0
    private var powerupSoundId = 0
    
    private var isMuted = false
    private const val TAG = "SoundManager"

    fun init(context: Context) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
            
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()
            
        // Assuming sound files exist in res/raw or assets. 
        // We will catch exceptions if they don't exist yet to prevent crashes.
        try {
            // jumpSoundId = soundPool?.load(context, R.raw.jump, 1) ?: 0
            // hitSoundId = soundPool?.load(context, R.raw.hit, 1) ?: 0
            // coinSoundId = soundPool?.load(context, R.raw.coin, 1) ?: 0
            // powerupSoundId = soundPool?.load(context, R.raw.powerup, 1) ?: 0
            Log.d(TAG, "SoundPool initialized. (No raw files loaded in stub)")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading sounds", e)
        }
        
        try {
            // bgmPlayer = MediaPlayer.create(context, R.raw.bgm)
            // bgmPlayer?.isLooping = true
            Log.d(TAG, "MediaPlayer initialized. (No BGM file loaded in stub)")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading BGM", e)
        }
    }
    
    fun playBgm() {
        if (isMuted) return
        bgmPlayer?.start()
    }
    
    fun pauseBgm() {
        if (bgmPlayer?.isPlaying == true) {
            bgmPlayer?.pause()
        }
    }
    
    fun playJumpSound() {
        if (!isMuted && jumpSoundId != 0) {
            soundPool?.play(jumpSoundId, 1f, 1f, 0, 0, 1f)
        }
    }

    fun playHitSound() {
        if (!isMuted && hitSoundId != 0) {
            soundPool?.play(hitSoundId, 1f, 1f, 0, 0, 1f)
        }
    }
    
    fun playCoinSound() {
        if (!isMuted && coinSoundId != 0) {
            soundPool?.play(coinSoundId, 1f, 1f, 0, 0, 1f)
        }
    }
    
    fun playPowerupSound() {
        if (!isMuted && powerupSoundId != 0) {
            soundPool?.play(powerupSoundId, 1f, 1f, 0, 0, 1f)
        }
    }
    
    fun release() {
        soundPool?.release()
        soundPool = null
        bgmPlayer?.release()
        bgmPlayer = null
    }
}

package com.devbyzenith.rabbitskater

import android.app.Activity

/**
 * Ad manager for handling interstitial advertisements.
 * Currently a stub implementation - ready for AppLovin MAX or AdMob integration.
 */
object AdManager {
    
    fun initialize(activity: Activity, onComplete: (() -> Unit)? = null) {
        onComplete?.invoke()
    }
    
    fun showInterstitial(activity: Activity, onDismiss: (() -> Unit)? = null) {
        onDismiss?.invoke()
    }
    
    fun showRewardVideo(activity: Activity, onRewarded: (Int) -> Unit, onDismiss: (() -> Unit)? = null) {
        // Stub: Immediately reward 50 coins
        onRewarded(50)
        onDismiss?.invoke()
    }
    
    fun destroy() {}
}

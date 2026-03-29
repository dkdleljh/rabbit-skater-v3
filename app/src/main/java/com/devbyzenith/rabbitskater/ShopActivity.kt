package com.devbyzenith.rabbitskater

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ShopActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private var totalCoins = 0
    private var equippedSkinIndex = 0

    // Dummy shop items for now
    private val skinPrices = listOf(0, 500, 1000)
    private val skinNames = listOf("Default Rabbit", "Ninja Rabbit", "Gold Rabbit")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Creating a simple UI pragmatically since we might not want to mess with XML right now
        // A complete implementation would use XML layout and RecyclerView
        
        prefs = getSharedPreferences("RabbitSkaterPrefs", Context.MODE_PRIVATE)
        totalCoins = prefs.getInt("totalCoins", 0)
        equippedSkinIndex = prefs.getInt("equippedSkin", 0)
        
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
            setBackgroundColor(android.graphics.Color.parseColor("#1A1A2E"))
        }

        val coinText = TextView(this).apply {
            text = "Coins: $totalCoins"
            textSize = 24f
            setTextColor(android.graphics.Color.YELLOW)
            setPadding(0, 0, 0, 50)
        }
        layout.addView(coinText)

        for (i in skinPrices.indices) {
            val isUnlocked = prefs.getBoolean("skin_unlocked_$i", i == 0)
            val btn = Button(this).apply {
                val statusText = if (i == equippedSkinIndex) "Equipped" else if (isUnlocked) "Equip" else "Buy (${skinPrices[i]})"
                text = "${skinNames[i]} - $statusText"
                
                setOnClickListener {
                    if (isUnlocked) {
                        equippedSkinIndex = i
                        prefs.edit().putInt("equippedSkin", i).apply()
                        recreate()
                    } else if (totalCoins >= skinPrices[i]) {
                        totalCoins -= skinPrices[i]
                        prefs.edit().putInt("totalCoins", totalCoins).putBoolean("skin_unlocked_$i", true).apply()
                        Toast.makeText(this@ShopActivity, "Purchased ${skinNames[i]}!", Toast.LENGTH_SHORT).show()
                        recreate()
                    } else {
                        Toast.makeText(this@ShopActivity, "Not enough coins!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            layout.addView(btn)
        }
        
        val backBtn = Button(this).apply {
            text = "Back to Game"
            setOnClickListener { finish() }
            setPadding(0, 50, 0, 0)
        }
        layout.addView(backBtn)

        setContentView(layout)
    }
}

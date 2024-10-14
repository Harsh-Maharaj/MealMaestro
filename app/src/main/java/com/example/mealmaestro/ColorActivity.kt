package com.example.mealmaestro

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class ColorActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var btnTheme1: ImageButton
    private lateinit var btnTheme2: ImageButton
    private lateinit var btnTheme3: ImageButton
    private lateinit var btnTheme4: ImageButton
    private lateinit var resetThemeButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize SharedPreferences before applying the theme
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // Apply the selected theme or default system theme
        applyThemeFromPreferences()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_color)

        // Initialize buttons
        btnTheme1 = findViewById(R.id.btnTheme1)
        btnTheme2 = findViewById(R.id.btnTheme2)
        btnTheme3 = findViewById(R.id.btnTheme3)
        btnTheme4 = findViewById(R.id.btnTheme4)
        resetThemeButton = findViewById(R.id.resetThemeButton)

        // Set click listeners for theme buttons
        btnTheme1.setOnClickListener(this)
        btnTheme2.setOnClickListener(this)
        btnTheme3.setOnClickListener(this)
        btnTheme4.setOnClickListener(this)

        // Set click listener for the reset button
        resetThemeButton.setOnClickListener {
            resetThemeToDefault()
        }
    }

    // Apply the selected theme from SharedPreferences
    private fun applyThemeFromPreferences() {
        val selectedTheme = sharedPreferences.getInt("SelectedTheme", 0)
        when (selectedTheme) {
            1 -> setTheme(R.style.DynamicTheme1)
            2 -> setTheme(R.style.DynamicTheme2)
            3 -> setTheme(R.style.DynamicTheme3)
            4 -> setTheme(R.style.DynamicTheme4)
            else -> setTheme(android.R.style.Theme_DeviceDefault) // Default system theme
        }
    }

    // Reset the theme to default by clearing preferences and restarting the app
    private fun resetThemeToDefault() {
        val editor = sharedPreferences.edit()
        editor.clear() // Clear all saved preferences, including the selected theme
        editor.apply()

        // Restart the MainActivity to apply the default theme across the app
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish() // Close the current activity
    }

    // Handle theme button clicks to select and apply a new theme
    override fun onClick(v: View?) {
        val editor = sharedPreferences.edit()
        var selectedTheme = 0

        when (v?.id) {
            R.id.btnTheme1 -> selectedTheme = 1
            R.id.btnTheme2 -> selectedTheme = 2
            R.id.btnTheme3 -> selectedTheme = 3
            R.id.btnTheme4 -> selectedTheme = 4
        }

        if (selectedTheme != 0) {
            editor.putInt("SelectedTheme", selectedTheme)
            editor.apply()

            // Restart MainActivity to apply the new theme
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

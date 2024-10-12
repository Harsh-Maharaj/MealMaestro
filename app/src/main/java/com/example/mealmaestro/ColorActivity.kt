package com.example.mealmaestro

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class ColorActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var btnTheme1: ImageButton
    private lateinit var btnTheme2: ImageButton
    private lateinit var btnTheme3: ImageButton
    private lateinit var btnTheme4: ImageButton
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val selectedTheme = sharedPreferences.getInt("SelectedTheme", 0)

        // Set the theme based on user selection
        setDynamicTheme(selectedTheme)

        // Set the layout for color selection
        setContentView(R.layout.custom_color)

        // Initialize buttons
        btnTheme1 = findViewById(R.id.btnTheme1)
        btnTheme2 = findViewById(R.id.btnTheme2)
        btnTheme3 = findViewById(R.id.btnTheme3)
        btnTheme4 = findViewById(R.id.btnTheme4)

        // Set click listeners for each theme button
        btnTheme1.setOnClickListener(this)
        btnTheme2.setOnClickListener(this)
        btnTheme3.setOnClickListener(this)
        btnTheme4.setOnClickListener(this)
    }

    // Method to dynamically set the theme
    private fun setDynamicTheme(selectedTheme: Int) {
        when (selectedTheme) {
            1 -> setTheme(R.style.DynamicTheme1)
            2 -> setTheme(R.style.DynamicTheme2)
            3 -> setTheme(R.style.DynamicTheme3)
            4 -> setTheme(R.style.DynamicTheme4)
        }
    }

    // Handle button clicks to select and apply the theme
    override fun onClick(v: View?) {
        val editor = sharedPreferences.edit()
        var selectedTheme = 0

        when (v?.id) {
            R.id.btnTheme1 -> {
                selectedTheme = 1
                setTheme(R.style.DynamicTheme1)
            }
            R.id.btnTheme2 -> {
                selectedTheme = 2
                setTheme(R.style.DynamicTheme2)
            }
            R.id.btnTheme3 -> {
                selectedTheme = 3
                setTheme(R.style.DynamicTheme3)
            }
            R.id.btnTheme4 -> {
                selectedTheme = 4
                setTheme(R.style.DynamicTheme4)
            }
        }

        // Save the selected theme to SharedPreferences
        editor.putInt("SelectedTheme", selectedTheme)
        editor.apply()

        // Restart the MainActivity to apply the new theme
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()  // Close ColorActivity
    }
}
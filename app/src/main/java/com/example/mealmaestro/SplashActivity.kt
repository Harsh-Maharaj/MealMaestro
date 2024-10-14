package com.example.mealmaestro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mealmaestro.Chats.ChatFriendsActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    // Firebase authentication instance to check if the user is logged in
    private lateinit var auth: FirebaseAuth

    // Called when the activity is first created
    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeFromPreferences()  // Apply theme from preferences before anything else
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge layout for immersive experience
        enableEdgeToEdge()

        // Set the content view to the splash screen layout
        setContentView(R.layout.activity_splash)

        // Adjust padding for system bars (status bar, navigation bar) to fit the layout properly
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Hide the action bar for a cleaner splash screen
        supportActionBar?.hide()

        // Initialize Firebase authentication to check user login status
        auth = FirebaseAuth.getInstance()

        // Use a Handler to introduce a delay before proceeding to the next activity
        Handler(Looper.getMainLooper()).postDelayed({
            // Check if the user is logged in (auth.currentUser != null)
            if (auth.currentUser != null) {
                // If the user is logged in, navigate to the MainActivity
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()  // Finish the splash activity so it won't appear when the user presses back
            } else {
                // If the user is not logged in, navigate to the LoginActivity
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                finish()  // Finish the splash activity
            }
        }, 2000)  // 2-second delay for the splash screen
    }

    // Apply the selected theme from SharedPreferences
    private fun applyThemeFromPreferences() {
        val sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this)
        val selectedTheme = sharedPreferences.getInt("SelectedTheme", 0)
        when (selectedTheme) {
            1 -> setTheme(R.style.DynamicTheme1)
            2 -> setTheme(R.style.DynamicTheme2)
            3 -> setTheme(R.style.DynamicTheme3)
            4 -> setTheme(R.style.DynamicTheme4)
        }
    }
}

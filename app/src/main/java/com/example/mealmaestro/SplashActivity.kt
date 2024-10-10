package com.example.mealmaestro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mealmaestro.Chats.ChatFriendsActivity
import com.example.mealmaestro.users.Users
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.auth.User

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({

            // Check if the user is already logged in
            val currentUser = auth.currentUser

            if (currentUser != null) {
                Log.d("SplashActivity", "User is logged in: ${currentUser.uid}")
                // The user is logged in, handle intent extras for notification
                if (intent.extras != null) {
                    Log.d("SplashActivity", "Notification received with extras: ${intent.extras}")
                    val userId = intent.extras?.getString("uid")
                    if (userId != null) {
                        val chatIntent = Intent(this, ChatFriendsActivity::class.java).apply {
                            putExtra("uid", userId)
                            putExtra("username", intent.extras?.getString("username"))
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(chatIntent)
                    } else {
                        Log.d("SplashActivity", "No userId in extras")
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                } else {
                    Log.d("SplashActivity", "No extras in intent, navigating to MainActivity")
                    startActivity(Intent(this, MainActivity::class.java))
                }

            } else {
                Log.d("SplashActivity", "User is not logged in. Redirecting to login screen")
                // The user is not logged in, redirect to the login screen
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            }
            finish()
        }, 2000)
    }
}
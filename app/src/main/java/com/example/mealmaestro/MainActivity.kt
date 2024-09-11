package com.example.mealmaestro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.mealmaestro.databinding.ActivityMainBinding
import com.example.mealmaestro.users.RecycleUserFriends
import com.example.mealmaestro.users.RecycleUserView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        drawerLayout = binding.drawerLayout

        // Set up the click listener for the logo (chatToolBar_img)
        val logoImageView: ImageView = binding.chatToolBarImgFrame.findViewById(R.id.chatToolBar_img)
        logoImageView.setOnClickListener {
            // Open the navigation drawer when the logo is clicked
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // Set up the BottomNavigationView with NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNavigationView: BottomNavigationView = binding.bottomNavigationView

        // Handle manual navigation for FriendsFragment
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_friends -> {
                    // Start the RecycleUserFriends Activity
                    val intent = Intent(this, RecycleUserFriends::class.java)
                    startActivity(intent)
                    true
                }
                else -> {
                    NavigationUI.onNavDestinationSelected(item, navController)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
            }
        }

        // Handle navigation drawer item clicks
        val navigationView: NavigationView = binding.navView
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_timer -> {
                    // Start the TimerActivity when timer item is clicked
                    val intent = Intent(this, TimerActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_logout -> {
                    // Log the user out and navigate to the login screen
                    FirebaseAuth.getInstance().signOut() // Log out the user
                    val intent = Intent(this, LoginActivity::class.java)
                    // Clear the activity stack so user cannot go back to previous screens
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }

        // Get FCM Token for messaging notifications
        getFCMToken()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    // ================ FOR MESSAGE NOTIFICATIONS ==================================================
    fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.i("My Token", token)
            } else {
                Log.e("Token error", "fail to retrieve FCM token", task.exception)
            }
        }
    }
}

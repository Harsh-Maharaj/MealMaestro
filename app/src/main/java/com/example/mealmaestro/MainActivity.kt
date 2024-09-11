package com.example.mealmaestro

import android.content.Intent
import android.os.Bundle
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
import com.google.android.material.bottomnavigation.BottomNavigationView

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
                    // Start the RecycleUserView Activity
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
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}

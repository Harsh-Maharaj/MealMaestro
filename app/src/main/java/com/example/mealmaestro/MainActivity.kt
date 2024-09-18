package com.example.mealmaestro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.mealmaestro.Helper.DataBase
import com.example.mealmaestro.databinding.ActivityMainBinding
import com.example.mealmaestro.users.RecycleUserFriends
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var dataBase: DataBase

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

        // set permission for android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }

        // Setup search bar handling
        val searchBar: EditText = binding.searchBar
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val searchQuery = s.toString()
                // Pass the search query to the HomeFragment
                val currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.childFragmentManager?.fragments?.get(0)
                if (currentFragment is HomeFragment) {
                    currentFragment.performSearch(searchQuery)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
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
        dataBase = DataBase()
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                val currentUser = FirebaseAuth.getInstance().currentUser?.uid

                if (currentUser != null)
                    dataBase.addFCMToken(token, currentUser)
                Log.i("My Token", token)
            } else {
                Log.e("Token error", "fail to retrieve FCM token", task.exception)
            }
        }
    }
}

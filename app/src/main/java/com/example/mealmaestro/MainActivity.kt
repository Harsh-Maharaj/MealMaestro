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

    // View binding to access views in the layout
    private lateinit var binding: ActivityMainBinding
    // NavController to handle navigation between fragments
    private lateinit var navController: NavController
    // DrawerLayout for the navigation drawer
    private lateinit var drawerLayout: DrawerLayout
    // Database reference for handling FCM token storage
    private lateinit var dataBase: DataBase

    // onCreate method - this is called when the activity is first created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up view binding to access the UI components
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable edge-to-edge layout for immersive UI experience
        enableEdgeToEdge()

        // Initialize the DrawerLayout from binding
        drawerLayout = binding.drawerLayout

        // Set up click listener for the logo (chatToolBar_img)
        val logoImageView: ImageView = binding.chatToolBarImgFrame.findViewById(R.id.chatToolBar_img)
        logoImageView.setOnClickListener {
            // Open the navigation drawer when the logo is clicked
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // Set up the NavController with the NavHostFragment to manage fragment navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Set up the BottomNavigationView with NavController
        val bottomNavigationView: BottomNavigationView = binding.bottomNavigationView

        // Handle manual navigation for FriendsFragment when the "Friends" item is selected
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_friends -> {
                    // Start RecycleUserFriends activity when "Friends" is clicked
                    val intent = Intent(this, RecycleUserFriends::class.java)
                    startActivity(intent)
                    true
                }
                else -> {
                    // For other items, use the NavController for navigation
                    NavigationUI.onNavDestinationSelected(item, navController)
                    drawerLayout.closeDrawer(GravityCompat.START) // Close drawer after navigation
                    true
                }
            }
        }

        // Set up navigation drawer item click listener
        val navigationView: NavigationView = binding.navView
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Navigate to the HomeFragment
                    navController.navigate(R.id.nav_home)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_timer -> {
                    val intent = Intent(this, TimerActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_shopping_list -> {
                    navController.navigate(R.id.shoppingListFragment)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }


        // Retrieve FCM (Firebase Cloud Messaging) token for push notifications
        getFCMToken()

        // Request notification permission for Android 13+ devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }

        // Setup search bar handling for filtering content in HomeFragment
        val searchBar: EditText = binding.searchBar
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val searchQuery = s.toString()
                // Pass the search query to the HomeFragment for filtering results
                val currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                    ?.childFragmentManager?.fragments?.get(0)
                if (currentFragment is HomeFragment) {
                    currentFragment.performSearch(searchQuery)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // Handle back button press: if the drawer is open, close it; otherwise, use default back behavior
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    // Retrieve FCM token for sending notifications
    fun getFCMToken() {
        dataBase = DataBase()  // Initialize the database
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Retrieve the FCM token
                val token = task.result
                val currentUser = FirebaseAuth.getInstance().currentUser?.uid

                // If user is logged in, save the token to the database
                if (currentUser != null)
                    dataBase.addFCMToken(token, currentUser)
                Log.i("My Token", token)  // Log the token for debugging
            } else {
                // Log an error if token retrieval fails
                Log.e("Token error", "Failed to retrieve FCM token", task.exception)
            }
        }
    }







}
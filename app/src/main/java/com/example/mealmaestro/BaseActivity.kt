package com.example.mealmaestro

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : AppCompatActivity() {

    // Declare variables for the navigation drawer, Firebase authentication, and drawer toggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var auth: FirebaseAuth

    // onCreate method - this is called when the activity is first created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize FirebaseAuth to manage user authentication
        auth = FirebaseAuth.getInstance()

        // Common functionality: Setup the navigation drawer and logout logic
        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Set up the drawer toggle for opening and closing the navigation drawer
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Set up the navigation item click listener
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                // Handle home navigation click
                R.id.nav_home -> {
                    // Start MainActivity and clear the back stack
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                // Handle logout click
                R.id.nav_logout -> {
                    logout()// Call the logout method
                }
                // Add more options here (e.g. settings) if needed
            }
            drawerLayout.closeDrawer(GravityCompat.START) // Close the navigation drawer after a menu item is clicked
            true
        }
    }

    // Logout logic
    private fun logout() {
        // Sign out from Firebase
        auth.signOut() // Sign the user out from Firebase authentication
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show() // Show a confirmation toast

        // Redirect to LoginActivity and clear back stack
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear the back stack to prevent returning to previous activities
        startActivity(intent) // Start LoginActivity after logout
    }

    // Handle drawer icon click
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // If the drawer toggle is clicked, handle it; otherwise, pass it to the superclass
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}

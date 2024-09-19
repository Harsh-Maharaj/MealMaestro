package com.example.mealmaestro

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.os.CountDownTimer
import com.google.firebase.auth.FirebaseAuth

class TimerActivity : AppCompatActivity() {

    // UI components for displaying the timer and progress
    private lateinit var timeTxt: TextView // TextView for displaying time
    private lateinit var circularProgressBar: ProgressBar // Progress bar to show remaining time visually
    private lateinit var editHours: EditText // Input field for hours
    private lateinit var editMinutes: EditText // Input field for minutes
    private lateinit var editSeconds: EditText // Input field for seconds
    private lateinit var drawerLayout: DrawerLayout // Layout for navigation drawer
    private lateinit var toggle: ActionBarDrawerToggle // Toggle for the navigation drawer

    // Timer control buttons
    private lateinit var startBtn: Button
    private lateinit var pauseBtn: Button
    private lateinit var resumeBtn: Button
    private lateinit var resetBtn: Button

    // Variables to handle the timer
    private var timerMillis: Long = 0 // Total timer duration in milliseconds
    private var remainingMillis: Long = 0 // Time left when the timer is paused
    private var timer: CountDownTimer? = null // Reference to the current CountDownTimer
    private val maxProgress = 100 // Maximum progress for the progress bar

    // Called when the activity is first created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        // Initialize the UI components
        drawerLayout = findViewById(R.id.drawer_layout)
        timeTxt = findViewById(R.id.timeTxt)
        circularProgressBar = findViewById(R.id.circularProgressBar)
        editHours = findViewById(R.id.editHours)
        editMinutes = findViewById(R.id.editMinutes)
        editSeconds = findViewById(R.id.editSeconds)
        startBtn = findViewById(R.id.startBtn)
        pauseBtn = findViewById(R.id.pauseBtn)
        resumeBtn = findViewById(R.id.resumeBtn)
        resetBtn = findViewById(R.id.resetBtn)

        // Set up the Toolbar as the ActionBar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Setup Drawer Toggle for the navigation drawer
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Handle navigation drawer item clicks
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Navigate to the main homepage (MainActivity)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                R.id.nav_settings -> {
                    // Handle Settings action
                }
                R.id.nav_logout -> {
                    // Log the user out and navigate to the login screen
                    FirebaseAuth.getInstance().signOut() // Log out the user
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START) // Close the drawer after selection
            true
        }

        // Initialize timer control buttons (start, pause, resume, reset)
        startBtn.setOnClickListener {
            // Parse the input time (hours, minutes, seconds) into milliseconds
            val hours = editHours.text.toString().toIntOrNull() ?: 0
            val minutes = editMinutes.text.toString().toIntOrNull() ?: 0
            val seconds = editSeconds.text.toString().toIntOrNull() ?: 0
            timerMillis = ((hours * 3600) + (minutes * 60) + seconds) * 1000L
            if (timerMillis > 0) {
                startTimer(timerMillis) // Start the timer with the parsed duration
                startBtn.isEnabled = false // Disable the start button
            }
        }

        pauseBtn.setOnClickListener { pauseTimer() } // Pause the timer

        resumeBtn.setOnClickListener { resumeTimer() } // Resume the timer

        resetBtn.setOnClickListener { resetTimer() } // Reset the timer
    }

    // Handle navigation drawer icon click
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    // Start the timer for the given duration in milliseconds
    private fun startTimer(durationMillis: Long) {
        circularProgressBar.max = maxProgress // Set the max value for the progress bar

        // Use remainingMillis if resuming the timer, otherwise use the full duration
        val startMillis = if (remainingMillis > 0) remainingMillis else durationMillis

        // Create a new CountDownTimer
        timer = object : CountDownTimer(startMillis, 1000) {
            // Called every second (1000 milliseconds)
            override fun onTick(millisUntilFinished: Long) {
                remainingMillis = millisUntilFinished // Update remaining time
                val secondsRemaining = (millisUntilFinished / 1000).toInt()
                updateTimerText(secondsRemaining) // Update the time display

                // Update the progress bar based on the remaining time
                val progressPercentage = ((millisUntilFinished.toFloat() / timerMillis) * maxProgress).toInt()
                circularProgressBar.progress = progressPercentage
            }

            // Called when the timer finishes
            override fun onFinish() {
                updateTimerText(0) // Set the time display to zero
                circularProgressBar.progress = 0 // Reset the progress bar
                startBtn.isEnabled = true // Enable the start button again when the timer finishes
            }
        }.start()
    }

    // Pause the timer by cancelling the CountDownTimer
    private fun pauseTimer() {
        timer?.cancel() // Cancel the timer to pause it
    }

    // Resume the timer from the remaining time
    private fun resumeTimer() {
        if (remainingMillis > 0) {
            startTimer(remainingMillis) // Start the timer with the remaining time
        }
    }

    // Reset the timer to its original state without starting it
    private fun resetTimer() {
        pauseTimer() // Stop the current timer
        remainingMillis = timerMillis // Reset the remaining time to the original duration

        // Reset the progress bar and time display
        circularProgressBar.progress = maxProgress
        updateTimerText((timerMillis / 1000).toInt()) // Update the time display to the original time

        // Re-enable the start button after the timer is reset
        startBtn.isEnabled = true
    }

    // Update the time display (TextView) based on the number of seconds left
    private fun updateTimerText(secondsLeft: Int) {
        val hours = secondsLeft / 3600
        val minutes = (secondsLeft % 3600) / 60
        val seconds = secondsLeft % 60

        // Format the time as HH:MM:SS and display it in the TextView
        timeTxt.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    // Cancel the timer when the activity is destroyed to prevent memory leaks
    override fun onDestroy() {
        timer?.cancel() // Cancel the timer if it's running
        super.onDestroy()
    }
}
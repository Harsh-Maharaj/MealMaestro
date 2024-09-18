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

    private lateinit var timeTxt: TextView
    private lateinit var circularProgressBar: ProgressBar
    private lateinit var editHours: EditText
    private lateinit var editMinutes: EditText
    private lateinit var editSeconds: EditText
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private var timerMillis: Long = 0 // Total timer duration in milliseconds
    private var remainingMillis: Long = 0 // Remaining time when the timer is paused
    private var timer: CountDownTimer? = null
    private val maxProgress = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout)
        timeTxt = findViewById(R.id.timeTxt)
        circularProgressBar = findViewById(R.id.circularProgressBar)
        editHours = findViewById(R.id.editHours)
        editMinutes = findViewById(R.id.editMinutes)
        editSeconds = findViewById(R.id.editSeconds)

        // Set up the Toolbar as the ActionBar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Setup Drawer Toggle
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
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Initialize timer control buttons
        val startBtn = findViewById<Button>(R.id.startBtn)
        startBtn.setOnClickListener {
            val hours = editHours.text.toString().toIntOrNull() ?: 0
            val minutes = editMinutes.text.toString().toIntOrNull() ?: 0
            val seconds = editSeconds.text.toString().toIntOrNull() ?: 0
            timerMillis = ((hours * 3600) + (minutes * 60) + seconds) * 1000L
            if (timerMillis > 0) {
                startTimer(timerMillis)
            }
        }

        val pauseBtn = findViewById<Button>(R.id.pauseBtn)
        pauseBtn.setOnClickListener { pauseTimer() }

        val resumeBtn = findViewById<Button>(R.id.resumeBtn)
        resumeBtn.setOnClickListener { resumeTimer() }

        val resetBtn = findViewById<Button>(R.id.resetBtn)
        resetBtn.setOnClickListener { resetTimer() }
    }

    // Handle drawer icon click
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    // Start the timer for the given duration
    private fun startTimer(durationMillis: Long) {
        circularProgressBar.max = maxProgress

        // If resuming, use the remainingMillis; if starting new, use durationMillis
        val startMillis = if (remainingMillis > 0) remainingMillis else durationMillis

        timer = object : CountDownTimer(startMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMillis = millisUntilFinished // Update remaining time

                val secondsRemaining = (millisUntilFinished / 1000).toInt()
                updateTimerText(secondsRemaining)

                // Update the progress bar
                val progressPercentage = ((millisUntilFinished.toFloat() / timerMillis) * maxProgress).toInt()
                circularProgressBar.progress = progressPercentage
            }

            override fun onFinish() {
                updateTimerText(0)
                circularProgressBar.progress = 0
            }
        }.start()
    }

    // Pause the timer and store the remaining time
    private fun pauseTimer() {
        timer?.cancel()
    }

    // Resume the timer from the remaining time
    private fun resumeTimer() {
        if (remainingMillis > 0) {
            startTimer(remainingMillis)
        }
    }

    // Reset the timer to its original state
    // Reset the timer to the original time that was set (timerMillis)
    private fun resetTimer() {
        pauseTimer()
        remainingMillis = timerMillis // Reset remaining time to the original duration
        circularProgressBar.progress = maxProgress
        startTimer(timerMillis) // Start the timer again with the original duration
    }

    // Update the displayed time based on the number of seconds left
    private fun updateTimerText(secondsLeft: Int) {
        val hours = secondsLeft / 3600
        val minutes = (secondsLeft % 3600) / 60
        val seconds = secondsLeft % 60

        timeTxt.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}
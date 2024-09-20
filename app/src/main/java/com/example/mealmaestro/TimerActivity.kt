package com.example.mealmaestro

// Import statements for various Android components and libraries used in the activity
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
import android.speech.tts.TextToSpeech
import android.os.Vibrator
import android.os.VibrationEffect
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class TimerActivity : AppCompatActivity() {

    // UI components for displaying the timer and progress
    private lateinit var timeTxt: TextView // TextView to display the current time
    private lateinit var circularProgressBar: ProgressBar // ProgressBar to show the remaining time visually
    private lateinit var editHours: EditText // EditText to input hours
    private lateinit var editMinutes: EditText // EditText to input minutes
    private lateinit var editSeconds: EditText // EditText to input seconds
    private lateinit var drawerLayout: DrawerLayout // DrawerLayout for the navigation drawer
    private lateinit var toggle: ActionBarDrawerToggle // ActionBarDrawerToggle to toggle the navigation drawer

    // Timer control buttons
    private lateinit var startBtn: Button // Button to start the timer
    private lateinit var pauseBtn: Button // Button to pause the timer
    private lateinit var resumeBtn: Button // Button to resume the timer
    private lateinit var resetBtn: Button // Button to reset the timer

    // Variables to handle the timer
    private var timerMillis: Long = 0 // Total duration of the timer in milliseconds
    private var remainingMillis: Long = 0 // Remaining time in milliseconds when the timer is paused
    private var timer: CountDownTimer? = null // Reference to the CountDownTimer object
    private val maxProgress = 100 // Maximum value for the progress bar (100%)

    // Text-to-Speech engine for announcing when the timer is up
    private lateinit var textToSpeech: TextToSpeech // TextToSpeech engine for spoken output

    // Called when the activity is first created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer) // Set the content view to the activity_timer XML layout

        // Initialize the UI components
        drawerLayout = findViewById(R.id.drawer_layout) // Get the DrawerLayout from the XML layout
        timeTxt = findViewById(R.id.timeTxt) // Get the TextView to display the time
        circularProgressBar = findViewById(R.id.circularProgressBar) // Get the ProgressBar for the timer
        editHours = findViewById(R.id.editHours) // Get the EditText for hours input
        editMinutes = findViewById(R.id.editMinutes) // Get the EditText for minutes input
        editSeconds = findViewById(R.id.editSeconds) // Get the EditText for seconds input
        startBtn = findViewById(R.id.startBtn) // Get the Button for starting the timer
        pauseBtn = findViewById(R.id.pauseBtn) // Get the Button for pausing the timer
        resumeBtn = findViewById(R.id.resumeBtn) // Get the Button for resuming the timer
        resetBtn = findViewById(R.id.resetBtn) // Get the Button for resetting the timer

        // Set up the Toolbar as the ActionBar
        val toolbar = findViewById<Toolbar>(R.id.toolbar) // Get the Toolbar from the layout
        setSupportActionBar(toolbar) // Set the Toolbar as the ActionBar for this activity

        // Setup Drawer Toggle for the navigation drawer
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle) // Add a listener to toggle the drawer
        toggle.syncState() // Synchronize the state of the drawer toggle

        // Initialize Text-to-Speech
        textToSpeech = TextToSpeech(this) { status -> // Create a new TextToSpeech instance
            if (status == TextToSpeech.SUCCESS) { // If initialization is successful
                textToSpeech.language = Locale.US // Set the language for the TextToSpeech to US English
            }
        }

        // Initialize timer control buttons (start, pause, resume, reset)
        startBtn.setOnClickListener {
            // Parse the input time (hours, minutes, seconds) into milliseconds
            val hours = editHours.text.toString().toIntOrNull() ?: 0 // Convert the input hours to an integer
            val minutes = editMinutes.text.toString().toIntOrNull() ?: 0 // Convert the input minutes to an integer
            val seconds = editSeconds.text.toString().toIntOrNull() ?: 0 // Convert the input seconds to an integer
            timerMillis = ((hours * 3600) + (minutes * 60) + seconds) * 1000L // Convert the total time to milliseconds
            if (timerMillis > 0) { // If the timer has a valid duration
                startTimer(timerMillis) // Start the timer with the parsed duration
                startBtn.isEnabled = false // Disable the start button while the timer is running
            }
        }

        pauseBtn.setOnClickListener { pauseTimer() } // Pause the timer when the pause button is clicked
        resumeBtn.setOnClickListener { resumeTimer() } // Resume the timer when the resume button is clicked
        resetBtn.setOnClickListener { resetTimer() } // Reset the timer when the reset button is clicked

        // Handle navigation drawer item clicks
        val navigationView: NavigationView = findViewById(R.id.nav_view) // Get the NavigationView from the layout
        navigationView.setNavigationItemSelectedListener { menuItem -> // Set a listener for navigation item clicks
            when (menuItem.itemId) { // Check which menu item was clicked
                R.id.nav_home -> { // If the home item is clicked
                    val intent = Intent(this, MainActivity::class.java) // Create an intent to navigate to the MainActivity
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear the back stack
                    startActivity(intent) // Start the MainActivity
                }
                R.id.nav_settings -> {
                    // Handle Settings action (currently empty)
                }
                R.id.nav_logout -> { // If the logout item is clicked
                    FirebaseAuth.getInstance().signOut() // Log out the user
                    val intent = Intent(this, LoginActivity::class.java) // Create an intent to navigate to the LoginActivity
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear the back stack
                    startActivity(intent) // Start the LoginActivity
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START) // Close the navigation drawer after a selection is made
            true // Return true to indicate the menu item click was handled
        }
    }

    // Start the timer for the given duration in milliseconds
    private fun startTimer(durationMillis: Long) {
        circularProgressBar.max = maxProgress // Set the maximum value for the progress bar
        val startMillis = if (remainingMillis > 0) remainingMillis else durationMillis // If there is remaining time, use it, otherwise use the full duration

        // Create a new CountDownTimer object
        timer = object : CountDownTimer(startMillis, 1000) { // CountDownTimer that ticks every second
            override fun onTick(millisUntilFinished: Long) {
                remainingMillis = millisUntilFinished // Update the remaining time
                val secondsRemaining = (millisUntilFinished / 1000).toInt() // Calculate the remaining seconds
                updateTimerText(secondsRemaining) // Update the time display

                // Update the progress bar based on the remaining time
                val progressPercentage = ((millisUntilFinished.toFloat() / timerMillis) * maxProgress).toInt() // Calculate the progress percentage
                circularProgressBar.progress = progressPercentage // Set the progress bar value
            }

            override fun onFinish() {
                updateTimerText(0) // Set the time display to zero when the timer finishes
                circularProgressBar.progress = 0 // Reset the progress bar to zero
                startBtn.isEnabled = true // Re-enable the start button
                vibratePhone() // Vibrate the phone when the timer finishes
                textToSpeech.speak("Timer is up!", TextToSpeech.QUEUE_FLUSH, null, null) // Announce that the timer is up using TextToSpeech
            }
        }.start() // Start the CountDownTimer
    }

    // Pause the timer by cancelling the CountDownTimer
    private fun pauseTimer() {
        timer?.cancel() // Cancel the CountDownTimer
    }

    // Resume the timer from the remaining time
    private fun resumeTimer() {
        if (remainingMillis > 0) { // If there is remaining time
            startTimer(remainingMillis) // Start the timer with the remaining time
        }
    }

    // Reset the timer to its original state without starting it
    private fun resetTimer() {
        pauseTimer() // Pause the current timer
        remainingMillis = timerMillis // Reset the remaining time to the original duration
        circularProgressBar.progress = maxProgress // Set the progress bar to its maximum value
        updateTimerText((timerMillis / 1000).toInt()) // Update the time display to the original time
        startBtn.isEnabled = true // Re-enable the start button
    }

    // Update the time display (TextView) based on the number of seconds left
    private fun updateTimerText(secondsLeft: Int) {
        val hours = secondsLeft / 3600 // Calculate the hours left
        val minutes = (secondsLeft % 3600) / 60 // Calculate the minutes left
        val seconds = secondsLeft % 60 // Calculate the seconds left
        timeTxt.text = String.format("%02d:%02d:%02d", hours, minutes, seconds) // Format the time and set it to the TextView
    }

    // Vibrate the phone when the timer finishes
    private fun vibratePhone() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator // Get the Vibrator system service
        if (vibrator.hasVibrator()) { // Check if the device has a vibrator
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate( // Vibrate the phone for 500 milliseconds using VibrationEffect on newer Android versions
                    VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                vibrator.vibrate(500) // Vibrate for 500 milliseconds on older Android versions
            }
        }
    }

    // Clean up Text-to-Speech and timer on activity destruction
    override fun onDestroy() {
        timer?.cancel() // Cancel the timer to prevent memory leaks
        if (::textToSpeech.isInitialized) { // Check if TextToSpeech is initialized
            textToSpeech.stop() // Stop any ongoing TextToSpeech output
            textToSpeech.shutdown() // Shut down the TextToSpeech engine
        }
        super.onDestroy() // Call the superclass's onDestroy method
    }
}

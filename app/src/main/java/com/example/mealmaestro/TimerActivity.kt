package com.example.mealmaestro

// Import statements for various Android components and libraries used in the activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.view.MenuItem
import android.view.WindowManager
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
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class TimerActivity : AppCompatActivity() {

    // UI components for displaying the timer and progress
    private lateinit var timeTxt: TextView
    private lateinit var circularProgressBar: ProgressBar
    private lateinit var editHours: EditText
    private lateinit var editMinutes: EditText
    private lateinit var editSeconds: EditText
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    // Timer control buttons
    private lateinit var startBtn: Button
    private lateinit var pauseBtn: Button
    private lateinit var resumeBtn: Button
    private lateinit var resetBtn: Button

    // Variables to handle the timer
    private var timerMillis: Long = 0
    private var remainingMillis: Long = 0
    private var timer: CountDownTimer? = null
    private val maxProgress = 100

    // Text-to-Speech engine for announcing when the timer is up
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeFromPreferences()  // Apply the selected theme
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        // Keep the screen on while using the TimerActivity
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

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
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Initialize Text-to-Speech
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.US
            }
        }

        // Initialize timer control buttons
        startBtn.setOnClickListener {
            val hours = editHours.text.toString().toIntOrNull() ?: 0
            val minutes = editMinutes.text.toString().toIntOrNull() ?: 0
            val seconds = editSeconds.text.toString().toIntOrNull() ?: 0
            timerMillis = ((hours * 3600) + (minutes * 60) + seconds) * 1000L
            if (timerMillis > 0) {
                startTimer(timerMillis)
                startBtn.isEnabled = false
            }
        }

        pauseBtn.setOnClickListener { pauseTimer() }
        resumeBtn.setOnClickListener { resumeTimer() }
        resetBtn.setOnClickListener { resetTimer() }

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                R.id.nav_settings -> {
                    // Handle Settings action (currently empty)
                }
                R.id.nav_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun applyThemeFromPreferences() {
        val sharedPreferences: SharedPreferences =
            android.preference.PreferenceManager.getDefaultSharedPreferences(this)
        val selectedTheme = sharedPreferences.getInt("SelectedTheme", 0)
        when (selectedTheme) {
            1 -> setTheme(R.style.DynamicTheme1)
            2 -> setTheme(R.style.DynamicTheme2)
            3 -> setTheme(R.style.DynamicTheme3)
            4 -> setTheme(R.style.DynamicTheme4)
        }
    }

    private fun startTimer(durationMillis: Long) {
        circularProgressBar.max = maxProgress
        val startMillis = if (remainingMillis > 0) remainingMillis else durationMillis

        timer = object : CountDownTimer(startMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMillis = millisUntilFinished
                val secondsRemaining = (millisUntilFinished / 1000).toInt()
                updateTimerText(secondsRemaining)

                val progressPercentage =
                    ((millisUntilFinished.toFloat() / timerMillis) * maxProgress).toInt()
                circularProgressBar.progress = progressPercentage
            }

            override fun onFinish() {
                updateTimerText(0)
                circularProgressBar.progress = 0
                startBtn.isEnabled = true
                vibratePhone()
                textToSpeech.speak("Timer is up!", TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }.start()
    }

    private fun pauseTimer() {
        timer?.cancel()
    }

    private fun resumeTimer() {
        if (remainingMillis > 0) {
            startTimer(remainingMillis)
        }
    }

    private fun resetTimer() {
        pauseTimer()
        remainingMillis = timerMillis
        circularProgressBar.progress = maxProgress
        updateTimerText((timerMillis / 1000).toInt())
        startBtn.isEnabled = true
    }

    private fun updateTimerText(secondsLeft: Int) {
        val hours = secondsLeft / 3600
        val minutes = (secondsLeft % 3600) / 60
        val seconds = secondsLeft % 60
        timeTxt.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun vibratePhone() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                vibrator.vibrate(500)
            }
        }
    }

    override fun onDestroy() {
        timer?.cancel()
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
}

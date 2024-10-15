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

    // UI components
    private lateinit var timeTxt: TextView
    private lateinit var circularProgressBar: ProgressBar
    private lateinit var editHours: EditText
    private lateinit var editMinutes: EditText
    private lateinit var editSeconds: EditText
    private lateinit var startBtn: Button

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    // Timer variables
    private var timerMillis: Long = 0
    private var remainingMillis: Long = 0
    private var timer: CountDownTimer? = null
    private val maxProgress = 100

    // Text-to-Speech engine
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeFromPreferences()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        // Keep the screen on while using the activity
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Initialize UI components
        timeTxt = findViewById(R.id.timeTxt)
        circularProgressBar = findViewById(R.id.circularProgressBar)
        editHours = findViewById(R.id.editHours)
        editMinutes = findViewById(R.id.editMinutes)
        editSeconds = findViewById(R.id.editSeconds)
        startBtn = findViewById(R.id.startBtn)

        // Set initial states
        timeTxt.text = "00:00:00"
        circularProgressBar.progress = 0
        startBtn.isEnabled = true
        editHours.setText("")
        editMinutes.setText("")
        editSeconds.setText("")

        // Set up Text-to-Speech
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.US
            }
        }

        // Handle Start button click
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
        timer = object : CountDownTimer(durationMillis, 1000) {
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

    private fun updateTimerText(secondsLeft: Int) {
        val hours = secondsLeft / 3600
        val minutes = (secondsLeft % 3600) / 60
        val seconds = secondsLeft % 60
        timeTxt.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun vibratePhone() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
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

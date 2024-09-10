package com.example.mealmaestro

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.DecimalFormat
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.mealmaestro.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle



    private lateinit var timeTxt: TextView
    private lateinit var circularProgressBar: ProgressBar
    private lateinit var linearProgressBar: ProgressBar

    private val countdownTime = 60
    private val clockTime = (countdownTime * 1000).toLong()
    private val progressTime = (clockTime / 1000).toFloat()

    private lateinit var TimerActivity: TimerActivity

    private val onBackPressedCallback = object : OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
        onBackPressedMethod()
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()



        onBackPressedDispatcher.addCallback(this,onBackPressedCallback)

        timeTxt =findViewById(R.id.timeTxt)
        circularProgressBar = findViewById(R.id.circularProgressBar)
        linearProgressBar = findViewById(R.id.linearProgressBar)

        var secondLeft = 0
        TimerActivity = object : TimerActivity(clockTime, 1000){}
        TimerActivity.onTick = {millisUntilFinished ->
            val second = (millisUntilFinished / 1000.0f).roundToInt()
            if (second != secondLeft){
                secondLeft = second

                timerFormat(
                    secondLeft,
                    timeTxt)
            }
        }
        TimerActivity.onFinish = {
            timerFormat(
                0,
                timeTxt)
        }
        circularProgressBar.max = progressTime.toInt()
        linearProgressBar.max = progressTime.toInt()

        circularProgressBar.progress = progressTime.toInt()
        linearProgressBar.progress = progressTime.toInt()

        TimerActivity.startTimer()

        val pauseBtn = findViewById<Button>(R.id.pauseBtn)
        val resumeBtn = findViewById<Button>(R.id.resumeBtn)
        val resetBtn = findViewById<Button>(R.id.resetBtn)

        pauseBtn.setOnClickListener {
            TimerActivity.pauseTimer()
        }

        resumeBtn.setOnClickListener {
            TimerActivity.resumeTimer()
        }

        resetBtn.setOnClickListener {
            circularProgressBar.max = progressTime.toInt()
            linearProgressBar.max = progressTime.toInt()
            TimerActivity.restartTimer()
        }



        //
        drawerLayout = binding.drawerLayout

        // Set up Toolbar
        val toolbar: Toolbar = binding.topToolbar
        setSupportActionBar(toolbar)

        // Set up the ActionBarDrawerToggle for the navigation drawer
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Set up the BottomNavigationView with NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNavigationView: BottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.setupWithNavController(navController)
    }



    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
     }



    @SuppressLint("SetTextI18n")
    private fun timerFormat(secondLeft: Int, timeTxt: TextView?) {
        linearProgressBar.progress = secondLeft
        circularProgressBar.progress = secondLeft
        val decimalFormat = DecimalFormat("00")
        val hour = secondLeft / 3600
        val min = (secondLeft % 3600) / 60
        val seconds = secondLeft % 60

        val timeFormat1 = decimalFormat.format(secondLeft)
        val timeFormat2 = decimalFormat.format(min) + ":" + decimalFormat.format(seconds)
        val timeFormat3 = decimalFormat.format(hour) + ":" + decimalFormat.format(min) + ":" + decimalFormat.format(seconds)

        timeTxt!!.text = timeFormat1 + "\n" + timeFormat2 + "\n" + timeFormat3

    }

    private fun onBackPressedMethod() {
        TimerActivity.destroyTimer()
        finish()
    }

    override fun onPause() {
        TimerActivity.pauseTimer()
        super.onPause()
    }

    override fun onResume() {
        TimerActivity.resumeTimer()
        super.onResume()
    }

    override fun onDestroy() {
        TimerActivity.destroyTimer()
        super.onDestroy()
    }



}

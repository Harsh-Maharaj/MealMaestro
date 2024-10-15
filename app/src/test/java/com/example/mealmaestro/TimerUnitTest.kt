package com.example.mealmaestro

import android.os.Build
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU], manifest = Config.NONE)
class TimerUnitTest {

    private lateinit var activity: TimerActivity

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(TimerActivity::class.java).setup().get()
    }

    @Test
    fun testTimerInitialization_Success() {
        // Get references to the UI components
        val timeTxt = activity.findViewById<TextView>(R.id.timeTxt)
        val circularProgressBar = activity.findViewById<ProgressBar>(R.id.circularProgressBar)
        val startBtn = activity.findViewById<Button>(R.id.startBtn)
        val editHours = activity.findViewById<EditText>(R.id.editHours)
        val editMinutes = activity.findViewById<EditText>(R.id.editMinutes)
        val editSeconds = activity.findViewById<EditText>(R.id.editSeconds)

        // Assert that all UI components are properly initialized
        assertNotNull("Time TextView should not be null", timeTxt)
        assertNotNull("ProgressBar should not be null", circularProgressBar)
        assertNotNull("Start Button should not be null", startBtn)
        assertNotNull("Hours EditText should not be null", editHours)
        assertNotNull("Minutes EditText should not be null", editMinutes)
        assertNotNull("Seconds EditText should not be null", editSeconds)

        // Check initial state
        assertEquals("Initial time should be 00:00:00", "00:00:00", timeTxt.text.toString())
        assertTrue("Start button should be initially enabled", startBtn.isEnabled)
        assertEquals("Progress bar should be initially at 0", 0, circularProgressBar.progress)
        assertEquals("Hours input should be initially empty", "", editHours.text.toString())
        assertEquals("Minutes input should be initially empty", "", editMinutes.text.toString())
        assertEquals("Seconds input should be initially empty", "", editSeconds.text.toString())
    }
}
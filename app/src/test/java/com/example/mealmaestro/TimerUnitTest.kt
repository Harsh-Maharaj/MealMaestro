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
        val timeTxt = activity.findViewById<TextView>(R.id.timeTxt)
        val circularProgressBar = activity.findViewById<ProgressBar>(R.id.circularProgressBar)
        val startBtn = activity.findViewById<Button>(R.id.startBtn)
        val editHours = activity.findViewById<EditText>(R.id.editHours)
        val editMinutes = activity.findViewById<EditText>(R.id.editMinutes)
        val editSeconds = activity.findViewById<EditText>(R.id.editSeconds)

        assertNotNull(timeTxt)
        assertNotNull(circularProgressBar)
        assertNotNull(startBtn)
        assertNotNull(editHours)
        assertNotNull(editMinutes)
        assertNotNull(editSeconds)

        assertEquals("00:00:00", timeTxt.text.toString())
        assertTrue(startBtn.isEnabled)
        assertEquals(0, circularProgressBar.progress)
        assertEquals("", editHours.text.toString())
        assertEquals("", editMinutes.text.toString())
        assertEquals("", editSeconds.text.toString())
    }
}

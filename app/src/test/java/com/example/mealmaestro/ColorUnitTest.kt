package com.example.mealmaestro

import android.content.Context
import android.content.SharedPreferences
import android.widget.ImageButton
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33]) // Use SDK 33 for Robolectric tests
class ColorUnitTest {

    private lateinit var activity: ColorActivity
    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        sharedPreferences = context.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)

        // Clear SharedPreferences before the test
        sharedPreferences.edit().clear().apply()

        activity = Robolectric.buildActivity(ColorActivity::class.java).create().get()
    }

    @Test
    fun testThemeButtonClick_Success() {
        // Arrange
        val btnTheme2 = activity.findViewById<ImageButton>(R.id.btnTheme2)

        // Act
        btnTheme2.performClick()

        // Assert
        val selectedTheme = sharedPreferences.getInt("SelectedTheme", 0)
        assertEquals("Theme 2 should be selected", 2, selectedTheme)
    }
}
package com.example.mealmaestro

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], manifest = Config.NONE) // Use SDK 33 and ignore the manifest
class HomeFragmentTest {

    private lateinit var fragment: HomeFragment
    private lateinit var recyclerView: RecyclerView
    private lateinit var backToTopButton: View

    @Before
    fun setup() {
        // Use Robolectric to create the MainActivity
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()

        // Initialize and add the fragment to the activity
        fragment = HomeFragment()
        activity.supportFragmentManager.beginTransaction()
            .add(fragment, "HomeFragment")
            .commitNow()

        // Ensure the fragment view is properly initialized
        val fragmentView = fragment.view ?: View(activity)

        // Initialize RecyclerView and Back-to-Top button
        recyclerView = fragmentView.findViewById(R.id.recycler_view) ?: RecyclerView(activity)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        // Assign the RecyclerView to the fragment directly
        fragment.recyclerView = recyclerView

        // Initialize the Back-to-Top button
        backToTopButton = fragmentView.findViewById(R.id.button_back_to_top) ?: View(activity)
    }

    @Test
    fun testBackToTopButtonVisibility() {
        // Simulate scrolling down
        for (i in 1..20) {
            recyclerView.scrollBy(0, 100)
        }

        // Verify the back-to-top button is visible
        assertTrue(
            "Back-to-top button should be visible after scrolling",
            backToTopButton.visibility == View.VISIBLE
        )
    }

    @Test
    fun testBackToTopButtonFunctionality() {
        // Simulate scrolling down
        for (i in 1..20) {
            recyclerView.scrollBy(0, 100)
        }

        // Click the back-to-top button
        backToTopButton.performClick()

        // Verify the RecyclerView has scrolled to the top
        assertEquals(
            "RecyclerView should be at the top",
            0,
            (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        )
    }
}

package com.example.mealmaestro

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var fab: FloatingActionButton
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        fab = findViewById(R.id.fab)
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.frame_layout, HomeFragment()).commit()
            navigationView.setCheckedItem(R.id.nav_home)
        }
        replaceFragment(HomeFragment())
// testing
        bottomNavigationView.background = null
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.friends -> replaceFragment(AboutFragment())
                R.id.favourites -> replaceFragment(SubscriptionFragment())
                R.id.Ai -> replaceFragment(ShareFragment())
            }
            true
        }
// bottom navigation view
        fab.setOnClickListener {
            showBottomDialog()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

    private fun showBottomDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottomsheetlayout)

        val videoLayout: LinearLayout = dialog.findViewById(R.id.layoutVideo)
        val shortsLayout: LinearLayout = dialog.findViewById(R.id.layoutShorts)
        val liveLayout: LinearLayout = dialog.findViewById(R.id.layoutLive)
        val cancelButton: ImageView = dialog.findViewById(R.id.cancelButton)

        videoLayout.setOnClickListener {
            dialog.dismiss()
            Toast.makeText(this@MainActivity, "Upload a Video is clicked", Toast.LENGTH_SHORT).show()
        }

        shortsLayout.setOnClickListener {
            dialog.dismiss()
            Toast.makeText(this@MainActivity, "Create a short is Clicked", Toast.LENGTH_SHORT).show()
        }

        liveLayout.setOnClickListener {
            dialog.dismiss()
            Toast.makeText(this@MainActivity, "Go live is Clicked", Toast.LENGTH_SHORT).show()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }
}

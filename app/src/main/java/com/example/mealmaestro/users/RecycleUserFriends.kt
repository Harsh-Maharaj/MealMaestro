package com.example.mealmaestro.users

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmaestro.Helper.DataBase
import com.example.mealmaestro.R
import com.example.mealmaestro.databinding.ActivityRecycleUserFriendsBinding

// Activity to display and manage a list of user's friends using a RecyclerView
class RecycleUserFriends : AppCompatActivity() {

    // Declare variables for binding, data, adapter, and database
    private lateinit var binding: ActivityRecycleUserFriendsBinding
    private lateinit var friendList: ArrayList<Users>
    private lateinit var adapter: FriendsAdapter
    private lateinit var friendRecyclerView: RecyclerView
    private lateinit var dataBase: DataBase

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeFromPreferences()  // Apply the selected theme before anything else
        super.onCreate(savedInstanceState)

        // Inflate the layout using view binding
        binding = ActivityRecycleUserFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root) // Set the content view to the binding's root
        enableEdgeToEdge() // Enable edge-to-edge display support

        // Handle window insets to manage padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclingFriendsView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the RecyclerView and database
        friendList = ArrayList()
        adapter = FriendsAdapter(this@RecycleUserFriends, friendList)

        friendRecyclerView = binding.recyclingFriendsView
        friendRecyclerView.layoutManager = LinearLayoutManager(this@RecycleUserFriends)
        friendRecyclerView.adapter = adapter
        dataBase = DataBase(this@RecycleUserFriends)
        dataBase.getFriendsList(friendList, adapter)

        // Set an onClickListener to open the user list when the button is clicked
        binding.friendsToUsers.setOnClickListener {
            val intent = Intent(this@RecycleUserFriends, RecycleUserView::class.java)
            userActivityLauncher.launch(intent)
        }

        // Set an onClickListener for the back button to finish the activity and go back
        binding.friendBack.setOnClickListener {
            finish()
        }
    }

    // Refresh the friend list by fetching the updated list from the database
    private fun refreshFriendList() {
        dataBase.getFriendsList(friendList, adapter)
    }

    // Register a launcher to handle the result from starting an activity (in this case, adding a new friend)
    private val userActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                refreshFriendList()
            }
        }

    // Apply the selected theme from SharedPreferences
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
}

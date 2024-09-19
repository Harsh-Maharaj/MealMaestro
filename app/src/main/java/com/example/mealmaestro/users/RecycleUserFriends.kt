package com.example.mealmaestro.users

import android.app.Activity
import android.content.Intent
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
import com.example.mealmaestro.databinding.ActivityRecycleUserFriendsBinding

// Activity to display and manage a list of user's friends using a RecyclerView
class RecycleUserFriends : AppCompatActivity() {

    // Declare variables for binding, data, adapter, and database
    private lateinit var binding: ActivityRecycleUserFriendsBinding // View binding for activity layout
    private lateinit var friendList: ArrayList<Users> // List to hold friends
    private lateinit var adapter: FriendsAdapter // Adapter to manage displaying friends in the RecyclerView
    private lateinit var friendRecyclerView: RecyclerView // RecyclerView to display the friends list
    private lateinit var dataBase: DataBase // Database helper for managing Firebase operations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using view binding
        binding = ActivityRecycleUserFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root) // Set the content view to the binding's root
        enableEdgeToEdge() // Enable edge-to-edge display support

        // Handle window insets to manage padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclingFriendsView) { v, insets ->
            // Apply padding to avoid overlapping with system bars (status bar, navigation bar)
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the RecyclerView and database
        friendList = ArrayList() // Initialize the friend list as an empty ArrayList
        adapter = FriendsAdapter(this@RecycleUserFriends, friendList) // Set up the adapter with context and friend list

        friendRecyclerView = binding.recyclingFriendsView // Reference to the RecyclerView in the layout
        friendRecyclerView.layoutManager = LinearLayoutManager(this@RecycleUserFriends) // Set the layout manager for vertical scrolling
        friendRecyclerView.adapter = adapter // Attach the adapter to the RecyclerView
        dataBase = DataBase(this@RecycleUserFriends) // Initialize the database helper
        dataBase.getFriendsList(friendList, adapter) // Fetch and display the friends list from the database

        // Set an onClickListener to open the user list when the button is clicked
        binding.friendsToUsers.setOnClickListener {
            val intent = Intent(this@RecycleUserFriends, RecycleUserView::class.java)
            userActivityLauncher.launch(intent) // Launch the user activity and expect a result
        }

        // Set an onClickListener for the back button to finish the activity and go back
        binding.friendBack.setOnClickListener {
            finish() // Close the activity and return to the previous screen
        }
    }

    // Refresh the friend list by fetching the updated list from the database
    private fun refreshFriendList() {
        dataBase.getFriendsList(friendList, adapter) // Re-fetch the friends list and update the adapter
    }

    // Register a launcher to handle the result from starting an activity (in this case, adding a new friend)
    private val userActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // If the result is OK (friend added), refresh the friend list
                refreshFriendList()
            }
        }
}


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

class RecycleUserFriends : AppCompatActivity() {

    private lateinit var binding: ActivityRecycleUserFriendsBinding
    private lateinit var friendList: ArrayList<Users>
    private lateinit var adapter: FriendsAdapter
    private lateinit var friendRecyclerView: RecyclerView
    private lateinit var dataBase: DataBase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecycleUserFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        // Handle window insets for proper layout
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

        // Open the users list
        binding.friendsToUsers.setOnClickListener {
            val intent = Intent(this@RecycleUserFriends, RecycleUserView::class.java)
            userActivityLauncher.launch(intent) // Launch the activity and expect a result
        }

        // Back button
        binding.friendBack.setOnClickListener {
            finish()
        }
    }

    // Refresh the friend list
    private fun refreshFriendList() {
        dataBase.getFriendsList(friendList, adapter)
    }

    // Register the launcher for activity result
    private val userActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Refresh the friend list when a friend is added
                refreshFriendList()
            }
        }
}

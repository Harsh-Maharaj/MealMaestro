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
import com.facebook.appevents.codeless.internal.ViewHierarchy.setOnClickListener

class RecycleUserFriends : AppCompatActivity() {

    private lateinit var binding: ActivityRecycleUserFriendsBinding
    private lateinit var friendList: ArrayList<Users>
    private lateinit var adapter: FriendsAdapter
    private lateinit var friendRecyclerView: RecyclerView // bring the user and allocate them in the recycler viewer
    private lateinit var dataBase: DataBase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecycleUserFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclingFriendsView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        friendList = ArrayList()
        adapter = FriendsAdapter(this@RecycleUserFriends, friendList)

        friendRecyclerView = binding.recyclingFriendsView
        friendRecyclerView.layoutManager = LinearLayoutManager(this@RecycleUserFriends)
        friendRecyclerView.adapter = adapter
        dataBase = DataBase(this@RecycleUserFriends)
        dataBase.getFriendsList(friendList, adapter) // call the database for the information

        // Open the users list
        binding.friendsToUsers.setOnClickListener {
            val intent = Intent(this@RecycleUserFriends, RecycleUserView::class.java)
            userActivityLauncher.launch(intent) // Launch the activity and expect a result
        }

        // back button
        binding.friendBack.setOnClickListener {
            finish()
        }
    }

    // to refresh the friend view
    private fun refreshFriendList() {
        dataBase.getFriendsList(friendList, adapter) // this refresh the friendList
        //adapter.notifyDataSetChanged() // Notify adapter that data has changed
    }

    // Register the launcher
    private val userActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Refresh the friend list when a friend is added
                refreshFriendList()
            }
        }
}
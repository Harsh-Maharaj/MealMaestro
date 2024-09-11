package com.example.mealmaestro.users

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmaestro.Helper.DataBase
import com.example.mealmaestro.R
import com.example.mealmaestro.databinding.ActivityRecycleUserFriendsBinding

class RecycleUserFriends : AppCompatActivity() {

    private lateinit var binding: ActivityRecycleUserFriendsBinding
    private lateinit var friendList: ArrayList<Users>
    private lateinit var adapter: FriendsAdapter
    private lateinit var userRecyclerView: RecyclerView
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
        userRecyclerView = binding.recyclingFriendsView
        userRecyclerView.layoutManager = LinearLayoutManager(this@RecycleUserFriends)
        userRecyclerView.adapter = adapter
        dataBase = DataBase(this@RecycleUserFriends)
        dataBase.getFriendsList(friendList, adapter)

        // Add Friends button click listener
        binding.friendsToUsers.setOnClickListener {
            // Start the RecycleUserView activity
            val intent = Intent(this, RecycleUserView::class.java)
            startActivity(intent)
        }
    }
}

package com.example.mealmaestro.users

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
import com.example.mealmaestro.databinding.ActivityRecycleUserViewBinding

class RecycleUserFriends : AppCompatActivity() {

    private lateinit var binding: ActivityRecycleUserFriendsBinding
    private lateinit var friendList: ArrayList<Users>
    private lateinit var adapter: FriendsAdapter
    private lateinit var userRecyclerView: RecyclerView // bring the user and allocate them in the recycler viewer
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
        userRecyclerView = binding.recyclingFriendsView
        userRecyclerView.layoutManager = LinearLayoutManager(this@RecycleUserFriends)
        userRecyclerView.adapter = adapter
        dataBase = DataBase(this@RecycleUserFriends)
        dataBase.getFriendsList(friendList,adapter) // call the database for the information


    }
}
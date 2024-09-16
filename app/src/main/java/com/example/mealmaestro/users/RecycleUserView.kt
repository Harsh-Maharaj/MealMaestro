package com.example.mealmaestro.users

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat.getSerializableExtra
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmaestro.Helper.DataBase
import com.example.mealmaestro.databinding.ActivityRecycleUserViewBinding

class RecycleUserView : AppCompatActivity(){

    private lateinit var binding: ActivityRecycleUserViewBinding
    private lateinit var userList: ArrayList<Users>
    private lateinit var adapter: UsersAdapter
    private lateinit var userRecyclerView: RecyclerView // bring the user and allocate them in the recycler viewer
    private lateinit var dataBase: DataBase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecycleUserViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclingUserView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        userList = ArrayList()

        adapter = UsersAdapter(this@RecycleUserView, userList)
        userRecyclerView = binding.recyclingUserView
        userRecyclerView.layoutManager = LinearLayoutManager(this@RecycleUserView)
        userRecyclerView.adapter = adapter
        dataBase = DataBase(this)
        dataBase.getUsersFromDataBase(userList,adapter, object: DataBase.DataFetchCallback {
            override fun onDataFetched() {
                // Once data is fetched from the database, update userListFull
                adapter.updateUserListFull(ArrayList(userList))
                // Notify the adapter that data has changed
                adapter.notifyDataSetChanged()
            }
        })

        binding.userBack.setOnClickListener {
            finish()
        }

        binding.searchBar.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d("SearchBar", "Text changed: $p0")
                adapter.filter.filter(p0)
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
    }
}
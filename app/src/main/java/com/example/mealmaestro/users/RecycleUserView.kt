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

// Activity to display a list of users and allow searching through a RecyclerView
class RecycleUserView : AppCompatActivity() {

    // Declare variables for binding, data, adapter, and database
    private lateinit var binding: ActivityRecycleUserViewBinding // View binding for the activity layout
    private lateinit var userList: ArrayList<Users> // List to hold the users
    private lateinit var adapter: UsersAdapter // Adapter to manage displaying users in the RecyclerView
    private lateinit var userRecyclerView: RecyclerView // RecyclerView to display the user list
    private lateinit var dataBase: DataBase // Database helper for managing Firebase operations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using view binding
        binding = ActivityRecycleUserViewBinding.inflate(layoutInflater)
        setContentView(binding.root) // Set the content view to the binding's root
        enableEdgeToEdge() // Enable edge-to-edge display support

        // Handle window insets to manage padding for system bars (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclingUserView) { v, insets ->
            // Apply padding to avoid overlapping with system bars
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the user list, adapter, and database
        userList = ArrayList() // Initialize the user list as an empty ArrayList
        adapter = UsersAdapter(this@RecycleUserView, userList) // Set up the adapter with context and user list
        userRecyclerView = binding.recyclingUserView // Reference to the RecyclerView in the layout
        userRecyclerView.layoutManager = LinearLayoutManager(this@RecycleUserView) // Set layout manager for vertical scrolling
        userRecyclerView.adapter = adapter // Attach the adapter to the RecyclerView
        dataBase = DataBase(this) // Initialize the database helper

        // Fetch the users from the database and update the RecyclerView
        dataBase.getUsersFromDataBase(userList, adapter, object: DataBase.DataFetchCallback {
            override fun onDataFetched() {
                // Once the data is fetched, update the full user list for filtering purposes
                adapter.updateUserListFull(ArrayList(userList))
                // Notify the adapter that data has changed so it can refresh the UI
                adapter.notifyDataSetChanged()
            }
        })

        // Set an onClickListener for the back button to finish the activity and go back
        binding.userBack.setOnClickListener {
            finish() // Close the activity and return to the previous screen
        }

        // Add a text watcher to the search bar to filter the list of users as the user types
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            // When the text in the search bar changes, filter the adapter based on the input
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d("SearchBar", "Text changed: $p0")
                adapter.filter.filter(p0) // Use the adapter's filter function to search the list
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
    }
}

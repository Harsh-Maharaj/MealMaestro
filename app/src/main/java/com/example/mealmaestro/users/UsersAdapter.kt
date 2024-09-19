package com.example.mealmaestro.users

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmaestro.Chats.ChatFriendsActivity
import com.example.mealmaestro.Helper.DataBase
import com.example.mealmaestro.R
import com.google.firebase.auth.FirebaseAuth
import android.widget.Filter
import android.widget.Filterable
import java.util.Locale

// Adapter class for displaying a list of users in a RecyclerView, with support for filtering the list
class UsersAdapter(
    val context: Context, // Context of the activity or fragment
    private var userList: ArrayList<Users> // List of users to be displayed
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>(), Filterable {

    // A full copy of the user list for filtering purposes
    private var userListFull: ArrayList<Users> = ArrayList(userList)

    // Create and return the ViewHolder for each user item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        // Inflate the layout for each user item
        val view: View = LayoutInflater.from(context).inflate(R.layout.users_layout, parent, false)
        return UserViewHolder(view)
    }

    // Return the total number of items in the user list
    override fun getItemCount(): Int {
        return userList.size
    }

    // Bind data to the ViewHolder for each user item
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position] // Get the current user from the list
        Log.d("UsersAdapter", "Binding user: ${currentUser.username}, UID: ${currentUser.uid}, Icon: ${currentUser.icon}")

        // Set the username to the TextView
        holder.textName.text = currentUser.username

        // Fetch views for chat and add friend actions
        val chat = holder.itemView.findViewById<ImageView>(R.id.user_message)
        val addFriend = holder.itemView.findViewById<ImageView>(R.id.add_friend)

        // Set an onClickListener to open chat with the current user
        chat?.setOnClickListener {
            // Create an Intent to start ChatFriendsActivity, passing user details as extras
            val intent = Intent(context, ChatFriendsActivity::class.java)
            intent.putExtra("username", currentUser.username)
            intent.putExtra("uid", currentUser.uid)
            intent.putExtra("icon", currentUser.icon)
            context.startActivity(intent) // Start the chat activity
        }

        // Set an onClickListener to add the current user as a friend
        addFriend?.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            val dataBase = DataBase(context)
            // Add the friend to the current user's friend list in the database
            dataBase.addFriendToDataBase(auth.currentUser!!.uid, currentUser.uid)

            // Notify the calling activity that a friend was added, then finish the current activity
            (context as Activity).setResult(Activity.RESULT_OK)
            context.finish()
        }
    }

    // ViewHolder class to hold the views for each user item
    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.user_id) // TextView to display the user's username
    }

    // Implementation of filtering functionality
    override fun getFilter(): Filter {
        return object : Filter() {
            // Perform filtering based on the search constraint
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = ArrayList<Users>() // List to hold filtered results
                Log.d("AdapterInit", "UserListFull size: ${userListFull.size}")
                Log.d("PerformFiltering", "Constraint: $constraint")

                // If the search constraint is empty, show the full user list
                if (constraint.isNullOrEmpty()) {
                    filteredList.addAll(userListFull)
                    Log.d("UserListFullCheck", "User list full size: ${userListFull.size}")
                } else {
                    // Convert the constraint to lowercase for case-insensitive search
                    val filterPattern = constraint.toString().lowercase().trim()

                    // Iterate through the full user list and add users that match the filter pattern
                    for (user in userListFull) {
                        Log.d("UserListFull", "User username: ${user.username}")
                        Log.d("PerformFiltering", "User username: ${user.username}")
                        if (user.username?.lowercase()?.contains(filterPattern) == true) {
                            filteredList.add(user)
                        }
                    }
                }

                Log.d("FilterResults", "Filtered list size: ${filteredList.size}")

                // Return the filtered list as the result
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            // Publish the filtering results by updating the user list and notifying the adapter
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                userList.clear() // Clear the current user list
                userList.addAll(results?.values as ArrayList<Users>) // Add the filtered users
                Log.d("PublishResults", "Updated list size: ${userList.size}")
                notifyDataSetChanged() // Notify the adapter to refresh the RecyclerView
            }
        }
    }

    // Update the full user list used for filtering
    fun updateUserListFull(newUserList: ArrayList<Users>) {
        userListFull = newUserList // Replace the current full user list with the new list
    }
}


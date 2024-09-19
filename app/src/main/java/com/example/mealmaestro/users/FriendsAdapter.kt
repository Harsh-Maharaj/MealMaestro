package com.example.mealmaestro.users

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmaestro.Chats.ChatFriendsActivity
import com.example.mealmaestro.Helper.DataBase
import com.example.mealmaestro.R
import com.google.firebase.auth.FirebaseAuth

// Adapter class to handle the display of friends in a RecyclerView
class FriendsAdapter(
    val context: Context, // Context of the activity/fragment where the adapter is being used
    val friendsList: ArrayList<Users> // List of friends to display in the RecyclerView
    // Uncomment this line if you want to use an interface to manage adding/removing friends
    // private val addOrRemoveFriend: AddOrRemoveFriend
) : RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder>() {

    // ViewHolder class that holds the views for each item in the RecyclerView
    class FriendsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.friend_id) // TextView for the friend's username
    }

    // Inflates the layout for each item in the RecyclerView and returns the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        // Inflate the layout for each friend item
        val view: View = LayoutInflater.from(context).inflate(R.layout.friends_layout, parent, false)
        return FriendsViewHolder(view) // Return the ViewHolder with the inflated layout
    }

    // Returns the total number of items in the friends list
    override fun getItemCount(): Int {
        return friendsList.size
    }

    // Binds data to the ViewHolder for each friend item
    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        val currentFriend = friendsList[position] // Get the current friend from the list
        holder.textName.text = currentFriend.username // Set the friend's username to the TextView

        // Find the ImageView for chat and remove friend functionality
        val chat = holder.itemView.findViewById<ImageView>(R.id.friend_message)
        val removeFriend = holder.itemView.findViewById<ImageView>(R.id.remove_friend)

        // Initialize the database helper and Firebase Authentication
        val dataBase = DataBase(context)
        val auth = FirebaseAuth.getInstance()

        // Set a click listener on the chat ImageView to start a chat with the selected friend
        chat?.setOnClickListener {
            // Create an intent to open ChatFriendsActivity with the friend's details
            val intent = Intent(context, ChatFriendsActivity::class.java)
            intent.putExtra("username", currentFriend.username) // Pass friend's username
            intent.putExtra("uid", currentFriend.uid) // Pass friend's UID
            intent.putExtra("icon", currentFriend.icon) // Pass friend's profile icon
            context.startActivity(intent) // Start the chat activity
        }

        // Set a click listener on the removeFriend ImageView to remove the friend from the user's list
        removeFriend?.setOnClickListener {
            // Get the current user ID from Firebase Authentication
            auth.currentUser?.uid?.let { userId ->
                // Get the friend's UID
                currentFriend.uid?.let { friendId ->
                    // Remove the friend from the database
                    dataBase.removeFriendFromDataBase(userId, friendId)
                }
            }

            // Refresh the friends list view after removing a friend
            val options = ActivityOptions.makeCustomAnimation(context, 0, 0) // No animation
            context.startActivity(Intent(context, RecycleUserFriends::class.java), options.toBundle())

            // Ensure no animation transition occurs on older Android versions
            if (context is RecycleUserFriends) {
                context.overridePendingTransition(0, 0) // Disable animation
                context.finish() // Close the activity to refresh the view
            }
        }
    }
}


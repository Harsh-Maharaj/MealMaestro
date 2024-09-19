package com.example.mealmaestro

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmaestro.R

// Adapter for the RecyclerView displaying a list of friends
class FriendsListAdapter(
    private val friendsList: List<String>, // List of friends (or friend IDs)
    private val onFriendSelected: (String) -> Unit // Lambda function to handle friend selection
) : RecyclerView.Adapter<FriendsListAdapter.FriendsViewHolder>() {

    // Called when RecyclerView needs a new ViewHolder to represent an item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        // Inflate the item layout (item_friend.xml) for each friend in the list
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return FriendsViewHolder(view) // Return a new ViewHolder instance
    }

    // Binds the data (friend ID) to the ViewHolder at the specified position
    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        val friendId = friendsList[position] // Get the friend ID at the current position
        holder.bind(friendId) // Bind the friend ID to the ViewHolder
    }

    // Returns the total number of items (friends) in the list
    override fun getItemCount(): Int {
        return friendsList.size
    }

    // ViewHolder class representing each item in the RecyclerView
    inner class FriendsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val friendNameTextView: TextView = itemView.findViewById(R.id.text_view_friend_name) // Reference to the TextView displaying the friend's name

        // Bind the friend ID to the TextView and set a click listener for the item
        fun bind(friendId: String) {
            // Set the friend name (assuming friendId can be replaced with actual names if needed)
            friendNameTextView.text = friendId
            // Set a click listener on the entire item view to handle friend selection
            itemView.setOnClickListener {
                // Trigger the lambda function when the friend is selected
                onFriendSelected(friendId)
            }
        }
    }
}

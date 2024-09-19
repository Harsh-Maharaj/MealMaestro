package com.example.mealmaestro

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmaestro.Helper.Comment
import com.example.mealmaestro.R
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

// Adapter for displaying comments in a RecyclerView
class CommentAdapter(
    private val context: Context, // Context to access resources and layouts
    private var commentList: MutableList<Comment> // List of comments to be displayed
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    // This method inflates the layout for each item (comment) and creates a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        // Inflate the layout for each comment item from XML
        val view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false)

        // Set the view to be focusable, which allows it to respond to focus events (like highlighting)
        view.isFocusable = true
        view.isFocusableInTouchMode = true

        return CommentViewHolder(view) // Return the created ViewHolder
    }

    // Binds the data (comment) to the ViewHolder at the specified position in the list
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position] // Get the comment for the current position
        holder.bind(comment) // Bind the comment data to the ViewHolder

        // Handle focus change to highlight the item when it gains or loses focus
        holder.itemView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Change the background color when the item is focused
                holder.itemView.setBackgroundColor(context.resources.getColor(R.color.focused_background))
            } else {
                // Revert to the default background color when not focused
                holder.itemView.setBackgroundColor(context.resources.getColor(android.R.color.white))
            }
        }
    }

    // Returns the total number of comments in the list
    override fun getItemCount(): Int {
        return commentList.size
    }

    // Update the list of comments with new data and notify the RecyclerView to refresh the display
    fun updateComments(newComments: MutableList<Comment>) {
        this.commentList = newComments
        notifyDataSetChanged() // Notify RecyclerView that the data has changed
    }

    // ViewHolder class to represent each comment item
    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // References to the TextViews for displaying the comment content, username, and timestamp
        private val textViewComment: TextView = itemView.findViewById(R.id.comment_text)
        private val textViewUsername: TextView = itemView.findViewById(R.id.comment_username)
        private val textViewTimestamp: TextView = itemView.findViewById(R.id.comment_timestamp)

        // Binds the comment data to the TextViews
        fun bind(comment: Comment) {
            textViewComment.text = comment.text // Set the comment text

            // Fetch the username from Firebase Realtime Database using the userId
            fetchUsernameFromRealtimeDatabase(comment.userId, textViewUsername)

            // Format the timestamp into a human-readable date string
            val formattedDate = getFormattedDate(comment.timestamp)
            textViewTimestamp.text = formattedDate // Set the formatted timestamp
        }

        // Helper function to convert timestamp (Long) into a formatted date string
        private fun getFormattedDate(timestamp: Long): String {
            // Use SimpleDateFormat to format the timestamp
            val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
            return sdf.format(Date(timestamp)) // Return the formatted date string
        }

        // Fetches the username from Firebase Realtime Database using the userId
        private fun fetchUsernameFromRealtimeDatabase(userId: String, textViewUsername: TextView) {
            // If userId is empty, set the username as "Unknown User"
            if (userId.isEmpty()) {
                textViewUsername.text = "Unknown User"
                return
            }

            // Get a reference to the user node in Firebase Realtime Database
            val userRef = FirebaseDatabase.getInstance("https://mealmaestro-46c0d-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("user")
                .child(userId)

            // Fetch the username from the database
            userRef.get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    // If the user exists, retrieve the username
                    val username = dataSnapshot.child("username").getValue(String::class.java)
                    textViewUsername.text = username ?: "Unknown" // Set the username or "Unknown" if null
                } else {
                    // If no user data is found, set "Unknown User"
                    textViewUsername.text = "Unknown User"
                }
            }.addOnFailureListener { e ->
                // Handle errors when fetching username from the database
                Log.e("CommentAdapter", "Error fetching username from Realtime Database: ${e.message}")
                textViewUsername.text = "Error" // Display "Error" if fetching fails
            }
        }
    }
}

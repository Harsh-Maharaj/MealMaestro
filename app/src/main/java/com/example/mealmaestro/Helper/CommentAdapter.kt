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

class CommentAdapter(
    private val context: Context,
    private var commentList: MutableList<Comment>
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false)

        // Set the entire view to be focusable
        view.isFocusable = true
        view.isFocusableInTouchMode = true

        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        holder.bind(comment)

        // Handle focus change to highlight the item when it's focused
        holder.itemView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Change the background color when the item is focused
                holder.itemView.setBackgroundColor(context.resources.getColor(R.color.focused_background))
            } else {
                // Revert to default background color when not focused
                holder.itemView.setBackgroundColor(context.resources.getColor(android.R.color.white))
            }
        }
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    fun updateComments(newComments: MutableList<Comment>) {
        this.commentList = newComments
        notifyDataSetChanged()
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewComment: TextView = itemView.findViewById(R.id.comment_text)
        private val textViewUsername: TextView = itemView.findViewById(R.id.comment_username)
        private val textViewTimestamp: TextView = itemView.findViewById(R.id.comment_timestamp)

        fun bind(comment: Comment) {
            textViewComment.text = comment.text

            // Fetch and set the username from Realtime Database using userId
            fetchUsernameFromRealtimeDatabase(comment.userId, textViewUsername)

            // Format the Long timestamp to a readable String
            val formattedDate = getFormattedDate(comment.timestamp)
            textViewTimestamp.text = formattedDate
        }

        // Helper function to convert timestamp from Long to String
        private fun getFormattedDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

        // Fetch the username from Realtime Database
        private fun fetchUsernameFromRealtimeDatabase(userId: String, textViewUsername: TextView) {
            if (userId.isEmpty()) {
                textViewUsername.text = "Unknown User"
                return
            }

            val userRef = FirebaseDatabase.getInstance("https://mealmaestro-46c0d-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("user")
                .child(userId)

            userRef.get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val username = dataSnapshot.child("username").getValue(String::class.java)
                    textViewUsername.text = username ?: "Unknown"
                } else {
                    textViewUsername.text = "Unknown User"
                }
            }.addOnFailureListener { e ->
                Log.e("CommentAdapter", "Error fetching username from Realtime Database: ${e.message}")
                textViewUsername.text = "Error"
            }
        }
    }
}

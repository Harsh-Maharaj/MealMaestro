package com.example.mealmaestro

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmaestro.CommentAdapter
import com.example.mealmaestro.Helper.Comment
import com.example.mealmaestro.R
import com.google.android.material.button.MaterialButton

// Activity to display and handle user comments on a post
class CommentsActivity : AppCompatActivity() {

    // Declare necessary UI elements and data structures
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var commentsList: RecyclerView
    private lateinit var postButton: MaterialButton
    private lateinit var commentInput: EditText
    private var comments: MutableList<Comment> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeFromPreferences()  // Apply the selected theme before setting the layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        // Initialize views by linking them to their respective XML components
        commentsList = findViewById(R.id.recyclerViewComments)
        postButton = findViewById(R.id.post_button)
        commentInput = findViewById(R.id.comment_input)

        // Set up RecyclerView with a LinearLayoutManager for vertical scrolling
        commentsList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        commentsList.setHasFixedSize(false)
        commentsList.isNestedScrollingEnabled = false

        // Initialize the adapter and bind it to the RecyclerView
        commentAdapter = CommentAdapter(this, comments)
        commentsList.adapter = commentAdapter

        // Fetch or simulate fetching comments
        fetchComments()

        // Set an onClickListener for the post button to handle posting new comments
        postButton.setOnClickListener {
            val newCommentText = commentInput.text.toString()
            if (newCommentText.isNotEmpty()) {
                val newComment = Comment(
                    userId = "current_user_id",
                    username = "current_user",
                    text = newCommentText,
                    timestamp = System.currentTimeMillis()
                )
                comments.add(newComment)
                commentAdapter.notifyItemInserted(comments.size - 1)
                commentsList.scrollToPosition(comments.size - 1)
                commentInput.text.clear()
            }
        }
    }

    // Apply the selected theme from SharedPreferences
    private fun applyThemeFromPreferences() {
        val sharedPreferences: SharedPreferences =
            android.preference.PreferenceManager.getDefaultSharedPreferences(this)
        val selectedTheme = sharedPreferences.getInt("SelectedTheme", 0)
        when (selectedTheme) {
            1 -> setTheme(R.style.DynamicTheme1)
            2 -> setTheme(R.style.DynamicTheme2)
            3 -> setTheme(R.style.DynamicTheme3)
            4 -> setTheme(R.style.DynamicTheme4)
        }
    }

    // Function to simulate fetching comments and populating the list
    private fun fetchComments() {
        val mockComments = mutableListOf(
            Comment(userId = "user1", username = "user1", text = "Great post!", timestamp = System.currentTimeMillis() - 60000),
            Comment(userId = "user2", username = "user2", text = "Thanks for the recipe!", timestamp = System.currentTimeMillis() - 120000),
            Comment(userId = "user3", username = "user3", text = "Looks good!", timestamp = System.currentTimeMillis() - 180000),
            Comment(userId = "user4", username = "user4", text = "Yummy!", timestamp = System.currentTimeMillis() - 240000),
            Comment(userId = "user5", username = "user5", text = "I will try this soon!", timestamp = System.currentTimeMillis() - 300000),
            Comment(userId = "user6", username = "user6", text = "This is amazing!", timestamp = System.currentTimeMillis() - 360000),
            Comment(userId = "user7", username = "user7", text = "Delicious!", timestamp = System.currentTimeMillis() - 420000),
            Comment(userId = "user8", username = "user8", text = "Thanks for sharing!", timestamp = System.currentTimeMillis() - 480000)
        )
        comments.addAll(mockComments)
        commentAdapter.notifyDataSetChanged()
    }
}

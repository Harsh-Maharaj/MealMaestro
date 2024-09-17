import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.EditText
import com.example.mealmaestro.CommentAdapter
import com.example.mealmaestro.Helper.Comment
import com.example.mealmaestro.R
import com.google.android.material.button.MaterialButton

class CommentsActivity : AppCompatActivity() {

    private lateinit var commentAdapter: CommentAdapter
    private lateinit var commentsList: RecyclerView
    private lateinit var postButton: MaterialButton // Changed to MaterialButton
    private lateinit var commentInput: EditText
    private var comments: MutableList<Comment> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        // Initialize views
        commentsList = findViewById(R.id.recyclerViewComments)
        postButton = findViewById(R.id.post_button)
        commentInput = findViewById(R.id.comment_input)

        // Set up RecyclerView with LinearLayoutManager
        commentsList.layoutManager = LinearLayoutManager(this)
        commentAdapter = CommentAdapter(this, comments)
        commentsList.adapter = commentAdapter

        // Simulate fetching comments or fetch from your database
        fetchComments()

        // Handle posting a new comment
        postButton.setOnClickListener {
            val newCommentText = commentInput.text.toString()
            if (newCommentText.isNotEmpty()) {
                val newComment = Comment(
                    userId = "current_user_id", // Replace with actual user ID
                    username = "current_user", // Replace with actual username
                    text = newCommentText,
                    timestamp = System.currentTimeMillis()
                )
                comments.add(newComment)
                commentAdapter.notifyItemInserted(comments.size - 1)
                commentsList.scrollToPosition(comments.size - 1) // Scroll to the latest comment
                commentInput.text.clear()  // Clear the input after posting
            }
        }
    }

    private fun fetchComments() {
        // comments to test scrolling
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


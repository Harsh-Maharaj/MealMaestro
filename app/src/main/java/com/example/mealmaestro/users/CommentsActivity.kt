import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.EditText
import com.example.mealmaestro.CommentAdapter
import com.example.mealmaestro.Helper.Comment
import com.example.mealmaestro.R
import com.google.android.material.button.MaterialButton

// Activity to display and handle user comments on a post
class CommentsActivity : AppCompatActivity() {

    // Declare necessary UI elements and data structures
    private lateinit var commentAdapter: CommentAdapter // Adapter to bind comment data to the RecyclerView
    private lateinit var commentsList: RecyclerView // RecyclerView to display the list of comments
    private lateinit var postButton: MaterialButton // Button to post a new comment
    private lateinit var commentInput: EditText // Input field for entering a new comment
    private var comments: MutableList<Comment> = mutableListOf() // List to hold the comments

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments) // Set the content view to the comments layout

        // Initialize views by linking them to their respective XML components
        commentsList = findViewById(R.id.recyclerViewComments)
        postButton = findViewById(R.id.post_button)
        commentInput = findViewById(R.id.comment_input)

        // Set up RecyclerView with a LinearLayoutManager for vertical scrolling
        commentsList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        commentsList.setHasFixedSize(false) // Allow RecyclerView to be dynamic in size
        commentsList.isNestedScrollingEnabled = false // Disable nested scrolling

        // Initialize the adapter and bind it to the RecyclerView
        commentAdapter = CommentAdapter(this, comments)
        commentsList.adapter = commentAdapter

        // Fetch or simulate fetching comments (mocked data in this case)
        fetchComments()

        // Set an onClickListener for the post button to handle posting new comments
        postButton.setOnClickListener {
            val newCommentText = commentInput.text.toString() // Get the text entered by the user
            if (newCommentText.isNotEmpty()) {
                // Create a new Comment object with the current user's data and the new comment text
                val newComment = Comment(
                    userId = "current_user_id", // Replace with actual user ID
                    username = "current_user", // Replace with actual username
                    text = newCommentText, // The comment text entered by the user
                    timestamp = System.currentTimeMillis() // Current timestamp
                )
                comments.add(newComment) // Add the new comment to the list
                commentAdapter.notifyItemInserted(comments.size - 1) // Notify the adapter to update the RecyclerView
                commentsList.scrollToPosition(comments.size - 1) // Scroll to the latest comment
                commentInput.text.clear()  // Clear the input field after posting the comment
            }
        }
    }

    // Function to simulate fetching comments and populating the list
    private fun fetchComments() {
        // Create a list of mock comments for testing purposes
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
        comments.addAll(mockComments) // Add the mock comments to the list
        commentAdapter.notifyDataSetChanged() // Notify the adapter to update the RecyclerView with new data
    }
}


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.mealmaestro.CommentAdapter
import com.example.mealmaestro.Helper.Comment
import com.example.mealmaestro.R

// A BottomSheetDialogFragment that displays a comment section
class CommentsBottomSheetFragment : BottomSheetDialogFragment() {

    // Declare the UI components and data structures for the comment section
    private lateinit var commentAdapter: CommentAdapter // Adapter to bind comment data to RecyclerView
    private lateinit var commentsList: RecyclerView // RecyclerView to display the list of comments
    private lateinit var postButton: Button // Button to post a new comment
    private lateinit var commentInput: EditText // Input field for entering a new comment
    private var comments: MutableList<Comment> = mutableListOf() // List to hold comments

    // Inflate the layout for the bottom sheet when it is created
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for the bottom sheet (R.layout.bottom_sheet_dialog_fragment)
        return inflater.inflate(R.layout.bottom_sheet_dialog_fragment, container, false)
    }

    // Initialize UI components and setup functionality after the view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the RecyclerView and input components
        commentsList = view.findViewById(R.id.recyclerViewComments)
        postButton = view.findViewById(R.id.post_button)
        commentInput = view.findViewById(R.id.comment_input)

        // Setup the CommentAdapter and bind it to the RecyclerView
        commentAdapter = CommentAdapter(requireContext(), comments)
        commentsList.adapter = commentAdapter
        commentsList.layoutManager = LinearLayoutManager(requireContext()) // Set LinearLayoutManager for vertical scrolling

        // Simulate fetching comments from a data source or database
        fetchComments()

        // Handle the posting of a new comment when the post button is clicked
        postButton.setOnClickListener {
            val newCommentText = commentInput.text.toString() // Get the text entered by the user
            if (newCommentText.isNotEmpty()) {
                // Create a new Comment object with the entered text and user details
                val newComment = Comment(
                    userId = "current_user_id", // Replace with actual user ID
                    username = "current_user", // Replace with actual username
                    text = newCommentText, // The text entered by the user
                    timestamp = System.currentTimeMillis() // Current timestamp
                )
                comments.add(newComment) // Add the new comment to the list
                commentAdapter.notifyItemInserted(comments.size - 1) // Notify the adapter of the new comment
                commentInput.text.clear()  // Clear the input field after posting
            }
        }
    }

    // Function to simulate fetching comments (replace with real data from a server/database)
    private fun fetchComments() {
        // Sample/mock comments for demonstration purposes
        val mockComments = mutableListOf(
            Comment(userId = "user1", username = "user1", text = "Great post!", timestamp = System.currentTimeMillis() - 60000),
            Comment(userId = "user2", username = "user2", text = "Thanks for the recipe!", timestamp = System.currentTimeMillis() - 120000)
        )
        comments.addAll(mockComments) // Add the mock comments to the list
        commentAdapter.notifyDataSetChanged() // Notify the adapter that the data has changed
    }

    // Ensure the bottom sheet expands to full height when opened
    override fun onStart() {
        super.onStart()

        // Get the view representing the bottom sheet
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        // Set the bottom sheet height to match the parent (full height)
        bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        // Get the BottomSheetBehavior and force the bottom sheet to expand fully
        val behavior = BottomSheetBehavior.from(bottomSheet!!)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED // Set the state to expanded
    }
}


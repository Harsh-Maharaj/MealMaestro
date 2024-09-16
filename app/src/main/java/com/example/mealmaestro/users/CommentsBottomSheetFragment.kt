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

class CommentsBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var commentAdapter: CommentAdapter
    private lateinit var commentsList: RecyclerView
    private lateinit var postButton: Button
    private lateinit var commentInput: EditText
    private var comments: MutableList<Comment> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate your bottom sheet layout
        return inflater.inflate(R.layout.bottom_sheet_dialog_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        commentsList = view.findViewById(R.id.recyclerViewComments)
        postButton = view.findViewById(R.id.post_button)
        commentInput = view.findViewById(R.id.comment_input)

        commentAdapter = CommentAdapter(requireContext(), comments)
        commentsList.adapter = commentAdapter
        commentsList.layoutManager = LinearLayoutManager(requireContext())

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
                commentInput.text.clear()  // Clear the input after posting
            }
        }
    }

    private fun fetchComments() {
        // Add mock comments (or fetch from your server/database)
        val mockComments = mutableListOf(
            Comment(userId = "user1", username = "user1", text = "Great post!", timestamp = System.currentTimeMillis() - 60000),
            Comment(userId = "user2", username = "user2", text = "Thanks for the recipe!", timestamp = System.currentTimeMillis() - 120000)
        )
        comments.addAll(mockComments)
        commentAdapter.notifyDataSetChanged()
    }

    // Ensure the bottom sheet expands fully when opened
    override fun onStart() {
        super.onStart()

        // Force the bottom sheet to expand to full height
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        val behavior = BottomSheetBehavior.from(bottomSheet!!)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}

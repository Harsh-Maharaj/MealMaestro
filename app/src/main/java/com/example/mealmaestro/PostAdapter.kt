package com.example.mealmaestro

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.mealmaestro.Helper.Post
import com.example.mealmaestro.Helper.DataBase
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(
    private val context: Context,
    private val postList: MutableList<Post>,
    private val onUnsave: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.image_view_post)
        private val textViewCaption: TextView = itemView.findViewById(R.id.text_view_caption)
        private val buttonSave: ImageButton = itemView.findViewById(R.id.button_save)
        private val buttonLike: ImageButton = itemView.findViewById(R.id.button_like)
        private val likeCount: TextView = itemView.findViewById(R.id.like_count)
        private val commentSection: TextView = itemView.findViewById(R.id.comment_section)
        private val editTextComment: EditText = itemView.findViewById(R.id.edit_text_comment)
        private val buttonPostComment: Button = itemView.findViewById(R.id.button_post_comment)
        private val commentsContainer: LinearLayout = itemView.findViewById(R.id.comment_input_section)

        fun bind(post: Post) {
            // Load the post image
            Glide.with(context)
                .load(post.image_url)
                .override(200, 200)
                .into(imageView)
            textViewCaption.text = post.caption

            // Check and update the save button state
            checkSaveStatus(post)

            // Setup Firestore listener for likes to always reflect real-time data
            setupLikeListener(post)

            // Set up comment posting functionality
            setupCommentSection(post)

            // Listen for new comments
            setupCommentListener(post)

            buttonSave.setOnClickListener {
                if (post.isSaved) {
                    unsavePost(post)
                } else {
                    savePost(post)
                }
            }

            buttonLike.setOnClickListener {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
                val dataBase = DataBase(context)

                // Toggle like and Firestore will handle the rest
                dataBase.likePost(post.postId, userId) { isLiked ->
                    val updatedLikes = if (isLiked) {
                        post.likes.toMutableMap().apply { put(userId, true) }
                    } else {
                        post.likes.toMutableMap().apply { remove(userId) }
                    }

                    // Optimistically update UI (real-time Firestore listener will also update)
                    updateLikeButton(post.copy(likes = updatedLikes))
                    likeCount.text = updatedLikes.size.toString()
                }
            }

            buttonPostComment.setOnClickListener {
                postComment(post)
            }
        }

        // Firestore listener for real-time likes synchronization
        private fun setupLikeListener(post: Post) {
            val postRef = FirebaseFirestore.getInstance().collection("posts").document(post.postId)

            postRef.addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    return@addSnapshotListener
                }

                val updatedPost = snapshot.toObject(Post::class.java) ?: return@addSnapshotListener

                // Update the like count and button color in real-time
                likeCount.text = updatedPost.likes.size.toString()
                updateLikeButton(updatedPost)
            }
        }

        // Method to update the like button color based on the user's like status
        private fun updateLikeButton(post: Post) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val color = if (post.likes.containsKey(currentUserId)) R.color.red else R.color.light_purple
            buttonLike.setColorFilter(ContextCompat.getColor(context, color))
        }

        // Check if the post is saved by the user and update the save button accordingly
        private fun checkSaveStatus(post: Post) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val favoritesRef = FirebaseFirestore.getInstance()
                .collection("favorites")
                .document(currentUserId)
                .collection("posts")

            favoritesRef.document(post.postId).get()
                .addOnSuccessListener { document ->
                    val isPostSaved = document.exists()
                    post.isSaved = isPostSaved
                    updateSaveButton(isPostSaved)
                }
                .addOnFailureListener {
                    // Handle the error
                }
        }

        private fun savePost(post: Post) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val favoritesRef = FirebaseFirestore.getInstance()
                .collection("favorites")
                .document(currentUserId)
                .collection("posts")

            favoritesRef.document(post.postId).set(post)
                .addOnSuccessListener {
                    post.isSaved = true
                    updateSaveButton(true)
                }
                .addOnFailureListener {
                    // Handle the error
                }
        }

        private fun unsavePost(post: Post) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val favoritesRef = FirebaseFirestore.getInstance()
                .collection("favorites")
                .document(currentUserId)
                .collection("posts")

            favoritesRef.document(post.postId).delete()
                .addOnSuccessListener {
                    post.isSaved = false
                    updateSaveButton(false)
                    onUnsave(post)
                }
                .addOnFailureListener {
                    // Handle the error
                }
        }

        private fun updateSaveButton(isSaved: Boolean) {
            val color = if (isSaved) R.color.yellow else R.color.standard_save
            buttonSave.setColorFilter(ContextCompat.getColor(context, color))
            buttonSave.setImageResource(if (isSaved) R.drawable.ic_save else R.drawable.ic_save)
        }

        // Set up comment section
        private fun setupCommentSection(post: Post) {
            // Show/Hide comment section based on visibility
            commentSection.setOnClickListener {
                if (commentsContainer.visibility == View.GONE) {
                    commentsContainer.visibility = View.VISIBLE
                } else {
                    commentsContainer.visibility = View.GONE
                }
            }
        }

        // Post a comment
        private fun postComment(post: Post) {
            val commentText = editTextComment.text.toString()
            if (commentText.isEmpty()) {
                Toast.makeText(context, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
                return
            }

            val currentUser = FirebaseAuth.getInstance().currentUser ?: return
            val comment = hashMapOf(
                "userId" to currentUser.uid,
                "username" to currentUser.displayName.orEmpty(),
                "text" to commentText,
                "timestamp" to System.currentTimeMillis()
            )

            val postRef = FirebaseFirestore.getInstance()
                .collection("posts")
                .document(post.postId)
                .collection("comments")

            postRef.add(comment)
                .addOnSuccessListener {
                    // Clear the input field and show success message
                    editTextComment.text.clear()
                    Toast.makeText(context, "Comment posted!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to post comment", Toast.LENGTH_SHORT).show()
                }
        }

        // Fetch comments in real-time and display them
        private fun setupCommentListener(post: Post) {
            val postRef = FirebaseFirestore.getInstance()
                .collection("posts")
                .document(post.postId)
                .collection("comments")
                .orderBy("timestamp")

            postRef.addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    return@addSnapshotListener
                }

                val commentText = StringBuilder()
                for (document in snapshot.documents) {
                    val commentData = document.data ?: continue
                    val username = commentData["username"].toString()
                    val text = commentData["text"].toString()
                    val timestamp = commentData["timestamp"] as? Long ?: 0L
                    val formattedDate = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
                        .format(Date(timestamp))

                    commentText.append("$username: $text\n$formattedDate\n\n")
                }

                commentSection.text = commentText.toString()
            }
        }
    }
}

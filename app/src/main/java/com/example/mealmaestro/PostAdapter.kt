package com.example.mealmaestro
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.mealmaestro.Helper.Post

class PostAdapter(
    private val context: Context,
    private val postList: MutableList<Post>,
    private val onUnsave: (Post) -> Unit  // Callback for unsaving
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
        private val buttonLike: ImageButton = itemView.findViewById(R.id.button_like)
        private val buttonSave: ImageButton = itemView.findViewById(R.id.button_save)
        private val buttonComment: ImageButton = itemView.findViewById(R.id.button_comment)

        fun bind(post: Post) {
            // Load the image using Glide
            Glide.with(context)
                .load(post.image_url)
                .override(200, 200)  // Optional size limitation to optimize memory usage
                .into(imageView)
            textViewCaption.text = post.caption

            // Set the initial save/unsave state
            buttonSave.text = if (post.isSaved) "Unsave" else "Save"

            // Save/Unsave functionality
            buttonSave.setOnClickListener {
                if (post.isSaved) {
                    unsavePost(post)
                    onUnsave(post)
                } else {
                    savePost(post)
                }
            }

            // Like button functionality
            buttonLike.setOnClickListener {
                likePost(post)
            }

            // Update the like button state
            updateLikeButton(post)
        }

        private fun likePost(post: Post) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val postRef = FirebaseFirestore.getInstance().collection("posts").document(post.postId)
            val likes = post.likes.toMutableMap()

            // Toggle like status
            if (likes.containsKey(currentUserId)) {
                likes.remove(currentUserId)
            } else {
                likes[currentUserId] = true
            }

            postRef.update("likes", likes)
                .addOnSuccessListener {
                    // Update like button color
                    updateLikeButton(post.copy(likes = likes))
                }
        }

        private fun savePost(post: Post) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val favoritesRef = FirebaseFirestore.getInstance()
                .collection("favorites")
                .document(currentUserId)
                .collection("posts")

            // Save the post in the user's favorites
            favoritesRef.document(post.postId).set(post)
                .addOnSuccessListener {
                    post.isSaved = true  // Update the post's saved state
                    buttonSave.text = "Unsave"
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

            // Unsave the post from the user's favorites
            favoritesRef.document(post.postId).delete()
                .addOnSuccessListener {
                    post.isSaved = false  // Update the post's saved state
                    buttonSave.text = "Save"
                }
                .addOnFailureListener {
                    // Handle the error
                }
        }

        private fun updateLikeButton(post: Post) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            if (post.likes.containsKey(currentUserId)) {
                buttonLike.setColorFilter(ContextCompat.getColor(context, R.color.red)) // Set to red when liked
            } else {
                buttonLike.setColorFilter(ContextCompat.getColor(context, R.color.light_purple)) // Set to light purple when not liked
            }
        }
    }
}

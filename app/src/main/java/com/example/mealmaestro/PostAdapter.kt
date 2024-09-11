package com.example.mealmaestro
import android.content.Context
import android.graphics.Color
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
import com.example.mealmaestro.R

data class Post(
    val user_id: String = "",
    val image_url: String = "",
    val caption: String = "",
    val likes: Map<String, Boolean> = mapOf()
)

class PostAdapter(private val context: Context, private val postList: MutableList<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

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
            Glide.with(context).load(post.image_url).into(imageView)
            textViewCaption.text = post.caption

            // Reset save button to light purple by default
            buttonSave.setColorFilter(ContextCompat.getColor(context, R.color.light_purple))

            // Check if the post is already saved asynchronously
            isPostSaved(post) { isSaved ->
                if (isSaved) {
                    buttonSave.setColorFilter(ContextCompat.getColor(context, R.color.yellow)) // Set to yellow when saved
                    buttonSave.setOnClickListener {
                        unsavePost(post)
                    }
                } else {
                    buttonSave.setColorFilter(ContextCompat.getColor(context, R.color.light_purple)) // Light purple (unsaved state)
                    buttonSave.setOnClickListener {
                        savePost(post)
                    }
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
            val postRef = FirebaseFirestore.getInstance().collection("posts").document(post.user_id)
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
            favoritesRef.document(post.user_id).set(post)
                .addOnSuccessListener {
                    buttonSave.setColorFilter(ContextCompat.getColor(context, R.color.yellow)) // Set to yellow when saved
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
            favoritesRef.document(post.user_id).delete()
                .addOnSuccessListener {
                    buttonSave.setColorFilter(ContextCompat.getColor(context, R.color.light_purple)) // Set back to light purple when unsaved
                }
                .addOnFailureListener {
                    // Handle the error
                }
        }

        private fun isPostSaved(post: Post, callback: (Boolean) -> Unit) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val favoritesRef = FirebaseFirestore.getInstance()
                .collection("favorites")
                .document(currentUserId)
                .collection("posts")
                .document(post.user_id)

            // Check if the post exists in the user's favorites collection
            favoritesRef.get().addOnSuccessListener { document ->
                callback(document.exists())
            }.addOnFailureListener {
                callback(false)
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

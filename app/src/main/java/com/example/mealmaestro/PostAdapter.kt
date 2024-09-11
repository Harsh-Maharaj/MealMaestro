package com.example.mealmaestro

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.mealmaestro.Helper.Post

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
        private val buttonLike: Button = itemView.findViewById(R.id.button_like)
        private val buttonSave: Button = itemView.findViewById(R.id.button_save)

        fun bind(post: Post) {
            // Load the image using Glide
            Glide.with(context).load(post.image_url).into(imageView)
            textViewCaption.text = post.caption

            // **Always reset the save button to default state** to avoid old state being shown
            buttonSave.text = "Save"

            // Check if the post is already saved asynchronously
            isPostSaved(post) { isSaved ->
                if (isSaved) {
                    buttonSave.text = "Unsave"
                    buttonSave.setOnClickListener {
                        unsavePost(post)
                    }
                } else {
                    buttonSave.text = "Save"
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
            favoritesRef.document(post.user_id).delete()
                .addOnSuccessListener {
                    buttonSave.text = "Save"
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
                buttonLike.text = "Liked"
            } else {
                buttonLike.text = "Like"
            }
        }
    }
}

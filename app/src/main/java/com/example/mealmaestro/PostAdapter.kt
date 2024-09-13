package com.example.mealmaestro

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.mealmaestro.Helper.Post
import com.example.mealmaestro.Helper.DataBase

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

        fun bind(post: Post) {
            Glide.with(context)
                .load(post.image_url)
                .override(200, 200)
                .into(imageView)
            textViewCaption.text = post.caption

            // Check and update the save button state
            checkSaveStatus(post)

            // Update UI for likes
            updateLikeButton(post)

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

                dataBase.likePost(post.postId, userId) { isLiked ->
                    // Create a new Post instance with updated likes
                    val updatedLikes = if (isLiked) {
                        post.likes.toMutableMap().apply { put(userId, true) }
                    } else {
                        post.likes.toMutableMap().apply { remove(userId) }
                    }

                    // Update the Post in the database
                    dataBase.dataBaseRef.child("posts").child(post.postId).setValue(post.copy(likes = updatedLikes))

                    // Update the UI
                    updateLikeButton(post.copy(likes = updatedLikes))
                    likeCount.text = updatedLikes.size.toString()
                }
            }

        }

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

            // Save the post in the user's favorites
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

            // Unsave the post from the user's favorites
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

        private fun updateLikeButton(post: Post) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val color = if (post.likes.containsKey(currentUserId)) R.color.red else R.color.light_purple
            buttonLike.setColorFilter(ContextCompat.getColor(context, color))
            likeCount.text = post.likes.size.toString()
        }
    }


}

package com.example.mealmaestro

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.mealmaestro.Helper.Comment
import com.example.mealmaestro.Helper.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

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
        private val buttonViewMore: TextView = itemView.findViewById(R.id.button_view_more)
        private val buttonComment: ImageButton = itemView.findViewById(R.id.button_comment)
        private val buttonSave: ImageButton = itemView.findViewById(R.id.button_save)
        private val buttonLike: ImageButton = itemView.findViewById(R.id.button_like)
        private val likeCount: TextView = itemView.findViewById(R.id.like_count)
        private val recyclerViewComments: RecyclerView = itemView.findViewById(R.id.recycler_view_comments)
        private val editTextComment: TextView = itemView.findViewById(R.id.edit_text_comment)
        private val buttonPostComment: TextView = itemView.findViewById(R.id.button_post_comment)

        fun bind(post: Post) {
            // Load the post image using Glide with dynamic height adjustment
            Glide.with(context)
                .load(post.image_url)
                .fitCenter()
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        resource?.let {
                            val imageViewWidth = imageView.width
                            val aspectRatio = it.intrinsicWidth.toFloat() / it.intrinsicHeight.toFloat()
                            val imageViewHeight = (imageViewWidth / aspectRatio).toInt()

                            // Set the ImageView height dynamically to maintain aspect ratio
                            imageView.layoutParams.height = imageViewHeight
                            imageView.requestLayout()
                        }
                        return false
                    }
                })
                .into(imageView)

            // Show "View More" button if the caption is long
            if (post.caption.length > 100) {
                buttonViewMore.visibility = View.VISIBLE
                textViewCaption.maxLines = if (post.isCaptionExpanded) Int.MAX_VALUE else 3
                buttonViewMore.text = if (post.isCaptionExpanded) "View Less" else "View More"
            } else {
                buttonViewMore.visibility = View.GONE
                textViewCaption.maxLines = Int.MAX_VALUE
            }

            // Handle View More button click
            buttonViewMore.setOnClickListener {
                post.isCaptionExpanded = !post.isCaptionExpanded
                notifyItemChanged(adapterPosition)
            }

            // Set caption text
            textViewCaption.text = post.caption

            // Set up the comment RecyclerView and Adapter
            val commentAdapter = CommentAdapter(context, post.comments)
            recyclerViewComments.layoutManager = LinearLayoutManager(context)
            recyclerViewComments.adapter = commentAdapter

            // Update like button and like count
            updateLikeButton(post)
            likeCount.text = "${post.likes.size} likes"

            // Handle like button click
            buttonLike.setOnClickListener {
                toggleLike(post)
            }

            // Toggle visibility of comments based on the post.isCommentsVisible flag
            recyclerViewComments.visibility = if (post.isCommentsVisible) View.VISIBLE else View.GONE

            // Handle comment icon click to toggle the comments visibility
            buttonComment.setOnClickListener {
                post.isCommentsVisible = !post.isCommentsVisible
                notifyItemChanged(adapterPosition)
            }

            // Post comment functionality
            buttonPostComment.setOnClickListener {
                postComment(post)
            }

            // Handle saving and unsaving the post
            buttonSave.setOnClickListener {
                if (post.isSaved) {
                    unsavePost(post)
                } else {
                    savePost(post)
                }
            }
        }

        private fun toggleLike(post: Post) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val postRef = FirebaseFirestore.getInstance().collection("posts").document(post.postId)

            if (post.likes.containsKey(userId)) {
                // Unlike the post (remove user's like)
                post.likes.remove(userId)
                postRef.update("likes.$userId", FieldValue.delete()) // Remove the like from Firestore
                    .addOnSuccessListener {
                        // Update the local post object and UI
                        updateLikeButton(post)
                        likeCount.text = "${post.likes.size} likes" // Update the like count text
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to unlike post.", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Like the post (add user's like)
                post.likes[userId] = true
                postRef.update("likes.$userId", true) // Add the like to Firestore
                    .addOnSuccessListener {
                        // Update the local post object and UI
                        updateLikeButton(post)
                        likeCount.text = "${post.likes.size} likes" // Update the like count text
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to like post.", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        private fun updateLikeButton(post: Post) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val isLiked = post.likes.containsKey(currentUserId)
            if (isLiked) {
                buttonLike.setImageResource(R.drawable.ic_liked)  // Change to red liked icon
            } else {
                buttonLike.setImageResource(R.drawable.ic_like)  // Change to default like icon
            }
        }

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
                    editTextComment.text = ""
                    Toast.makeText(context, "Comment posted!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to post comment", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(context, "Failed to save post", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(context, "Failed to unsave post", Toast.LENGTH_SHORT).show()
                }
        }

        private fun updateSaveButton(isSaved: Boolean) {
            val icon = if (isSaved) R.drawable.ic_save else R.drawable.ic_unsave
            buttonSave.setImageResource(icon)
        }
    }
}


package com.example.mealmaestro

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

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
        private val usernameTextView: TextView = itemView.findViewById(R.id.username)
        private val postTimeTextView: TextView = itemView.findViewById(R.id.post_time)

        fun bind(post: Post) {
            // Load the post image using Glide
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

            // Fetch the username from Realtime Database
            fetchUsernameFromRealtimeDatabase(post.user_id, usernameTextView)

            // Set timestamp and other post details
            post.created_at?.let {
                postTimeTextView.text = getTimeAgo(it.toDate().time)
            }

            if (post.caption.length > 100) {
                buttonViewMore.visibility = View.VISIBLE
                textViewCaption.maxLines = if (post.isCaptionExpanded) Int.MAX_VALUE else 3
                buttonViewMore.text = if (post.isCaptionExpanded) "View Less" else "View More"
            } else {
                buttonViewMore.visibility = View.GONE
                textViewCaption.maxLines = Int.MAX_VALUE
            }

            buttonViewMore.setOnClickListener {
                post.isCaptionExpanded = !post.isCaptionExpanded
                notifyItemChanged(adapterPosition)
            }

            textViewCaption.text = post.caption

            val commentAdapter = CommentAdapter(context, post.comments)
            recyclerViewComments.layoutManager = LinearLayoutManager(context).apply {
                isSmoothScrollbarEnabled = true
            }
            recyclerViewComments.adapter = commentAdapter
            recyclerViewComments.setHasFixedSize(true)

            updateLikeButton(post)
            likeCount.text = "${post.likes.size} likes"

            buttonLike.setOnClickListener {
                toggleLike(post)
            }

            recyclerViewComments.visibility =
                if (post.isCommentsVisible) View.VISIBLE else View.GONE

            buttonComment.setOnClickListener {
                post.isCommentsVisible = !post.isCommentsVisible
                notifyItemChanged(adapterPosition)
            }

            buttonPostComment.setOnClickListener {
                postComment(post)
            }

            buttonSave.setOnClickListener {
                if (post.isSaved) {
                    unsavePost(post)
                } else {
                    savePost(post)
                }
            }
        }

        private fun fetchUsernameFromRealtimeDatabase(userId: String, usernameTextView: TextView) {
            if (userId.isEmpty()) {
                usernameTextView.text = "Unknown User"
                return
            }

            val userRef = FirebaseDatabase.getInstance("https://mealmaestro-46c0d-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("user")
                .child(userId)

            userRef.get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val username = dataSnapshot.child("username").getValue(String::class.java)
                    usernameTextView.text = username ?: "Unknown"
                } else {
                    usernameTextView.text = "Unknown User"
                }
            }.addOnFailureListener { e ->
                Log.e("PostAdapter", "Error fetching username from Realtime Database: ${e.message}")
                usernameTextView.text = "Error"
            }
        }

        private fun toggleLike(post: Post) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val postRef = FirebaseFirestore.getInstance().collection("posts").document(post.postId)

            if (post.likes.containsKey(userId)) {
                post.likes.remove(userId)
                postRef.update("likes.$userId", FieldValue.delete())
                    .addOnSuccessListener {
                        updateLikeButton(post)
                        likeCount.text = "${post.likes.size} likes"
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to unlike post.", Toast.LENGTH_SHORT).show()
                    }
            } else {
                post.likes[userId] = true
                postRef.update("likes.$userId", true)
                    .addOnSuccessListener {
                        updateLikeButton(post)
                        likeCount.text = "${post.likes.size} likes"
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to like post.", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        private fun updateLikeButton(post: Post) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val isLiked = post.likes.containsKey(currentUserId)
            buttonLike.setImageResource(if (isLiked) R.drawable.ic_liked else R.drawable.ic_like)
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

        private fun getTimeAgo(time: Long): String {
            val diff = System.currentTimeMillis() - time
            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            return when {
                seconds < 60 -> "Just now"
                minutes < 60 -> "$minutes minute(s) ago"
                hours < 24 -> "$hours hour(s) ago"
                else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(time)
            }
        }
    }
}

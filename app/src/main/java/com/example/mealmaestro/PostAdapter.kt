package com.example.mealmaestro

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
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
        private val videoView: VideoView = itemView.findViewById(R.id.video_view_post)
        private val textViewCaption: TextView = itemView.findViewById(R.id.text_view_caption)
        private val buttonViewMore: TextView = itemView.findViewById(R.id.button_view_more)
        private val buttonComment: ImageButton = itemView.findViewById(R.id.button_comment)
        private val buttonSave: ImageButton = itemView.findViewById(R.id.button_save)
        private val buttonLike: ImageButton = itemView.findViewById(R.id.button_like)
        private val buttonShare: ImageButton = itemView.findViewById(R.id.button_share)
        private val buttonMore: ImageButton = itemView.findViewById(R.id.button_more)
        private val likeCount: TextView = itemView.findViewById(R.id.like_count)
        private val recyclerViewComments: RecyclerView = itemView.findViewById(R.id.recycler_view_comments)
        private val editTextComment: TextView = itemView.findViewById(R.id.edit_text_comment)
        private val buttonPostComment: TextView = itemView.findViewById(R.id.button_post_comment)
        private val usernameTextView: TextView = itemView.findViewById(R.id.username)
        private val postTimeTextView: TextView = itemView.findViewById(R.id.post_time)

        init {
            buttonMore.setOnClickListener {
                showGenerateShoppingListDialog(postList[adapterPosition])
            }
        }

        fun bind(post: Post) {
            if (post.video_url.isNotEmpty()) {
                // Display the video if video_url is not empty
                imageView.visibility = View.GONE
                videoView.visibility = View.VISIBLE
                videoView.setVideoURI(Uri.parse(post.video_url))
                videoView.setOnPreparedListener { mp ->
                    mp.setVolume(1f, 1f)
                    videoView.start()
                }
                videoView.setOnCompletionListener {
                    // Handle video playback completion if needed
                }
                videoView.setOnErrorListener { mp, what, extra ->
                    // Handle video playback errors
                    true
                }
            } else {
                // Display the image if there is no video
                videoView.visibility = View.GONE
                imageView.visibility = View.VISIBLE
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
                                val aspectRatio =
                                    it.intrinsicWidth.toFloat() / it.intrinsicHeight.toFloat()
                                val imageViewHeight = (imageViewWidth / aspectRatio).toInt()
                                imageView.layoutParams.height = imageViewHeight
                                imageView.requestLayout()
                            }
                            return false
                        }
                    })
                    .into(imageView)
            }

            fetchUsernameFromRealtimeDatabase(post.user_id, usernameTextView)
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

            buttonShare.setOnClickListener {
                showFriendsDialog(post)
            }
        }

        // Fetch the username from Firebase Realtime Database based on the user ID
        private fun fetchUsernameFromRealtimeDatabase(userId: String, usernameTextView: TextView) {
            if (userId.isEmpty()) {
                usernameTextView.text = "Unknown User"
                return
            }

            // Reference the user in Firebase Realtime Database
            val userRef =
                FirebaseDatabase.getInstance("https://mealmaestro-46c0d-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("user")
                    .child(userId)

            // Fetch the username and update the TextView
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

        // Toggle the like status for the current post
        private fun toggleLike(post: Post) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val postRef = FirebaseFirestore.getInstance().collection("posts").document(post.postId)

            if (post.likes.containsKey(userId)) {
                // Unlike the post if already liked
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
                // Like the post if not already liked
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

        // Update the like button icon based on whether the post is liked by the user
        private fun updateLikeButton(post: Post) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val isLiked = post.likes.containsKey(currentUserId)
            buttonLike.setImageResource(if (isLiked) R.drawable.ic_liked else R.drawable.ic_like)
        }

        // Post a comment to the current post
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

            // Add the comment to the Firestore comments collection
            val postRef = FirebaseFirestore.getInstance()
                .collection("posts")
                .document(post.postId)
                .collection("comments")

            postRef.add(comment)
                .addOnSuccessListener {
                    editTextComment.text = ""  // Clear the comment input field
                    Toast.makeText(context, "Comment posted!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to post comment", Toast.LENGTH_SHORT).show()
                }
        }

        // Save the post to the user's favorites
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

        // Unsave the post from the user's favorites
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
                    onUnsave(post)  // Notify that the post has been unsaved
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to unsave post", Toast.LENGTH_SHORT).show()
                }
        }

        // Update the save button icon based on whether the post is saved
        private fun updateSaveButton(isSaved: Boolean) {
            val icon = if (isSaved) R.drawable.ic_save else R.drawable.ic_unsave
            buttonSave.setImageResource(icon)
        }

        // Get the relative time from the post creation time
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

        // Show a dialog to select a friend to share the post with
        private fun showFriendsDialog(post: Post) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val friendsRef = FirebaseDatabase.getInstance()
                .getReference("user")
                .child(userId)
                .child("friends")

            friendsRef.get().addOnSuccessListener { dataSnapshot ->
                val friendsList = mutableListOf<String>()
                dataSnapshot.children.forEach { friendSnapshot ->
                    friendSnapshot.getValue(String::class.java)?.let { friendsList.add(it) }
                }

                // Inflate the custom dialog layout
                val dialogView =
                    LayoutInflater.from(context).inflate(R.layout.dialog_friends_list, null)
                val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recycler_view_friends)

                // Set up RecyclerView with FriendsListAdapter
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = FriendsListAdapter(friendsList) { selectedFriendId ->
                    sharePostWithFriend(post, selectedFriendId)
                }

                // Show the dialog with the friends list
                AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setNegativeButton("Cancel", null)
                    .show()
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to fetch friends", Toast.LENGTH_SHORT).show()
            }
        }

        // Share the post with a selected friend by sending a message
        private fun sharePostWithFriend(post: Post, friendId: String) {
            val message = hashMapOf(
                "sender" to FirebaseAuth.getInstance().currentUser?.uid,
                "receiver" to friendId,
                "message" to "Check out this post!",
                "postId" to post.postId,
                "timestamp" to System.currentTimeMillis()
            )

            val messagesRef = FirebaseFirestore.getInstance()
                .collection("messages")
                .document()

            messagesRef.set(message)
                .addOnSuccessListener {
                    Toast.makeText(context, "Post shared!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to share post", Toast.LENGTH_SHORT).show()
                }
        }

        // Show dialog to confirm generating a shopping list from post's caption
        private fun showGenerateShoppingListDialog(post: Post) {
            AlertDialog.Builder(context)
                .setTitle("Generate Shopping List")
                .setMessage("Do you want to generate a shopping list from this post's caption?")
                .setPositiveButton("Yes") { dialog, which ->
                    generateShoppingList(post.caption)
                }
                .setNegativeButton("No", null)
                .show()
        }

        // Generate a shopping list from the post's caption
        private fun generateShoppingList(caption: String) {
            val ingredients = extractIngredients(caption)

            if (ingredients.isEmpty()) {
                Log.d("ShoppingList", "No valid ingredients found in caption.")
                return
            }

            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val shoppingListRef = FirebaseFirestore.getInstance()
                .collection("shoppingLists")
                .document(currentUserId)
                .collection("items")

            ingredients.forEach { ingredient ->
                val item = ShoppingListItem(name = ingredient, checked = false)
                shoppingListRef.add(item)
                    .addOnSuccessListener {
                        Log.d("ShoppingList", "Ingredient added to shopping list: $ingredient")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ShoppingList", "Failed to add ingredient: $ingredient", e)
                    }
            }
        }


        // Add ingredients to Firestore under the current user's shopping list
        private fun addToShoppingList(ingredients: List<String>) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val shoppingListRef = FirebaseFirestore.getInstance()
                .collection("shoppingLists")
                .document(userId)

            // Check if document exists or create a new one
            shoppingListRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Document exists, update the ingredients
                    shoppingListRef.update(
                        "ingredients",
                        FieldValue.arrayUnion(*ingredients.toTypedArray())
                    )
                        .addOnSuccessListener {
                            Toast.makeText(context, "Shopping list updated!", Toast.LENGTH_SHORT)
                                .show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Failed to update shopping list.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    // Create new document with ingredients
                    val newShoppingList = hashMapOf("ingredients" to ingredients)
                    shoppingListRef.set(newShoppingList)
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "Shopping list created and updated!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Failed to create shopping list.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to access shopping list.", Toast.LENGTH_SHORT)
                    .show()
            }
        }


        // Extract ingredients from the caption by splitting on commas
        private fun extractIngredients(caption: String): List<String> {
            // Keywords that are commonly found in cooking instructions
            val instructionKeywords = listOf(
                "cook",
                "stir",
                "boil",
                "sauté",
                "heat",
                "mix",
                "bake",
                "slice",
                "preheat",
                "chop",
                "simmer",
                "remove",
                "serve",
                "add"
            )

            // Extract the section after "Ingredients:" and split by line
            val ingredientsSection = caption.substringAfter("Ingredients:", "")
            return ingredientsSection.split("\n")
                .map { it.trim() }
                .filter { line ->
                    // Check if the line is not empty and does not contain any of the instruction keywords
                    line.isNotEmpty() && instructionKeywords.none { keyword ->
                        line.contains(
                            keyword,
                            ignoreCase = true
                        )
                    }
                }
        }

    }

}


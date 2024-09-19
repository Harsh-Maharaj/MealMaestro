package com.example.mealmaestro.Helper

import com.google.firebase.Timestamp
import java.util.UUID

// Data class to represent a Post in the app
data class Post(
    var postId: String = UUID.randomUUID().toString(), // Unique identifier for the post, generated using UUID
    val user_id: String = "", // The ID of the user who created the post
    val username: String = "", // The username of the user who created the post
    val image_url: String = "", // The URL of the image associated with the post
    val caption: String = "", // The caption text for the post
    var likes: MutableMap<String, Boolean> = mutableMapOf(), // A map to store users who liked the post, where the key is the user ID and the value is a Boolean (liked status)
    val isPublic: Boolean = true, // A flag to indicate if the post is public or private (default is public)
    var isSaved: Boolean = false, // A flag to track if the post is saved by the user (default is not saved)
    val created_at: Timestamp? = null, // The timestamp when the post was created, using Firebase's Timestamp object
    var comments: MutableList<Comment> = mutableListOf(), // A list to store comments on the post, initialized as an empty list
    var isCommentsVisible: Boolean = false, // A flag to track whether comments are currently visible or hidden
    var isCaptionExpanded: Boolean = false // A flag to track whether the caption is expanded (showing the full caption) or collapsed
)

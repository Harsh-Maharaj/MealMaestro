package com.example.mealmaestro.Helper

import com.google.firebase.Timestamp
import java.util.UUID

data class Post(
    val postId: String = UUID.randomUUID().toString(),  // Generate a unique ID if not provided
    val user_id: String = "",
    val image_url: String = "",
    val caption: String = "",
    val likes: Map<String, Boolean> = emptyMap(),  // Initialize likes as an empty map
    val isPublic: Boolean = true,  // Optional field with a default value
    var isSaved: Boolean = false,  // Track saved state with default value of false
    val created_at: Timestamp? = null,  // Add this field to store the creation time
    var comments: String = ""  // Add a field to store the comments as a string
)

package com.example.mealmaestro.Helper

import com.google.firebase.Timestamp
import java.util.UUID

data class Post(
    var postId: String = UUID.randomUUID().toString(),
    val user_id: String = "",
    val image_url: String = "",
    val caption: String = "",
    var likes: MutableMap<String, Boolean> = mutableMapOf(), // Change to MutableMap
    val isPublic: Boolean = true,
    var isSaved: Boolean = false,
    val created_at: Timestamp? = null,
    var comments: MutableList<Comment> = mutableListOf(),
    var isCommentsVisible: Boolean = false // Field to track if comments are visible
)

package com.example.mealmaestro.Helper

import com.google.firebase.Timestamp
import java.util.UUID

data class Post(
    val postId: String = UUID.randomUUID().toString(),
    val user_id: String = "",
    val image_url: String = "",
    val caption: String = "",
    val likes: Map<String, Boolean> = emptyMap(),
    val isPublic: Boolean = true,
    var isSaved: Boolean = false,
    val created_at: Timestamp? = null
)

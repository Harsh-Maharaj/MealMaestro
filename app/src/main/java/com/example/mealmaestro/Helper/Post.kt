package com.example.mealmaestro.Helper

import java.util.UUID

data class Post(
    val id: String = "",
    val user_id: String = "",
    val image_url: String = "",
    val caption: String = "",
    val likes: Map<String, Boolean> = emptyMap(),
    val isPublic: Boolean = true // Add this field to the Post class
)

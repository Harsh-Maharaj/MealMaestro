package com.example.mealmaestro.Helper

import java.util.UUID

data class Post(
    val id: String = UUID.randomUUID().toString(),
    val user_id: String = "",
    val image_url: String = "",
    val caption: String = "",
    val likes: Map<String, Boolean> = mapOf()
)

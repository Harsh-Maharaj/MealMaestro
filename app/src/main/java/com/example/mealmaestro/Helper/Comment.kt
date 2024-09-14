package com.example.mealmaestro.Helper

data class Comment(
    val userId: String = "",
    val username: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

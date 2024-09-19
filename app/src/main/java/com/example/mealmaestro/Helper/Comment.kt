package com.example.mealmaestro.Helper

// Data class representing a comment on a post
data class Comment(
    val userId: String = "", // The ID of the user who made the comment, defaults to an empty string
    val username: String = "", // The username of the user who made the comment, defaults to an empty string
    val text: String = "", // The actual text content of the comment, defaults to an empty string
    val timestamp: Long = 0L // The time the comment was made, stored as a long (epoch time), defaults to 0
)

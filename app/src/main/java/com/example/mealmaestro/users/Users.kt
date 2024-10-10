package com.example.mealmaestro.users

import com.example.mealmaestro.R

// Data class to represent a user in the app
class Users(
    var name: String? = null, // The user's name (can be null)
    val email: String? = null, // The user's email (can be null)
    val uid: String? = null, // The unique user ID (can be null)
    var friends: ArrayList<String>? = null, // List of the user's friends (represented by their UIDs, can be null)
    var username: String? = null, // The user's username (can be null)
    icon: String? = null // The user's profile icon (can be null, default handling below)
) {
    // Custom setter for the icon field
    var icon = icon
        set(value) {
            // If the value is null, set it to the default "person" drawable
            field = value ?: R.drawable.person.toString() // Default icon if no profile picture is set
        }

    // No-argument constructor, initializes all fields to null or default values
    constructor() : this(null, null, null, null, null, null)
}

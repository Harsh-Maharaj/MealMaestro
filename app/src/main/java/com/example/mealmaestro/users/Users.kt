package com.example.mealmaestro.users

import androidx.browser.trusted.Token
import com.example.mealmaestro.R

class Users(
    val email: String? = null,
    val uid: String? = null,
    var friends: ArrayList<String>? = null,
    val username: String? = null,
    val fcmToken: String? = null,
    icon: String? = null
) {
    var icon = icon
        set(value) {
            field = value ?: R.drawable.person.toString()
        }

    // No-argument constructor
    constructor() : this(null, null, null, null, null, null)
}

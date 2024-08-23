package com.example.mealmaestro.users

import com.example.mealmaestro.R

class Users(
    var username: String? = null,
    var name: String? = null,
    val email: String? = null,
    val uid: String? = null,
    var friends : ArrayList<String>? = null,
    icon: String? = null)
{

    var icon = icon
    set(value) {
        field = value ?: R.drawable.person.toString()
    }

    // No-argument constructor
    constructor() : this(null, null, null, null, null, null)
}

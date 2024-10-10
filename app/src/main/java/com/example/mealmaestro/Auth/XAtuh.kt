package com.example.mealmaestro.Auth

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.mealmaestro.MainActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.OAuthProvider

// XAuth class handles Twitter authentication using Firebase
class XAuth(private val activity: Activity) {

    // FirebaseAuth instance to manage authentication
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Configure the OAuth provider for Twitter login
    private val provider = OAuthProvider.newBuilder("twitter.com")
        .addCustomParameter("lang", "en") // Set custom parameter for Twitter OAuth (e.g., language)

    init {
        // Set the language code for Firebase authentication (optional, here it's set to English)
        auth.setLanguageCode("en") // You can change this to any other language code
    }

    // Function to start the Twitter authentication process
    fun xAuth() {
        // Check if there's an existing pending authentication result
        val pendingResultTask = auth.pendingAuthResult
        if (pendingResultTask != null) {
            // Handle the pending authentication result (e.g., user was halfway through authentication)
            pendingResultTask
                .addOnSuccessListener {
                    // If the pending authentication is successful
                    Log.d("XAuth", "Pending authentication success")
                    onSuccess() // Proceed to the next step (successful authentication)
                }
                .addOnFailureListener { e ->
                    // If the pending authentication fails, log the error
                    Log.e("XAuth", "Pending authentication failed", e)
                    handleAuthFailure(e) // Handle the failure (e.g., show error message)
                }
        } else {
            // If no pending result, start a new authentication process with Twitter
            auth.startActivityForSignInWithProvider(activity, provider.build())
                .addOnSuccessListener {
                    // Authentication with Twitter is successful
                    Log.d("XAuth", "Authentication success")
                    onSuccess() // Proceed to the next step
                }
                .addOnFailureListener { e ->
                    // Authentication with Twitter failed, log the error
                    Log.e("XAuth", "Authentication failed", e)
                    handleAuthFailure(e) // Handle the failure case
                }
        }
    }

    // Function to handle successful authentication
    private fun onSuccess() {
        // Show a success message to the user
        Toast.makeText(activity, "Login Successfully", Toast.LENGTH_SHORT).show()
        // Navigate to MainActivity after successful login
        activity.startActivity(Intent(activity, MainActivity::class.java))
        activity.finish() // Close the current activity
    }

    // Function to handle authentication failures
    private fun handleAuthFailure(e: Exception) {
        // Log the error message to help with debugging
        Log.e("XAuth", "Error: ${e.message}")
        if (e is FirebaseAuthException) {
            // Log specific FirebaseAuth errors (e.g., invalid credentials, network issues)
            Log.e("XAuth", "Firebase Auth Error: ${e.errorCode} - ${e.message}")
        }
        // Show a toast message to the user indicating that the login failed
        Toast.makeText(activity, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

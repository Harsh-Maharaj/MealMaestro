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

class XAuth(private val activity: Activity) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val provider = OAuthProvider.newBuilder("twitter.com")
        .addCustomParameter("lang", "en")

    init {
        // Set the language code for Firebase (optional)
        auth.setLanguageCode("en") // or any other language code
    }

    fun xAuth() {
        val pendingResultTask = auth.pendingAuthResult
        if (pendingResultTask != null) {
            // There is a pending result, handle it
            pendingResultTask
                .addOnSuccessListener {
                    Log.d("XAuth", "Pending authentication success")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e("XAuth", "Pending authentication failed", e)
                    handleAuthFailure(e)
                }
        } else {
            // No pending result, start a new authentication process
            auth.startActivityForSignInWithProvider(activity, provider.build())
                .addOnSuccessListener {
                    Log.d("XAuth", "Authentication success")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e("XAuth", "Authentication failed", e)
                    handleAuthFailure(e)
                }
        }
    }

    private fun onSuccess() {
        Toast.makeText(activity, "Login Successfully", Toast.LENGTH_SHORT).show()
        activity.startActivity(Intent(activity, MainActivity::class.java))
        activity.finish()
    }

    private fun handleAuthFailure(e: Exception) {
        Log.e("XAuth", "Error: ${e.message}")
        if (e is FirebaseAuthException) {
            Log.e("XAuth", "Firebase Auth Error: ${e.errorCode} - ${e.message}")
        }
        Toast.makeText(activity, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

package com.example.mealmaestro

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider

class XAuth(private val activity: Activity) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val provider = OAuthProvider.newBuilder("twitter.com")

    fun xAuth() {
        val pendingResultTask = auth.pendingAuthResult
        if (pendingResultTask != null) {
            pendingResultTask
                .addOnSuccessListener {
                    Toast.makeText(activity, "Login Successfully", Toast.LENGTH_SHORT).show()
                    activity.startActivity(Intent(activity, MainActivity::class.java))
                    activity.finish()
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Login failed, please try again", Toast.LENGTH_SHORT).show()
                }
        } else {
            auth.startActivityForSignInWithProvider(activity, provider.build())
                .addOnSuccessListener {
                    Toast.makeText(activity, "Login Successfully", Toast.LENGTH_SHORT).show()
                    activity.startActivity(Intent(activity, MainActivity::class.java))
                    activity.finish()
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Login failed, please try again", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

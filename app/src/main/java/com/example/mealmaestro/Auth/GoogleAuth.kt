package com.example.mealmaestro.Auth

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.example.mealmaestro.Helper.DataBase
import com.example.mealmaestro.MainActivity
import com.example.mealmaestro.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

// GoogleAuth class handles Google Sign-In functionality for the app
class GoogleAuth(
    private val activity: Activity, // The current activity context, passed in from the calling activity
    private val launcher: ActivityResultLauncher<Intent> // A launcher for starting activities and handling results
) {

    private lateinit var oneTapClient: SignInClient // SignInClient instance for managing Google One-Tap sign-in
    private val auth: FirebaseAuth = FirebaseAuth.getInstance() // FirebaseAuth instance to handle Firebase authentication

    init {
        // Initialize the SignInClient for Google One-Tap sign-in
        oneTapClient = Identity.getSignInClient(activity)
    }

    // Function to launch the sign-in process
    fun launchSignIn() {
        // Sign the user out of both Firebase and Google
        signOut() // Sign-out is called to ensure the user is not already logged in (can be removed for autologin)

        // Build the sign-in request for Google One-Tap sign-in
        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true) // Enable ID token support
                    .setServerClientId(activity.getString(R.string.google_client_id)) // Use the app's Google client ID
                    .setFilterByAuthorizedAccounts(false) // Allow the user to select a different account if necessary
                    .build()
            )
            .setAutoSelectEnabled(false) // Prevent automatic selection of the account, show account picker
            .build()

        // Begin the sign-in process
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                // Successfully got the sign-in result, start the Google sign-in UI
                try {
                    activity.startIntentSenderForResult(
                        result.pendingIntent.intentSender, // Start the pending intent for sign-in
                        REQ_ONE_TAP, // Request code for sign-in result handling
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                } catch (e: IntentSender.SendIntentException) {
                    // Handle any error in starting the One Tap UI
                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener { e ->
                // Log failure in the sign-in process
                Log.e(TAG, "One Tap failed: ${e.localizedMessage}")
            }
    }

    // Handle the result of the Google sign-in process
    fun handleSignInResult(requestCode: Int, data: Intent?) {
        // Check if the result is from the Google One-Tap sign-in
        if (requestCode == REQ_ONE_TAP) {
            try {
                // Retrieve the Google sign-in credential from the intent data
                val googleCredential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = googleCredential.googleIdToken

                // If ID token is not null, authenticate with Firebase using the token
                if (idToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(activity) { task ->
                            if (task.isSuccessful) {
                                // Sign-in success, update the UI with the signed-in user's info
                                Log.d(TAG, "signInWithCredential:success")
                                val user = auth.currentUser
                                updateUI(user) // Proceed to the main activity
                            } else {
                                // Sign-in failed, log the failure and update the UI accordingly
                                Log.w(TAG, "signInWithCredential:failure", task.exception)
                                updateUI(null)
                            }
                        }
                } else {
                    // Log if no ID token was found
                    Log.d(TAG, "No ID token!")
                }
            } catch (e: ApiException) {
                // Handle sign-in cancellation or failure
                if (e.statusCode == 16) {
                    // Case when user cancels the sign-in
                    Log.d(TAG, "Google Sign-In was cancelled by the user.")
                    Toast.makeText(activity, "Sign-in cancelled", Toast.LENGTH_SHORT).show()
                } else {
                    // Log other sign-in failures
                    Log.e(TAG, "Google Sign-In failed", e)
                }
            }
        }
    }

    // Sign out of both Firebase and Google Sign-In
    private fun signOut() {
        // Sign out from Firebase authentication
        auth.signOut()

        // Sign out from Google account
        val googleSignInClient = GoogleSignIn.getClient(activity, GoogleSignInOptions.DEFAULT_SIGN_IN)
        googleSignInClient.signOut().addOnCompleteListener(activity) {
            // Log successful Google sign-out
            Log.d(TAG, "Signed out of Google")
        }
    }

    // Update the UI based on whether the user is signed in or not
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // If user is signed in, proceed to the main activity
            val intent = Intent(activity, MainActivity::class.java)
            activity.startActivity(intent)
            activity.finish() // Close the current activity after navigating to MainActivity
        } else {
            // If sign-in failed, show a sign-in failed message
            Toast.makeText(activity, "Sign-in failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Companion object to hold constants used in the class
    companion object {
        const val REQ_ONE_TAP = 100 // Request code used for handling One-Tap sign-in result
    }
}

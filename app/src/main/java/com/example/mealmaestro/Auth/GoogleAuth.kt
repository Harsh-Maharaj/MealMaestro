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

class GoogleAuth(
    private val activity: Activity,
    private val launcher: ActivityResultLauncher<Intent>
) {

    private lateinit var oneTapClient: SignInClient
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    init {
        oneTapClient = Identity.getSignInClient(activity)
    }

    fun launchSignIn() {
        // Sign the user out of Firebase and Google
        signOut() // remove this for autologin

        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(activity.getString(R.string.google_client_id)) // Replace with your client ID
                    .setFilterByAuthorizedAccounts(false) // Allow the user to choose another account
                    .build()
            )
            .setAutoSelectEnabled(false) // Disable auto-select to force showing the account picker
            .build()

        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                try {
                    activity.startIntentSenderForResult(
                        result.pendingIntent.intentSender,
                        REQ_ONE_TAP,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "One Tap failed: ${e.localizedMessage}")
            }
    }

    fun handleSignInResult(requestCode: Int, data: Intent?) {
        if (requestCode == REQ_ONE_TAP) {
            try {
                val googleCredential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = googleCredential.googleIdToken

                if (idToken != null) {
                    // Got an ID token from Google. Use it to authenticate with Firebase.
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(activity) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithCredential:success")
                                val user = auth.currentUser
                                updateUI(user)
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithCredential:failure", task.exception)
                                updateUI(null)
                            }
                        }
                } else {
                    Log.d(TAG, "No ID token!")
                }
            } catch (e: ApiException) {
                // Handle the "Cancelled by user" case
                if (e.statusCode == 16) {
                    Log.d(TAG, "Google Sign-In was cancelled by the user.")
                    Toast.makeText(activity, "Sign-in cancelled", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG, "Google Sign-In failed", e)
                }
            }
        }
    }

    // Sign out of both Firebase and Google Sign-In
    private fun signOut() {
        // Sign out from Firebase
        auth.signOut()

        // Sign out from Google
        val googleSignInClient = GoogleSignIn.getClient(activity, GoogleSignInOptions.DEFAULT_SIGN_IN)
        googleSignInClient.signOut().addOnCompleteListener(activity) {
            Log.d(TAG, "Signed out of Google")
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // User is signed in, proceed to the next activity
            val intent = Intent(activity, MainActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
        } else {
            // Sign-in failed, show error message
            Toast.makeText(activity, "Sign-in failed", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val REQ_ONE_TAP = 100
    }
}

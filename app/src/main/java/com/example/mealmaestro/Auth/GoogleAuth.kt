package com.example.mealmaestro.Auth

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.example.mealmaestro.Helper.DataBase
import com.example.mealmaestro.MainActivity
import com.example.mealmaestro.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

@Suppress("DEPRECATION")
class GoogleAuth(
    private val activity: Activity,
    private val launcher: ActivityResultLauncher<Intent>
) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
/*
    private lateinit var oneTapClient: SignInClient
    init {
        oneTapClient = Identity.getSignInClient(activity)
    }
*/
    fun launchSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.google_client_id)) // Your Google Client ID
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(activity, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            // Start the sign-in intent after signing out
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)

        }
    }

    fun handleSignInResult(requestCode: Int, data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            // Get the ID token from the account
            val idToken = account?.idToken
            if (idToken != null) {
                // Authenticate with Firebase
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener(activity) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val dataBase = DataBase()
                            if (user != null) {
                                dataBase.saveUsernameToDatabaseFromGoogle(user)
                            }
                            updateUI(user)
                        } else {
                            // Sign-in failed
                            Log.w(TAG, "signInWithCredential:failure", task.exception)
                            updateUI(null)
                        }
                    }
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google sign-in failed", e)
            updateUI(null)
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

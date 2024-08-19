package com.example.mealmaestro

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class GoogleAuth(private val activity: Activity, private val launcher: ActivityResultLauncher<Intent>) {

    private val gso: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.google_client_id))
            .requestEmail()
            .build()
    }

    private val gsc: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(activity, gso)
    }

    // Launch Google Sign-In
    fun launchSignIn() {
        val signInIntent = gsc.signInIntent
        launcher.launch(signInIntent)
    }

    // Handle Sign-In result
    fun handleSignInResult(data: Intent?, auth: FirebaseAuth) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                signInWithGoogle(account, auth)
            }
        } catch (e: ApiException) {
            Toast.makeText(activity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithGoogle(account: GoogleSignInAccount, auth: FirebaseAuth) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(activity, "Successfully Logged In", Toast.LENGTH_SHORT).show()
                    activity.startActivity(Intent(activity, MainActivity::class.java))
                    activity.finish()
                } else {
                    Toast.makeText(activity, "Login failed, please try again", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(activity, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

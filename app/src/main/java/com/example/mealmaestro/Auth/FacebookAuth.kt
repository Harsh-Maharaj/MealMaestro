package com.example.mealmaestro.Auth

import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mealmaestro.MainActivity
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.CallbackManager
import com.google.firebase.auth.FirebaseAuth

class FacebookAuth(private val activity: AppCompatActivity) {

    private val callbackManager: CallbackManager = CallbackManager.Factory.create()
    private val auth = FirebaseAuth.getInstance()

    init {
        // Register callback to handle login responses
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    // Handle success
                    Toast.makeText(activity, "Login successfully", Toast.LENGTH_SHORT).show()
                    activity.startActivity(Intent(activity, MainActivity::class.java))
                }

                override fun onCancel() {
                    Toast.makeText(activity, "Login cancelled", Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(activity, "Login Failed, please try again", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    fun logInWithFacebook() {
        LoginManager.getInstance()
            .logInWithReadPermissions(activity, mutableListOf("public_profile"))
    }
    // Handle the result of the Facebook login
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

}

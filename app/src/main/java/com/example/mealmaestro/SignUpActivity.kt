package com.example.mealmaestro

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mealmaestro.databinding.ActivitySignUpBinding
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FirebaseAuth


class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleAuth: GoogleAuth
    private lateinit var callbackManager: CallbackManager
    private lateinit var xAuth: XAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sign_up)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        binding.signUpLogin.setOnClickListener {
            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
            startActivity(intent)
        }
        // ========================= GOOGLE ========================================================
        val getResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            googleAuth.handleSignInResult(result.data, auth)
        }

        googleAuth = GoogleAuth(this, getResult)

        binding.googleBtn.setOnClickListener {
            googleAuth.launchSignIn()
        }

        // ======================= FACEBOOK ========================================================

        callbackManager = CallbackManager.Factory.create()

        // register callback to handle login responses
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    //        handleFacebookAccessToken(result.accessToken)
                    Toast.makeText(this@SignUpActivity, "Login successfully", Toast.LENGTH_SHORT)
                        .show()
                    startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
                }

                override fun onCancel() {
                    Toast.makeText(this@SignUpActivity, "Login cancelled", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Login Failed, please try again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)

            // Pass the activity result back to the Facebook SDK
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }


        binding.facebookBtn.setOnClickListener {
            LoginManager.getInstance()
                .logInWithReadPermissions(this@SignUpActivity, mutableListOf("public_profile"))
        }
        // =============================  X ========================================================

        xAuth = XAuth(this@SignUpActivity)

        binding.xBtn.setOnClickListener {
            xAuth.xAuth()
        }
        // ======================= EMAIL & PASSWORD ================================================
        binding.signUpContinueBtn.setOnClickListener {
            val email = binding.signUpEmail.text.toString()
            val password = binding.signUpPassword.text.toString()
            val confirmPass = binding.signUpConfirmPassword.text.toString()

            if (email.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this@SignUpActivity, "Please fill all details", Toast.LENGTH_SHORT)
                    .show()
            } else if (password != confirmPass) {
                Toast.makeText(
                    this@SignUpActivity,
                    "Password's must be the same",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this@SignUpActivity) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this@SignUpActivity,
                                "Registration successful",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
                        } else {
                            Toast.makeText(
                                this@SignUpActivity,
                                "Registration fail, please try again",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }

    /*
        // Handle the result of the Facebook login
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }
    */
}

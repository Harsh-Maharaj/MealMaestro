package com.example.mealmaestro

import com.example.mealmaestro.Auth.FacebookAuth
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mealmaestro.Auth.GoogleAuth
import com.example.mealmaestro.Auth.XAuth
import com.example.mealmaestro.databinding.ActivitySignUpBinding
import com.facebook.CallbackManager
import com.google.firebase.auth.FirebaseAuth


class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleAuth: GoogleAuth
    private lateinit var callbackManager: CallbackManager
    private lateinit var facebookAuth: FacebookAuth
    private lateinit var xAuth: XAuth
    private val dataBase: DataBase = DataBase()

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
        supportActionBar?.hide()
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
        facebookAuth = FacebookAuth(this@SignUpActivity)

        binding.facebookBtn.setOnClickListener {
            facebookAuth.logInWithFacebook()
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
                            // add user to database
                            dataBase.addUserToDataBase(email, auth.currentUser!!.uid)
                            startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
                            finish()
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
    //===================== FACEBOOK ===============================================================

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        facebookAuth.onActivityResult(requestCode, resultCode, data)
    }
}

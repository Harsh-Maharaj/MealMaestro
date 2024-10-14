package com.example.mealmaestro

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mealmaestro.Auth.GoogleAuth
import com.example.mealmaestro.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    private lateinit var googleAuth: GoogleAuth
    lateinit var auth: FirebaseAuth

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            googleAuth.handleSignInResult(GoogleAuth.REQ_ONE_TAP, result.data)
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        googleAuth.handleSignInResult(requestCode, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeFromPreferences()
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()

        binding.loginBtn.setOnClickListener {
            UserLogin()
        }
        binding.loginSignupBtn.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
        }

        binding.googleBtn.setOnClickListener {
            googleAuth.launchSignIn()
        }

        googleAuth = GoogleAuth(this, launcher)
    }

    fun UserLogin() {
        val userName = binding.loginUsername.text.toString()
        val password = binding.loginPassword.text.toString()

        if (userName.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                this@LoginActivity,
                "Please fill Username and Password",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            auth.signInWithEmailAndPassword(userName, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@LoginActivity, "Login Successfully", Toast.LENGTH_SHORT)
                            .show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Login failed, please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun applyThemeFromPreferences() {
        val sharedPreferences: SharedPreferences =
            android.preference.PreferenceManager.getDefaultSharedPreferences(this)
        val selectedTheme = sharedPreferences.getInt("SelectedTheme", 0)
        when (selectedTheme) {
            1 -> setTheme(R.style.DynamicTheme1)
            2 -> setTheme(R.style.DynamicTheme2)
            3 -> setTheme(R.style.DynamicTheme3)
            4 -> setTheme(R.style.DynamicTheme4)
        }
    }
}

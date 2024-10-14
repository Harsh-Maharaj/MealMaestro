package com.example.mealmaestro

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mealmaestro.Auth.GoogleAuth
import com.example.mealmaestro.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    // View binding for accessing views in the layout
    lateinit var binding: ActivityLoginBinding

    // Google authentication helper class
    private lateinit var googleAuth: GoogleAuth

    // Firebase authentication instance
    lateinit var auth: FirebaseAuth

    // Register an activity result launcher to handle Google Sign-In result
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            googleAuth.handleSignInResult(GoogleAuth.REQ_ONE_TAP, result.data)
        }

    // Handle activity result for Google sign-in (if not using the launcher)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        googleAuth.handleSignInResult(requestCode, data)
    }

    // onCreate method - this is called when the activity is first created
    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeFromPreferences()  // Apply the selected theme before setting the layout
        super.onCreate(savedInstanceState)

        // Set up view binding to access the UI components
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable edge-to-edge layout for immersive UI
        enableEdgeToEdge()

        // Handle window insets (system bars) for proper layout adjustments
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Hide the action bar for this activity
        supportActionBar?.hide()

        // Initialize Firebase authentication
        auth = FirebaseAuth.getInstance()

        // Set up button click listeners for login and signup
        binding.loginBtn.setOnClickListener {
            UserLogin()  // Trigger login when the login button is clicked
        }
        binding.loginSignupBtn.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(intent)
        }

        // Set up Google sign-in button click listener
        binding.googleBtn.setOnClickListener {
            googleAuth.launchSignIn()
        }

        // Initialize GoogleAuth with the activity and launcher
        googleAuth = GoogleAuth(this, launcher)
    }

    // Function to handle user login via email and password
    fun UserLogin() {
        val userName = binding.loginUsername.text.toString()
        val password = binding.loginPassword.text.toString()

        if (userName.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                this@LoginActivity,
                "please fill UserName and Password",
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

    // Apply the selected theme from SharedPreferences
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

package com.example.mealmaestro

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mealmaestro.Auth.GoogleAuth
import com.example.mealmaestro.Auth.XAuth
import com.example.mealmaestro.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    // View binding for accessing views in the layout
    private lateinit var binding: ActivityLoginBinding
    // Google authentication helper class
    private lateinit var googleAuth: GoogleAuth
    // Firebase authentication instance
    private lateinit var auth: FirebaseAuth
    // Additional authentication helper (if needed)
    private lateinit var xAuth: XAuth

    // ========================== GOOGLE AUTH HANDLING ==============================================
    // Register an activity result launcher to handle Google Sign-In result
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            googleAuth.handleSignInResult(GoogleAuth.REQ_ONE_TAP, result.data)  // Handle sign-in result
        }

    // Handle activity result for Google sign-in (if not using the launcher)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Forward the result to the GoogleAuth class to process it
        googleAuth.handleSignInResult(requestCode, data)
    }
    // ==============================================================================================

    // onCreate method - this is called when the activity is first created
    override fun onCreate(savedInstanceState: Bundle?) {
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
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)  // Start signup activity
            startActivity(intent)
        }

        // ========================= GOOGLE SIGN-IN ================================================
        // Set up Google sign-in button click listener
        binding.googleBtn.setOnClickListener {
            googleAuth.launchSignIn()  // Trigger Google sign-in
        }
        // Initialize GoogleAuth with the activity and launcher
        googleAuth = GoogleAuth(this, launcher)
    }

    // ========================= EMAIL & PASSWORD LOGIN ============================================
    // Function to handle user login via email and password
    private fun UserLogin() {
        // Retrieve the username and password from the input fields
        val userName = binding.loginUsername.text.toString()
        val password = binding.loginPassword.text.toString()

        // Check if either the username or password is empty
        if (userName.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                this@LoginActivity,
                "please fill UserName and Password",  // Show a toast message if fields are empty
                Toast.LENGTH_SHORT
            ).show()
        } else {
            // Use Firebase Authentication to sign in with email and password
            auth.signInWithEmailAndPassword(userName, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Show success message and navigate to the main activity if login is successful
                        Toast.makeText(this@LoginActivity, "Login Successfully", Toast.LENGTH_SHORT)
                            .show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    } else {
                        // Show failure message if login fails
                        Toast.makeText(
                            this@LoginActivity,
                            "Login failed, please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}
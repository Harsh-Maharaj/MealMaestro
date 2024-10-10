package com.example.mealmaestro

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
import com.example.mealmaestro.Helper.DataBase
import com.example.mealmaestro.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth


class SignUpActivity : AppCompatActivity() {

    // View binding to access views in the layout
    private lateinit var binding: ActivitySignUpBinding
    // Firebase authentication instance for email/password sign-up
    private lateinit var auth: FirebaseAuth
    // Google authentication helper class
    private lateinit var googleAuth: GoogleAuth
    // Additional authentication helper (if needed)
    private lateinit var xAuth: XAuth
    // Database instance for adding user data to the database
    private val dataBase: DataBase = DataBase()

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

        // Enable edge-to-edge layout for a more immersive experience
        enableEdgeToEdge()

        // Set up view binding to access the UI components
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle window insets (system bars) for proper layout adjustments
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sign_up)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Hide the action bar for this activity
        supportActionBar?.hide()

        // Initialize Firebase authentication for email/password sign-up
        auth = FirebaseAuth.getInstance()

        // Set up the login button click listener
        binding.signUpLogin.setOnClickListener {
            // Start the LoginActivity when the login button is clicked
            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        // ========================= GOOGLE SIGN-IN ================================================
        // Set up Google sign-in button click listener
        binding.googleBtn.setOnClickListener {
            googleAuth.launchSignIn()  // Trigger Google sign-in
        }
        // Initialize GoogleAuth with the activity and launcher
        googleAuth = GoogleAuth(this, launcher)

        // ======================= EMAIL & PASSWORD SIGN-UP ========================================
        // Set up the sign-up continue button click listener for email/password registration
        binding.signUpContinueBtn.setOnClickListener {
            // Retrieve user input (email, password, confirm password, username) from the input fields
            val email = binding.signUpEmail.text.toString()
            val password = binding.signUpPassword.text.toString()
            val confirmPass = binding.signUpConfirmPassword.text.toString()
            val username = binding.signUpUsername.text.toString()

            // Check if any field is empty and show a toast message if so
            if (email.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this@SignUpActivity, "Please fill all details", Toast.LENGTH_SHORT)
                    .show()
            }
            // Check if the password and confirmation password match
            else if (password != confirmPass) {
                Toast.makeText(
                    this@SignUpActivity,
                    "Passwords must be the same",
                    Toast.LENGTH_SHORT
                ).show()
            }
            // If validation passes, attempt to create the user account with Firebase Authentication
            else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this@SignUpActivity) { task ->
                        if (task.isSuccessful) {
                            // If sign-up is successful, show a success message
                            Toast.makeText(
                                this@SignUpActivity,
                                "Registration successful",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Add the user to the database with their email, user ID, and username
                            dataBase.addUserToDataBase(email, auth.currentUser!!.uid, username)
                            // Navigate to the MainActivity and finish the sign-up activity
                            startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
                            finish()
                        } else {
                            // If sign-up fails, show a failure message
                            Toast.makeText(
                                this@SignUpActivity,
                                "Registration failed, please try again",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }
}
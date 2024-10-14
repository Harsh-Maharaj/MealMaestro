package com.example.mealmaestro

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mealmaestro.Auth.GoogleAuth
import com.example.mealmaestro.Helper.DataBase
import com.example.mealmaestro.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    // View binding to access views in the layout
    lateinit var binding: ActivitySignUpBinding

    // Firebase authentication instance for email/password sign-up
    lateinit var auth: FirebaseAuth

    // Google authentication helper class
    private lateinit var googleAuth: GoogleAuth

    // Database instance for adding user data to the database
    var dataBase: DataBase = DataBase()

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
        applyThemeFromPreferences()  // Apply the selected theme
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
            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        // Set up Google sign-in button click listener
        binding.googleBtn.setOnClickListener {
            googleAuth.launchSignIn()
        }

        // Initialize GoogleAuth with the activity and launcher
        googleAuth = GoogleAuth(this, launcher)

        // Set up the sign-up continue button click listener for email/password registration
        binding.signUpContinueBtn.setOnClickListener {
            val email = binding.signUpEmail.text.toString()
            val password = binding.signUpPassword.text.toString()
            val confirmPass = binding.signUpConfirmPassword.text.toString()
            val username = binding.signUpUsername.text.toString()

            if (email.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this@SignUpActivity, "Please fill all details", Toast.LENGTH_SHORT)
                    .show()
            } else if (password != confirmPass) {
                Toast.makeText(this@SignUpActivity, "Passwords must be the same", Toast.LENGTH_SHORT)
                    .show()
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this@SignUpActivity) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@SignUpActivity, "Registration successful", Toast.LENGTH_SHORT).show()
                            dataBase.addUserToDataBase(email, auth.currentUser!!.uid, username)
                            startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@SignUpActivity, "Registration failed, please try again", Toast.LENGTH_SHORT)
                                .show()
                        }
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

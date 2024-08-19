package com.example.mealmaestro

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputBinding
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mealmaestro.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleAuth: GoogleAuth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        binding.loginBtn.setOnClickListener {
            UserLogin()
        }
        binding.loginSignupBtn.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(intent)
        }

        // ============================= GOOGLE ====================================================
        val getResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            googleAuth.handleSignInResult(result.data, auth)
        }

        googleAuth = GoogleAuth(this, getResult)
        binding.googleBtn.setOnClickListener {
            googleAuth.launchSignIn()
        }

    }

    // ========================= email and Password ================================================
        private fun UserLogin(){
            val userName = binding.loginUsername.text.toString()
            val password = binding.loginPassword.text.toString()

            if(userName.isEmpty()||password.isEmpty()){
                Toast.makeText(this@LoginActivity,"please fill UserName and Password",Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(userName, password)
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            Toast.makeText(this@LoginActivity, "Login Successfully",Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        }else{
                            Toast.makeText(this@LoginActivity, "Login failed, please try again", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
}
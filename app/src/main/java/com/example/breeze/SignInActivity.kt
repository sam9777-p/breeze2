package com.example.breeze

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.breeze.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        InternetChecker().checkInternet(this, lifecycle)
        firebaseAuth = FirebaseAuth.getInstance()

        binding.textView.setOnClickListener {
            Log.d("SignInActivity", "Navigating to SignUpActivity")
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.forgotPasswordText.setOnClickListener {
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }


        binding.sgninbutton.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val pass = binding.passET.text.toString().trim()

            // Validate fields
            if (!validateInput(email, pass)) return@setOnClickListener

            // Sign in with Firebase
            signInWithFirebase(email, pass)
        }
    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun validateInput(email: String, password: String): Boolean {

        if (email.isEmpty()) {
            binding.emailEt.error = "Email is required"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEt.error = "Enter a valid email address"
            return false
        }


        if (password.isEmpty()) {
            binding.passET.error = "Password is required"
            return false
        }
        if (password.length < 6) {
            binding.passET.error = "Password must be at least 6 characters"
            return false
        }

        return true
    }

    private fun signInWithFirebase(email: String, password: String) {
        binding.sgninbutton.isEnabled = false

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.sgninbutton.isEnabled = true

                if (task.isSuccessful) {
                    Log.d("SignInActivity", "Sign-in successful")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    val errorMessage = when (val exception = task.exception) {
                        is FirebaseAuthException -> {
                            handleFirebaseAuthException(exception)
                        }
                        else -> {
                            Log.e("SignInActivity", "Unknown error: ${exception?.message}")
                            "An unexpected error occurred. Please try again."
                        }
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }


    private fun handleFirebaseAuthException(exception: FirebaseAuthException): String {
        Log.e("SignInActivity", "FirebaseAuthException: ${exception.message}")

        return when (exception.errorCode) {
            "ERROR_INVALID_EMAIL" -> "The email address is badly formatted."
            "ERROR_WRONG_PASSWORD" -> "The password is incorrect."
            "ERROR_USER_DISABLED" -> "This user account has been disabled."
            "ERROR_USER_NOT_FOUND" -> "No account found with this email."
            "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your internet connection."
            else -> "Authentication failed. Please try again."
        }
    }
}

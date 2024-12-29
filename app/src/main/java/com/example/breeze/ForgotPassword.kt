package com.example.breeze

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var resetBtn: Button
    private lateinit var errorMsg: TextView
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        email = findViewById(R.id.emailEt)
        resetBtn = findViewById(R.id.btnResetPassword)
        errorMsg = findViewById(R.id.errorMsg)
        firebaseAuth = FirebaseAuth.getInstance()

        resetBtn.setOnClickListener {
            val email = email.text.toString().trim()

            if (email.isNotEmpty()) {
                sendPasswordResetEmail(email)
            } else {
                errorMsg.text = "Please enter a valid email address"
                errorMsg.visibility = TextView.VISIBLE
            }
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    errorMsg.text = task.exception?.message ?: "Failed to send email"
                    errorMsg.visibility = TextView.VISIBLE
                }
            }
    }
}

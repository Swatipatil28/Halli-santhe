package com.hallisanthe.hallisanthe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.hallisanthe.hallisanthe.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val mobile = binding.etMobile.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (mobile.length != 10 || password.isEmpty()) {
                Toast.makeText(this, "Enter valid mobile and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email = "$mobile@hallisanthe.com"
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener { error ->
                    Log.e(TAG, "Login failed for mobile=$mobile", error)
                    val message = when (error) {
                        is FirebaseAuthInvalidCredentialsException -> "Invalid mobile or password."
                        is FirebaseNetworkException -> "Network error. Check your internet connection and try again."
                        is FirebaseAuthException -> {
                            when (error.errorCode) {
                                "ERROR_INTERNAL_ERROR" ->
                                    "Firebase Authentication is not configured correctly for this app."
                                "ERROR_USER_NOT_FOUND" -> "No account found for this mobile number."
                                else -> "Login failed: ${error.errorCode}"
                            }
                        }
                        else -> "Login failed: ${error.localizedMessage ?: "unknown error"}"
                    }
                    val firebaseCode = (error as? FirebaseAuthException)?.errorCode ?: "N/A"
                    val details = buildString {
                        appendLine(message)
                        appendLine()
                        appendLine("Exception: ${error::class.java.simpleName}")
                        appendLine("Firebase code: $firebaseCode")
                        appendLine("Raw message: ${error.localizedMessage ?: "unknown error"}")
                    }
                    AlertDialog.Builder(this)
                        .setTitle("Login failed")
                        .setMessage(details)
                        .setPositiveButton("OK", null)
                        .show()
                }
        }

        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}

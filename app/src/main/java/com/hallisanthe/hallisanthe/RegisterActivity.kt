package com.hallisanthe.hallisanthe

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.FirebaseNetworkException
import com.hallisanthe.hallisanthe.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGeneratePassword.setOnClickListener {
            val password = generateRandomPassword()
            binding.etPassword.setText(password)
            copyToClipboard(password)
            Snackbar.make(binding.root, "Password copied to clipboard", Snackbar.LENGTH_SHORT).show()
        }

        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun generateRandomPassword(): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..8).map { chars.random() }.joinToString("")
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("password", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun registerUser() {
        val name = binding.etName.text.toString().trim()
        val mobile = binding.etMobile.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val role = if (binding.rbBuyer.isChecked) "Buyer" else "Artisan"

        if (name.isEmpty() || !mobile.matches(Regex("\\d{10}")) || password.length < 6) {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        val email = "$mobile@hallisanthe.com"

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = hashMapOf(
                    "uid" to result.user!!.uid,
                    "name" to name,
                    "mobile" to mobile,
                    "role" to role,
                    "village" to "",
                    "profileImageUrl" to "",
                    "createdAt" to com.google.firebase.Timestamp.now()
                )

                db.collection("users").document(result.user!!.uid)
                    .set(user)
                    .addOnSuccessListener {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to save user profile", e)
                        Toast.makeText(
                            this,
                            "Account created, but profile save failed: ${e.localizedMessage ?: "unknown error"}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Registration failed for mobile=$mobile", e)
                showErrorDetails("Registration failed", e, getRegistrationErrorMessage(e))
            }
    }

    private fun getRegistrationErrorMessage(error: Exception): String {
        return when (error) {
            is FirebaseAuthWeakPasswordException -> "Password must be at least 6 characters."
            is FirebaseAuthUserCollisionException -> "This mobile number is already registered. Please log in."
            is FirebaseAuthInvalidCredentialsException -> "Registration failed due to invalid credentials."
            is FirebaseNetworkException -> "Network error. Check your internet connection and try again."
            is FirebaseAuthException -> {
                when (error.errorCode) {
                    "ERROR_INTERNAL_ERROR" ->
                        "Firebase Authentication is not configured correctly for this app. Enable Email/Password sign-in in Firebase Console and verify google-services.json matches package com.hallisanthe.hallisanthe."
                    "ERROR_OPERATION_NOT_ALLOWED" ->
                        "Email/Password sign-in is disabled in Firebase Console. Enable it in Authentication > Sign-in method."
                    else -> "Registration failed: ${error.errorCode}"
                }
            }
            else -> "Registration failed: ${error.localizedMessage ?: "unknown error"}"
        }
    }

    private fun showErrorDetails(title: String, error: Exception, friendlyMessage: String) {
        val firebaseCode = (error as? FirebaseAuthException)?.errorCode ?: "N/A"
        val details = buildString {
            appendLine(friendlyMessage)
            appendLine()
            appendLine("Exception: ${error::class.java.simpleName}")
            appendLine("Firebase code: $firebaseCode")
            appendLine("Raw message: ${error.localizedMessage ?: "unknown error"}")
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }
}

package com.hallisanthe.hallisanthe

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.hallisanthe.hallisanthe.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private var imageUri: Uri? = null
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etEditName.setText(UserSession.name)
        binding.etEditVillage.setText(UserSession.village)
        if (UserSession.profileImageUrl.isNotEmpty()) {
            Glide.with(this).load(UserSession.profileImageUrl).circleCrop().into(binding.ivEditProfilePic)
        }

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                imageUri = uri
                binding.ivEditProfilePic.setImageURI(uri)
            }
        }

        binding.cardEditPhoto.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }
    }

    private fun saveProfile() {
        val newName = binding.etEditName.text.toString().trim()
        val newVillage = binding.etEditVillage.text.toString().trim()

        if (newName.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            val ref = storage.child("profile_images/${UserSession.uid}")
            ref.putFile(imageUri!!).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->
                    updateFirestore(newName, newVillage, url.toString())
                }
            }
        } else {
            updateFirestore(newName, newVillage, UserSession.profileImageUrl)
        }
    }

    private fun updateFirestore(name: String, village: String, imageUrl: String) {
        val updates = mapOf(
            "name" to name,
            "village" to village,
            "profileImageUrl" to imageUrl
        )

        db.collection("users").document(UserSession.uid)
            .update(updates)
            .addOnSuccessListener {
                UserSession.name = name
                UserSession.village = village
                UserSession.profileImageUrl = imageUrl
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

package com.hallisanthe.hallisanthe

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class UploadActivity : AppCompatActivity() {

    private var imageUri: Uri? = null
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        // Find views by ID
        val btnPick = findViewById<Button>(R.id.btnSelectImg)
        val btnPost = findViewById<Button>(R.id.btnUpload)
        val etName = findViewById<EditText>(R.id.etName)
        val etPrice = findViewById<EditText>(R.id.etPrice)

        // Photo Picker Logic
        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                imageUri = uri
                Toast.makeText(this, "Photo selected!", Toast.LENGTH_SHORT).show()
            }
        }

        btnPick.setOnClickListener {
            pickImage.launch("image/*")
        }

        btnPost.setOnClickListener {
            val name = etName.text.toString().trim()
            val price = etPrice.text.toString().trim()

            if (name.isNotEmpty() && price.isNotEmpty() && imageUri != null) {
                uploadToFirebase(name, price)
            } else {
                Toast.makeText(this, "Please provide Name, Price, and Photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadToFirebase(name: String, price: String) {
        val fileName = UUID.randomUUID().toString()
        val imageRef = storage.child("product_images/$fileName")

        // 1. Upload to Storage
        imageUri?.let { uri ->
            imageRef.putFile(uri).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->

                    // 2. Save to Firestore
                    val productMap = hashMapOf(
                        "name" to name,
                        "price" to price,
                        "imageUrl" to downloadUrl.toString()
                    )

                    db.collection("products")
                        .add(productMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Product Posted Successfully!", Toast.LENGTH_LONG).show()
                            finish() // Closes this screen and goes back
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Firestore Error: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Upload Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
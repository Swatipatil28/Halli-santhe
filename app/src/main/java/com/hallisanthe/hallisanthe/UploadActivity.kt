package com.hallisanthe.hallisanthe

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.hallisanthe.hallisanthe.databinding.ActivityUploadBinding
import java.io.ByteArrayOutputStream
import java.util.UUID

class UploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding
    private var imageUri: Uri? = null
    private var imageBase64: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (UserSession.role != "Artisan") {
            Toast.makeText(this, "Only Artisans can list products", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Auto-fill and disable
        binding.etArtisan.setText(UserSession.name)
        binding.etArtisan.isEnabled = false
        
        if (UserSession.village.isNotEmpty()) {
            binding.etVillage.setText(UserSession.village)
        }

        // Set up category dropdown
        val categories = listOf(
            "Pottery", "Textiles", "Woodwork", "Organic",
            "Metalwork", "Jewelry", "Paintings", "Basketry"
        )
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.spinnerCategory.setAdapter(categoryAdapter)
        binding.spinnerCategory.setOnClickListener {
            binding.spinnerCategory.showDropDown()
        }
        // Also show dropdown on focus
        binding.spinnerCategory.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.spinnerCategory.showDropDown()
        }

        // Image picker launcher
        binding.cardPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        // Upload button:
        binding.btnUpload.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val artisan = binding.etArtisan.text.toString().trim()
            val village = binding.etVillage.text.toString().trim()
            val priceStr = binding.etPrice.text.toString().trim().replace("₹", "").trim()
            val category = binding.spinnerCategory.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()

            if (name.isEmpty()) { Toast.makeText(this, "Enter product name", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (artisan.isEmpty()) { Toast.makeText(this, "Enter artisan name", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (village.isEmpty()) { Toast.makeText(this, "Enter village name", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (priceStr.isEmpty()) { Toast.makeText(this, "Enter price", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (category.isEmpty()) { Toast.makeText(this, "Select a category", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (imageBase64 == null) { Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show(); return@setOnClickListener }

            val price = priceStr.toLongOrNull() ?: 0L

            binding.progressIndicator.visibility = View.VISIBLE
            binding.btnUpload.isEnabled = false

            val product = hashMapOf(
                "name" to name,
                "artisanName" to artisan,
                "villageName" to village,
                "price" to price,
                "category" to category,
                "description" to description,
                "imageBase64" to imageBase64,   // stored as Base64, no Storage needed
                "imageUrl" to "",               // empty since no Storage
                "wishlistedBy" to emptyList<String>(),
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            FirebaseFirestore.getInstance().collection("products")
                .add(product)
                .addOnSuccessListener {
                    binding.progressIndicator.visibility = View.GONE
                    binding.btnUpload.isEnabled = true
                    AlertDialog.Builder(this)
                        .setTitle("✅ Listed!")
                        .setMessage("Your product \"$name\" is now live in the marketplace.")
                        .setPositiveButton("OK") { _, _ -> finish() }
                        .setCancelable(false)
                        .show()
                }
                .addOnFailureListener { e ->
                    binding.progressIndicator.visibility = View.GONE
                    binding.btnUpload.isEnabled = true
                    Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            imageUri = result.data?.data
            binding.imgPreview.setImageURI(imageUri)
            binding.layoutPlaceholder.visibility = View.GONE
            binding.imgPreview.visibility = View.VISIBLE

            // Convert to Base64 immediately
            try {
                val inputStream = contentResolver.openInputStream(imageUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                // Compress to reduce size (max 200x200, quality 60)
                val resized = Bitmap.createScaledBitmap(bitmap, 300, 300, true)
                val baos = ByteArrayOutputStream()
                resized.compress(Bitmap.CompressFormat.JPEG, 60, baos)
                val byteArray = baos.toByteArray()
                imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
            } catch (e: Exception) {
                Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
                imageUri = null
                imageBase64 = null
            }
        }
    }
}

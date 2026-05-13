package com.hallisanthe.hallisanthe

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.hallisanthe.hallisanthe.databinding.ActivityProductDetailBinding

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var product: Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        product = intent.getParcelableExtra("product") ?: run {
            finish()
            return
        }

        setupUI()
    }

    private fun setupUI() {
        binding.tvDetailName.text = product.name
        binding.tvDetailPrice.text = "₹${product.price}"
        binding.chipDetailCategory.text = product.category
        binding.tvDetailArtisan.text = "By ${product.artisanName} · ${product.villageName}"
        binding.tvDetailDescription.text = product.description

        // Load image — try Base64 first, fall back to URL
        if (product.imageBase64.isNotEmpty()) {
            try {
                val bytes = Base64.decode(product.imageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                binding.ivProductLarge.setImageBitmap(bitmap)
            } catch (e: Exception) {
                binding.ivProductLarge.setImageResource(R.drawable.gray_placeholder)
            }
        } else if (product.imageUrl.isNotEmpty()) {
            Glide.with(this).load(product.imageUrl).into(binding.ivProductLarge)
        } else {
            binding.ivProductLarge.setImageResource(R.drawable.gray_placeholder)
        }

        updateWishlistIcon()

        binding.fabWishlist.setOnClickListener {
            toggleWishlist()
        }

        binding.btnContactArtisan.setOnClickListener {
            showContactDialog()
        }
    }

    private fun updateWishlistIcon() {
        val isWishlisted = product.wishlistedBy.contains(UserSession.uid)
        binding.fabWishlist.setImageResource(
            if (isWishlisted) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )
    }

    private fun toggleWishlist() {
        if (UserSession.uid.isEmpty()) {
            Toast.makeText(this, "Please login to wishlist products", Toast.LENGTH_SHORT).show()
            return
        }

        val isWishlisted = product.wishlistedBy.contains(UserSession.uid)
        val updateTask = if (isWishlisted) {
            db.collection("products").document(product.id)
                .update("wishlistedBy", FieldValue.arrayRemove(UserSession.uid))
        } else {
            db.collection("products").document(product.id)
                .update("wishlistedBy", FieldValue.arrayUnion(UserSession.uid))
        }

        updateTask.addOnSuccessListener {
            val newList = product.wishlistedBy.toMutableList()
            if (isWishlisted) newList.remove(UserSession.uid) else newList.add(UserSession.uid)
            product = product.copy(wishlistedBy = newList)
            updateWishlistIcon()
        }
    }

    private fun showContactDialog() {
        // Look up artisan's mobile from Firestore users collection by artisan name
        FirebaseFirestore.getInstance()
            .collection("users")
            .whereEqualTo("name", product.artisanName)
            .limit(1)
            .get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty) {
                    val mobile = docs.documents[0].getString("mobile") ?: ""
                    if (mobile.isNotEmpty()) {
                        val message = "Hello ${product.artisanName}, is the product '${product.name}' available?"
                        AlertDialog.Builder(this)
                            .setTitle("Contact ${product.artisanName}")
                            .setMessage("Mobile: +91 $mobile\n\nVillage: ${product.villageName}")
                            .setPositiveButton("Send Message") { _, _ ->
                                val intent = Intent(Intent.ACTION_SENDTO)
                                intent.data = Uri.parse("smsto:+91$mobile")
                                intent.putExtra(Intent.EXTRA_TEXT, message)
                                startActivity(intent)
                            }
                            .setNegativeButton("Close", null)
                            .show()
                    } else {
                        Toast.makeText(this, "Artisan contact not available", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Artisan not registered — show village info
                    AlertDialog.Builder(this)
                        .setTitle("Contact ${product.artisanName}")
                        .setMessage("Village: ${product.villageName}\n\nThis artisan has not registered their contact details yet.")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Could not fetch contact info", Toast.LENGTH_SHORT).show()
            }
    }
}

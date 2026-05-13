package com.hallisanthe.hallisanthe

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ProductAdapter(private var list: List<Product>) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.txtProductName)
        val artisan: TextView = v.findViewById(R.id.txtArtisanName)
        val village: TextView = v.findViewById(R.id.txtVillageName)
        val priceTag: TextView = v.findViewById(R.id.tvPriceTag)
        val img: ImageView = v.findViewById(R.id.imgProduct)
        val btnWishlist: ImageButton = v.findViewById(R.id.btnWishlist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = list[position]
        val context = holder.itemView.context
        
        holder.name.text = p.name
        holder.artisan.text = "By ${p.artisanName}"
        holder.village.text = p.villageName
        holder.priceTag.text = "₹${p.price}"

        val color = when {
            p.price <= 299 -> ContextCompat.getColor(context, R.color.budget_green)
            p.price <= 999 -> ContextCompat.getColor(context, R.color.medium_orange)
            else -> ContextCompat.getColor(context, R.color.premium_purple)
        }
        holder.priceTag.backgroundTintList = ColorStateList.valueOf(color)

        // Load image — try Base64 first, fall back to URL
        if (p.imageBase64.isNotEmpty()) {
            try {
                val bytes = Base64.decode(p.imageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                holder.img.setImageBitmap(bitmap)
            } catch (e: Exception) {
                holder.img.setImageResource(R.drawable.gray_placeholder)
            }
        } else if (p.imageUrl.isNotEmpty()) {
            Glide.with(context)
                .load(p.imageUrl)
                .placeholder(R.drawable.gray_placeholder)
                .centerCrop()
                .into(holder.img)
        } else {
            holder.img.setImageResource(R.drawable.gray_placeholder)
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val db = FirebaseFirestore.getInstance()

        // Set initial heart state
        val isWishlisted = p.wishlistedBy.contains(uid)
        holder.btnWishlist.setImageResource(
            if (isWishlisted) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )

        // Heart click toggle
        holder.btnWishlist.setOnClickListener {
            if (uid.isEmpty()) {
                Toast.makeText(holder.itemView.context, "Please login first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val productRef = db.collection("products").document(p.id)
            if (p.wishlistedBy.contains(uid)) {
                // Remove from wishlist
                productRef.update("wishlistedBy", FieldValue.arrayRemove(uid))
                    .addOnSuccessListener {
                        holder.btnWishlist.setImageResource(R.drawable.ic_heart_outline)
                        Toast.makeText(holder.itemView.context, "Removed from wishlist", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Add to wishlist
                productRef.update("wishlistedBy", FieldValue.arrayUnion(uid))
                    .addOnSuccessListener {
                        holder.btnWishlist.setImageResource(R.drawable.ic_heart_filled)
                        Toast.makeText(holder.itemView.context, "Added to wishlist ❤️", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProductDetailActivity::class.java)
            intent.putExtra("product", p)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<Product>) {
        list = newList
        notifyDataSetChanged()
    }
}

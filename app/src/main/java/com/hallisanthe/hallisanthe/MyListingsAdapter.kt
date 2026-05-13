package com.hallisanthe.hallisanthe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class MyListingsAdapter(private var list: MutableList<Product>) : RecyclerView.Adapter<MyListingsAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.txtProductName)
        val price: TextView = v.findViewById(R.id.tvPriceTag)
        val img: ImageView = v.findViewById(R.id.imgProduct)
        val btnDelete: ImageButton = v.findViewById(R.id.btnWishlist) // Reusing btnWishlist ID as trash
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = list[position]
        holder.name.text = p.name
        holder.price.text = "₹${p.price}"
        
        Glide.with(holder.itemView.context).load(p.imageUrl).into(holder.img)

        holder.btnDelete.setImageResource(android.R.drawable.ic_menu_delete)
        holder.btnDelete.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete this product?")
                .setMessage("This action cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    FirebaseFirestore.getInstance().collection("products").document(p.id).delete()
                        .addOnSuccessListener {
                            list.removeAt(position)
                            notifyItemRemoved(position)
                        }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun getItemCount() = list.size
}

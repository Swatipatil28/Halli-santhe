package com.hallisanthe.hallisanthe

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hallisanthe.hallisanthe.databinding.ItemCategoryGridBinding

class CategoryAdapter(
    private val categories: List<String>,
    private val onCategoryClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private val categoryEmojis = mapOf(
        "Pottery" to "🏺",
        "Textiles" to "🧵",
        "Woodwork" to "🪵",
        "Organic" to "🌿",
        "Metalwork" to "⚒️",
        "Jewelry" to "📿",
        "Paintings" to "🎨",
        "Basketry" to "🧺"
    )

    class ViewHolder(val binding: ItemCategoryGridBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.binding.tvCategoryName.text = category
        holder.binding.tvCategoryEmoji.text = categoryEmojis[category] ?: "📦"
        holder.itemView.setOnClickListener {
            onCategoryClick(category)
        }
    }

    override fun getItemCount() = categories.size
}

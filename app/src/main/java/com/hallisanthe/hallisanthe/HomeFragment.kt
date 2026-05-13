package com.hallisanthe.hallisanthe

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import com.hallisanthe.hallisanthe.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ProductAdapter
    private val allProducts = mutableListOf<Product>()
    private val filteredProducts = mutableListOf<Product>()
    private var selectedCategory = "All"
    private var searchQuery = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupViews(binding.root)
        loadProductsFromFirestore()

        // Handle pre-selected category from CategoriesFragment
        arguments?.getString("selected_category")?.let { category ->
            selectedCategory = category
            autoSelectChip(category)
        }

        return binding.root
    }

    private fun setupViews(view: View) {
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = ProductAdapter(filteredProducts)
        binding.recyclerView.adapter = adapter

        // Chip click listeners
        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                selectedCategory = "All"
            } else {
                val chip = group.findViewById<Chip>(checkedIds[0])
                val rawText = chip?.text?.toString()?.trim() ?: "All"
                // Extract the English word after the emoji and space: "🔥 Trending" -> "Trending"
                selectedCategory = if (rawText.contains(" ")) rawText.substringAfter(" ").trim() else rawText
            }
            applyFilter()
        }

        // Search
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchQuery = query ?: ""
                applyFilter()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery = newText ?: ""
                applyFilter()
                return true
            }
        })
    }

    private fun autoSelectChip(category: String) {
        for (i in 0 until binding.chipGroupCategories.childCount) {
            val chip = binding.chipGroupCategories.getChildAt(i) as? Chip
            if (chip?.text?.toString()?.contains(category, ignoreCase = true) == true) {
                chip.isChecked = true
                break
            }
        }
    }

    private fun loadProductsFromFirestore() {
        FirebaseFirestore.getInstance()
            .collection("products")
            .addSnapshotListener { snapshot, error ->
                if (!isAdded) return@addSnapshotListener
                if (error != null) {
                    Log.e("HomeFragment", "Firestore error: ${error.message}")
                    return@addSnapshotListener
                }
                allProducts.clear()
                snapshot?.documents?.forEach { doc ->
                    val p = doc.toObject(Product::class.java)
                    if (p != null) allProducts.add(p.copy(id = doc.id))
                }
                applyFilter()
            }
    }

    private fun applyFilter() {
        filteredProducts.clear()
        
        // "Trending" acts like "All"
        val byCategory = if (selectedCategory == "All" || selectedCategory == "Trending" || selectedCategory.isEmpty()) {
            allProducts.toList()
        } else {
            allProducts.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
        
        val bySearch = if (searchQuery.isEmpty()) {
            byCategory
        } else {
            byCategory.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.artisanName.contains(searchQuery, ignoreCase = true) ||
                it.villageName.contains(searchQuery, ignoreCase = true)
            }
        }
        
        // Sort alphabetically within category
        filteredProducts.addAll(bySearch.sortedWith(compareBy({ it.category }, { it.name })))
        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

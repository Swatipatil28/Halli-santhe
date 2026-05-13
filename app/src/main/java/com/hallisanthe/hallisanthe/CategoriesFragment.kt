package com.hallisanthe.hallisanthe

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.hallisanthe.hallisanthe.databinding.FragmentCategoriesBinding

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)

        val categories = listOf("Pottery", "Textiles", "Woodwork", "Organic", "Metalwork", "Jewelry", "Paintings", "Basketry")

        val adapter = CategoryAdapter(categories) { selectedCategory ->
            navigateToHomeWithCategory(selectedCategory)
        }

        val recyclerView = _binding!!.rvCategories
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter

        return _binding!!.root
    }

    private fun navigateToHomeWithCategory(category: String) {
        try {
            val fragment = HomeFragment()
            val bundle = Bundle()
            bundle.putString("selected_category", category)
            fragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()

            // Update bottom nav selection to Home
            (activity as? MainActivity)?.setBottomNavSelected(R.id.nav_home)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

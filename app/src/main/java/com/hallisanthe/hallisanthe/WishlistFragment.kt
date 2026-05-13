package com.hallisanthe.hallisanthe

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hallisanthe.hallisanthe.databinding.FragmentWishlistBinding

class WishlistFragment : Fragment() {

    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!
    private val wishlistProducts = mutableListOf<Product>()
    private lateinit var adapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProductAdapter(wishlistProducts)
        binding.rvWishlist.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvWishlist.adapter = adapter

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            showEmpty()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("products")
            .whereArrayContains("wishlistedBy", uid)
            .addSnapshotListener { snapshot, error ->
                if (!isAdded || _binding == null) return@addSnapshotListener
                if (error != null) {
                    Toast.makeText(requireContext(), "Error loading wishlist", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                wishlistProducts.clear()
                snapshot?.documents?.forEach { doc ->
                    val p = doc.toObject(Product::class.java)
                    if (p != null) wishlistProducts.add(p.copy(id = doc.id))
                }
                if (wishlistProducts.isEmpty()) showEmpty() else showList()
                adapter.notifyDataSetChanged()
            }
    }

    private fun showEmpty() {
        binding.rvWishlist.visibility = View.GONE
        binding.emptyWishlistLayout.visibility = View.VISIBLE
    }

    private fun showList() {
        binding.rvWishlist.visibility = View.VISIBLE
        binding.emptyWishlistLayout.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.hallisanthe.hallisanthe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class MyListingsFragment : Fragment() {
    private lateinit var adapter: MyListingsAdapter
    private val db = FirebaseFirestore.getInstance()
    private val myListings = mutableListOf<Product>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_my_listings, container, false)
        val rv = view.findViewById<RecyclerView>(R.id.rvMyListings)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyListings)

        rv.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = MyListingsAdapter(myListings)
        rv.adapter = adapter

        db.collection("products")
            .whereEqualTo("artisanName", UserSession.name)
            .addSnapshotListener { value, _ ->
                myListings.clear()
                value?.documents?.forEach { doc ->
                    val p = doc.toObject(Product::class.java)
                    if (p != null) myListings.add(p.copy(id = doc.id))
                }
                
                tvEmpty.visibility = if (myListings.isEmpty()) View.VISIBLE else View.GONE
                adapter.notifyDataSetChanged()
            }

        return view
    }
}

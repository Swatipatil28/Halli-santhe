package com.hallisanthe.hallisanthe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.hallisanthe.hallisanthe.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Also bind immediately with whatever is cached
        bindUserData()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show loading state
        binding.tvUserName.text = "Loading..."
        binding.tvUserMobile.text = ""

        // Reload from Firestore to get fresh data
        UserSession.load {
            if (!isAdded) return@load
            requireActivity().runOnUiThread {
                binding.tvUserName.text = UserSession.name.ifEmpty { "No name set" }
                binding.tvUserMobile.text = if (UserSession.mobile.isNotEmpty()) "+91 ${UserSession.mobile}" else ""
                binding.tvUserRole.text = UserSession.role.ifEmpty { "Buyer" }
                binding.tvUserLocation.text = UserSession.village.ifEmpty { "—" }

                if (UserSession.profileImageUrl.isNotEmpty()) {
                    Glide.with(this).load(UserSession.profileImageUrl).circleCrop().into(binding.ivProfilePic)
                }
            }
        }

        setupMenuClicks()
    }

    private fun bindUserData() {
        binding.tvUserName.text = UserSession.name.ifEmpty { "—" }
        binding.tvUserMobile.text = if (UserSession.mobile.isNotEmpty())
            "+91 ${UserSession.mobile}" else "+91"
        binding.tvUserRole.text = UserSession.role.ifEmpty { "Buyer" }
        binding.tvUserLocation.text = UserSession.village.ifEmpty { "—" }

        if (UserSession.profileImageUrl.isNotEmpty()) {
            Glide.with(this).load(UserSession.profileImageUrl).circleCrop().into(binding.ivProfilePic)
        }
    }

    private fun setupMenuClicks() {
        // My Wishlist — safely switch tab
        binding.menuWishlist.setOnClickListener {
            try {
                val activity = requireActivity() as? MainActivity ?: return@setOnClickListener
                activity.loadFragment(WishlistFragment())
                activity.setBottomNavSelected(R.id.nav_wishlist)
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Wishlist nav error: ${e.message}")
            }
        }

        binding.menuMyListings.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(MyListingsFragment())
        }

        // Messages
        binding.menuMessages.setOnClickListener {
            Snackbar.make(binding.root, "Messages coming soon", Snackbar.LENGTH_SHORT).show()
        }

        // Settings
        binding.menuSettings.setOnClickListener {
            Snackbar.make(binding.root, "Settings coming soon", Snackbar.LENGTH_SHORT).show()
        }

        // Logout
        binding.menuLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    UserSession.clear()
                    startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Edit Profile
        binding.btnEditProfile.setOnClickListener {
            Snackbar.make(binding.root, "Edit Profile coming soon", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

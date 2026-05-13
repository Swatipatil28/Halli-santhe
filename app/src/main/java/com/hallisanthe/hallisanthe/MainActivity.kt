package com.hallisanthe.hallisanthe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.hallisanthe.hallisanthe.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        UserSession.load {
            // Hide Sell tab for Buyers
            runOnUiThread {
                binding.bottomNavigation.menu.findItem(R.id.nav_upload).isVisible = UserSession.role == "Artisan"
            }
        }

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { loadFragment(HomeFragment()); true }
                R.id.nav_categories -> { loadFragment(CategoriesFragment()); true }
                R.id.nav_upload -> {
                    startActivity(Intent(this, UploadActivity::class.java)); false
                }
                R.id.nav_wishlist -> { loadFragment(WishlistFragment()); true }
                R.id.nav_profile -> { loadFragment(ProfileFragment()); true }
                else -> false
            }
        }
    }

    fun loadFragment(fragment: Fragment) {
        try {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss()   // use commitAllowingStateLoss to prevent state crashes
        } catch (e: Exception) {
            Log.e("MainActivity", "loadFragment error: ${e.message}")
        }
    }

    fun setBottomNavSelected(itemId: Int) {
        try {
            binding.bottomNavigation.selectedItemId = itemId
        } catch (e: Exception) {
            Log.e("MainActivity", "setBottomNavSelected error: ${e.message}")
        }
    }
}

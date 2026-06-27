package com.dragonic.decryptor.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.dragonic.decryptor.R
import com.dragonic.decryptor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        // Hide bottom nav on result/file decryptor screens
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.resultFragment, R.id.fileDecryptorFragment -> {
                    binding.bottomNav.visibility = android.view.View.GONE
                    binding.navDivider.visibility = android.view.View.GONE
                }
                else -> {
                    binding.bottomNav.visibility = android.view.View.VISIBLE
                    binding.navDivider.visibility = android.view.View.VISIBLE
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()
}

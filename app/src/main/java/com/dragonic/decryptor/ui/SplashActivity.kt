package com.dragonic.decryptor.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dragonic.decryptor.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        animateSplash()

        lifecycleScope.launch {
            delay(2500)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

    private fun animateSplash() {
        // Logo pulse
        val scaleX = ObjectAnimator.ofFloat(binding.logoImage, "scaleX", 0.5f, 1.05f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.logoImage, "scaleY", 0.5f, 1.05f, 1f)
        val alpha = ObjectAnimator.ofFloat(binding.logoImage, "alpha", 0f, 1f)
        val set = AnimatorSet()
        set.playTogether(scaleX, scaleY, alpha)
        set.duration = 900
        set.interpolator = DecelerateInterpolator()
        set.start()

        // Text fade in
        binding.appName.alpha = 0f
        binding.appSubName.alpha = 0f
        binding.tagline.alpha = 0f
        binding.appName.animate().alpha(1f).setStartDelay(600).setDuration(500).start()
        binding.appSubName.animate().alpha(1f).setStartDelay(750).setDuration(500).start()
        binding.tagline.animate().alpha(1f).setStartDelay(900).setDuration(500).start()

        // Glow pulse
        val glowScale = ObjectAnimator.ofFloat(binding.glowCircle, "scaleX", 0.8f, 1.3f, 0.8f)
        val glowScaleY = ObjectAnimator.ofFloat(binding.glowCircle, "scaleY", 0.8f, 1.3f, 0.8f)
        val glowSet = AnimatorSet()
        glowSet.playTogether(glowScale, glowScaleY)
        glowSet.duration = 2000
        glowSet.repeatCount = ObjectAnimator.INFINITE
        glowSet.start()
    }
}

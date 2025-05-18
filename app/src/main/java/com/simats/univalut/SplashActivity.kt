package com.simats.univalut

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Option 1: Animate the whole layout
        val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        rootLayout.startAnimation(fadeIn)

        // Option 2: Animate just the logo image (uncomment if preferred)
        // val splashLogo = findViewById<ImageView>(R.id.splashLogo)
        // splashLogo.startAnimation(fadeIn)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2500)
    }
}

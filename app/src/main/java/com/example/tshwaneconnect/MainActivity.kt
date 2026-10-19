package com.example.tshwaneconnect

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val splashDelay = 2500L

    private val handler = Handler(Looper.getMainLooper())

    private val openLogin = Runnable {
        if (!isFinishing && !isDestroyed) {
            val intent = Intent(
                this@MainActivity,
                LoginActivity::class.java
            )

            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Preserve the original splash screen colours
        window.statusBarColor = android.graphics.Color.rgb(5, 11, 99)
        window.navigationBarColor = android.graphics.Color.rgb(5, 11, 99)

        window.clearFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Display the existing splash screen XML
        setContentView(R.layout.activity_main)

        // Automatically open Sign In after 2.5 seconds
        handler.postDelayed(openLogin, splashDelay)
    }

    override fun onDestroy() {
        handler.removeCallbacks(openLogin)
        super.onDestroy()
    }
}

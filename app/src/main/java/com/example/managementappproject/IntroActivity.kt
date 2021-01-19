package com.example.managementappproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN

@Suppress("DEPRECATION")
class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        window.setFlags(
                FLAG_FULLSCREEN,
                FLAG_FULLSCREEN
        )
    }


}
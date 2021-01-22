package com.example.managementappproject.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import com.example.managementappproject.R
import kotlinx.android.synthetic.main.activity_intro.*

@Suppress("DEPRECATION")
class IntroActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        window.setFlags(
                FLAG_FULLSCREEN,
                FLAG_FULLSCREEN
        )

        btn_Intro_signUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        btn_Intro_signIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }


}
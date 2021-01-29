 package com.example.managementappproject.activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import com.example.managementappproject.R
import com.example.managementappproject.firebase.FirestoreClass
import kotlinx.android.synthetic.main.activity_splash.*

 @Suppress("DEPRECATION")
 class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.setFlags(
            FLAG_FULLSCREEN,
            FLAG_FULLSCREEN
        )

        /** we use Handler to execute a block of code after the time frame we gave to it,
         *  we start an Intent to switch from an activity to another. By applying finish()
         *  we destroy the activity and the user can't come back to it.*/
        Handler().postDelayed({
            // here we can check if the currentUserID is not null and send the user directly to MainActivity
            var currentUserID = FirestoreClass().getCurrentUserId()
            if (currentUserID.isNotEmpty()){
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }else{
                // if the string is empty it will send us to the IntroActivity
                startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
            }

            finish()
           }, 2500)

        /** in order to use a specif typeface/font */
        val typeFace: Typeface = Typeface.createFromAsset(assets, "teen.ttf")
        txtView_splash_layout.typeface = typeFace
    }


}
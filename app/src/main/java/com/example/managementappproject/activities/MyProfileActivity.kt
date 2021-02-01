package com.example.managementappproject.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.managementappproject.R
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MyProfileActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setUpActionBar()
    }

    private fun setUpActionBar(){
        setSupportActionBar(toolbar_my_profile_activity)
        // we don't have a navigation item here but we do have the action bar
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_color_white)
            actionBar.title = resources.getString(R.string.my_profile_title)
        }
    //functionality
        toolbar_my_profile_activity.setNavigationOnClickListener { onBackPressed() }

    }
}
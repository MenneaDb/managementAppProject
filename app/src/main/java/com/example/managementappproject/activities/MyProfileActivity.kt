package com.example.managementappproject.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.managementappproject.R
import com.example.managementappproject.firebase.FirestoreClass
import com.example.managementappproject.models.User
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MyProfileActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setUpActionBar()

        // we need a method to get the existing data from the logged in user and show it in this activity
        FirestoreClass().loadUserData(this@MyProfileActivity)

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
    //method to call inside loadUserData(FireStoreClass) to pass the user object we got with this method
    fun setUserDataInUI(user: User){
        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(iv_user_image)

        et_name.setText(user.name)
        et_email.setText(user.email)
        // if is not 0 (0 is assigned inside the database if the number is missing) we can set the mobile editText
        if (user.mobile != 0L){
            et_mobile.setText(user.mobile.toString())
        }

    }
}
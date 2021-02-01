package com.example.managementappproject.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.example.managementappproject.R
import com.example.managementappproject.firebase.FirestoreClass
import com.example.managementappproject.models.User
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // we need to call this method in order to enable the functionality of the icon
        setUpActionBar()
        /* because this class will be a NavigationItemSelectedListener as we specified on top of the code(extension)
           When one of the buttons is clicked, the compiler will execute the logic this class*/
        nav_view.setNavigationItemSelectedListener(this)

        FirestoreClass().loadUserData(this@MainActivity)
    }

    private fun setUpActionBar(){
        setSupportActionBar(toolbar_main_activity)
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        //to have a functionality for this icon we need a OnClickListener
        toolbar_main_activity.setNavigationOnClickListener{
            toggleDrawer()

        }
    }

    private fun toggleDrawer(){
        if (drawer_layout.isDrawerOpen(GravityCompat.START)){
            // if the drawer is open, by pressing the menu icon we can close it
            drawer_layout.closeDrawer(GravityCompat.START)
        }else{
            // if it's close, we can open the drawer
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    // double tap to exit the app
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START)
        }else{
            // double press to exit
            doubleBackToExit()
        }
    }

    fun updateNavigationUserDetails(user: User){
     // load image for the user and text for nav_menu
        Glide
                .with(this@MainActivity) // specify where you need it
                .load(user.image) // url of where the image is stored
                .fitCenter()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(nav_user_image) // where we want to display the picture

        //set the txtView of the nav_menu with the name of the specific user
        tv_username.text = user.name
    }

    // functionality that execute once with press to one of navigation item buttons
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_my_profile -> {
                startActivity(Intent(this@MainActivity, MyProfileActivity::class.java))
            }
            R.id.nav_sign_out -> {
                //1st we want to signOut from firebase
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this@MainActivity, IntroActivity::class.java)
                // in this way if the intro activity is already inside the stack we don't create a new one and we go back to it
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                // user signOut
                finish()
            }
        }
        // when the user touch one of the buttons we want to close the drawer menu and push it to the left side of the UI
        drawer_layout.closeDrawer(GravityCompat.START)

        return true
    }
}
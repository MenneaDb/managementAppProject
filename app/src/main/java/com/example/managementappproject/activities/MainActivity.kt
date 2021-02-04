package com.example.managementappproject.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.managementappproject.R
import com.example.managementappproject.adapters.BoardItemsAdapter
import com.example.managementappproject.firebase.FireStoreClass
import com.example.managementappproject.models.Board
import com.example.managementappproject.models.User
import com.example.managementappproject.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        const val MY_PROFILE_REQUEST_CODE = 11
        const val CREATE_BOARD_REQUEST_CODE = 12
    }
    // to store the username
    private lateinit var mUserName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // we need to call this method in order to enable the functionality of the icon
        setUpActionBar()
        /* because this class will be a NavigationItemSelectedListener as we specified on top of the code(extension)
           When one of the buttons is clicked, the compiler will execute the logic this class*/
        nav_view.setNavigationItemSelectedListener(this)

        /** we call this method before the next one because is the one that interact with a method from the FireStore class
           and load the data from the user to this activity. We also want to load the boards, in this case we don't have to
           load the board every time we change activity. */
        FireStoreClass().loadUserData(this@MainActivity, true)

        fab_create_board.setOnClickListener{
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }
    }

    /** new method to populate the board - create the Board inside the UI. We will download the list
       inside the FireStoreClass and use it to populate this method and display the boardList inside
       the recyclerView */
    fun populateBoardsListToUI(boardList: ArrayList<Board>){
        hideProgressDialog()
        // if the list contain elements .. > 0 we set these values fot it
        if (boardList.size > 0){
            rv_boards_list.visibility = View.VISIBLE
            tv_no_boards_available.visibility = View.GONE

            rv_boards_list.layoutManager = LinearLayoutManager(this@MainActivity)
            rv_boards_list.setHasFixedSize(true)

            // refer to the adapter and assign it to the board lists
            val adapter = BoardItemsAdapter(this, boardList)
            rv_boards_list.adapter = adapter

            /** we set a click event for each object of the boardsList - we implementing the interface we created inside the
                BoardItemsAdapter and we are saying what should happen when the onClick method is triggered(click of one of
                the boardList element)  */
            adapter.setOnClickListener(object: BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    /** we want to put it inside DOCUMENT_ID, the info we want to pass is the one we get from the model of
                        the one we get inside the onClick method, we get documentId from the Board class and assign it to
                        our Constant and pass it over when we start the activity */
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }

            })
        }else{
            // if the board list is empty we don't want to display it
            rv_boards_list.visibility = View.GONE
            tv_no_boards_available.visibility = View.VISIBLE
        }
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

    fun updateNavigationUserDetails(user: User, readBoardsList: Boolean){

        mUserName = user.name

     // load image for the user and text for nav_menu
        Glide
                .with(this@MainActivity) // specify where you need it
                .load(user.image) // url of where the image is stored
                .fitCenter()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(nav_user_image) // where we want to display the picture

        //set the txtView of the nav_menu with the name of the specific user
        tv_username.text = user.name

        // I want to load the BoardsList if the condition is set TRUE
        if (readBoardsList){
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().getBoardsList(this@MainActivity)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE){
            FireStoreClass().loadUserData(this)
        }else if (resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE) {
            FireStoreClass().getBoardsList(this@MainActivity) // in this way we can reload the board list after we create a new one

        }else{
            Log.e("Cancelled", "Cancelled")
        }
    }

    // functionality that execute once with press to one of navigation item buttons
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_my_profile -> {
                startActivityForResult(Intent(this@MainActivity, MyProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
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
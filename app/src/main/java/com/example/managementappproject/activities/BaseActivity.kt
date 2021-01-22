package com.example.managementappproject.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.managementappproject.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.dialog_progress.*

/**
 * I need this class to holds several functions that I will use in multiple different activities and I am going to use it
 * instead of AppCompactActivity in order to let other classes inherits from the BaseActivity and use its methods
 */
open class BaseActivity : AppCompatActivity() {

    // var to track the back btn functionality. if the user press twice, tha app should close(example).
    private var doubleBackToExitPressedOnce = false

    // I want to display a progress dialog to the user each time something is loading(background operations).
    private lateinit var mProgressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }

    fun showProgressDialog(text: String) {
       // Dialog initialize
        mProgressDialog = Dialog(this)

        // Set the screen content from a layout resource.
        // The resource will be inflated, adding all top-level views to the screen
        mProgressDialog.setContentView(R.layout.dialog_progress)

        mProgressDialog.tv_progress_txt.text = text

        // Start the dialog and display it on screen.
        mProgressDialog.show()
    }

    // method to hide the progressDialog
    fun hideProgressDialog(){
        mProgressDialog.dismiss()
    }

    /* method to get the current user id from firebase in order to display the relevant
     data, I only show to the user the project and tasks that he/she's assigned to,
     nothing else */
    fun getCurrentUserId(): String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    // user pressed the back button twice in order to exit the app and close it. onBackPressed functionality.
    fun doubleBackToExit(){
        if (doubleBackToExitPressedOnce){
            super.onBackPressed()
            return
        }

        // In case the back button is clicked just once we can display a Toast message to the user
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(
            this,
            resources.getString(R.string.please_click_back_again_to_exit),
            Toast.LENGTH_SHORT
        ).show()

      /* if user pressed once and waits too much to press again, I want to reset the back btn functionality.
         after 2 seconds the double exit pressed is going to be again false. If the user press twice within
         2 seconds it will close the application or the activity where he/she is */
        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    /** I create this method to display an Error message (internet connection, operation didn't work..),
      When I call this function I want to display a message to the user as SnackBar(text on bottom screen)
      We make it, we found the view where we display the message, the message to display and how long is
      going to last. We create a snackBarView in order to set the individual background color(example) and
     we also need to show() it.
     */
    fun showErrorSnackBar(message: String){
        val snackBar = Snackbar.make(findViewById(android.R.id.content),
            message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
            snackBarView.setBackgroundColor(ContextCompat.getColor(this,
            R.color.snackBar_error_color))
        snackBar.show()
    }
}
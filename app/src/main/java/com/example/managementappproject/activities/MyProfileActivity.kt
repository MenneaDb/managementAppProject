package com.example.managementappproject.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.managementappproject.R
import com.example.managementappproject.firebase.FirestoreClass
import com.example.managementappproject.models.User
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.app_bar_main.*
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    // we create constants for the permission codes
    companion object {
        private const val READ_STORAGE_PERMISSION_CODE = 1
        private const val PICK_IMAGE_REQUEST_CODE = 2
    }
    // it can be a String as well but I want to store it as a Uri type
    private var mSelectedImageFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setUpActionBar()

        // we need a method to get the existing data from the logged in user and show it in this activity
        FirestoreClass().loadUserData(this@MyProfileActivity)

        iv_profile_user_image.setOnClickListener{
            // condition to check if the user granted the permission to read from the external storage
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
            ) {

               showImageChooser()

            }else{
                // if it is not granted, we ask the user for it.
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        READ_STORAGE_PERMISSION_CODE
                )
            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
               showImageChooser()
            }
        }else{
            // if the user didn't grant the permission
            Toast.makeText(
                    this,
                    "You just denied the permission for storage. You can allow it from Settings.",
                    Toast.LENGTH_LONG
            ).show()
        }
    }

    // to access and choose an Image from internal storage
    private fun showImageChooser(){
        val galleryIntent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        // we need to get a result
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    //I want to do something when I get startActivityForResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE && data!!.data != null){
            //data that we get from ActivityResult and store it inside this var
            mSelectedImageFileUri = data.data

            try {
                Glide
                        .with(this@MyProfileActivity)
                        .load(Uri.parse(mSelectedImageFileUri.toString()))
                        .centerCrop()
                        .placeholder(R.drawable.ic_user_place_holder)
                        .into(iv_profile_user_image)
            }catch (e: IOException){
                e.printStackTrace()
            }

        }
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
        // we don't copy the image from its location(URI) and paste it in the app, we just reuse the URI of the existing file inside the devide
        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(iv_profile_user_image)

        et_name.setText(user.name)
        et_email.setText(user.email)
        // if is not 0 (0 is assigned inside the database if the number is missing) we can set the mobile editText
        if (user.mobile != 0L){
            et_mobile.setText(user.mobile.toString())
        }

    }
}
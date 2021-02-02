package com.example.managementappproject.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.managementappproject.R
import com.example.managementappproject.firebase.FirestoreClass
import com.example.managementappproject.models.User
import com.example.managementappproject.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
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
    private var mProfileImageURL: String = ""
    private lateinit var mUserDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setUpActionBar()

        // we need a method to get the existing data from the logged in user and show it in this activity
        FirestoreClass().loadUserData(this@MyProfileActivity)

        iv_profileUser_image.setOnClickListener {
            // condition to check if the user granted the permission to read from the external storage
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {

               showImageChooser()

            }else{
                // if it is not granted, we ask the user for it.
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        READ_STORAGE_PERMISSION_CODE)
            }
        }

        btn_update.setOnClickListener {
            if (mSelectedImageFileUri != null){
                uploadUserImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                // update profile data
                updateUserProfileData()
            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImageChooser()
            } else {
                // if the user didn't grant the permission
                Toast.makeText(this, "You just denied the permission for storage. You can allow it from Settings.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // to access and choose an Image from internal storage
    private fun showImageChooser(){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
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
                        .into(iv_profileUser_image)
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

        mUserDetails = user

        // we don't copy the image from its location(URI) and paste it in the app, we just reuse the URI of the existing file inside the devide
        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(iv_profileUser_image)

        et_name.setText(user.name)
        et_email.setText(user.email)
        // if is not 0 (0 is assigned inside the database if the number is missing) we can set the mobile editText
        if (user.mobile != 0L){
            et_mobile.setText(user.mobile.toString())
        }
    }

    // same name as the method we have in FireStoreClass, doesn't need specific data because the profile data is already inside this class
    private fun updateUserProfileData(){
        // we create a userHashMap
        val userHashMap = HashMap<String, Any>()

        // now we can add values to the HashMap(image url, name, mobile) condition -> if there's an existing url for the image we can set a new one
        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image){
            // by referring to the key of the value inside the database, we can assign a value to it - UPDATE AN HashMap
            userHashMap[Constants.IMAGE] = mProfileImageURL

        }

        // if the name is the same, we don't need to update the database
        if (et_name.text.toString() != mUserDetails.name){
            userHashMap[Constants.NAME] = et_name.text.toString()

        }
        // same for the mobile, the hashMap of the mobile expects a Long Value(as we set in the database)-> .toLong()
        if (et_mobile.text.toString() != mUserDetails.mobile.toString()){
            userHashMap[Constants.MOBILE] = et_mobile.text.toString().toLong()

        }

            FirestoreClass().updateUserProfileData(this, userHashMap)

    }

    // when we call this method we will also call the updateUserProfileData() method
    private fun uploadUserImage(){
        showProgressDialog(resources.getString(R.string.please_wait))
        //if the uri is not null, we want to store the image inside the storage
        if (mSelectedImageFileUri != null){
            val sRef :StorageReference =
                FirebaseStorage.getInstance().reference.child(
                        "USER_IMAGE" + System.currentTimeMillis()
                            + "." + getFileExtension(mSelectedImageFileUri))
            // if the task is successful
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->
                Log.i(
                        "Firebase Image URL",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString() // with this line we create a string out of where the file is stored downloadUrl -> we need the link from the storage to store it inside the database
                )
                // we add another successListener to store the actual link (uri) somewhere
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri -> Log.i("Downloadable Image URL", uri.toString())
                    // now we can store this value in this var
                    mProfileImageURL = uri.toString()

                    updateUserProfileData()
                }
            }.addOnFailureListener{
                exception ->
                Toast.makeText(
                        this@MyProfileActivity,
                        exception.message,
                        Toast.LENGTH_LONG
                ).show()

                hideProgressDialog()
            }
        }
    }

    /** method to help us understand the extension fil we get from the download (if is an image we use it as an image,
       if is not we can't use it as image or profile image). MimeTypeMap class allow us to understand the type of Uri
       we got and .getSingleton creates an instance of the class that allow us to use its functions.
       .getExtensionFromMimeType() will allow us to get the Type of the Uri we pass to it and use it to return the extension,
       the type of it or find it based on the extension(.png .mp4 ..examples of extensions) */
    private fun getFileExtension(uri: Uri?): String?{
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    // we need to store the image also inside the database, not only in the storage

    // we create this method to close MyProfileActivity in the moment that the user updated its info
    fun profileUpdateSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}
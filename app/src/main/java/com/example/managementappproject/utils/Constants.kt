package com.example.managementappproject.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.example.managementappproject.activities.MyProfileActivity

object Constants {

    const val USERS: String = "users"

    const val BOARDS: String = "boards"

    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val MOBILE: String = "mobile"

    const val ASSIGNED_TO: String = "assignedTo"
    // we need to pass info from boardList to TaskList
    const val DOCUMENT_ID: String = "documentId"

    const val READ_STORAGE_PERMISSION_CODE = 1
    const val PICK_IMAGE_REQUEST_CODE = 2

    const val TASK_LIST: String = "taskList"

    /** We need to reuse these methods inside different activities(MyProfile and CreateBoard), that is why pass activity
        as parameter in order to know from which activity we should do that */
    fun showImageChooser(activity: Activity){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        // we need to get a result
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(activity: Activity, uri: Uri?): String?{
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}
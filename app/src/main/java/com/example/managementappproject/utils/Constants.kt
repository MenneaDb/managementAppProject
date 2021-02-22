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
    const val BOARD_DETAIL: String = "board_detail"
    const val ID: String = "id"
    const val EMAIL: String = "email"
    // when we send an intent with extra info it is always good to have key to request them
    const val TASK_LIST_ITEM_POSITION: String = "task_list_item_position"
    const val CARD_LIST_ITEM_POSITION: String = "card_list_item_position"
    const val BOARD_MEMBERS_LIST: String = "board_members_list"
    const val SELECT: String = "Select"
    const val UN_SELECT: String = "UnSelect"
    // we need to store the token inside the device
    const val MANAGE_IT_SHARED_PREFERENCES = "manageIt_preferences"
    const val FCM_TOKEN = "fcmToken"
    const val FCM_TOKEN_UPDATED = "fcmTokenUpdated"
    // constants related to the notification
    const val FCM_BASE_URL: String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION: String = "authorization"
    const val FCM_KEY:String = "key"
    const val FCM_SERVER_KEY: String = "AAAAWD3AIk0:APA91bE1AEegM7MKn-uxRzM6Bd9Tb41vRvPEVTnRapZ1B9Nik3-7oWEUObccFYg1hU9l9b7UDnkDg4UZbjiaXSI_TyQcYBeXFAUGKRsVMS69iPX1oOK7GPEnNqK76-3gGYu3d9Vx_oNu"
    const val FCM_KEY_TITLE: String = "title"
    const val FCM_KEY_MESSAGE: String = "message"
    const val FCM_KEY_DATA: String = "data"
    const val FCM_KEY_TO: String = "to"

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
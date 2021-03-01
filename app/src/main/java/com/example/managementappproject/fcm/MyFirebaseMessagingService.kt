package com.example.managementappproject.fcm


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.managementappproject.R
import com.example.managementappproject.activities.MainActivity
import com.example.managementappproject.activities.SignInActivity
import com.example.managementappproject.firebase.FireStoreClass
import com.example.managementappproject.utils.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService: FirebaseMessagingService() {

    /** when we get the message this method will be called, there are 2 types of messages: data messages and notification messages.
    the 1st type  will be handle here inside this method whatever is foreground or background. data messages are normally working
    with GCM (GoogleCloudMessaging). notification messages works with this method when the app is in the foreground , when is in
    the background automatically generated notifications are displayed. when the user taps on the notification  he returns to the
    app and messages contain both notification and data payloads are treated as notification messages.*/
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        // we can read from a remoteMessage
        Log.d(TAG, "From: ${remoteMessage.from}")

        //check if the message is not empty and display a message in case
        remoteMessage.data.isNotEmpty().let{
            Log.i(TAG, "Message Data Payload: " + remoteMessage.data)

            // if the message is not empty we want to do more then Log it, prepare the core of the title & message of the notification
            val title = remoteMessage.data[Constants.FCM_KEY_TITLE]!!
            val message = remoteMessage.data[Constants.FCM_KEY_MESSAGE]!!
            // we send a notification to a user with a title and a message
            sendNotifications(title, message)
        }

        // check if the message contains a notification
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }

    }

    // if the program get problems with the old token, whit this method we will create a new one
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.e(TAG, "Refreshed Token: $token") // we want to know about the new token

        sendRegistrationToServer(token)
    }

    /** if we a new token we need to send it to server - whenever you register somewhere and you're logged in, a token is sent
     *  to your device. we want to use that token with the sending notification functionality */
    private fun sendRegistrationToServer(token: String?) {
        // here we have saved the token in Shared preferences
        val sharedPreferences = this.getSharedPreferences(Constants.MANAGE_IT_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(Constants.FCM_TOKEN, token)
        editor.apply()
    }

    // we create this method to send notifications to the user
    private fun sendNotifications(title: String, message: String) {
        // we need to make sure before the Intent(click from the user on the notification) that a User is logged in to send him directly to the Main Activity, if not we send him to the SignInActivity
        val intent =  if (FireStoreClass().getCurrentUserId().isNotEmpty()){
            Intent(this@MyFirebaseMessagingService, MainActivity::class.java)
        } else {
            Intent(this@MyFirebaseMessagingService, SignInActivity::class.java)
        }
        /** before we launch the app we want to make sure that the Flags are correct, we gave different option to it, in this
            way we can avoid the overlapping of activities(not too many of the same activity open(ex if the user click back btn
            the app works because there's only one activity of that type open)
            Make sure that specific activity is set to a specific position inside of the stack(if we have 2 activities, with this
            intent we set the activity passed on top)*/
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP)
        // the user is in another app, we can't open an intent from the other app to our --> we need a pending intent
        val pendingIntent = PendingIntent.getActivity(this@MyFirebaseMessagingService, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        // we need to create a channel id
        val channelId = this.resources.getString(R.string.default_notification_channel_id)
        // set the type of notification sound for the user
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        // display the icon in the notification bar
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_ic_notification)
            .setContentTitle(title) // now we can use the title and the message that are passed to the function
            .setContentText(message) // we get it from the remoteMessage once it's received(when the phone get the message from the firebase server it will give us all the info we sent in the 1st place from the MembersActivity(doInBackground())
            .setAutoCancel(true) // when user clicks on the notification it will be automatically cancelled
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent) // when the user clicks on it then it should open up the main activity(pendingIntent set before)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // if it's android oreo notification channel is needed as well
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Channel ManageIt title", NotificationManager.IMPORTANCE_DEFAULT)
            // create the notification channel based on the one we prepared
            notificationManager.createNotificationChannel(channel)
        }
        // now we can use the notificationManager to notify
        notificationManager.notify(0, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}
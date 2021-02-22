package com.example.managementappproject.fcm


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.managementappproject.R
import com.example.managementappproject.activities.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService: FirebaseMessagingService() {

    /** when we get the message this method will be called, there are 2 types of messages: data messages and notification messages.
    the 1st type  will be handle here inside this method whatever is foreground or background. data messages are normally working
    with GCM (GoogleCloudMessaging). notification messages works with this method when the app is in the foreground , when is in
    the background automatically generated notifications are displayed. when the user taps on the notification  he returns to the
    app and messages contain both notification and data payloads are treated as notification messages.*/
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // we can read from a remoteMessage
        Log.d(TAG, "FROM: ${remoteMessage.from}")

        //check if the message is not empty and display a message in case
        remoteMessage.data.isNotEmpty().let{
            Log.d(TAG, "Message Data Payload: ${remoteMessage.data}")
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
        // TODO
    }

    // we create this method to send notifications
    private fun sendNotifications(messageBody: String) {
        val intent = Intent(this@MyFirebaseMessagingService, MainActivity::class.java) // when the user clicks on the notifications should be sent to the MainActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // make sure that specific activity is set to a specific position inside of the stack(if we have 2 activities, with this intent we set the activity passed on top)
        // the user is in another app, we can't open an intent from the other app to our --> we need a pending intent
        val pendingIntent = PendingIntent.getActivity(this@MyFirebaseMessagingService, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        // we need to create a channel id
        val channelId = this.resources.getString(R.string.default_notification_channel_id)
        // set the type of notification sound for the user
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        // display the icon in the notification bar
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_ic_notification)
            .setContentTitle(getString(R.string.app_name_logo))
            .setContentText("Message")
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